package main.java;

public class Main {

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
                } else if ( index == 1) {
                    file1 = args[1];
                } else if ( index == 2 ) {
                    file2 = args[2];
                }
                index++;
            } while (!error && index < args.length);

            if (error){
                print_help();
            } else {
                try {
                    program = new IntegerArrayCompressor(file1, file2, decompress);
                    program.startProgram();
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                    print_help();
                }
            }
        }
    }

    public static void print_help() {
        System.out.println("Java –jar compressor.jar –[c/d] file1 file2");
    }



}

