package android.graphics;

public class RectF {

    public float left;
    public float top;
    public float right;
    public float bottom;

    public RectF() {
    }

    public RectF(float left, float top, float right, float bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    public RectF(RectF other) {
        this(other.left, other.top, other.right, other.bottom);
    }

    public float width() {
        return right - left;
    }

    public float height() {
        return bottom - top;
    }

    public void shift(float x, float y) {
        left += x;
        right += x;
        top += y;
        bottom += y;
    }
}
