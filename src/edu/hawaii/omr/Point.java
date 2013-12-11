package edu.hawaii.omr;

import java.util.Comparator;

public class Point implements Cloneable {
  private int x;
  private int y;

  public Point(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public void setX(int x) {
    this.x = x;
  }

  public void setY(int y) {
    this.y = y;
  }

  public int getX() {
    return this.x;
  }

  public int getY() {
    return this.y;
  }

  @Override
  public Point clone() {
    return new Point(this.x, this.y);
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof Point) {
      Point other = (Point) o;
      if (this.x == other.x && this.y == other.y) {
        return true;
      }
    }
    return false;
  }

  @Override
  public String toString() {
    return "(" + this.x + ", " + this.y + ")";
  }

  public static class XComparator implements Comparator<Point> {

    @Override
    public int compare(Point o1, Point o2) {
      int difference = o1.x - o2.x;
      if (difference == 0) {
        return o1.y - o2.y;
      }
      else {
        return difference;
      }
    }

  }

  public static class YComparator implements Comparator<Point> {

    @Override
    public int compare(Point o1, Point o2) {
      int difference = o1.y - o2.y;
      if (difference == 0) {
        return o1.x - o2.x;
      }
      else {
        return difference;
      }
    }

  }
}
