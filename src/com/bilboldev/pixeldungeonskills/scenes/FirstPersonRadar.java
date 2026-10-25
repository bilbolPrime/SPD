package com.bilboldev.pixeldungeonskills.scenes;

import android.opengl.GLES20;
import com.bilboldev.gltextures.SmartTexture;
import com.bilboldev.gltextures.TextureCache;
import com.bilboldev.glwrap.Shader;
import com.bilboldev.glwrap.Uniform;
import com.bilboldev.noosa.Camera;
import com.bilboldev.noosa.ColorBlock;
import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.Image;
import com.bilboldev.noosa.NoosaScript;

final class FirstPersonRadar extends Image {
    private final Camera map, renderCamera;
    private final RadarTexture target;
    private final ColorBlock background;
    private final int[] previousFramebuffer = new int[1], previousViewport = new int[4];
    private FadeScript fade;
    private int shaderContext;
    private boolean drawing, destroyed;

    FirstPersonRadar(Camera ui, Camera map) {
        this.map = map;
        float resolution = Math.min(1, 512f / Math.max(map.width * map.zoom, map.height * map.zoom));
        float zoom = map.zoom * resolution;
        int w = Math.max(1, (int)(map.width * zoom)), h = Math.max(1, (int)(map.height * zoom));

        renderCamera = new Camera(0, Game.height - h, map.width, map.height, zoom);
        target = new RadarTexture(w, h);
        TextureCache.add(target, target);
        texture(target);
        flipVertical = true;
        updateFrame();
        camera = ui;
        x = (map.x - ui.x) / ui.zoom;
        y = (map.y - ui.y) / ui.zoom;
        scale.set((int)(map.width * map.zoom) / (ui.zoom * w), (int)(map.height * map.zoom) / (ui.zoom * h));
        background = new ColorBlock(map.width, map.height, 0xFF000000);
        background.camera = renderCamera;
    }

    Camera begin() {
        GLES20.glGetIntegerv(GLES20.GL_FRAMEBUFFER_BINDING, previousFramebuffer, 0);
        GLES20.glGetIntegerv(GLES20.GL_VIEWPORT, previousViewport, 0);
        drawing = true;
        try {
            target.bindFramebuffer();
            GLES20.glViewport(0, 0, Game.width, Game.height);
            renderCamera.scroll.set(map.scroll);
            renderCamera.update();
            NoosaScript.get().resetCamera();
            background.x = map.scroll.x;
            background.y = map.scroll.y;
            background.draw();
            return renderCamera;
        } catch (RuntimeException | Error failure) {
            end();
            throw failure;
        }
    }

    void end() {
        if (!drawing) return;
        drawing = false;
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, previousFramebuffer[0]);
        GLES20.glViewport(previousViewport[0], previousViewport[1], previousViewport[2], previousViewport[3]);
        NoosaScript.get().resetCamera();
    }

    @Override
    protected NoosaScript script() {
        if (fade == null || shaderContext != Game.graphicsContext) {
            fade = new FadeScript();
            shaderContext = Game.graphicsContext;
        }

        NoosaScript.get();
        fade.use();
        fade.resetCamera();
        float radius = Math.min(map.width, map.height) * 0.5f;
        fade.scale.value2f(map.width / radius, map.height / radius);
        return fade;
    }

    @Override
    public void draw() {
        try {
            super.draw();
        }
        finally {
            NoosaScript.get().use();
            NoosaScript.get().resetCamera();
        }
    }

    @Override
    public void destroy() {
        if (destroyed) return;
        destroyed = true;
        end();
        super.destroy();
        background.destroy();
        renderCamera.destroy();
        TextureCache.remove(target);
        if (fade != null && shaderContext == Game.graphicsContext) fade.delete();
        fade = null;
    }

    private static final class RadarTexture extends SmartTexture {
        private final int[] framebuffer = new int[1];
        private int context = Game.graphicsContext;
        RadarTexture(int width, int height) {
            this.width = width;
            this.height = height;
        }

        @Override
        protected void generate() {
            int[] name = new int[1];
            GLES20.glGenTextures(1, name, 0);
            id = name[0];
            filter(LINEAR, LINEAR);
            wrap(CLAMP, CLAMP);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0,
                    GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        }
        void bindFramebuffer() {
            reload();
            bind();
            if (framebuffer[0] == 0) {
                GLES20.glGenFramebuffers(1, framebuffer, 0);
                GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, framebuffer[0]);
                GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                        GLES20.GL_TEXTURE_2D, id, 0);
                if (GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) != GLES20.GL_FRAMEBUFFER_COMPLETE)
                    throw new IllegalStateException("Radar framebuffer unavailable");
            } else GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, framebuffer[0]);
        }

        @Override
        public void reload() {
            if (context != Game.graphicsContext) {
                context = Game.graphicsContext;
                id = -1;
                framebuffer[0] = 0;
            }

        }

        @Override
        public void delete() {
            if (context == Game.graphicsContext) {
                if (framebuffer[0] != 0) GLES20.glDeleteFramebuffers(1, framebuffer, 0);
                if (id != -1) super.delete();
            }
            id = -1;
            framebuffer[0] = 0;
        }
    }

    private static final class FadeScript extends NoosaScript {
        final Uniform scale = uniform("uFadeScale");
        @Override
        public void lighting(float rm, float gm, float bm, float am, float ra, float ga, float ba, float aa) {}

        @Override
        public void compile(String source) {
            String[] parts = source.split("//\n");
            Shader vertex = new Shader(Shader.VERTEX), fragment = new Shader(Shader.FRAGMENT);
            try {
                vertex.source(parts[0]);
                vertex.compile();
                attach(vertex);
                fragment.source(parts[1]);
                fragment.compile();
                attach(fragment);
                link();
            } catch (Error failure) {
                delete();
                throw new IllegalStateException("Radar shader unavailable", failure);
            } finally {

                vertex.delete();
                fragment.delete();
            }
        }

        @Override
        protected String shader() {
            return "uniform mat4 uCamera; uniform mat4 uModel; attribute vec4 aXYZW; attribute vec2 aUV;"
                    + "varying mediump vec2 vUV; void main(){ gl_Position=uCamera*uModel*aXYZW; vUV=aUV; } //\n"
                    + "varying mediump vec2 vUV; uniform lowp sampler2D uTex; uniform mediump vec2 uFadeScale;"
                    + "void main(){ mediump float distance=length((vUV-vec2(0.5))*uFadeScale);"
                    + "lowp float opacity=mix(0.8,0.2,smoothstep(0.12,1.0,distance));"
                    + "gl_FragColor=vec4(texture2D(uTex,vUV).rgb,opacity); }";
        }
    }
}
