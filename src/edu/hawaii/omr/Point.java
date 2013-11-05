package edu.hawaii.omr;

import java.util.Comparator;

/**
 * A point in a 2D Cartesian Coordinate system.
 * 
 * @author Christopher Foo
 * 
 * @param <E>
 *          The type of the value at that point (i.e. f(x, y)).
 */
public class Point<E> {

  /**
   * The x and y coordinates of the point.
   */
  private long x, y;

  /**
   * The value at that point (i.e. f(x, y)).
   */
  private E value;

  /**
   * Creates a new Point with a null value.
   * 
   * @param x
   *          The x coordinate of the Point.
   * @param y
   *          The y coordinate of the Point.
   */
  public Point(long x, long y) {
    this(x, y, null);
  }

  /**
   * Creates a new Point with the given value.
   * 
   * @param x
   *          The x coordinate of the Point.
   * @param y
   *          The y coordinate of the Point.
   * @param value
   *          The value of at the Point (i.e. f(x, y)).
   */
  public Point(long x, long y, E value) {
    this.x = x;
    this.y = y;
    this.value = value;
  }

  /**
   * Gets the x coordinate of this Point.
   * 
   * @return The x coordinate of this Point.
   */
  public long getX() {
    return this.x;
  }

  /**
   * Sets the x coordinate of this Point.
   * 
   * @param x
   *          The new x coordinate of this Point.
   */
  public void setX(long x) {
    this.x = x;
  }

  /**
   * Gets the y coordinate of this Point.
   * 
   * @return The y coordinate of this Point.
   */
  public long getY() {
    return this.y;
  }

  /**
   * Sets the y coordinate of this Point.
   * 
   * @param y
   *          The new y coordinate of this Point.
   */
  public void setY(long y) {
    this.y = y;
  }

  /**
   * Gets the current value of this Point (i.e. f(x, y)).
   * 
   * @return The current value of this Point.
   */
  public E getValue() {
    return this.value;
  }

  /**
   * Sets the value of this Point (i.e. f(x, y)).
   * 
   * @param value
   *          The new value of this Point.
   */
  public void setValue(E value) {
    this.value = value;
  }

  /**
   * Gets the {@link String} format of this Point.
   */
  @Override
  public String toString() {
    return String.format("(%d, %d, %s)", this.x, this.y, this.value.toString());
  }

  /**
   * Checks if this Point equals the given Object.
   */
  @Override
  public boolean equals(Object otherPoint) {
    return otherPoint == null || !(otherPoint instanceof Point<?>) ? false
        : this.x == ((Point<?>) otherPoint).x && this.y == ((Point<?>) otherPoint).y;
  }
}

/**
 * Compares the x values of two {@link Point}s.
 * 
 * @author Christopher Foo
 * 
 * @param <E>
 *          The type of data stored at that Point (i.e. f(x, y)).
 */
class XComparator<E> implements Comparator<Point<E>> {

  @Override
  public int compare(Point<E> o1, Point<E> o2) {
    int returnCode;
    if (o1 == null && o2 != null) {
      returnCode = 1;
    }
    else if (o2 == null && o1 != null) {
      returnCode = -1;
    }
    else if (o1 == null && o2 == null) {
      returnCode = 0;
    }
    else {
      long x1 = o1.getX();
      long x2 = o2.getX();
      long y1 = o1.getY();
      long y2 = o2.getY();
      if (x1 < x2) {
        returnCode = -1;
      }
      else if (x1 > x2) {
        returnCode = 1;
      }
      else if(y1 < y2){
        returnCode = -1;
      }
      else if(y1 > y2) {
        returnCode = 1;
      }
      else {
        returnCode = 0;
      }
    }
    return returnCode;
  }
}