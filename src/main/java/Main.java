package main.java;

public class Main {

    /**
     * console program
     * options
     *      -c initiate compressor program
     *          receives two file - file1: file that contain data to compress
     *                              file2: file or name of file where to to save compressed data
     *
     *      -d initiate decompressor program
     *          receives two file - file1: file that contain data to decompress
     *                                     file2: file or name of file where to to save data decompressed
     * @param args -c/d file1 file2
     */
    public static void main(String[] args) {
        // write your code here
        String file1 = null;
        String file2 = null;
        boolean error = false;
        int decompress = 0;
        IntegerArrayCompressor program;

        if (args.length != 3) {
            System.err.println("Incorrect entry");
            print_help();
        } else {
            int index = 0;

            do {
                if (index == 0) {
                    if (args[0].equals("-d")) {
                        decompress = 1;
                    } else {
                        if (!args[0].equals("-c")) {
                            error = true;
                        }
                    }
                } else if (index == 1) {
                    file1 = args[1];
                } else if (index == 2) {
                    file2 = args[2];
                }
                index++;
            } while (!error && index < args.length);

            if (error) {
                print_help();
            } else {
                try {
                    program = new IntegerArrayCompressor(file1, file2, decompress);
                    program.startProgram();
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
        }
    }

    public static void print_help() {
        System.out.println("Please try: \nJava –jar compressor.jar –[c/d] file1 file2");
    }

}

