package de.saar.coli.minecraft;

import java.lang.IllegalArgumentException;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.lang.NullPointerException;
import java.io.File;
import java.io.IOException;

public class PrinterMain {
  public static boolean isValidPath(String path) {
    try {
        Paths.get(path);
    } catch (InvalidPathException | NullPointerException ex) {
        return false;
    }
    return true;
  }

  public static boolean fileAlreadyExists(String path) {
    var file = new File(path);
    return file.exists() && file.isFile();
  }

  public static boolean isFolder(String path) {
    var file = new File(path);
    assert file.exists();
    return file.isDirectory();
  }

  //run with ./gradlew irtgPrinter --args="outputpath"
  public static void main(String[] args) {
    // argument handling
    if (args.length != 1) {
      throw new IllegalArgumentException("Provide exactly one argument. The output path.");
    }
    
    String path = args[0];
    if (!isValidPath(path)) {
      throw new IllegalArgumentException("The provided argument is not a valid path.");
    } else if (fileAlreadyExists(path)) {
      throw new IllegalArgumentException("File already exists. Please delete before calling this.");
    } else if (isFolder(path)) {
      path += System.getProperty("file.separator") + "simplified.irtg";
    }

    // transform minecraft.irtg and write to output path
    String transformedString = IRTGTransformer.simplify(MinecraftRealizer.getStandardInput());
    try {
      Files.write(Paths.get(path), transformedString.getBytes());
    } catch (IOException e) {
      throw new UnsupportedOperationException("Wasn't able to write to file.");
    }

    System.out.println("File was succesfully written to '" + path + "'.");
  }

}
