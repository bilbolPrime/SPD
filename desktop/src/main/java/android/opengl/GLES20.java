package android.opengl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

public final class GLES20 {

    public static final int GL_ALPHA = GL20.GL_ALPHA;
    public static final int GL_ARRAY_BUFFER = GL20.GL_ARRAY_BUFFER;
    public static final int GL_CLAMP_TO_EDGE = GL20.GL_CLAMP_TO_EDGE;
    public static final int GL_COLOR_ATTACHMENT0 = GL20.GL_COLOR_ATTACHMENT0;
    public static final int GL_COLOR_BUFFER_BIT = GL20.GL_COLOR_BUFFER_BIT;
    public static final int GL_COMPILE_STATUS = GL20.GL_COMPILE_STATUS;
    public static final int GL_DEPTH_ATTACHMENT = GL20.GL_DEPTH_ATTACHMENT;
    public static final int GL_DEPTH_COMPONENT16 = GL20.GL_DEPTH_COMPONENT16;
    public static final int GL_DYNAMIC_DRAW = GL20.GL_DYNAMIC_DRAW;
    public static final int GL_ELEMENT_ARRAY_BUFFER = GL20.GL_ELEMENT_ARRAY_BUFFER;
    public static final int GL_FALSE = GL20.GL_FALSE;
    public static final int GL_FLOAT = GL20.GL_FLOAT;
    public static final int GL_FRAGMENT_SHADER = GL20.GL_FRAGMENT_SHADER;
    public static final int GL_FRAMEBUFFER = GL20.GL_FRAMEBUFFER;
    public static final int GL_FRAMEBUFFER_BINDING = GL20.GL_FRAMEBUFFER_BINDING;
    public static final int GL_FRAMEBUFFER_COMPLETE = GL20.GL_FRAMEBUFFER_COMPLETE;
    public static final int GL_LINEAR = GL20.GL_LINEAR;
    public static final int GL_LINK_STATUS = GL20.GL_LINK_STATUS;
    public static final int GL_MIRRORED_REPEAT = GL20.GL_MIRRORED_REPEAT;
    public static final int GL_NEAREST = GL20.GL_NEAREST;
    public static final int GL_RENDERBUFFER = GL20.GL_RENDERBUFFER;
    public static final int GL_REPEAT = GL20.GL_REPEAT;
    public static final int GL_RGBA = GL20.GL_RGBA;
    public static final int GL_SCISSOR_TEST = GL20.GL_SCISSOR_TEST;
    public static final int GL_STATIC_DRAW = GL20.GL_STATIC_DRAW;
    public static final int GL_STENCIL_ATTACHMENT = GL20.GL_STENCIL_ATTACHMENT;
    public static final int GL_STENCIL_INDEX8 = GL20.GL_STENCIL_INDEX8;
    public static final int GL_TEXTURE_2D = GL20.GL_TEXTURE_2D;
    public static final int GL_TEXTURE_MAG_FILTER = GL20.GL_TEXTURE_MAG_FILTER;
    public static final int GL_TEXTURE_MIN_FILTER = GL20.GL_TEXTURE_MIN_FILTER;
    public static final int GL_TEXTURE_WRAP_S = GL20.GL_TEXTURE_WRAP_S;
    public static final int GL_TEXTURE_WRAP_T = GL20.GL_TEXTURE_WRAP_T;
    public static final int GL_TEXTURE0 = GL20.GL_TEXTURE0;
    public static final int GL_TRIANGLES = GL20.GL_TRIANGLES;
    public static final int GL_UNPACK_ALIGNMENT = GL20.GL_UNPACK_ALIGNMENT;
    public static final int GL_UNSIGNED_BYTE = GL20.GL_UNSIGNED_BYTE;
    public static final int GL_UNSIGNED_SHORT = GL20.GL_UNSIGNED_SHORT;
    public static final int GL_VERTEX_SHADER = GL20.GL_VERTEX_SHADER;
    public static final int GL_VIEWPORT = GL20.GL_VIEWPORT;

    private GLES20() {
    }

    public static void glActiveTexture(int texture) {
        gl().glActiveTexture(texture);
    }

    public static void glAttachShader(int program, int shader) {
        gl().glAttachShader(program, shader);
    }

    public static void glBindBuffer(int target, int buffer) {
        gl().glBindBuffer(target, buffer);
    }

