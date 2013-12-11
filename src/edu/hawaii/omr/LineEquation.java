package edu.hawaii.omr;

public class LineEquation {
  private double slope;
  private double xIntercept;

  public LineEquation(double x0, double y0, double x1, double y1) {
    this.slope = (y1 - y0) / ((double) x1 - x0);
    this.xIntercept = y0 - (slope * x0);
  }

  public double calculateY(double x) {
    return (this.slope * x) + this.xIntercept;
  }

  public double calculateX(double y) {
    return (y - this.xIntercept) / this.slope;
  }
}
