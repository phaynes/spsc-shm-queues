package vn.tech;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Utility class for file processing.
 *
 */
public class FileUtils {

    /**
     * Ensure a string (usually a URI or a filename) ends with a slosh.
     *
     * @param aString
     * @return
     */
    public static String ensureSloshed(String aString) {
        if ((aString == null) || aString.length() == 0) {
            return "/";
        }
        return (aString.charAt(aString.length() - 1) == '/') ? aString : aString + '/';
    }

    /**
     * Removes starting and ending sloshes.
     *
     * @param aString
     * @return
     */
    public static String trimSloshes(String aString) {
        if ((aString == null) || (aString.length() == 0)) {
            return "";
        }
        if (aString.length() == 1) {
            return (aString.charAt(0) == '/') ? "" : aString;
        }
        int startPos = (aString.charAt(0) == '/') ? 1 : 0;
        int endPos = aString.length() - 1;
        endPos = (aString.charAt(endPos) == '/') ? endPos : endPos + 1;
        return aString.substring(startPos, endPos);
    }

    /**
     * Designed to quickly read files whose contents are strings.
     *
     * @param fileName
     * @return
     */
    public static String getContents(String fileName) {
        return getContents(new File(fileName));
    }

    /**
     * Designed to quickly read files whose contents are strings.
     *
     * @param file
     * @return
     */
    public static String getContents(File file) {
        if (!file.exists()) {
            return null;
        }
        byte[] contents = getFileBytes(file);
        return (contents == null) ? null : new String(contents);
    }

    /**
     * Designed to quickly read contents of small file into a byte array.
     */
    public static byte[] getFileBytes(File file) {

        try (RandomAccessFile rfile =  new RandomAccessFile(file, "r")){
            try (FileChannel srcChannel = rfile.getChannel()) {
                // Limit max file size to a G.
                if (srcChannel.size() > 1024*1024*1024) {
                    return null;
                }
                byte[] contents  = new byte[(int) srcChannel.size()]; ;
                ByteBuffer buf = ByteBuffer.wrap(contents);
                srcChannel.read(buf);
                return contents;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace(); // NOSONAR
        } catch (IOException e1) {
            e1.printStackTrace(); // NOSONAR
        }
        return null;
    }

}
