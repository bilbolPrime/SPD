package android.graphics;

import android.content.res.Resources;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public final class BitmapFactory {

    public static final class Options {
        public boolean inDither;
        public boolean inScaled;
        public Bitmap.Config inPreferredConfig;
    }

    private BitmapFactory() {
    }

    public static Bitmap decodeStream(InputStream stream, Rect rect, Options options) {
        try (InputStream input = stream) {
            BufferedImage image = ImageIO.read(stream);
            if (image == null) {
                return null;
            }
            BufferedImage argbImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = argbImage.createGraphics();
            try {
                graphics.drawImage(image, 0, 0, null);
            } finally {
                graphics.dispose();
            }
            Bitmap.Config config = options != null && options.inPreferredConfig != null
                    ? options.inPreferredConfig
                    : Bitmap.Config.ARGB_8888;
            return new Bitmap(argbImage, config);
        } catch (IOException exception) {
            return null;
        }
    }

    public static Bitmap decodeResource(Resources resources, int resId) {
        return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
    }

    public static Bitmap decodeResource(Resources resources, int resId, Options options) {
        return decodeResource(resources, resId);
    }
}
