package edu.hawaii.omr;

import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.opencv.core.Core;
import org.opencv.core.Scalar;

public class StaffLine {

  private static final Point.XComparator xComparator = new Point.XComparator();
  private static final Point.YComparator yComparator = new Point.YComparator();
  private static final int lineHeightAdjustment = 1;

  private SortedSet<Point> points;
  private SortedMap<Integer, SortedSet<Point>> rows;
  private SortedMap<Integer, SortedSet<Point>> columns;
  private int leftEdgeX = -1;
  private int leftEdgeTopY = -1;
  private int leftEdgeBottomY = -1;

  private int rightEdgeX = -1;
  private int rightEdgeTopY = -1;
  private int rightEdgeBottomY = -1;

  public StaffLine() {
    this.points = new TreeSet<>(xComparator);
    this.rows = new TreeMap<>();
    this.columns = new TreeMap<>();
  }

  public void addPoint(Point point) {
    int x = (int) point.getX();
    int y = (int) point.getY();
    if (!this.rows.containsKey(y)) {
      this.rows.put(y, new TreeSet<>(xComparator));
    }
    if (!this.columns.containsKey(x)) {
      this.columns.put(x, new TreeSet<>(yComparator));
    }

    this.points.add(point);
    this.rows.get(y).add(point);
    this.columns.get(x).add(point);
    
    this.leftEdgeX = -1;
    this.leftEdgeTopY = -1;
    this.leftEdgeBottomY = -1;

    this.rightEdgeX = -1;
    this.rightEdgeTopY = -1;
    this.rightEdgeBottomY = -1;
  }

  public int getLeftEdgeX() {
    if (this.leftEdgeX == -1) {
      this.leftEdgeX = this.columns.firstKey();
    }
    return this.leftEdgeX;
  }

  public int getLeftEdgeTopY() {
    if (this.leftEdgeTopY == -1) {
      this.leftEdgeTopY = this.columns.get(this.getLeftEdgeX()).first().getY();
    }
    return this.leftEdgeTopY;
  }

  public int getLeftEdgeBottomY() {
    if (this.leftEdgeBottomY == -1) {
      this.leftEdgeBottomY = this.columns.get(this.getLeftEdgeX()).last().getY();
    }
    return this.leftEdgeBottomY;
  }

  public int getRightEdgeX() {
    if (this.rightEdgeX == -1) {
      this.rightEdgeX = this.columns.lastKey();
    }
    return this.rightEdgeX;
  }

  public int getRightEdgeTopY() {
    if (this.rightEdgeTopY == -1) {
      this.rightEdgeTopY = this.columns.get(this.getRightEdgeX()).first().getY();
    }
    return this.rightEdgeTopY;
  }

  public int getRightEdgeBottomY() {
    if (this.rightEdgeBottomY == -1) {
      this.rightEdgeBottomY = this.columns.get(this.getRightEdgeX()).last().getY();
    }
    return this.rightEdgeBottomY;
  }

  public void addStaffLine(StaffLine line) {
    for (Point point : line.points) {
      this.addPoint(point);
    }
  }

  public void addToImage(ImageMatrix image) {
    double middleLeft = (this.getLeftEdgeBottomY() + this.getLeftEdgeTopY()) / 2.0;
    int leftX = this.getLeftEdgeX();
    double middleRight = (this.getRightEdgeBottomY() + this.getRightEdgeTopY()) / 2.0;
    int rightX = this.getRightEdgeX();
    int height = this.rows.lastKey() - this.rows.firstKey();
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

  public boolean adjacentTo(StaffLine other) {
    Point dummy = new Point(0, 0);

    SortedSet<Point> leftEdge = this.columns.get(this.leftEdgeX);
    SortedSet<Point> rightEdge = this.columns.get(this.rightEdgeX);

    boolean adjacent = false;
    dummy.setX(this.leftEdgeX - 1);
    for (Point point : leftEdge) {
      if (other.points.contains(point)) {
        adjacent = true;
        break;
      }
      else if (point.getX() > 0) {
        dummy.setY(point.getY());
        if (other.points.contains(dummy)) {
          adjacent = true;
          break;
        }
      }
    }

    if (!adjacent) {
      dummy.setX(this.rightEdgeX + 1);
      for (Point point : rightEdge) {
        if (other.points.contains(point)) {
          adjacent = true;
          break;
        }
        else if (point.getX() > 0) {
          dummy.setY(point.getY());
          if (other.points.contains(dummy)) {
            adjacent = true;
            break;
          }
        }
      }
    }
    return adjacent;
  }

  public boolean contains(Point point) {
    return this.points.contains(point);
  }
}
