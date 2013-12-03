package edu.hawaii.omr;

import java.io.IOException;
import java.util.Map;
import java.util.SortedSet;
import org.opencv.core.Core;
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

    try {
      System.out.println("Press enter to go on...");
      System.in.read();
    }
    catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    System.out.println("Going on...");
    SheetMusicMatrix image =
        SheetMusicMatrix.readImage("YaGottaTry_1.png", Highgui.CV_LOAD_IMAGE_GRAYSCALE);

    // Do some image clean up
    image.makeBinary();
    image.invert();
    image.close(Imgproc.MORPH_RECT, 3, 3);

     StaffInfo info = image.getStaffInfo();
     ImageMatrix testLines = image.findStaffLines(info);
     testLines.writeImage("lines.png");
     
     ConnectedComponentFinder finder = new ConnectedComponentFinder();
     finder.findConnectedComponents(testLines, 255);
     Map<Integer, SortedSet<Point>> labelMap = finder.makeLabelMap();
     image.mergeConnectedStaffs(labelMap);
     image.getStaffLineImage().writeImage("merged-connected.png");
     image.mergeSeparatedStaffs();
     ImageMatrix mergedLines = image.getStaffLineImage();
     mergedLines.writeImage("merged.png");

     image.subtractImagePreserve(mergedLines, false).invert().writeImage("heuristic.png");
     image.subtractImagePreserve(mergedLines, true).invert().writeImage("heuristic-8.png");
     
     ImageMatrix[] split = image.splitImage();
     for(int i = 0, numSplit = split.length; i < numSplit; i++) {
       split[i].writeImage("split_" + i + ".png");
    
    MeasureDetection measureMat = new MeasureDetection(Highgui.imread("diff4.png", Highgui.CV_LOAD_IMAGE_GRAYSCALE));
    measureMat.setStaffLineHeight(lineImage);
    measureMat.detectMeasure();
    
   // NoteHead noteheadtest = new NoteHead(19);
    //noteheadtest.findNotes(lineImage);
     }

  }
}
