package edu.hawaii.omr;

import java.awt.image.BufferedImage;

public class Driver {
  public static void main(String[] args) {
    BufferedImage sheet = Helpers.readImage("YaGottaTry_1.gif");
    if (sheet != null) {
      try {
        StaffLineFinder lineFinder = new StaffLineFinder(sheet);
        int[] histogram = lineFinder.getVerticalHistogram();
        Helpers.histogramToCsv(histogram, "histogram.csv");
        int[] maxima = lineFinder.findMaxima(histogram);
        Helpers.histogramToCsv(histogram, "maxima.csv");        
      }
      catch (NotGrayscaleException e) {
        System.out.println("Error: Image is not grayscale!");
      }
    }
  }

}
