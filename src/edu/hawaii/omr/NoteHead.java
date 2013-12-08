package edu.hawaii.omr;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class NoteHead implements Comparable<NoteHead> {

  // 0.8 for cropping
  // 0.72 is also a good candidate
  public static final double templateThreshold = 0.72;
  
  private int yCoordinate;
  private int xCoordinate;
  private static int noteWidth;
  
  public static Mat template = null;

  public NoteHead(int xCoordinate, int yCoordinate) {
    this.xCoordinate = xCoordinate;
    this.yCoordinate = yCoordinate;
  }

  public boolean adjacentTo(NoteHead fromNote) {
    boolean ret;

    // distance formula used to find if note is adjacent and is a double counted note.
    int width = (int) (noteWidth / 2.0);
    double xDistance = Math.pow(xCoordinate - fromNote.getXCoordinate(), 2);
    double yDistance = Math.pow(yCoordinate - fromNote.getYCoordinate(), 2);
    double totalDistance = Math.sqrt(xDistance + yDistance);
    if (totalDistance < width) {
      ret = true;
    }
    else {
      ret = false;
    }
    return ret;
  }

  public static void setNoteWidth(int noteWidth) {
    NoteHead.noteWidth = noteWidth;
  }
  
  public static void makeNoteHeadTemplate(int height) {

    template = new Mat((int) (2 * height), (int) (2 * height), CvType.CV_8UC1);

    for (int y = 0; y > (2 * height * -1); y--) {
      for (int x = 0; x < (2 * height); x++) {
        double value =
            (Math.pow((x - height) * Math.cos(Math.PI / 6) + (y + height) * Math.sin(Math.PI / 6),
                2) / Math.pow(height, 2))
                + (Math.pow(
                    (x - height) * Math.sin(Math.PI / 6) - (y + height) * Math.cos(Math.PI / 6), 2) / Math
                    .pow(height / 2.0, 2));
        if (value < 1) {
          double data[] = { 255 };
          template.put(Math.abs(y), x, data);
        }
        else {
          double data[] = { 0 };
          template.put(Math.abs(y), x, data);
        }
      }
    }
  }

  public int getYCoordinate() {
    return yCoordinate;
  }

  public int getXCoordinate() {
    return xCoordinate;
  }

  @Override
  public int compareTo(NoteHead o) {
    return (int) Math.signum(xCoordinate - o.getXCoordinate());

  }

}
