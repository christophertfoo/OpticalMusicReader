package edu.hawaii.omr;

import java.awt.image.BufferedImage;
import java.awt.image.ImageProducer;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

public class Driver {
  static {
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
  }

  public static void main(String[] args) {

    Mat image = Highgui.imread("synthetic.bmp", Highgui.CV_LOAD_IMAGE_GRAYSCALE);
    Mat dilated = image.clone();
    Mat closed = image.clone();
    Mat lines = new Mat(image.rows(), image.cols(), CvType.CV_8UC1);
    Mat binary = image.clone();
    Imgproc.threshold(image, binary, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
    Highgui.imwrite("binary.png", binary);
    Mat inverted =  Helpers.inverseMat(binary);
    Mat edges = image.clone();
    Mat se = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
    Imgproc.dilate(inverted, dilated, se);
    Highgui.imwrite("dilated.png", dilated);
    Imgproc.erode(dilated, closed, se);
    Highgui.imwrite("closed.png", closed);
//    Imgproc.Canny(binary, edges, 0.05, 0.1);
//    Highgui.imwrite("edges.png", edges);
    Imgproc.HoughLinesP(closed, lines, 1, Math.PI / 90, 10, closed.cols() / 5.0, 10);
    System.out.println(lines.dump());
    int[] points = new int[4];
    Mat lineMat = new Mat(image.rows(), image.cols(), CvType.CV_8UC1);
    Mat extendedLines = Helpers.extendLines(lines, image.cols() - 1);
    for (int i = 0, size = (int) extendedLines.size().width; i < size; i++) {
      extendedLines.get(0, i, points);
      int x0 = points[0], y0 = points[1], x1 = points[2], y1 = points[3];
      Core.line(lineMat, new org.opencv.core.Point(x0, y0), new org.opencv.core.Point(x1, y1),
          new Scalar(255));
    }
    Highgui.imwrite("test.png", lineMat);
    
    Highgui.imwrite("diff4.png", Helpers.inverseMat(Helpers.subtractMatPreserve(inverted, lineMat, false)));
    Highgui.imwrite("diff8.png", Helpers.inverseMat(Helpers.subtractMatPreserve(inverted, lineMat, true)));
    // BufferedImage sheet = Helpers.readImage("YaGottaTry_1.gif");
    // if (sheet != null) {
    // try {
    // StaffLineFinder lineFinder = new StaffLineFinder(sheet);
    //
    // StaffInfo info = lineFinder.getStaffInfo();
    //
    // System.out.println("Mode Distance: " + info.getModeDistance());
    // System.out.println("Mode Height: " + info.getModeHeight());
    //
    // Range distanceRange = info.getModeRangeDistance(0.25);
    // Range heightRange = info.getModeRangeHeight(0.25);
    //
    // System.out.println("Distance Range: [" + distanceRange.getLowerBound() + ", "
    // + distanceRange.getUpperBound() + "]");
    // System.out.println("Height Range: [" + heightRange.getLowerBound() + ", "
    // + heightRange.getUpperBound() + "]");
    //
    // BufferedImage lines = lineFinder.findStaffLines(info);
    // Helpers.writeGifImage(lines, "lines.gif");
    //
    // BufferedImage noLines =
    // Morph.subtract(lineFinder.getImage(), lines, StaffLineFinder.foregroundColor,
    // StaffLineFinder.backgroundColor, Runtime.getRuntime().availableProcessors());
    // Helpers.writeGifImage(noLines, "nolines.gif");
    //
    // BufferedImage extended =
    // Morph.extend(lines, StaffLineFinder.foregroundColor, Runtime.getRuntime()
    // .availableProcessors());
    // BufferedImage noLinesEx =
    // Morph.subtract(lineFinder.getImage(), extended, StaffLineFinder.foregroundColor,
    // StaffLineFinder.backgroundColor, Runtime.getRuntime().availableProcessors());
    // Helpers.writeGifImage(noLinesEx, "nolines-extended.gif");
    //
    // // lineFinder.getConnectedHistogram(0.25);
    //
    // // int[] histogram = lineFinder.getVerticalHistogram();
    // // Helpers.histogramToCsv(histogram, "histogram1.csv");
    // // int[] maxima = lineFinder.findMaxima(histogram);
    // // Helpers.histogramToCsv(maxima, "maxima1.csv");
    // //
    // // int[] thresholded = lineFinder.thresholdHistogram(histogram, 0.6);
    // // Helpers.histogramToCsv(thresholded, "thresh1.csv");
    // //
    // // int[] maximaThresh = lineFinder.findMaxima(thresholded);
    // // Helpers.histogramToCsv(maximaThresh, "maxThres1.csv");
    // }
    // catch (NotGrayscaleException e) {
    // System.out.println("Error: Image is not grayscale!");
    // }
    // }
  }

}