    public static void glBindFramebuffer(int target, int framebuffer) {
        gl().glBindFramebuffer(target, framebuffer);
    }

    public static void glBindRenderbuffer(int target, int renderbuffer) {
        gl().glBindRenderbuffer(target, renderbuffer);
    }

    public static void glBindTexture(int target, int texture) {
        gl().glBindTexture(target, texture);
    }

    public static void glBlendFunc(int sfactor, int dfactor) {
        gl().glBlendFunc(sfactor, dfactor);
    }

    public static void glBufferData(int target, int size, Buffer data, int usage) {
        gl().glBufferData(target, size, data, usage);
    }

    public static void glBufferSubData(int target, int offset, int size, Buffer data) {
        gl().glBufferSubData(target, offset, size, data);
    }

    public static int glCheckFramebufferStatus(int target) {
        return gl().glCheckFramebufferStatus(target);
    }

    public static void glClear(int mask) {
        gl().glClear(mask);
    }

    public static void glCompileShader(int shader) {
        gl().glCompileShader(shader);
    }

    public static int glCreateProgram() {
        return gl().glCreateProgram();
    }

    public static int glCreateShader(int type) {
        return gl().glCreateShader(type);
    }

    public static void glDeleteBuffers(int n, int[] buffers, int offset) {
        gl().glDeleteBuffers(n, toIntBuffer(buffers, offset, n));
    }

    public static void glDeleteFramebuffers(int n, int[] framebuffers, int offset) {
        gl().glDeleteFramebuffers(n, toIntBuffer(framebuffers, offset, n));
    }

    public static void glDeleteProgram(int program) {
        gl().glDeleteProgram(program);
    }

    public static void glDeleteRenderbuffers(int n, int[] renderbuffers, int offset) {
        gl().glDeleteRenderbuffers(n, toIntBuffer(renderbuffers, offset, n));
    }

    public static void glDeleteShader(int shader) {
        gl().glDeleteShader(shader);
    }

    public static void glDeleteTextures(int n, int[] textures, int offset) {
        gl().glDeleteTextures(n, toIntBuffer(textures, offset, n));
    }

    public static void glDisable(int cap) {
        gl().glDisable(cap);
    }

    public static void glDisableVertexAttribArray(int index) {
        gl().glDisableVertexAttribArray(index);
    }

    public static void glDrawElements(int mode, int count, int type, Buffer indices) {
        gl().glDrawElements(mode, count, type, indices);
    }

    public static void glDrawElements(int mode, int count, int type, int offset) {
        gl().glDrawElements(mode, count, type, offset);
    }

    public static void glEnable(int cap) {
        gl().glEnable(cap);
    }

    public static void glEnableVertexAttribArray(int index) {
        gl().glEnableVertexAttribArray(index);
    }

    public static void glFramebufferRenderbuffer(int target, int attachment, int renderbuffertarget, int renderbuffer) {
        gl().glFramebufferRenderbuffer(target, attachment, renderbuffertarget, renderbuffer);
    }

    public static void glFramebufferTexture2D(int target, int attachment, int textarget, int texture, int level) {
        gl().glFramebufferTexture2D(target, attachment, textarget, texture, level);
    }

    public static void glGenBuffers(int n, int[] buffers, int offset) {
        readBack(gl()::glGenBuffers, n, buffers, offset);
    }

    public static void glGenFramebuffers(int n, int[] framebuffers, int offset) {
        readBack(gl()::glGenFramebuffers, n, framebuffers, offset);
    }

    public static void glGetIntegerv(int pname, int[] params, int offset) {
        IntBuffer buffer = newIntBuffer(params.length - offset);
        gl().glGetIntegerv(pname, buffer);
        for (int i = offset; i < params.length; i++) params[i] = buffer.get(i - offset);
    }

    public static void glGenRenderbuffers(int n, int[] renderbuffers, int offset) {
        readBack(gl()::glGenRenderbuffers, n, renderbuffers, offset);
    }

    public static void glGenTextures(int n, int[] textures, int offset) {
        readBack(gl()::glGenTextures, n, textures, offset);
    }

    public static int glGetAttribLocation(int program, String name) {
        return gl().glGetAttribLocation(program, name);
    }

    public static String glGetProgramInfoLog(int program) {
        return gl().glGetProgramInfoLog(program);
    }

