package android.graphics;

public final class Color {

    public static final int WHITE = 0xFFFFFFFF;

    private Color() {
    }

    public static int parseColor(String colorString) {
        if (colorString == null) {
            throw new IllegalArgumentException("colorString == null");
        }

        String normalized = colorString.trim();
        if (!normalized.startsWith("#")) {
            throw new IllegalArgumentException("Unknown color format: " + colorString);
        }

        String hex = normalized.substring(1);
        if (hex.length() == 6) {
            return (int) (0xFF000000L | Long.parseLong(hex, 16));
        }
        if (hex.length() == 8) {
            return (int) Long.parseLong(hex, 16);
        }

        throw new IllegalArgumentException("Unknown color format: " + colorString);
    }
}
