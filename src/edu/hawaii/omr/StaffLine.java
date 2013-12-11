package edu.hawaii.omr;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class StaffLine implements Cloneable {

  private static final OmrPoint.XComparator xComparator = new OmrPoint.XComparator();
  private static final int lineHeightAdjustment = 2;
  private static final double modeThreshold = 0.33;

  private SortedSet<OmrPoint> points;

  private int leftEdgeX = -1;
  private int leftEdgeTopY = -1;
  private double leftEdgeMiddleY = -1;
  private int leftEdgeBottomY = -1;

  private int rightEdgeX = -1;
  private int rightEdgeTopY = -1;
  private double rightEdgeMiddleY = -1;
  private int rightEdgeBottomY = -1;

  private int minY = -1;
  private int maxY = -1;

  private LineEquation equation = null;

  public StaffLine() {
    this.points = new TreeSet<>(xComparator);
  }

  public void addPoint(OmrPoint point) {
    this.points.add(point);

    this.leftEdgeX = -1;
    this.leftEdgeTopY = -1;
    this.leftEdgeMiddleY = -1;
    this.leftEdgeBottomY = -1;

    this.rightEdgeX = -1;
    this.rightEdgeTopY = -1;
    this.leftEdgeMiddleY = -1;
    this.rightEdgeBottomY = -1;

    this.minY = -1;
    this.maxY = -1;

    this.equation = null;
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

  public double getLeftEdgeMiddleY() {
    if (Double.compare(this.leftEdgeMiddleY, -1) == 0) {
      this.setBounds();
    }
    return this.leftEdgeMiddleY;
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

  public double getRightEdgeMiddleY() {
    if (Double.compare(this.rightEdgeMiddleY, -1) == 0) {
      this.setBounds();
    }
    return this.rightEdgeMiddleY;
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
    if (this.minY == -1) {
      this.setBounds();
    }
    return this.minY;
  }

  public int getMaxY() {
    if (this.maxY == -1) {
      this.setBounds();
    }

    return this.maxY;
  }

  public int getHorizontalCoverage() {
    Set<Integer> xCoordinates = new HashSet<>();
    for (OmrPoint point : this.points) {
      xCoordinates.add((int) point.x);
    }
    return xCoordinates.size();
  }

  public LineEquation getLineEquation() {
    if (this.equation == null) {
      Mat line = new Mat();
      Imgproc.fitLine(new MatOfPoint2f(this.points.toArray(new OmrPoint[0])), line,
          Imgproc.CV_DIST_L2, 0, 1, 0.1);
      this.equation =
          new LineEquation(line.get(2, 0)[0], line.get(3, 0)[0], line.get(2, 0)[0]
              + line.get(0, 0)[0], line.get(3, 0)[0] + line.get(1, 0)[0]);
      // new LineEquation(this.getLeftEdgeX(), this.leftEdgeMiddleY, this.getRightEdgeX(),
      // this.getRightEdgeMiddleY());
    }
    return this.equation;
  }

  public void addStaffLine(StaffLine line) {
    for (OmrPoint point : line.points) {
      this.addPoint(point);
    }
  }

  public void translateVertically(int amount) {

    // Keep pointer to old set of points
    SortedSet<OmrPoint> points = this.points;

    // Reset the point sets / maps
    this.points = new TreeSet<>(xComparator);

    // Re-add the translated points
    for (OmrPoint point : points) {
      this.addPoint(new OmrPoint((int) point.x, (int) point.y + amount));
    }
  }

  public void extend(int leftEdgeX, int rightEdgeX) {
    this.addPoint(new OmrPoint(leftEdgeX, (int) Math.round(this.getLineEquation()
        .calculateY(leftEdgeX))));
    this.addPoint(new OmrPoint(rightEdgeX, (int) Math.round(this.getLineEquation().calculateY(
        rightEdgeX))));
  }

  public void addToImage(ImageMatrix image) {
    double middleLeft = (this.getLeftEdgeBottomY() + this.getLeftEdgeTopY()) / 2.0;
    int leftX = this.getLeftEdgeX();
    double middleRight = (this.getRightEdgeBottomY() + this.getRightEdgeTopY()) / 2.0;
    int rightX = this.getRightEdgeX();
    int height = this.getMaxY() - this.getMinY();
    if (leftX == rightX) {
      for (OmrPoint point : this.points) {
        image.put((int) point.y, (int) point.x, 255);
      }
    }
    else {
      Core.line(image, new org.opencv.core.Point(leftX, middleLeft), new org.opencv.core.Point(
          rightX, middleRight), new Scalar(255), height);
    }
  }

  public void addToImage(ImageMatrix image, StaffInfo info) {
    int leftX = this.getLeftEdgeX();
    int rightX = this.getRightEdgeX();
    int height =
        (int) Math.ceil((info.getModeLineHeight(modeThreshold).getUpperBound()) / 2.0)
            + lineHeightAdjustment;
    if (leftX == rightX) {
      for (OmrPoint point : this.points) {
        image.put((int) point.y, (int) point.x, 255);
      }
    }
    else {
      Core.line(image, new org.opencv.core.Point(leftX, this.getLeftEdgeMiddleY()),
          new org.opencv.core.Point(rightX, this.getRightEdgeMiddleY()), new Scalar(255), height);
    }
  }

  public boolean contains(OmrPoint point) {
    return this.points.contains(point);
  }

  public boolean contains(OmrPoint point, StaffInfo info) {
    return this.contains(point.x, point.y, info);
  }

  public boolean contains(double x, double y, StaffInfo info) {
    if (this.equation == null) {
      this.getLineEquation();
    }

    double margin = Math.ceil(info.getModeLineHeight(modeThreshold).getUpperBound() / 2.0);
    double lineValue = this.equation.calculateY(x);
    return y >= lineValue - margin && y <= lineValue + margin;
  }

  public MatOfPoint2f toMatOfPoint() {
    MatOfPoint2f mat = new MatOfPoint2f(this.points.toArray(new OmrPoint[0]));
    return mat;
  }

  @Override
  public StaffLine clone() {
    StaffLine clone = new StaffLine();
    for (OmrPoint point : this.points) {
      clone.addPoint(new OmrPoint((int) point.x, (int) point.y));
    }
    return clone;
  }

  private void setBounds() {
    this.leftEdgeX = (int) this.points.first().x;
    this.rightEdgeX = (int) this.points.last().x;

    SortedSet<Integer> rows = new TreeSet<Integer>();
    for (OmrPoint point : this.points) {
      rows.add((int) point.y);
    }
    this.minY = rows.first();
    this.maxY = rows.last();

    SortedSet<OmrPoint> leftColumn =
        this.points.subSet(new OmrPoint(this.leftEdgeX, 0), new OmrPoint(this.leftEdgeX + 1, 0));
    this.leftEdgeTopY = (int) leftColumn.first().y;
    this.leftEdgeBottomY = (int) leftColumn.last().y;
    this.leftEdgeMiddleY = (this.leftEdgeTopY + this.leftEdgeBottomY) / 2.0;

    SortedSet<OmrPoint> rightColumn =
        this.points.subSet(new OmrPoint(this.rightEdgeX, 0), new OmrPoint(this.rightEdgeX + 1, 0));
    this.rightEdgeTopY = (int) rightColumn.first().y;
    this.rightEdgeBottomY = (int) rightColumn.last().y;
    this.rightEdgeMiddleY = (this.rightEdgeTopY + this.rightEdgeBottomY) / 2.0;
  }
}
