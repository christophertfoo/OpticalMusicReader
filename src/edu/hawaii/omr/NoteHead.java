package edu.hawaii.omr;

public class NoteHead implements Comparable<NoteHead> {

  private int yCoordinate;
  private int xCoordinate;
  private static int noteWidth;

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
