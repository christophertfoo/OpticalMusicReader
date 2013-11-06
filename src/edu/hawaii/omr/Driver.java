package edu.hawaii.omr;

import java.awt.image.BufferedImage;

public class Driver {
  public static void main(String[] args) {
    BufferedImage sheet = Helpers.readImage("YaGottaTry_2.gif");
    if (sheet != null) {
      try {
        StaffLineFinder lineFinder = new StaffLineFinder(sheet);
        
        StaffInfo info = lineFinder.getStaffInfo();
        
        System.out.println("Mode Distance: " + info.getModeDistance());
        System.out.println("Mode Height: " + info.getModeHeight());
        
        Range distanceRange = info.getModeRangeDistance(0.3333);
        Range heightRange = info.getModeRangeHeight(0.3333);
        
        System.out.println("Distance Range: [" + distanceRange.getLowerBound() + ", " + distanceRange.getUpperBound() + "]");
        System.out.println("Height Range: [" + heightRange.getLowerBound() + ", " + heightRange.getUpperBound() + "]");

        
//        lineFinder.getConnectedHistogram(0.25);
        
//        int[] histogram = lineFinder.getVerticalHistogram();
//        Helpers.histogramToCsv(histogram, "histogram1.csv");
//        int[] maxima = lineFinder.findMaxima(histogram);
//        Helpers.histogramToCsv(maxima, "maxima1.csv");
//        
//        int[] thresholded = lineFinder.thresholdHistogram(histogram, 0.6);
//        Helpers.histogramToCsv(thresholded, "thresh1.csv");
//        
//        int[] maximaThresh = lineFinder.findMaxima(thresholded);
//        Helpers.histogramToCsv(maximaThresh, "maxThres1.csv");
      }
      catch (NotGrayscaleException e) {
        System.out.println("Error: Image is not grayscale!");
      }
    }
  }

}
