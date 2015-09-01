package cn.fishy.plugin.idea.ponytail.process;

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * User: duxing
 * Date: 2015-08-27 21:18
 */
public class LogReader {


    public static int getFileLineNumber(String filepath) {
        return getFileLineNumber(new File(filepath));
    }

    public static int getFileLineNumber(File file) {
        LineNumberReader lineNumberReader = null;
        try {
            lineNumberReader = new LineNumberReader(new FileReader(file));
            lineNumberReader.skip(Long.MAX_VALUE);
            int lineNumber = lineNumberReader.getLineNumber();
            lineNumber++;
            lineNumberReader.close();
            return lineNumber;
        } catch (Exception e) {
            return 0;
        }
    }

    public static long getFileCharacterLength(File file) {
        LineNumberReader lineNumberReader = null;
        try {
            lineNumberReader = new LineNumberReader(new FileReader(file));
            long length = lineNumberReader.skip(Long.MAX_VALUE);
            lineNumberReader.close();
            return length;
        } catch (Exception e) {
            return 0;
        }
    }

    public static byte[] readFile(File f) throws Exception {
        final int BUFFER_SIZE = 0x300000;// 缓冲区大小为3M
        /**
         * map(FileChannel.MapMode mode,long position, long size) mode -
         * 根据是按只读、读取/写入或专用（写入时拷贝）来映射文件，分别为 FileChannel.MapMode 类中所定义的
         * READ_ONLY、READ_WRITE 或 PRIVATE 之一 position - 文件中的位置，映射区域从此位置开始；必须为非负数
         * size - 要映射的区域大小；必须为非负数且不大于 Integer.MAX_VALUE
         * 所以若想读取文件后半部分内容，如例子所写；若想读取文本后1
         * /8内容，需要这样写map(FileChannel.MapMode.READ_ONLY,
         * f.length()*7/8,f.length()/8)
         * 想读取文件所有内容，需要这样写map(FileChannel.MapMode.READ_ONLY, 0,f.length())
         */

        MappedByteBuffer inputBuffer = new RandomAccessFile(f, "r").getChannel().map(FileChannel.MapMode.READ_ONLY, f.length() / 2, f.length() / 2);
        byte[] dst = new byte[BUFFER_SIZE];// 每次读出3M的内容
        long start = System.currentTimeMillis();
        for (int offset = 0; offset < inputBuffer.capacity(); offset += BUFFER_SIZE) {
            if (inputBuffer.capacity() - offset >= BUFFER_SIZE) {
                for (int i = 0; i < BUFFER_SIZE; i++)
                    dst[i] = inputBuffer.get(offset + i);
            } else {
                for (int i = 0; i < inputBuffer.capacity() - offset; i++)
                    dst[i] = inputBuffer.get(offset + i);
            }
            int length = (inputBuffer.capacity() % BUFFER_SIZE == 0) ? BUFFER_SIZE : inputBuffer.capacity()
                    % BUFFER_SIZE;
            System.out.println(new String(dst, 0, length));// new
            // String(dst,0,length)这样可以取出缓存保存的字符串，可以对其进行操作
        }
        long end = System.currentTimeMillis();
        System.out.println("read half file need " + (end - start) + " ms");
        return dst;

    }
    public static void main1(String[] args) throws Exception {
        String log1 = "G:/11111111111111111111111111.txt";
        String log2 = "G:/22222222222222222222222222222222.txt";
        long start = System.currentTimeMillis();
        int lineNumber = getFileLineNumber(log1);
        long end = System.currentTimeMillis();
        System.out.println("use " + (end - start) + " ms");
        System.out.println("the line number of specified file is " + lineNumber);
    }
    public static void main2(String[] args) throws Exception {
        final int BUFFER_SIZE = 0x300000;// 缓冲区大小为3M
        System.out.println(BUFFER_SIZE);
    }
    public static void main(String[] args) throws Exception {
        String log1 = "G:/11111111111111111111111111.txt";
        File file = new File(log1);
        LineNumberReader lineNr = new LineNumberReader(new FileReader(file));
        System.out.println(file.length());
        long skip = file.length() - 10240;
        System.out.println(skip);
//        long ak = lineNr.skip(skip);
        long ak1 = lineNr.skip(Long.MAX_VALUE);
//        System.out.println(ak);
        System.out.println(ak1);
        for (String line = lineNr.readLine(); line != null; line = lineNr.readLine()) {
            System.out.println(line);
            System.out.println(lineNr.getLineNumber());
            file.length();
        }
    }


}
