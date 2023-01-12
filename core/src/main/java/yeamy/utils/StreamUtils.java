package yeamy.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

public class StreamUtils {

    public static void close(OutputStream os) {
        try {
            if (os != null) {
                os.close();
            }
        } catch (Exception e) {
        }
    }

    public static void close(InputStream is) {
        try {
            if (is != null) {
                is.close();
            }
        } catch (Exception e) {
        }
    }

    public static boolean write(OutputStream os, InputStream is) {
        try {
            byte[] buf = new byte[8192];
            while (true) {
                int l = is.read(buf);
                if (l == -1) {
                    os.flush();
                    break;
                }
                os.write(buf, 0, l);
            }
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            close(os);
            close(is);
        }
    }

    public static boolean write(OutputStream os, InputStream is, long begin, long len) {
        try {
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
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            close(os);
            close(is);
        }
    }

    public static boolean writeWithoutClose(OutputStream os, InputStream is) throws IOException {
        byte[] buf = new byte[8192];
        while (true) {
            int l = is.read(buf);
            if (l == -1) {
                os.flush();
                break;
            }
            os.write(buf, 0, l);
        }
        return true;
    }

    public static boolean writeWithoutClose(OutputStream os, InputStream is, long begin, long len) {
        try {
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
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean write(OutputStream os, byte[] out) {
        try {
            os.write(out);
            os.flush();
            return true;
        } catch (IOException e) {
            return false;
        } finally {
            close(os);
        }
    }

    private static ByteArrayOutputStream read(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(is.available());
        byte[] buf = new byte[8192];
        while (true) {
            int l = is.read(buf);
            if (l == -1) {
                break;
            }
            baos.write(buf, 0, l);
        }
        return baos;
    }

    public static byte[] readByte(InputStream is) {
        try {
            return read(is).toByteArray();
        } catch (Exception e) {
            return null;
        } finally {
            close(is);
        }
    }

    public static String readString(InputStream is, String charset) {
        return readString(is, Charset.forName(charset));
    }

    public static String readString(InputStream is, Charset charset) {
        try {
            return read(is).toString(charset);
        } catch (Exception e) {
            return null;
        } finally {
            close(is);
        }
    }
}
