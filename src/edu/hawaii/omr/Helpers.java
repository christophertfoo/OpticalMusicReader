package edu.hawaii.omr;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import javax.imageio.ImageIO;

public class Helpers {
  private Helpers() {
    // Private constructor
  }
  
  /**
   * Reads and returns the image at the given path or returns null and prints an error message if a
   * problem occurs.
   * 
   * @param filePath
   *          The path of the image to be read.
   * @return The image as a {@link BufferedImage} or null if the image could not be read.
   */
  public static BufferedImage readImage(String filePath) {
    BufferedImage image = null;
    if (filePath == null) {
      System.out.println("Error: Cannot read from a null file.");
    }
    else {
      try {
        image = ImageIO.read(new File(filePath));
      }
      catch (IOException e) {
        System.out.printf("Error: Could not read from the file \"%s\"\n", filePath);
      }
    }
    return image;
  }

  /**
   * Writes the given image to the given file as a GIF.
   * 
   * @param image
   *          The {@link BufferedImage} to be written.
   * @param outputFilePath
   *          The path of the file that the image will be written to.
   */
  public static void writeGifImage(BufferedImage image, String outputFilePath) {
    boolean validParams = true;
    if (image == null) {
      System.out.println("Error: Cannot write a null image.");
      validParams = false;
    }

    if (outputFilePath == null) {
      System.out.println("Error: Cannot write to a null file.");
    }

    if (validParams) {
      File outputFile = new File(outputFilePath);
      try {
        ImageIO.write(image, "gif", outputFile);
      }
      catch (IOException e) {
        System.out.printf("Error: Could not write to file \"%s\"", outputFilePath);
      }
    }
  }
  
  /**
   * Makes a copy of the given image.
   * 
   * @param image
   *          The {@link BufferedImage} to be copied.
   * @return The copy.
   */
  public static BufferedImage copyImage(BufferedImage image) {
    ColorModel cm = image.getColorModel();
    boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
    WritableRaster raster = image.copyData(null);
    BufferedImage copy = new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    return copy;
  }
  
  /**
   * Creates a binary copy of the given image using the given threshold. Values that equal the
   * threshold are considered to be black.
   * 
   * @param image The {@link BufferedImage} to be thresholded.
   * @param threshold The threshold that will be used to create the binary image.
   * @return A binary copy of the image.
   */
  public static BufferedImage createBinaryImage(BufferedImage image, int threshold) {
    BufferedImage copy = Helpers.copyImage(image);
    ColorModel model = image.getColorModel();
    for (int i = 0; i < image.getHeight(); i++) {
      for (int j = 0; j < image.getWidth(); j++) {
        if (model.getRed(image.getRGB(j, i)) >= threshold) {
          copy.setRGB(j, i, 0xFFFFFFFF);
        }
        else {
          copy.setRGB(j, i, 0xFF000000);
        }
      }
    }
    return copy;
  }
  
  /**
   * Determines if the given image is a grayscale image.
   * 
   * @param image The {@link BufferedImage} to be checked.
   * @return true if the image is grayscale, false otherwise.
   */
  public static boolean isGrayscale(BufferedImage image) {
    boolean grayscale = true;
    int imageHeight = image.getHeight();
    int imageWidth = image.getWidth();
    for (int i = 0; grayscale && i < imageHeight; i++) {
      for (int j = 0; grayscale && j < imageWidth; j++) {
        Color color = new Color(image.getRGB(j, i));
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();

        if (r != g || g != b || b != r) {
          grayscale = false;
        }
      }
    }
    return grayscale;
  }
    
  public static void histogramToCsv(int[] histogram, String csvName) {
    try {
      FileWriter writer = new FileWriter(new File(csvName));
      for (int i = 0; i < histogram.length; i++) {
        writer.write(String.format("%d,%d\n", i, histogram[i]));
      }
      writer.close();
    }
    catch (IOException e) {
      System.err.println(e.getMessage());
    }
  }
  
  public static void histogramToCsv(Map<Integer, Integer> histogram, String csvName) {
    try {
      FileWriter writer = new FileWriter(new File(csvName));
      for (Integer key : histogram.keySet()) {
        writer.write(String.format("%d,%d\n", key, histogram.get(key)));
      }
      writer.close();
    }
    catch (IOException e) {
      System.err.println(e.getMessage());
    }
  }
  
  public static void histogramToCsv(double[] histogram, String csvName) {
    try {
      FileWriter writer = new FileWriter(new File(csvName));
      for (int i = 0; i < histogram.length; i++) {
        writer.write(String.format("%d,%f\n", i, histogram[i]));
      }
      writer.close();
    }
    catch (IOException e) {
      System.err.println(e.getMessage());
    }
  }
}
