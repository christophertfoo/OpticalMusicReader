package edu.hawaii.omr;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A 2D plane modeling a 2D Cartesian Coordinate system. Only handles whole number x and y
 * coordinates.
 * 
 * @author Christopher Foo
 * 
 * @param <E>
 *          The value stored at each {@link Point} (i.e. f(x, y)).
 */
public class Plane<E> {

  /**
   * A dummy {@link Point} to be used for calculations.
   */
  private Point<E> dummyPoint = new Point<E>(0, 0);

  /**
   * A {@link List} of all {@link Point}s in this Plane.
   */
  private List<Point<E>> points;

  /**
   * A {@link Map} between the column indexes (i.e. x values) and the actual column of {@link Point}
   * s in a {@link SortedSet}.
   */
  private Map<Long, SortedSet<Point<E>>> columns;

  /**
   * A {@link Map} between the row indexes (i.e. y values) and the actual row of {@link Point}s in a
   * {@link SortedSet}.
   */
  private Map<Long, SortedSet<Point<E>>> rows;

  /**
   * Creates a new Plane containing the given {@link Point}s.
   * 
   * @param points
   *          A {@link List} of Points used to create the new Plane.
   */
  public Plane(List<Point<E>> points) {
    this.columns = new HashMap<>();
    this.rows = new HashMap<>();
    this.points = new ArrayList<>();
    for (Point<E> point : points) {
      this.addPoint(point);
    }
  }

  /**
   * Adds a new {@link Point} to this Plane.
   * 
   * @param point
   *          The Point to be added.
   */
  public void addPoint(Point<E> point) {
    long x = point.getX();
    long y = point.getY();
    if (!this.columns.containsKey(point.getX())) {
      this.columns.put(x, new TreeSet<>(new YComparator<E>()));
    }
    if (!this.columns.get(x).add(point)) {
      this.columns.get(x).tailSet(point).first().setValue(point.getValue());
    }

    if (!this.rows.containsKey(point.getY())) {
      this.rows.put(y, new TreeSet<>(new XComparator<E>()));
    }
    if (!this.rows.get(y).add(point)) {
      this.rows.get(y).tailSet(point).first().setValue(point.getValue());
    }

    this.points.add(point);
  }

  /**
   * Gets the {@link Point} at the given coordinates if it exists in this Plane or null if it does
   * not exist.
   * 
   * @param x
   *          The x coordinate of the target Point.
   * @param y
   *          The y coordinate of the target Point.
   * @return The Point at the given coordinates or null if it does not exist in this Plane.
   */
  public Point<E> getPoint(long x, long y) {
    Point<E> point = null;
    if (this.rows.containsKey(y) && this.columns.containsKey(x)) {
      this.dummyPoint.setX(x);
      this.dummyPoint.setY(y);
      point = this.rows.get(y).tailSet(this.dummyPoint).first();
    }
    return point;
  }

  /**
   * Gets a copy of the {@link Point}s in this Plane.
   * 
   * @return A {@link List} containing the Points in this Plane.
   */
  public List<Point<E>> getPoints() {
    return new ArrayList<>(this.points);
  }

  /**
   * Gets the row with the given y coordinate from this Plane.
   * 
   * @param y
   *          The y coordinate of the target row.
   * @return The row as a {@link SortedSet}.
   */
  public SortedSet<Point<E>> getRow(long y) {
    SortedSet<Point<E>> row = null;
    if (this.rows.containsKey(y)) {
      row = this.rows.get(y);
    }
    return row;
  }

  /**
   * Gets the column with the given x coordinate from this Plane.
   * 
   * @param x
   *          The x coordinate of the target row.
   * @return The row as a {@link SortedSet}.
   */
  public SortedSet<Point<E>> getColumn(long x) {
    SortedSet<Point<E>> column = null;
    if (this.columns.containsKey(x)) {
      column = this.columns.get(x);
    }
    return column;
  }

  /**
   * Gets the column keys (i.e. x values) that are used in this Plane (i.e. have at least one
   * {@link Point} with that x value).
   * 
   * @return A {@link SortedSet} of the column keys ordered in ascending order.
   */
  public SortedSet<Long> getColumnKeys() {
    return new TreeSet<Long>(this.columns.keySet());
  }

  /**
   * Gets the row keys (i.e. y values) that are used in this Plane (i.e. have at least one
   * {@link Point} with that y value).
   * 
   * @return A {@link SortedSet} of the row keys ordered in ascending order.
   */
  public SortedSet<Long> getRowKeys() {
    return new TreeSet<Long>(this.rows.keySet());
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

/**
 * Compares the y values of two {@link Point}s.
 * 
 * @author Christopher Foo
 * 
 * @param <E>
 *          The type of data stored at that Point (i.e. f(x, y)).
 */
class YComparator<E> implements Comparator<Point<E>> {

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
      long y1 = o1.getY();
      long y2 = o2.getY();
      long x1 = o1.getX();
      long x2 = o2.getX();
      if (y1 < y2) {
        returnCode = -1;
      }
      else if (y1 > y2) {
        returnCode = 1;
      }
      else if(x1 < x2) {
        returnCode = -1;
      }
      else if(x1 > x2) {
        returnCode = 1;
      }
      else {
        returnCode = 0;
      }
    }
    return returnCode;
  }
}
