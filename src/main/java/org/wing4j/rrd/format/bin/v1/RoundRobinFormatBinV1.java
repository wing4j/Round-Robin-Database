package org.wing4j.rrd.format.bin.v1;

import lombok.Data;
import org.wing4j.rrd.RoundRobinFormat;
import org.wing4j.rrd.RoundRobinRuntimeException;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * Created by wing4j on 2017/7/29.
 * 循环结构文件格式
 */
@Data
public class RoundRobinFormatBinV1 implements RoundRobinFormat {
    int version = 1;
    int current = 0;
    String[] header = null;
    long[][] data = null;
    static final boolean DEBUG = false;

    public RoundRobinFormatBinV1() {
    }

    public RoundRobinFormatBinV1(String[] header, long[][] data, int current) {
        this.header = header;
        this.data = data;
        this.current = current;
    }

    public void read(String fileName) throws IOException {
        FileInputStream fis = new FileInputStream(fileName);
        FileChannel fileChannel = fis.getChannel();
        try {
            read(fileChannel);
        } finally {
            if (fileChannel != null) {
                fileChannel.close();
            }
            if (fis != null) {
                fis.close();
            }
        }
    }

    public void write(String fileName) throws IOException {
        FileOutputStream fos = new FileOutputStream(fileName);
        FileChannel channel = null;
        try {
            channel = fos.getChannel();
            write(channel);
        } finally {
            if (channel != null) {
                channel.close();
            }
            if (fos != null) {
                fos.close();
            }
        }
    }

    @Override
    public void read(ReadableByteChannel channel) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(5 * 1024 * 1024);
        try {
            channel.read(buffer);
        } finally {
            if (channel != null) {
                channel.close();
            }
        }
        buffer.flip();
        int version0 = buffer.getInt();
        if (DEBUG) {
            System.out.println("version:" + version0);
        }
        int current0 = buffer.getInt();
        if (DEBUG) {
            System.out.println("current:" + current0);
        }
        int headerLen = buffer.getInt();
        if (DEBUG) {
            System.out.println("head size:" + headerLen);
        }
        int headerMaxLen = buffer.getInt();
        if (DEBUG) {
            System.out.println("head length:" + headerMaxLen);
        }
        String[] header0 = new String[headerLen + 1];
        header0[0] = "index";
        for (int i = 1; i < header0.length; i++) {
            char[] chars = new char[headerMaxLen];
            for (int j = 0; j < chars.length; j++) {
                chars[j] = buffer.getChar();
            }
            header0[i] = new String(chars).trim();
            if (DEBUG) {
                System.out.println("header:" + header0[i]);
            }
        }
        int dataLen0 = buffer.getInt();
        if (DEBUG) {
            System.out.println("head size:" + dataLen0);
        }
        int dataSize0 = buffer.getInt();
        if (DEBUG) {
            System.out.println("data size:" + dataSize0);
        }
        long[][] data0 = new long[dataSize0][dataLen0 + 1];
        for (int i = 0; i < data0.length; i++) {
            data0[i][0] = i;
            for (int j = 1; j < header0.length; j++) {
                data0[i][j] = buffer.getLong();
                if (DEBUG) {
                    System.out.println("data[" + i + "][" + j + "]:" + data0[i][j]);
                }
            }
        }
        this.version = version0;
        this.current = current0;
        this.header = header0;
        this.data = data0;
    }

    @Override
    public void write(WritableByteChannel channel) throws IOException {
        int headerMaxLen = 0;
        String[] header0 = new String[header.length - 1];
        for (int i = 1; i < header.length; i++) {
            header0[i - 1] = header[i];
            if (header0[i - 1].length() > headerMaxLen) {
                headerMaxLen = header0[i - 1].length();
            }
        }
        int fileSize = 4;//文件版本号
        fileSize += 4;//文件时间指针
        fileSize += 4;//头数量
        fileSize += 4;//头长度
        fileSize += headerMaxLen * header0.length * 4;//文件头
        fileSize += header0.length * data.length * 8;//数据区

        ByteBuffer buffer = ByteBuffer.allocate(fileSize);
        buffer.putInt(version);
        if (DEBUG) {
            System.out.println("version:" + version);
        }
        buffer.putInt(current);
        if (DEBUG) {
            System.out.println("current:" + current);
        }
        buffer.putInt(header0.length);
        if (DEBUG) {
            System.out.println("head size:" + header0.length);
        }
        buffer.putInt(headerMaxLen);
        if (DEBUG) {
            System.out.println("head length:" + headerMaxLen);
        }
        for (int i = 0; i < header0.length; i++) {
            header0[i] = fill(header0[i], true, ' ', headerMaxLen);
            char[] chars = header0[i].toCharArray();
            for (char c : chars) {
                buffer.putChar(c);
            }
            if (DEBUG) {
                System.out.println("header:" + header0[i]);
            }
        }
        buffer.putInt(header0.length);
        if (DEBUG) {
            System.out.println("head size:" + header0.length);
        }
        buffer.putInt(data.length);
        if (DEBUG) {
            System.out.println("data size:" + header0.length);
        }
        for (int i = 0; i < data.length; i++) {
            for (int j = 1; j < header.length; j++) {
                buffer.putLong(data[i][j]);
                if (DEBUG) {
                    System.out.println("data[" + i + "][" + j + "]:" + data[i][j]);
                }
            }
        }
        buffer.flip();
        channel.write(buffer);
    }

    String fill(String in, boolean rightFillStyle, char fillChar, int len) {
        String str = in;
        while (str.length() < len) {
            str = rightFillStyle ? str + fillChar : fillChar + str;
        }
        return str;
    }
}