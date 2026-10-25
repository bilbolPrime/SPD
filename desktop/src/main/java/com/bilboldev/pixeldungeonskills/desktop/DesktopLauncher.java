package com.bilboldev.pixeldungeonskills.desktop;

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowAdapter;
import com.bilboldev.noosa.Game;
import com.bilboldev.pixeldungeonskills.PixelDungeon;

public final class DesktopLauncher {
    private DesktopLauncher() {
    }

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("Skillful Pixel Dungeon");
        configuration.setResizable(true);
        configuration.setWindowListener(new Lwjgl3WindowAdapter() {
            @Override
            public void iconified(boolean minimized) {
                Game.setInputMinimized(minimized);
            }

            @Override
            public void focusLost() {
                Game.setInputFocused(false);
            }

            @Override
            public void focusGained() {
                Game.setInputFocused(true);
            }
        });

        Graphics.DisplayMode displayMode = Lwjgl3ApplicationConfiguration.getDisplayMode();
        if (PixelDungeon.borderlessWindowedMode()) {
            configuration.setDecorated(false);
            configuration.setWindowedMode(displayMode.width, displayMode.height);
        } else if (PixelDungeon.windowedMode()) {
            configuration.setDecorated(true);
            configuration.setWindowedMode(
                    Math.max(800, Math.min(PixelDungeon.windowedWidth(), displayMode.width)),
                    Math.max(600, Math.min(PixelDungeon.windowedHeight(), displayMode.height))
            );
        } else {
            configuration.setDecorated(true);
            configuration.setFullscreenMode(displayMode);
        }
        configuration.useVsync(true);
        configuration.setForegroundFPS(60);

        new Lwjgl3Application(new PixelDungeon(), configuration);
    }

}