    public static void glGetProgramiv(int program, int pname, int[] params, int offset) {
        IntBuffer buffer = newIntBuffer(1);
        gl().glGetProgramiv(program, pname, buffer);
        params[offset] = buffer.get(0);
    }

    public static String glGetShaderInfoLog(int shader) {
        return gl().glGetShaderInfoLog(shader);
    }

    public static void glGetShaderiv(int shader, int pname, int[] params, int offset) {
        IntBuffer buffer = newIntBuffer(1);
        gl().glGetShaderiv(shader, pname, buffer);
        params[offset] = buffer.get(0);
    }

    public static int glGetUniformLocation(int program, String name) {
        return gl().glGetUniformLocation(program, name);
    }

    public static void glLinkProgram(int program) {
        gl().glLinkProgram(program);
    }

    public static void glPixelStorei(int pname, int param) {
        gl().glPixelStorei(pname, param);
    }

    public static void glRenderbufferStorage(int target, int internalformat, int width, int height) {
        gl().glRenderbufferStorage(target, internalformat, width, height);
    }

    public static void glScissor(int x, int y, int width, int height) {
        gl().glScissor(x, y, width, height);
    }

    public static void glShaderSource(int shader, String string) {
        gl().glShaderSource(shader, preprocessShaderSource(string));
    }

    public static void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format, int type, Buffer pixels) {
        gl().glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);
    }

    public static void glTexParameterf(int target, int pname, float param) {
        gl().glTexParameterf(target, pname, param);
    }

    public static void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format, int type, Buffer pixels) {
        gl().glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels);
    }

    public static void glUniform1f(int location, float x) {
        gl().glUniform1f(location, x);
    }

    public static void glUniform1i(int location, int x) {
        gl().glUniform1i(location, x);
    }

    public static void glUniform2f(int location, float x, float y) {
        gl().glUniform2f(location, x, y);
    }

    public static void glUniform4f(int location, float x, float y, float z, float w) {
        gl().glUniform4f(location, x, y, z, w);
    }

    public static void glUniformMatrix3fv(int location, int count, boolean transpose, float[] value, int offset) {
        gl().glUniformMatrix3fv(location, count, transpose, value, offset);
    }

    public static void glUniformMatrix4fv(int location, int count, boolean transpose, float[] value, int offset) {
        gl().glUniformMatrix4fv(location, count, transpose, value, offset);
    }

    public static void glUseProgram(int program) {
        gl().glUseProgram(program);
    }

    public static void glVertexAttribPointer(int indx, int size, int type, boolean normalized, int stride, Buffer ptr) {
        gl().glVertexAttribPointer(indx, size, type, normalized, stride, ptr);
    }

    public static void glVertexAttribPointer(int indx, int size, int type, boolean normalized, int stride, int ptr) {
        gl().glVertexAttribPointer(indx, size, type, normalized, stride, ptr);
    }

    public static void glViewport(int x, int y, int width, int height) {
        gl().glViewport(x, y, width, height);
    }

    private static GL20 gl() {
        GL20 gl = Gdx.gl20 != null ? Gdx.gl20 : Gdx.gl;
        if (gl == null) {
            throw new IllegalStateException("LibGDX GL20 is not initialized");
        }
        return gl;
    }

    private static IntBuffer newIntBuffer(int size) {
        return ByteBuffer.allocateDirect(size * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
    }

    private static IntBuffer toIntBuffer(int[] values, int offset, int length) {
        IntBuffer buffer = newIntBuffer(length);
        for (int index = 0; index < length; index++) {
            buffer.put(values[offset + index]);
        }
        buffer.position(0);
        return buffer;
    }

    private static void readBack(GenCall call, int n, int[] values, int offset) {
        IntBuffer buffer = newIntBuffer(n);
        call.accept(n, buffer);
        for (int index = 0; index < n; index++) {
            values[offset + index] = buffer.get(index);
        }
    }

    private static String preprocessShaderSource(String source) {
        if (source == null) {
            return null;
        }

        String normalized = source;
        normalized = normalized.replace("lowp ", "");
        normalized = normalized.replace("mediump ", "");
        normalized = normalized.replace("highp ", "");
        normalized = normalized.replaceAll("precision\\s+(lowp|mediump|highp)\\s+float\\s*;", "");
        return normalized;
    }

    private interface GenCall {
        void accept(int count, IntBuffer buffer);
    }
}
