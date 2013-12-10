package edu.hawaii.omr;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.opencv.core.Point;

public class OmrPoint extends Point implements Cloneable {

  public OmrPoint(int x, int y) {
    super(x, y);
  }

  public static List<Point> toOpenCvPoint(List<OmrPoint> points) {
    List<Point> converted = new ArrayList<>();
    for (OmrPoint point : points) {
      converted.add(new Point(point.x, point.y));
    }
    return converted;
  }

  @Override
  public OmrPoint clone() {
    return new OmrPoint((int) this.x, (int) this.y);
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof OmrPoint) {
      OmrPoint other = (OmrPoint) o;
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

  public static class XComparator implements Comparator<OmrPoint> {

    @Override
    public int compare(OmrPoint o1, OmrPoint o2) {
      double difference = o1.x - o2.x;
      if (Double.compare(difference, 0) == 0) {
        difference = o1.y - o2.y;
      }
      return (int) Math.signum(difference);
    }

  }

  public static class YComparator implements Comparator<OmrPoint> {

    @Override
    public int compare(OmrPoint o1, OmrPoint o2) {
      double difference = o1.y - o2.y;
      if (Double.compare(difference, 0) == 0) {
        difference = o1.x - o2.x;
      }
      return (int) Math.signum(difference);      
    }

  }
}
