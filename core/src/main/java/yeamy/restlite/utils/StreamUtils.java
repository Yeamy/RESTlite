package yeamy.restlite.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

public class StreamUtils {

    public static void close(AutoCloseable obj) {
        try (obj) {
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

    public static byte[] readByte(InputStream is) {
        try (is) {
            return is.readAllBytes();
        } catch (Exception e) {
            return null;
        }
    }

    public static String readString(InputStream is, String charset) {
        return readString(is, Charset.forName(charset));
    }

    public static String readString(InputStream is, Charset charset) {
        try (is) {
            return new String(is.readAllBytes(), charset);
        } catch (Exception e) {
            return null;
        }
    }
}
