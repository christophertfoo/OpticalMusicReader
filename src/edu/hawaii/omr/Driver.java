package edu.hawaii.omr;

import java.io.IOException;
import java.util.Map;
import java.util.SortedSet;
import org.opencv.core.Core;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

public class Driver {
  static {
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
  }

  public static void main(String[] args) {

    try {
      System.in.read();
    }
    catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    System.out.println("Going on...");
    SheetMusicMatrix image =
        SheetMusicMatrix.readImage("Acha_1.bmp", Highgui.CV_LOAD_IMAGE_GRAYSCALE);
//    LineMatrix lines = new LineMatrix(image.rows(), image.cols(), CvType.CV_8UC1);

    // Do some image clean up
    image.makeBinary();
    image.invert();
    image.close(Imgproc.MORPH_RECT, 3, 3);
    image.writeImage("fixed.png");
//
//    // Find the lines
//    Imgproc.HoughLinesP(image, lines, 1, Math.PI / 90, 10, image.cols() / 5.0, 10);
//
//    // Extend the lines
//    lines.extendLines(image.cols() - 1);
//
//    // Convert the lines into an image
//    ImageMatrix lineImage = lines.toImageMatrix(image);
//    lineImage.writeImage("lines.png");
//
//    // Perform subtractions
//    image.subtractImage(lineImage).invert().writeImage("diff.png");
//    image.subtractImagePreserve(lineImage, false).invert().writeImage("diff4.png");
//    image.subtractImagePreserve(lineImage, true).invert().writeImage("diff8.png");

     StaffInfo info = image.getStaffInfo();
     ImageMatrix testLines = image.findStaffLines(info);
     testLines.writeImage("test-lines.png");
     
     ConnectedComponentFinder finder = new ConnectedComponentFinder();
     finder.findConnectedComponents(testLines, 255);
     Map<Integer, SortedSet<Point>> labelMap = finder.makeLabelMap();
     image.mergeConnectedStaffs(labelMap);
     image.getStaffLineImage().writeImage("merged.png");
     image.mergeSeparatedStaffs();
     ImageMatrix mergedLines = image.getStaffLineImage();
     mergedLines.writeImage("lines.png");
     image.subtractImage(mergedLines).invert().writeImage("heuristic-sub.png");
     image.subtractImagePreserve(mergedLines, false).invert().writeImage("heuristic.png");
  }
}
