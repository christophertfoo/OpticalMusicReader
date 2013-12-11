package edu.hawaii.omr;

import java.util.Comparator;

public class Staff implements Cloneable {
  private StaffLine[] lines;

  private int leftBound;
  private int rightBound;
  private int topBound;
  private int bottomBound;
  private int horizontalCoverage;

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

  public boolean contains(double x, double y, StaffInfo info, int lineIndex) {
    return this.lines[lineIndex].contains(x, y, info);
  }

  public void translateVertically(int amount) {
    for (StaffLine line : this.lines) {
      line.translateVertically(amount);
    }
    this.setBounds();
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

  public int getHorizontalCoverage() {
    return this.horizontalCoverage;
  }

  public String getPitchTreble(double x, double y, StaffInfo info) {
    StringBuilder pitchBuilder = new StringBuilder();
    double halfLineHeight = info.getModeLineHeight() / 2.0;
    int lineDistance = info.getModeLineDistance();
    double lineCenterDistance = info.getModeLineHeight() + info.getModeLineDistance();
    double margin = lineDistance / 4.0 + halfLineHeight;
    if (y >= this.topBound && y <= this.bottomBound) {
      double line1Center = this.lines[0].getLineEquation().calculateY(x);
      double line2Center = this.lines[1].getLineEquation().calculateY(x);
      double line3Center = this.lines[2].getLineEquation().calculateY(x);
      double line4Center = this.lines[3].getLineEquation().calculateY(x);
      double line5Center = this.lines[4].getLineEquation().calculateY(x);

      if (y < line1Center || y <= line1Center + margin) {
        pitchBuilder.append("F5");
      }
      else if (y < line2Center - margin) {
        pitchBuilder.append("E5");
      }
      else if (y <= line2Center + margin) {
        pitchBuilder.append("D5");
      }
      else if (y < line3Center - margin) {
        pitchBuilder.append("C5");
      }
      else if (y <= line3Center + margin) {
        pitchBuilder.append("B4");
      }
      else if (y < line4Center - margin) {
        pitchBuilder.append("A4");
      }
      else if (y <= line4Center + margin) {
        pitchBuilder.append("G4");
      }
      else if (y <= line5Center - margin) {
        pitchBuilder.append("F4");
      }
      else {
        pitchBuilder.append("E4");
      }
    }
    else if (y < this.topBound) {
      double topCenterY = this.lines[0].getLineEquation().calculateY(x);
      int nearestLedgerLine = (int) Math.round((topCenterY - y) / lineCenterDistance);
      double nearestLedgerCenter = topCenterY - (nearestLedgerLine * lineCenterDistance);

      if (y >= nearestLedgerCenter - margin && y <= nearestLedgerCenter + margin) {
        pitchBuilder.append(getPitchAboveTreble(nearestLedgerLine, false));
      }
      else {
        pitchBuilder.append(getPitchAboveTreble(y < nearestLedgerCenter ? nearestLedgerLine
            : nearestLedgerLine - 1, true));
      }
    }
    else {
      double bottomCenterY = this.lines[4].getLineEquation().calculateY(x);
      int nearestLedgerLine = (int) Math.round((y - bottomCenterY) / lineCenterDistance);
      double nearestLedgerCenter = bottomCenterY + (nearestLedgerLine * lineCenterDistance);

      if (y >= nearestLedgerCenter - margin && y <= nearestLedgerCenter + margin) {
        pitchBuilder.append(getPitchBelowTreble(nearestLedgerLine, false));
      }
      else {
        pitchBuilder.append(getPitchBelowTreble(y > nearestLedgerCenter ? nearestLedgerLine
            : nearestLedgerLine - 1, true));
      }
    }
    return pitchBuilder.toString();
  }

  private String getPitchAboveTreble(int ledgerLine, boolean half) {
    String pitch = "";
    if (ledgerLine == 0) {
      if (half) {
        pitch = "G5";
      }
      else {
        pitch = "F5";
      }
    }
    else if (ledgerLine == 1) {
      if (half) {
        pitch = "B5";
      }
      else {
        pitch = "A5";
      }
    }
    else if (ledgerLine == 2) {
      if (half) {
        pitch = "D6";
      }
      else {
        pitch = "C6";
      }
    }
    else if (ledgerLine == 3) {
      if (half) {
        pitch = "F6";
      }
      else {
        pitch = "E6";
      }
    }
    else if (ledgerLine == 4) {
      if (half) {
        pitch = "A6";
      }
      else {
        pitch = "G6";
      }
    }
    else if (ledgerLine == 5) {
      if (half) {
        pitch = "C7";
      }
      else {
        pitch = "B6";
      }
    }
    return pitch;
  }

  private String getPitchBelowTreble(int ledgerLine, boolean half) {
    String pitch = "";
    if (ledgerLine == 0) {
      if (half) {
        pitch = "D4";
      }
      else {
        pitch = "E4";
      }
    }
    else if (ledgerLine == 1) {
      if (half) {
        pitch = "B3";
      }
      else {
        pitch = "C4";
      }
    }
    else if (ledgerLine == 2) {
      if (half) {
        pitch = "G3";
      }
      else {
        pitch = "A3";
      }
    }
    else if (ledgerLine == 3) {
      if (half) {
        pitch = "E3";
      }
      else {
        pitch = "F3";
      }
    }
    else if (ledgerLine == 4) {
      if (half) {
        pitch = "C3";
      }
      else {
        pitch = "D3";
      }
    }
    else if (ledgerLine == 5) {
      if (half) {
        pitch = "A2";
      }
      else {
        pitch = "B2";
      }
    }
    return pitch;
  }

  @Override
  public Staff clone() {
    Staff clone =
        new Staff(this.lines[0].clone(), this.lines[1].clone(), this.lines[2].clone(),
            this.lines[3].clone(), this.lines[4].clone());
    return clone;
  }

  void setBounds() {
    this.setXBounds();
    this.setYBounds();
  }

  private void setXBounds() {
    int leftValue;
    int rightValue;
    int lengthValue;
    int leftX = -1;
    int rightX = -1;
    int length = -1;
    for (StaffLine line : this.lines) {
      leftValue = line.getLeftEdgeX();
      if (leftX == -1) {
        leftX = leftValue;
      }
      else if (leftX > leftValue) {
        leftX = leftValue;
      }

      rightValue = line.getRightEdgeX();
      if (rightX == -1) {
        rightX = rightValue;
      }
      else if (rightX < rightValue) {
        rightX = rightValue;
      }

      lengthValue = line.getHorizontalCoverage();
      if (length == -1) {
        length = lengthValue;
      }
      else if (length > lengthValue) {
        length = lengthValue;
      }
    }

    this.leftBound = leftX;
    this.rightBound = rightX;
    this.horizontalCoverage = length;
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
      if (difference != 0) {
        return difference;
      }
      // Compare bottom boundaries second
      difference = o1.bottomBound - o2.bottomBound;
      if (difference != 0) {
        return difference;
      }
      // Compare left boundaries third
      difference = o1.leftBound - o2.leftBound;
      if (difference != 0) {
        return difference;
      }
      // Compare right boundaries last
      difference = o1.rightBound - o2.rightBound;
      return difference;
    }

  }

  public static class BottomComparator implements Comparator<Staff> {

    @Override
    public int compare(Staff o1, Staff o2) {

      // Compare bottom boundaries first
      int difference = o1.bottomBound - o2.bottomBound;
      if (difference != 0) {
        return difference;
      }
      // Compare top boundaries second
      difference = o1.topBound - o2.topBound;
      if (difference != 0) {
        return difference;
      }
      // Compare left boundaries third
      difference = o1.leftBound - o2.leftBound;
      if (difference != 0) {
        return difference;
      }
      // Compare right boundaries last
      difference = o1.rightBound - o2.rightBound;
      return difference;
    }
  }
}
