/*
 * Display density correction adapted from Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.bilboldev.noosa;

import android.content.res.AssetManager;
import android.content.Context;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.bilboldev.glscripts.Script;
import com.bilboldev.gltextures.TextureCache;
import com.bilboldev.glwrap.Vertexbuffer;
import com.bilboldev.input.Touchscreen;
import com.bilboldev.noosa.audio.Music;
import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.pixeldungeonskills.PixelDungeon;
import com.bilboldev.pixeldungeonskills.VersionNewsInfo;
import com.bilboldev.pixeldungeonskills.desktop.DesktopInput;
import com.bilboldev.pixeldungeonskills.network.PartyPrompts;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.ui.Window;
import com.bilboldev.utils.BitmapCache;
import com.bilboldev.utils.SystemTime;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class Game extends ApplicationAdapter {

    public static final int MODE_PRIVATE = 0;
    public static final String VIBRATOR_SERVICE = "vibrator";

    public static Game instance;
    public static Game mContext;

    public static int width;
    public static int height;
    public static int graphicsContext;
    public static float density = 1f;

    private static final Properties BUILD_VERSION = loadBuildVersion();
    public static String version = BUILD_VERSION.getProperty("name", "2.1.0");
    public static int versionBuild = Integer.parseInt(BUILD_VERSION.getProperty("code", "143"));
    public static String vanillaVersion = "Vanilla PD v 1.9.2a";

    public static float timeScale = 1f;
    public static float elapsed = 0f;

    public final DesktopSignalRService mService = new DesktopSignalRService();

    private final Context desktopContext = new Context();

    protected Scene scene;
    protected Scene requestedScene;
    protected boolean requestedReset = true;
    private Runnable afterSceneCreate;
    protected Class<? extends Scene> sceneClass;

    protected long now;
    protected long step;

    private DesktopInput inputProcessor;
    private int lastValidWidth, lastValidHeight;

    private static Properties loadBuildVersion() {
        Properties properties = new Properties();
        try (InputStream input = Game.class.getResourceAsStream("/game-version.properties")) {
            if (input != null) properties.load(input);
        } catch (java.io.IOException ignored) {
        }
        return properties;
    }

    public Game(Class<? extends Scene> sceneClass) {
        this.sceneClass = sceneClass;
    }

    @Override
    public void create() {
        graphicsContext++;
        instance = this;
        mContext = this;
        BitmapCache.context = desktopContext;
        TextureCache.context = desktopContext;
        width = Gdx.graphics.getWidth();
        height = Gdx.graphics.getHeight();
        density = displayDensity();
        lastValidWidth = width;
        lastValidHeight = height;
        VersionNewsInfo.alreadySeen = false;
        inputProcessor = new DesktopInput();
        Gdx.input.setInputProcessor(inputProcessor);
        PartyPrompts.install(new PartyPrompts.Backend() {
            @Override
            public void copy(String value) {
                Gdx.app.getClipboard().setContents(value);
            }

            @Override
            public void paste(PartyPrompts.Result result) {
                result.complete(Gdx.app.getClipboard().getContents());
            }
        });

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);
    }

    private static float displayDensity() {
        float value = Gdx.graphics.getDensity();
        if (!Float.isFinite(value) || value <= 0) return 100f / 160f;

        int displayWidth = Gdx.graphics.getDisplayMode().width;
        int displayHeight = Gdx.graphics.getDisplayMode().height;
        float physicalWidth = displayWidth / Gdx.graphics.getPpiX();
        float physicalHeight = displayHeight / Gdx.graphics.getPpiY();
        if (Float.isFinite(physicalWidth) && Float.isFinite(physicalHeight)
                && physicalWidth > 0 && physicalHeight > 0
                && (displayWidth > displayHeight) != (physicalWidth > physicalHeight)) {

            value = displayWidth / physicalHeight / 160f;
        }
        return value;
    }

    @Override
    public void render() {
        if (width == 0 || height == 0) {
            return;
        }

        SystemTime.tick();
        long rightNow = SystemTime.now;
        step = now == 0 ? 0 : rightNow - now;
        now = rightNow;

        step();

        NoosaScript.get().resetCamera();
        Gdx.gl.glScissor(0, 0, width, height);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        draw();
        inputProcessor.drawCursor();
    }

    @Override
    public void resize(int width, int height) {
        boolean samePerspectiveSize = GameScene.isPerspectiveView() && width == lastValidWidth && height == lastValidHeight;
        Gdx.gl.glViewport(0, 0, width, height);
        Game.width = width;
        Game.height = height;

        if (inputProcessor != null) inputProcessor.windowChanged();
        else Touchscreen.cancelAllTouches();

        if (width <= 0 || height <= 0) {
            return;
        }

        PixelDungeon.rememberWindowSize(width, height);
        lastValidWidth = width;
        lastValidHeight = height;

        if (samePerspectiveSize) return;

        if (sceneClass != null) {
            switchScene(sceneClass);
        }
    }

    @Override
    public void pause() {
        if (inputProcessor != null) inputProcessor.setFocused(false);
        if (scene != null) {
            scene.pause();
        }
        Script.reset();
        Music.INSTANCE.pause();
        Sample.INSTANCE.pause();
    }

    @Override
    public void resume() {
        if (inputProcessor != null) inputProcessor.setFocused(true);
        Gdx.input.setInputProcessor(inputProcessor);
        now = 0;
        Music.INSTANCE.resume();
        Sample.INSTANCE.resume();
    }

    @Override
    public void dispose() {
        com.bilboldev.pixeldungeonskills.network.PartySession.cancelLobby();
        if (inputProcessor != null) inputProcessor.dispose();
        destroyGame();
        Music.INSTANCE.mute();
        Sample.INSTANCE.reset();
    }

    public void reloadCache() {
        TextureCache.reload();
        RenderedText.reloadCache();
        Vertexbuffer.refreshAllBuffers();
    }

    protected void destroyGame() {
        if (scene != null) {
            scene.destroy();
            scene = null;
        }
        instance = null;
        mContext = null;
    }

    public static void resetScene() {
        switchScene(instance.sceneClass);
    }

    public static void resetScene(Runnable afterCreate) {
        resetScene();
        instance.afterSceneCreate = afterCreate;
    }

    public static void switchScene(Class<? extends Scene> sceneClass) {
        instance.sceneClass = sceneClass;
        instance.requestedReset = true;
        instance.afterSceneCreate = null;
    }

    public static Scene scene() {
        return instance.scene;
    }

    protected Class<? extends Scene> resolveScene(Class<? extends Scene> sceneClass) {
        return sceneClass;
    }

    protected void step() {
        if (requestedReset) {
            requestedReset = false;
            try {
                requestedScene = resolveScene(sceneClass).newInstance();
                switchScene();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }

        update();
    }

    protected void draw() {
        if (scene != null) {
            scene.draw();
        }
    }

    protected void switchScene() {
        Runnable afterCreate = afterSceneCreate;
        afterSceneCreate = null;
        resetInputState();
        Camera.reset();

        if (scene != null) {
            scene.destroy();
        }
        scene = requestedScene;
        scene.create();
        if (afterCreate != null && !requestedReset) afterCreate.run();

        Game.elapsed = 0f;
        Game.timeScale = 1f;
    }

    protected void update() {
        Game.elapsed = Game.timeScale * step * 0.001f;
        com.bilboldev.pixeldungeonskills.network.PartySession.update();
        if (inputProcessor != null) inputProcessor.update(Game.elapsed);
        scene.update();
        Camera.updateAll();
    }

    private void resetInputState() {
        if (inputProcessor != null) {
            inputProcessor.resetState();
        } else {
            Touchscreen.cancelAllTouches();
        }
    }

    public static void setInputFocused(boolean focused) {
        if (instance != null && instance.inputProcessor != null) {
            instance.inputProcessor.setFocused(focused);
        }
    }

    public static void setInputMinimized(boolean minimized) {
        if (instance != null && instance.inputProcessor != null) {
            instance.inputProcessor.setMinimized(minimized);
        }
    }

    public static void resetInputForWindowChange() {
        if (instance != null && instance.inputProcessor != null && GameScene.isPerspectiveView()) {
            instance.inputProcessor.windowChanged();
        }
    }

    public static void vibrate(int milliseconds) {
    }

    public void showWindow(Window window) {
        if (scene != null) {
            scene.add(window);
        }
    }

    public void finish() {
        Gdx.app.exit();
    }

    public void runOnUiThread(Runnable runnable) {
        runnable.run();
    }

    public void setRequestedOrientation(int orientation) {
    }

    public OutputStream openFileOutput(String name, int mode) throws FileNotFoundException {
        FileHandle file = Gdx.files.local(name);
        file.parent().mkdirs();
        return new FileOutputStream(file.file());
    }

    public InputStream openFileInput(String name) throws FileNotFoundException {
        FileHandle file = Gdx.files.local(name);
        return new FileInputStream(file.file());
    }

    public boolean deleteFile(String name) {
        return Gdx.files.local(name).delete();
    }

    public String[] fileList() {
        FileHandle[] files = Gdx.files.local("").list();
        String[] names = new String[files.length];
        for (int i = 0; i < files.length; i++) names[i] = files[i].name();
        return names;
    }

    public void startActivity(Object intent) {
    }

    public AssetManager getAssets() {
        return desktopContext.getAssets();
    }

    public void BindSignarlR() {
    }

    public static final class DesktopSignalRService {
        public final DesktopHubConnection mHubConnection = new DesktopHubConnection();

        public void startSignalR() {
        }

        public void sendMessage(String message) {
        }
    }

    public static final class DesktopHubConnection {
        public void stop() {
        }
    }

}
