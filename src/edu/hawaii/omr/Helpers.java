package edu.hawaii.omr;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import org.opencv.core.Mat;

public class Helpers {
  private Helpers() {
    // Private constructor
  }

  public static Mat inverseMat(Mat original) {
    Mat inverted = original.clone();
    int numChannels = inverted.channels();

    for (int y = 0, height = inverted.rows(); y < height; y++) {
      for (int x = 0, width = inverted.width(); x < width; x++) {
        double[] values = inverted.get(y, x);
        for (int i = 0; i < numChannels; i++) {
          values[i] = 255 - values[i];
        }
        inverted.put(y, x, values);
      }
    }

    return inverted;
  }

  public static Mat extendLines(Mat lineMat, int endX) {
    return Helpers.extendLines(lineMat, 0, endX);
  }

  public static Mat extendLines(Mat lineMat, int startX, int endX) {
    Mat extended = lineMat.clone();
    int[] points = new int[4];
    for (int i = 0, numLines = (int) lineMat.size().width; i < numLines; i++) {
      lineMat.get(0, i, points);
      int x0 = points[0];
      int y0 = points[1];
      int x1 = points[2];
      int y1 = points[3];

      LineEquation currentLine = new LineEquation(x0, y0, x1, y1);
      int[] newPoints = new int[4];
      newPoints[0] = startX;
      newPoints[1] = (int) Math.round(currentLine.calculateY(startX));
      newPoints[2] = endX;
      newPoints[3] = (int) Math.round(currentLine.calculateY(endX));
      extended.put(0, i, newPoints);
    }
    return extended;
  }

  public static Mat subtractMat(Mat first, Mat second) {
    Mat difference = first.clone();

    int numChannels = difference.channels();

    double[] newValues = new double[numChannels];
    for (int i = 0; i < numChannels; i++) {
      newValues[i] = 0;
    }

    for (int y = 0, height = difference.rows(); y < height; y++) {
      for (int x = 0, width = difference.width(); x < width; x++) {
        double[] firstValues = difference.get(y, x);
        double[] secondValues = second.get(y, x);

        boolean isMatch = true;
        for (int i = 0; i < numChannels; i++) {
          if (Double.compare(firstValues[i], secondValues[i]) != 0) {
            isMatch = false;
            break;
          }
        }
        if (isMatch) {
          difference.put(y, x, newValues);
        }
      }
    }

    return difference;
  }

  public static Mat subtractMatPreserve(Mat first, Mat second, boolean eightWay) {
    Mat difference = first.clone();

    int numChannels = difference.channels();

    double[] newValues = new double[numChannels];
    for (int i = 0; i < numChannels; i++) {
      newValues[i] = 0;
    }

    for (int y = 0, height = difference.rows(); y < height; y++) {
      for (int x = 0, width = difference.width(); x < width; x++) {
        double[] firstValues = difference.get(y, x);
        double[] secondValues = second.get(y, x);

        boolean isMatch = true;
        for (int i = 0; i < numChannels; i++) {
          if (Double.compare(firstValues[i], secondValues[i]) != 0) {
            isMatch = false;
            break;
          }
        }
        if (isMatch) {
          int yOffset = 0;
          boolean preserve = false;
          while (y + yOffset < second.rows() - 1 && second.get(y + yOffset, x)[0] == 255) {
            yOffset++;
          }
          if (first.get(y + yOffset, x)[0] == 255) {
            preserve = true;
          }
          else if(eightWay && yOffset > 0) {
            if(x > 0 && first.get(y + yOffset, x - 1)[0] == 255) {
              preserve = true;
            }
            else if(x < first.cols() - 1 && first.get(y + yOffset, x + 1)[0] == 255) {
              preserve = true;
            }
          }

          if (!preserve) {
            yOffset = 0;
            while (y + yOffset > 0 && second.get(y + yOffset, x)[0] == 255) {
              yOffset--;
            }
            if (first.get(y + yOffset, x)[0] == 255) {
              preserve = true;
            }
            else if(eightWay && yOffset < 0) {
              if(x > 0 && first.get(y + yOffset, x - 1)[0] == 255) {
                preserve = true;
              }
              else if(x < first.cols() - 1 && first.get(y + yOffset, x + 1)[0] == 255) {
                preserve = true;
              }
            }
          }
          if (!preserve) {
            difference.put(y, x, newValues);
          }
        }
      }
    }
    return difference;
  }

  // TODO: The ones below are probably junk and will be deleted later

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
   * @param image
   *          The {@link BufferedImage} to be thresholded.
   * @param threshold
   *          The threshold that will be used to create the binary image.
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
   * @param image
   *          The {@link BufferedImage} to be checked.
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

  /**
   * Writes the given 2D int array to a CSV file at the given path.
   * 
   * @param values
   *          The values to be written.
   * @param path
   *          The path of the resulting CSV file.
   */
  public static void labelsToCsv(int[][] values, String path) {
    try {
      FileWriter writer = new FileWriter(new File(path));
      for (int i = 0; i < values.length; i++) {
        for (int j = 0; j < values[i].length; j++) {
          if (j == 0) {
            writer.write(String.valueOf(values[i][j]));
          }
          else {
            writer.write("," + String.valueOf(values[i][j]));
          }
        }
        writer.write('\n');
      }
      writer.close();
    }
    catch (IOException e) {
      System.err.println(e.getMessage());
    }
  }

  public static float getStdDev(List<Integer> values) {
    float stdDev = 0;
    float average = 0;
    for (int value : values) {
      average += value;
    }
    average /= (float) values.size();

    for (int value : values) {
      stdDev += Math.pow(value - average, 2);
    }
    stdDev /= (float) values.size();

    stdDev = (float) Math.sqrt(stdDev);

    return stdDev;
  }
}
