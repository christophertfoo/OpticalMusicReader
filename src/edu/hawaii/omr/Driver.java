package edu.hawaii.omr;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

public class Driver {
  static {
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
  }

  public static void main(String[] args) {

    ImageMatrix image = ImageMatrix.readImage("synthetic.bmp", Highgui.CV_LOAD_IMAGE_GRAYSCALE);
    LineMatrix lines = new LineMatrix(image.rows(), image.cols(), CvType.CV_8UC1);
    
    // Do some image clean up
    image.makeBinary();
    image.invert();
    image.close(Imgproc.MORPH_RECT, 3, 3);
    
    // Find the lines
    Imgproc.HoughLinesP(image, lines, 1, Math.PI / 90, 10, image.cols() / 5.0, 10);

    // Extend the lines
    lines.extendLines(image.cols() - 1);
    
    // Convert the lines into an image
    ImageMatrix lineImage = lines.toImageMatrix(image);
      
    // Perform subtractions
    image.subtractImage(lineImage).invert().write("diff.png");
    image.subtractImagePreserve(lineImage, false).invert().write("diff4.png");
    image.subtractImagePreserve(lineImage, true).invert().write("diff8.png");
    
    MeasureDetection measureMat = new MeasureDetection(Highgui.imread("diff4.png", Highgui.CV_LOAD_IMAGE_GRAYSCALE));
    measureMat.setStaffLineHeight(lineImage);
    measureMat.detectMeasure();
    
   // NoteHead noteheadtest = new NoteHead(19);
    //noteheadtest.findNotes(lineImage);
  }

}
