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
      System.out.println("Waiting... Press enter to go on...");
      System.in.read();
    }
    catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    System.out.println("Going on...");
    Helpers.deleteFolder("results");
    for (String file : args) {
      System.out.println("Processing " + file + "...");
      SheetMusicMatrix image =
          SheetMusicMatrix.readImage(file, Highgui.CV_LOAD_IMAGE_GRAYSCALE, false);

      // Do some image clean up
      image.close(Imgproc.MORPH_RECT, 3, 3);

      System.out.println("Finding Staff Lines...");
      file = file.substring(0, file.lastIndexOf('.'));
      String folder = "results/" + file + "/lines";
      Helpers.makeFolder("results/" + file + "/lines");
      image.findStaffLines();
      ImageMatrix testLines = image.getStaffLineImage();
      testLines.writeImage(folder + "/lines.png");

      System.out.println("Merging Staff Lines...");
      image.mergeStaffs();
      ImageMatrix lines = image.getStaffLineImage();
      lines.writeImage(folder + "/merged.png");
      image.subtractImagePreserve(lines, false).invert().writeImage(folder + "/removed.png");

      System.out.println("Splitting the image into staffs...");
      List<StaffMatrix> staffs = image.splitImage();

      System.out.println("Splitting the staffs into measures...");

      runStaffTests("results/" + file + "/staffs_line", staffs, false);
      runStaffTests("results/" + file + "/staffs_noline", staffs, true);

      runMeasureTests("results/" + file + "/measures_line", staffs, false);
      runMeasureTests("results/" + file + "/measures_noline", staffs, true);
    }

    System.out.println("Finished.");
  }

  private static void runStaffTests(String folder, List<StaffMatrix> staffs, boolean removeLines) {
    int i = 1;
    Helpers.makeFolder(folder);
    StringBuilder builder = new StringBuilder();
    for (StaffMatrix staff : staffs) {
      staff.findMeasureLines(removeLines);
      staff.getMeasureLineImage().writeImage(folder + "/staff_" + i + ".png");
      MeasureMatrix measure = staff.toMeasureMatrix(removeLines);
      measure.getNoteLocationsImage().writeImage(folder + "/notes_" + i + ".png");
      measure.getPitches(builder);
      i++;
    }
    MusicStringParser parser = new MusicStringParser();
    MusicXmlRenderer renderer = new MusicXmlRenderer();
    parser.addParserListener(renderer);
    parser.parse(new Pattern(builder.toString()));

    String musicXml = renderer.getMusicXMLString();
    Helpers.writeToFile(folder + "/musicXml.xml", musicXml);
    Helpers.writeToFile(folder + "/musicstring.txt", builder.toString());
  }

  private static void runMeasureTests(String folder, List<StaffMatrix> staffs, boolean removeLines) {
    int i = 1;
    Helpers.makeFolder(folder);

    StringBuilder builder = new StringBuilder();
    for (StaffMatrix staff : staffs) {
      List<MeasureMatrix> measures = staff.splitIntoMeasures(removeLines);
      for (MeasureMatrix measure : measures) {
        measure.getNoteLocationsImage().writeImage(folder + "/measure_" + i + ".png");
        measure.getPitches(builder);
        i++;
      }
    }
    MusicStringParser parser = new MusicStringParser();
    MusicXmlRenderer renderer = new MusicXmlRenderer();
    parser.addParserListener(renderer);
    parser.parse(new Pattern(builder.toString()));

    String musicXml = renderer.getMusicXMLString();
    Helpers.writeToFile(folder + "/musicXml.xml", musicXml);
    Helpers.writeToFile(folder + "/musicstring.txt", builder.toString());
  }
}
