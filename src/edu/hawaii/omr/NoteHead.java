package edu.hawaii.omr;

import org.opencv.core.Mat;

public abstract class NoteHead implements Comparable<NoteHead> {

  protected int yCoordinate;
  protected int xCoordinate;
  private static int noteWidth;

  protected double angleRotation;

  protected String type;

  public Mat template = null;

  public boolean adjacentTo(NoteHead fromNote) {
    boolean ret;

    // distance formula used to find if note is adjacent and is a double counted note.
    int width = (int) (noteWidth);
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

  public abstract Mat makeNoteHeadTemplate();

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

  public String getType() {
    return type;
  }
}
