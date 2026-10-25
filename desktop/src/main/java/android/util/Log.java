package android.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public final class Log {

    private Log() {
    }

    public static int d(String tag, String msg) {
        System.out.println(format(tag, msg));
        return 0;
    }

    public static int i(String tag, String msg) {
        System.out.println(format(tag, msg));
        return 0;
    }

    public static int w(String tag, String msg) {
        System.out.println(format(tag, msg));
        return 0;
    }

    public static int e(String tag, String msg) {
        System.err.println(format(tag, msg));
        return 0;
    }

    public static String getStackTraceString(Throwable throwable) {
        if (throwable == null) {
            return "";
        }
        StringWriter writer = new StringWriter();
        PrintWriter printer = new PrintWriter(writer);
        throwable.printStackTrace(printer);
        printer.flush();
        return writer.toString();
    }

    private static String format(String tag, String msg) {
        String safeTag = tag == null ? "" : tag;
        String safeMessage = msg == null ? "" : msg;
        return safeTag.isEmpty() ? safeMessage : safeTag + ": " + safeMessage;
    }
}
