# IntegerCompressor
compress and decompress an array of integer
-	Main Class: make sure the input arguments are correct and start the program.
-	IntegerArrayCompressor Class: this class has two main functions, compress an array of integer and decompress an array of integers. 
  It uses a deflater and inflater to compress and decompress the array. The compressor reads the file and load the array into the 
  program, so later the compressor can iterate this array and convert every integer to bytes and compress these bytes into the other 
  file at the end of the process the result is the output file with all integers compressed. The decompressor works on the same 
  methodology, but the decompressing happens on reading the input file.
