package edu.hawaii.omr;

import java.util.Comparator;

public class MeasureLine {

  double yBeginCoordinate;
  double xBeginCoordinate;
  double yEndCoordinate;
  double xEndCoordinate;

  public MeasureLine(double xBeginCoordinate, double yBeginCoordinate, double xEndCoordinate,
      double yEndCoordinate) {
    this.yBeginCoordinate = yBeginCoordinate <= yEndCoordinate ? yBeginCoordinate : yEndCoordinate;
    this.xBeginCoordinate = xBeginCoordinate <= xEndCoordinate ? xBeginCoordinate : xEndCoordinate;
    this.yEndCoordinate = yBeginCoordinate > yEndCoordinate ? yBeginCoordinate : yEndCoordinate;;
    this.xEndCoordinate = xBeginCoordinate > xEndCoordinate ? xBeginCoordinate : xEndCoordinate;;
  }

  public double getyBeginCoordinate() {
    return yBeginCoordinate;
  }

  public double getxBeginCoordinate() {
    return xBeginCoordinate;
  }

  public double getyEndCoordinate() {
    return yEndCoordinate;
  }

  public double getxEndCoordinate() {
    return xEndCoordinate;
  }

  public static class BeginYComparator implements Comparator<MeasureLine> {

    @Override
    public int compare(MeasureLine o1, MeasureLine o2) {
      int difference = (int) Math.round(o1.yBeginCoordinate - o2.yBeginCoordinate);
      if(difference == 0) {
        difference = (int) Math.round(o1.xBeginCoordinate - o2.xBeginCoordinate);
      }
      return difference;
    }
    
  }
  
  public static class BeginXComparator implements Comparator<MeasureLine> {

    @Override
    public int compare(MeasureLine o1, MeasureLine o2) {
      return (int) Math.signum(o1.xBeginCoordinate - o2.xBeginCoordinate);
    }    
  }
}
