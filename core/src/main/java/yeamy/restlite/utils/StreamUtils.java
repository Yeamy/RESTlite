package yeamy.restlite.utils;

import java.io.*;
import java.nio.charset.Charset;

public class StreamUtils {

    public static void close(Closeable obj) {
        try {
            if (obj != null) {
                obj.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean write(OutputStream os, InputStream is) {
        try (os; is) {
            writeWithoutClose(os, is);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    public static boolean write(OutputStream os, InputStream is, long begin, long len) {
        try (os; is) {
            writeWithoutClose(os, is, begin, len);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    public static void writeWithoutClose(OutputStream os, InputStream is) throws IOException {
        byte[] buf = new byte[8192];
        while (true) {
            int l = is.read(buf);
            if (l == -1) {
                os.flush();
                break;
            }
            os.write(buf, 0, l);
        }
    }

    public static void writeWithoutClose(OutputStream os, InputStream is, long begin, long len) throws IOException {
        long skip = 0;
        do {
            skip += is.skip(begin - skip);
        } while (skip != begin);
        byte[] buf = new byte[8192];
        while (true) {
            int l = is.read(buf);
            if (l == -1) {
                os.flush();
                break;
            }
            len -= l;
            if (len > 0) {
                os.write(buf, 0, l);
            } else if (len < 0) {
                os.write(buf, 0, (int) (l + len));
                break;
            } else {// len == 0
                os.write(buf, 0, l);
                break;
            }
        }
    }

    private static ByteArrayOutputStream readWithoutClose(InputStream is) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream(is.available());
        byte[] buf = new byte[8192];
        while (true) {
            int l = is.read(buf);
            if (l == -1) {
                break;
            }
            os.write(buf, 0, l);
        }
        return os;
    }

    public static byte[] readByte(InputStream is) {
        try (is) {
            return readWithoutClose(is).toByteArray();
        } catch (Exception e) {
            return null;
        }
    }

    public static String readString(InputStream is, String charset) {
        return readString(is, Charset.forName(charset));
    }

    public static String readString(InputStream is, Charset charset) {
        try (is) {
            return readWithoutClose(is).toString(charset);
        } catch (Exception e) {
            return null;
        }
    }
}
