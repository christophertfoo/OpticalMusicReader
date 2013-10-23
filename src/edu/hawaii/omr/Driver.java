package edu.hawaii.omr;

import java.awt.image.BufferedImage;

public class Driver {
  public static void main(String[] args) {
    BufferedImage sheet = Helpers.readImage("YaGottaTry_1.gif");
    if (sheet != null) {
      try {
        StaffLineFinder lineFinder = new StaffLineFinder(sheet);
        int[] histogram = lineFinder.getVerticalHistogram();
        Helpers.histogramToCsv(histogram, "histogram1.csv");
        int[] maxima = lineFinder.findMaxima(histogram);
        Helpers.histogramToCsv(maxima, "maxima1.csv");
        
        int[] thresholded = lineFinder.thresholdHistogram(histogram, 0.6);
        Helpers.histogramToCsv(thresholded, "thresh1.csv");
        
        int[] maximaThresh = lineFinder.findMaxima(thresholded);
        Helpers.histogramToCsv(maximaThresh, "maxThres1.csv");
      }
      catch (NotGrayscaleException e) {
        System.out.println("Error: Image is not grayscale!");
      }
    }
  }

}
