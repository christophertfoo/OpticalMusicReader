package edu.hawaii.omr;

import java.util.Comparator;

public class Staff {
  private StaffLine[] lines;

  private int leftBound;
  private int rightBound;
  private int topBound;
  private int bottomBound;

  public Staff(int topBound, int bottomBound, int leftBound, int rightBound) {
    this.topBound = topBound;
    this.bottomBound = bottomBound;
    this.leftBound = leftBound;
    this.rightBound = rightBound;
  }
  
  public Staff(StaffLine line1, StaffLine line2, StaffLine line3, StaffLine line4, StaffLine line5) {
    this.lines = new StaffLine[5];
    this.lines[0] = line1;
    this.lines[1] = line2;
    this.lines[2] = line3;
    this.lines[3] = line4;
    this.lines[4] = line5;
    this.setBounds();
  }

  public void addStaff(Staff other) {
    this.lines[0].addStaffLine(other.lines[0]);
    this.lines[1].addStaffLine(other.lines[1]);
    this.lines[2].addStaffLine(other.lines[2]);
    this.lines[3].addStaffLine(other.lines[3]);
    this.lines[4].addStaffLine(other.lines[4]);
    this.setBounds();
  }

  public void addToImage(ImageMatrix image) {
    for (StaffLine line : this.lines) {
      line.addToImage(image);
    }
  }
  
  public void addToImage(ImageMatrix image, StaffInfo info) {
    for (StaffLine line : this.lines) {
      line.addToImage(image, info);
    }
  }

  public StaffLine contains(Point point) {
    int x = point.getX();
    int y = point.getY();
    if (x >= this.leftBound && x <= this.rightBound && y >= this.topBound && y <= this.bottomBound) {
      for (StaffLine line : this.lines) {
        if (line.contains(point)) {
          return line;
        }
      }
    }
    return null;
  }

  public boolean adjacentTo(Staff other) {
    boolean adjacent = true;
    for (int i = 0, length = this.lines.length; adjacent && i < length; i++) {
      adjacent = adjacent && this.lines[i].adjacentTo(other.lines[i]);
    }
    return adjacent;
  }
  
  public int getTopBound() {
    return this.topBound;
  }
  public int getBottomBound() {
    return this.bottomBound;
  }
  public int getLeftBound() {
    return this.leftBound;
  }
  public int getRightBound() {
    return this.rightBound;
  }

  private void setBounds() {
    this.setXBounds();
    this.setYBounds();
  }

  private void setXBounds() {
    int lineValue;
    int leftX = -1;
    int rightX = -1;
    for (StaffLine line : this.lines) {
      lineValue = line.getLeftEdgeX();
      if (leftX == -1) {
        leftX = lineValue;
      }
      else if (leftX > lineValue) {
        leftX = lineValue;
      }

      lineValue = line.getRightEdgeX();
      if (rightX == -1) {
        rightX = lineValue;
      }
      else if (rightX < lineValue) {
        rightX = lineValue;
      }
    }

    this.leftBound = leftX;
    this.rightBound = rightX;
  }

  private void setYBounds() {
    StaffLine first = this.lines[0];
    this.topBound =
        first.getLeftEdgeTopY() > first.getRightEdgeTopY() ? first.getRightEdgeTopY() : first
            .getLeftEdgeTopY();

    StaffLine last = this.lines[4];
    this.bottomBound =
        last.getLeftEdgeBottomY() > last.getRightEdgeBottomY() ? last.getLeftEdgeBottomY() : last
            .getRightEdgeBottomY();
  }
  
  public static class TopComparator implements Comparator<Staff> {

    @Override
    public int compare(Staff o1, Staff o2) {
      
      // Compare top boundaries first
      int difference = o1.topBound - o2.topBound;
      if(difference != 0) {
        return difference;
      }
      // Compare bottom boundaries second
      difference = o1.bottomBound - o2.bottomBound;
      if(difference != 0) {
        return difference;
      }
      // Compare left boundaries third
      difference = o1.leftBound - o2.leftBound;
      if(difference != 0) {
        return difference;
      }
      // Compare right boundaries last
      difference = o1.rightBound - o2.rightBound;
      return difference;
    }
    
  }
}
