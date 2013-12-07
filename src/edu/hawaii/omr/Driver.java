package edu.hawaii.omr;

import java.io.IOException;
import java.util.List;
import org.jfugue.MusicStringParser;
import org.jfugue.MusicXmlRenderer;
import org.jfugue.Pattern;
import org.opencv.core.Core;
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
        SheetMusicMatrix.readImage("Acha_1.bmp", Highgui.CV_LOAD_IMAGE_GRAYSCALE, false);

    // Do some image clean up
    image.close(Imgproc.MORPH_RECT, 3, 3);

    System.out.println("Finding Staff Lines...");
    image.findStaffLines();
    ImageMatrix testLines = image.getStaffLineImage();
    testLines.writeImage("results/lines.png");

    System.out.println("Merging Staff Lines...");
    image.mergeStaffs();
    ImageMatrix lines = image.getStaffLineImage();
    lines.writeImage("results/merged.png");
    image.subtractImage(lines).invert().writeImage("results/removed-sub.png");
    image.subtractImagePreserve(lines, false).invert().writeImage("results/removed.png");
    
    System.out.println("Splitting the image into staffs...");
    List<StaffMatrix> split = image.splitImage();
    int i = 1;
    int j = 1;
    System.out.println("Splitting the staffs into measures...");
    for (StaffMatrix staff : split) {
      staff.getMeasureLineImage().writeImage("results/staff_"+ j + ".png");
      List<MeasureMatrix> measures = staff.splitIntoMeasures();
      for(ImageMatrix measure : measures) {
        measure.writeImage("results/measure_" + i + ".png");
        i++;
      }
      j++;
    }
  }
}
