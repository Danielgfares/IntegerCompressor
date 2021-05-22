package main.java;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

public class IntegerArrayCompressor {

    private final String file1;
    private final String file2;
    private final int d;
    private DeflaterOutputStream dos;
    private InflaterInputStream iis;

    public IntegerArrayCompressor(String file1, String file2, int d) {
        this.file1 = file1;
        this.file2 = file2;
        this.d = d;
    }

    public void startProgram() {
        if (this.d == 0) {
            try {
                this.startProgramCompressor();
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        } else {
            try {
                this.startProgramDecompressor();
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    private void startProgramCompressor() throws IOException {
        int[] dataToCompress;
        FileOutputStream fos;

        dataToCompress = readFileIntegers();

        try {
            fos = new FileOutputStream(this.file2);
        } catch (IOException e) {
            throw new IOException(String.format("Exception occurred while trying to open '%s'.\n " +
                    "Make sure path to this file is correct", this.file2));
        }

        this.dos = new DeflaterOutputStream(fos, new Deflater(Deflater.DEFAULT_COMPRESSION, true), 4096, true);

        try {
            for (int datum : dataToCompress) {
                write_int32(datum);
            }
        } catch (IOException e) {
            throw new IOException(e.getMessage());
        }

        try {
            this.dos.close();
            fos.close();
        } catch (IOException e) {
            throw new IOException(String.format("Exception occurred while trying to close '%s'.\n", this.file2));
        }
    }

    private int[] readFileIntegers() throws IOException {
        FileReader fileReader;
        BufferedReader reader;
        int[] readiedData = null;
        String line;
        try {
            fileReader = new FileReader(this.file1);
            reader = new BufferedReader(fileReader);
            try {
                while ((line = reader.readLine()) != null) {
                    readiedData = Arrays.stream(line.split(",")).mapToInt(Integer::parseInt).toArray();
                }
            } catch (IOException e) {
                throw new IOException(
                        String.format("Exception occurred while trying to read line from '%s'.\n", this.file1));
            } finally {
                reader.close();
            }
        } catch (FileNotFoundException e) {
            throw new IOException(String.format("Exception occurred while trying to open '%s'.\n", this.file1));
        } catch (IOException e) {
            throw new IOException(String.format("Exception occurred while trying to close '%s'.\n", this.file1));
        }
        return readiedData;
    }

    private void write_int32(int number) throws IOException {
        byte[] bytes = int32ToBytes(number, Endianness.BIG_ENDIAN);
        dos.write(bytes, 0, 4);
    }

    private byte[] int32ToBytes(int number, Endianness endianness) {
        byte[] bytes = new byte[4];

        if (Endianness.BIG_ENDIAN == endianness) {
            bytes[0] = (byte) ((number >> 24) & 0xFF);
            bytes[1] = (byte) ((number >> 16) & 0xFF);
            bytes[2] = (byte) ((number >> 8) & 0xFF);
            bytes[3] = (byte) (number & 0xFF);
        } else {
            bytes[0] = (byte) (number & 0xFF);
            bytes[1] = (byte) ((number >> 8) & 0xFF);
            bytes[2] = (byte) ((number >> 16) & 0xFF);
            bytes[3] = (byte) ((number >> 24) & 0xFF);
        }
        return bytes;
    }

    // *************************** //
    //          Decompressor       //
    // *************************** //

    public void startProgramDecompressor() throws IOException {

        FileOutputStream fos;
        FileInputStream fis;
        int[] decompressedData;
        PrintStream outputWriter;

        try {
            fis = new FileInputStream(this.file1);
        } catch (IOException e) {
            throw new IOException(String.format("Exception occurred while trying to open '%s'.\n " +
                    "Make sure path to this file is correct", this.file1));
        }

        this.iis = new InflaterInputStream(fis, new Inflater(true), 4096);

        try {
            fos = new FileOutputStream(this.file2);
        } catch (IOException e) {
            throw new IOException(String.format("Exception occurred while trying to open '%s'.\n " +
                    "Make sure path to this file is correct", this.file2));
        }

        decompressedData = readFileBytes();
        outputWriter = new PrintStream(fos);
        outputWriter.print(Arrays.toString(decompressedData).replaceAll("\\[|\\]|\\s", ""));

        try {
            iis.close();
            fis.close();
            outputWriter.close();
            fos.close();
        } catch (IOException e) {
            throw new IOException(String.format("Exception occurred while trying to close '%s'.\n", this.file2));
        }
    }

    private int[] readFileBytes() {
        List<Integer> integers = new ArrayList<>();
        int decompressedInteger;
        boolean error = false;
        do {
            try {
                decompressedInteger = read_int32();
                integers.add(decompressedInteger);
            } catch (IOException e) {
                error = true;
            }
        } while (!error);

        return integers.stream().mapToInt(i -> i).toArray();
    }

    private int read_int32() throws IOException {
        byte[] bytes = read_bytes(4);
        return bytesToInt32(bytes, Endianness.BIG_ENDIAN);
    }

    private byte[] read_bytes(int numBytes) throws IOException {
        int len = 0;
        byte[] b = new byte[numBytes];
        int bytesRead;
        do {
            bytesRead = iis.read(b, len, numBytes - len);
            if (bytesRead == -1)
                throw new IOException("Broken Pipe");
            len += bytesRead;
        } while (len < numBytes);
        return b;
    }

    private int bytesToInt32(byte[] bytes, Endianness endianness) {
        int number;

        if (Endianness.BIG_ENDIAN == endianness) {
            number = ((bytes[0] & 0xFF) << 24) | ((bytes[1] & 0xFF) << 16) |
                    ((bytes[2] & 0xFF) << 8) | (bytes[3] & 0xFF);
        } else {
            number = (bytes[0] & 0xFF) | ((bytes[1] & 0xFF) << 8) |
                    ((bytes[2] & 0xFF) << 16) | ((bytes[3] & 0xFF) << 24);
        }
        return number;
    }

    private enum Endianness {
        BIG_ENDIAN,
        LITTLE_ENDIAN
    }
}
