package edu.hawaii.omr;

import java.io.File;
import java.io.FileWriter;

public class Helpers {
  private Helpers() {
    // Private constructor
  }

  public static void deleteFolder(String path) {
    File folder = new File(path);
    if (folder.exists()) {
      if (folder.isDirectory()) {
        String[] files = folder.list();
        for (String fileName : files) {
          deleteFolder(path + "/" + fileName);
        }
      }
      folder.delete();
    }
  }

  public static void writeToFile(String filePath, String contents) {
    try {
      FileWriter writer = new FileWriter(filePath);
      writer.write(contents);
      writer.close();
    }
    catch (Exception e) {

    }
  }

  public static void makeFolder(String folderPath) {
    File folder = new File(folderPath);
    folder.mkdirs();
  }
}