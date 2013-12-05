package edu.hawaii.omr;

import java.util.SortedSet;
import java.util.TreeSet;
import org.opencv.core.Core;
import org.opencv.core.Scalar;

public class StaffLine implements Cloneable {

  private static final Point.XComparator xComparator = new Point.XComparator();
  private static final int lineHeightAdjustment = 1;

  private SortedSet<Point> points;
  
  private int leftEdgeX = -1;
  private int leftEdgeTopY = -1;
  private int leftEdgeBottomY = -1;

  private int rightEdgeX = -1;
  private int rightEdgeTopY = -1;
  private int rightEdgeBottomY = -1;
  
  private int minY = -1;
  private int maxY = -1;

  public StaffLine() {
    this.points = new TreeSet<>(xComparator);
  }

  public void addPoint(Point point) {
    this.points.add(point);

    
    this.leftEdgeX = -1;
    this.leftEdgeTopY = -1;
    this.leftEdgeBottomY = -1;

    this.rightEdgeX = -1;
    this.rightEdgeTopY = -1;
    this.rightEdgeBottomY = -1;
    
    this.minY = -1;
    this.maxY = -1;
  }

  public int getLeftEdgeX() {
    if (this.leftEdgeX == -1) {
      this.setBounds();
    }
    return this.leftEdgeX;
  }

  public int getLeftEdgeTopY() {
    if (this.leftEdgeTopY == -1) {
      this.setBounds();
    }
    return this.leftEdgeTopY;
  }

  public int getLeftEdgeBottomY() {
    if (this.leftEdgeBottomY == -1) {
      this.setBounds();
    }
    return this.leftEdgeBottomY;
  }

  public int getRightEdgeX() {
    if (this.rightEdgeX == -1) {
      this.setBounds();
    }
    return this.rightEdgeX;
  }

  public int getRightEdgeTopY() {
    if (this.rightEdgeTopY == -1) {
      this.setBounds();
    }
    return this.rightEdgeTopY;
  }

  public int getRightEdgeBottomY() {
    if (this.rightEdgeBottomY == -1) {
      this.setBounds();
    }
    return this.rightEdgeBottomY;
  }
  
  public int getMinY() {
    if(this.minY == -1) {
      this.setBounds();
    }
    return this.minY;
  }
  
  public int getMaxY() {
    if(this.maxY == -1) {
      this.setBounds();
    }
    
    return this.maxY;
  }

  public void addStaffLine(StaffLine line) {
    for (Point point : line.points) {
      this.addPoint(point);
    }
  }
  
  public void translateVertically(int amount) {
    
    // Keep pointer to old set of points
    SortedSet<Point> points = this.points;
    
    // Reset the point sets / maps
    this.points = new TreeSet<>(xComparator);
    
    // Re-add the translated points
    for(Point point : points) {
      this.addPoint(new Point(point.getX(), point.getY() + amount));
    }
  }

  public void addToImage(ImageMatrix image) {
    double middleLeft = (this.getLeftEdgeBottomY() + this.getLeftEdgeTopY()) / 2.0;
    int leftX = this.getLeftEdgeX();
    double middleRight = (this.getRightEdgeBottomY() + this.getRightEdgeTopY()) / 2.0;
    int rightX = this.getRightEdgeX();
    int height = this.getMaxY() - this.getMinY();
    if (leftX == rightX) {
      for(Point point : this.points) {
        image.put(point.getY(), point.getX(), 255);
      }
    }
    else {
      Core.line(image, new org.opencv.core.Point(leftX, middleLeft), new org.opencv.core.Point(
          rightX, middleRight), new Scalar(255), height);
    }
  }
  
  public void addToImage(ImageMatrix image, StaffInfo info) {
    double middleLeft = (this.getLeftEdgeBottomY() + this.getLeftEdgeTopY()) / 2.0;
    int leftX = this.getLeftEdgeX();
    double middleRight = (this.getRightEdgeBottomY() + this.getRightEdgeTopY()) / 2.0;
    int rightX = this.getRightEdgeX();
    int height = info.getModeLineHeight(0.33).getUpperBound() + lineHeightAdjustment;
    if (leftX == rightX) {
      for(Point point : this.points) {
        image.put(point.getY(), point.getX(), 255);
      }
    }
    else {
      Core.line(image, new org.opencv.core.Point(leftX, middleLeft), new org.opencv.core.Point(
          rightX, middleRight), new Scalar(255), height);
    }
  }

  public boolean contains(Point point) {
    return this.points.contains(point);
  }
  
  @Override
  public StaffLine clone() {
    StaffLine clone = new StaffLine();
    for(Point point : this.points) {
      clone.addPoint(new Point(point.getX(), point.getY()));
    }
    return clone;
  }
  
  private void setBounds() {
    this.leftEdgeX = this.points.first().getX();
    this.rightEdgeX = this.points.last().getX();
    
    SortedSet<Integer> rows = new TreeSet<Integer>();
    for(Point point : this.points) {
      rows.add(point.getY());
    }
    this.minY = rows.first();
    this.maxY = rows.last();
    
    SortedSet<Point> leftColumn = this.points.subSet(new Point(this.leftEdgeX, 0), new Point(this.leftEdgeX + 1, 0));
    this.leftEdgeTopY = leftColumn.first().getY();
    this.leftEdgeBottomY = leftColumn.last().getY();
    
    SortedSet<Point> rightColumn = this.points.subSet(new Point(this.rightEdgeX, 0), new Point(this.rightEdgeX + 1, 0));
    this.rightEdgeTopY = rightColumn.first().getY();
    this.rightEdgeBottomY = rightColumn.last().getY();
  }
}
