package android.graphics;

import android.content.res.AssetManager;

import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class Typeface {

    public static final Typeface DEFAULT = new Typeface(new Font(Font.SANS_SERIF, Font.PLAIN, 12));

    private final Font baseFont;

    private Typeface(Font baseFont) {
        this.baseFont = baseFont;
    }

    public static Typeface createFromAsset(AssetManager assetManager, String asset) {
        try (InputStream input = assetManager.open(asset)) {
            Font font = Font.createFont(Font.TRUETYPE_FONT, input);
            return new Typeface(font);
        } catch (Exception exception) {
            return DEFAULT;
        }
    }

	public static Typeface createFromFile(String path) {
		try (InputStream input = new FileInputStream(new File(path))) {
			Font font = Font.createFont(Font.TRUETYPE_FONT, input);
			return new Typeface(font);
		} catch (Exception exception) {
			return DEFAULT;
		}
	}

    Font toFont(float size) {
        return baseFont.deriveFont(size);
    }

    boolean canDisplay(String text, float size) {
        if (text == null || text.isEmpty()) {
            return true;
        }
        return toFont(size).canDisplayUpTo(text) == -1;
    }

    public static final class Builder {

        private final String path;

        public Builder(String path) {
            this.path = path;
        }

        public Builder setTtcIndex(int ttcIndex) {
            return this;
        }

        public Typeface build() {
            return Typeface.createFromFile(path);
        }
    }
}
