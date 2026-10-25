package android.graphics;

import java.awt.image.BufferedImage;

public class Bitmap {

    public enum Config {
        ARGB_8888,
        ARGB_4444
    }

    private final BufferedImage image;
    private final Config config;

    Bitmap(BufferedImage image) {
        this(image, Config.ARGB_8888);
    }

    Bitmap(BufferedImage image, Config config) {
        this.image = image;
        this.config = config;
    }

    public static Bitmap createBitmap(int width, int height, Config config) {
        return new Bitmap(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB), config);
    }

    public int getWidth() {
        return image.getWidth();
    }

    public int getHeight() {
        return image.getHeight();
    }

    public Config getConfig() {
        return config;
    }

    public int getPixel(int x, int y) {
        return image.getRGB(x, y);
    }

    public void setPixel(int x, int y, int color) {
        image.setRGB(x, y, color);
    }

    public void setPixels(int[] pixels, int offset, int stride, int x, int y, int width, int height) {
        image.setRGB(x, y, width, height, pixels, offset, stride);
    }

    public void eraseColor(int color) {
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                image.setRGB(x, y, color);
            }
        }
    }

    public void getPixels(int[] pixels, int offset, int stride, int x, int y, int width, int height) {
        for (int row = 0; row < height; row++) {
            for (int column = 0; column < width; column++) {
                pixels[offset + row * stride + column] = image.getRGB(x + column, y + row);
            }
        }
    }

    public void recycle() {
    }

    BufferedImage bufferedImage() {
        return image;
    }
}
