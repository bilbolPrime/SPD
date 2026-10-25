package android.opengl;

public final class Matrix {

    private Matrix() {
    }

    public static void multiplyMM(float[] result, int resultOffset, float[] lhs, int lhsOffset, float[] rhs, int rhsOffset) {
        for (int i = 0; i < 4; i++) {
            final float rhs0 = rhs[rhsOffset + 4 * i];
            final float rhs1 = rhs[rhsOffset + 4 * i + 1];
            final float rhs2 = rhs[rhsOffset + 4 * i + 2];
            final float rhs3 = rhs[rhsOffset + 4 * i + 3];

            result[resultOffset + 4 * i] = lhs[lhsOffset] * rhs0
                    + lhs[lhsOffset + 4] * rhs1
                    + lhs[lhsOffset + 8] * rhs2
                    + lhs[lhsOffset + 12] * rhs3;
            result[resultOffset + 4 * i + 1] = lhs[lhsOffset + 1] * rhs0
                    + lhs[lhsOffset + 5] * rhs1
                    + lhs[lhsOffset + 9] * rhs2
                    + lhs[lhsOffset + 13] * rhs3;
            result[resultOffset + 4 * i + 2] = lhs[lhsOffset + 2] * rhs0
                    + lhs[lhsOffset + 6] * rhs1
                    + lhs[lhsOffset + 10] * rhs2
                    + lhs[lhsOffset + 14] * rhs3;
            result[resultOffset + 4 * i + 3] = lhs[lhsOffset + 3] * rhs0
                    + lhs[lhsOffset + 7] * rhs1
                    + lhs[lhsOffset + 11] * rhs2
                    + lhs[lhsOffset + 15] * rhs3;
        }
    }
}
