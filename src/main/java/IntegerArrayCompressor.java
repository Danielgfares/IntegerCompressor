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
    private Endianness endianness;

    public IntegerArrayCompressor(String file1, String file2, int d) {
        this.file1 = file1;
        this.file2 = file2;
        this.d = d;
        endianness = Endianness.BIG_ENDIAN;
    }

    /**
     * in case the user prefier to use little endian instead of the default methode
     * @param e value 0 for default and any other decimal value for little endian
     */
    public void setIntegerBytesRepresentation(int e) {
        if (e == 0) {
            endianness = Endianness.BIG_ENDIAN;
        } else {
            endianness = Endianness.LITTLE_ENDIAN;
        }
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

    /**
     * Main methode to compress a data from a file
     *
     * @throws IOException
     */
    private void startProgramCompressor() throws IOException {

        int[] dataToCompress;
        FileOutputStream fos;
        // open and read integer from file1
        dataToCompress = readFileIntegers();

        try {
            fos = new FileOutputStream(this.file2);
        } catch (IOException e) {
            throw new IOException(String.format("Exception occurred while trying to open '%s'.\n " +
                    "Make sure path to this file is correct", this.file2));
        }
        // use a deflater to compress data with buffer size of 4069
        this.dos = new DeflaterOutputStream(fos, new Deflater(Deflater.DEFAULT_COMPRESSION, true), 4096, true);

        try {
            // compress and write data to file2
            for (int datum : dataToCompress) {
                write_int32(datum);
            }
        } catch (IOException e) {
            throw new IOException(e.getMessage());
        }

        try {
            // close files
            this.dos.close();
            fos.close();
        } catch (IOException e) {
            throw new IOException(String.format("Exception occurred while trying to close '%s'.\n", this.file2));
        }
    }

    /**
     * read file of integers separated with comma
     * @return array od int
     * @throws IOException while opening, reading and closing file1
     */
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

    /**
     * convert int to byte and compress it with deflater and finally write it to a file
     * @param number number to compress
     * @throws IOException deflater output stream
     */
    private void write_int32(int number) throws IOException {
        byte[] bytes = int32ToBytes(number, endianness);
        dos.write(bytes, 0, 4);
    }

    /**
     * convert int to bytes
     * @param number number to convert
     * @param endianness decides the order of bytes
     * @return array of bytes with size of int
     */
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

    /**
     * Main methode to compress a data from a file
     * @throws IOException
     */
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

        // inflater to decompress the data
        this.iis = new InflaterInputStream(fis, new Inflater(true), 4096);

        try {
            fos = new FileOutputStream(this.file2);
        } catch (IOException e) {
            throw new IOException(String.format("Exception occurred while trying to open '%s'.\n " +
                    "Make sure path to this file is correct", this.file2));
        }

        // read and decompress the compressed file
        // while reading decompress the bytes readied with inflater
        // then convert those bytes to int
        decompressedData = readFileBytes();

        outputWriter = new PrintStream(fos);
        // write in one line decompressed data (integers) separated with comma
        outputWriter.print(Arrays.toString(decompressedData).replaceAll("\\[|\\]|\\s", ""));

        try {
            // close files
            iis.close();
            fis.close();
            outputWriter.close();
            fos.close();
        } catch (IOException e) {
            throw new IOException(String.format("Exception occurred while trying to close '%s'.\n", this.file2));
        }
    }

    /**
     * read bytes from file
     * uses an inflater to decompress the readied data
     * @return return an array of ints
     */
    private int[] readFileBytes() {
        List<Integer> integers = new ArrayList<>();
        int decompressedInteger;
        boolean error = false;
        do {
            try {
                // keep reading while there is data
                decompressedInteger = read_int32();
                // add int to the list
                integers.add(decompressedInteger);
            } catch (IOException e) {
                error = true;
            }
        } while (!error);

        // convert a list of integers to array of int
        return integers.stream().mapToInt(i -> i).toArray();
    }

    /**
     *  read an int from file
     * @return readied int
     * @throws IOException
     */
    private int read_int32() throws IOException {
        // read four bytes
        byte[] bytes = read_bytes(4);
        // convert to int
        return bytesToInt32(bytes, endianness);
    }

    /**
     * make sure that 4 bytes where readied
     * @param numBytes number of bytes to read
     * @return array of bytes with size of 4
     * @throws IOException
     */
    private byte[] read_bytes(int numBytes) throws IOException {
        int len = 0;
        byte[] b = new byte[numBytes];
        int bytesRead;
        do {
            // read 4 bytes
            // iis uses an inflater while reading the bytes
            bytesRead = iis.read(b, len, numBytes - len);
            if (bytesRead == -1)
                throw new IOException("Broken Pipe");
            len += bytesRead;
        } while (len < numBytes);
        return b;
    }

    /**
     * convert 4 bytes to int
     * @param bytes array of bytes with size 4
     * @param endianness order of bytes
     * @return int
     */
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
