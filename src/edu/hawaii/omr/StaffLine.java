package edu.hawaii.omr;

import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

public class StaffLine {

  private static final Point.XComparator xComparator = new Point.XComparator();
  private static final Point.YComparator yComparator = new Point.YComparator();

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
    if(this.leftEdgeTopY == -1) {
      this.leftEdgeTopY = this.columns.get(this.getLeftEdgeX()).first().getY();
    }
    return this.leftEdgeTopY;
  }
  
  public int getLeftEdgeBottomY() {
    if(this.leftEdgeBottomY == -1) {
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
    if(this.rightEdgeTopY == -1) {
      this.rightEdgeTopY = this.columns.get(this.getRightEdgeX()).first().getY();
    }
    return this.rightEdgeTopY;
  }
  
  public int getRightEdgeBottomY() {
    if(this.rightEdgeBottomY == -1) {
      this.rightEdgeBottomY = this.columns.get(this.getRightEdgeX()).last().getY();
    }
    return this.rightEdgeBottomY;
  }

  public void addStaffLine(StaffLine line) {
    for (int y : line.rows.keySet()) {
      for (Point point : line.rows.get(y)) {
        this.addPoint(point);
      }
    }
  }

  public void addToImage(ImageMatrix image) {
    for (Point point : this.points) {
      image.put((int) point.getY(), (int) point.getX(), 255);
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
