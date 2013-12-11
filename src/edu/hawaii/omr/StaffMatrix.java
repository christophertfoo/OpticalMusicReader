package edu.hawaii.omr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class StaffMatrix extends ImageMatrix {

  private static MeasureLine.BeginYComparator measureYComparator =
      new MeasureLine.BeginYComparator();
  private static MeasureLine.BeginXComparator measureXComparator =
      new MeasureLine.BeginXComparator();

  private final StaffInfo info;
  private final Staff staff;
  private SortedSet<MeasureLine> measureLines = null;

  public StaffMatrix(Mat matrix, Staff staff, StaffInfo info) {
    super(matrix);
    this.staff = staff;
    this.info = info;
  }

  public Staff getStaff() {
    return this.staff;
  }

  public StaffInfo getStaffInfo() {
    return this.info;
  }

  public ImageMatrix getStaffLineImage() {
    ImageMatrix image = new ImageMatrix(this.rows(), this.cols(), this.type());
    if (this.staff != null) {
      if (this.info != null) {
        this.staff.addToImage(image, this.info);
      }
      else {
        this.staff.addToImage(image);
      }
    }
    image.isBinary = this.isBinary;
    image.hasWhiteForeground = this.hasWhiteForeground;
    return image;
  }

  public ImageMatrix getNoLineImage() {
    return this.subtractImagePreserve(this.getStaffLineImage(), true);
  }

  public ImageMatrix getMeasureLineImage() {
    if (this.measureLines == null) {
      this.findMeasureLines();
    }
    ImageMatrix image = new ImageMatrix(this.rows(), this.cols(), this.type());
    Imgproc.cvtColor(this, image, Imgproc.COLOR_GRAY2RGB);

    for (MeasureLine measure : this.measureLines) {
      Core.line(image, new Point(measure.getxBeginCoordinate(), measure.getyBeginCoordinate()),
          new Point(measure.getxEndCoordinate(), measure.getyEndCoordinate()),
          new Scalar(255, 0, 0), 2);
    }
    return image;
  }

  public void findMeasureLines() {
    this.findMeasureLines(true);
  }

  public void findMeasureLines(boolean removeLines) {
    this.measureLines = new TreeSet<>(measureYComparator);
    Mat lines = new Mat();
    Imgproc.HoughLinesP(removeLines ? this.getNoLineImage() : this, lines, 1, Math.PI / 10, 5,
        this.info.getModeStaffHeight() - 5, 10);

    for (int i = 0; i < lines.cols(); i++) {
      double[] vec = lines.get(0, i);
      if (vec[0] == vec[2]) {
        MeasureLine line = new MeasureLine(vec[0], vec[1], vec[2], vec[3]);
        this.measureLines.add(line);
      }
    }
  }

  public List<MeasureMatrix> splitIntoMeasures(boolean removeLines) {

    if (this.measureLines == null) {
      this.findMeasureLines();
    }

    List<MeasureMatrix> measureImages = new ArrayList<>();
    List<MeasureLine> sorted = new ArrayList<>(this.measureLines);
    Collections.sort(sorted, measureXComparator);

    int endCol = this.cols() - 1;
    int startX = 0;
    int endY = this.rows() - 1;
    int noteWidth = this.info.getModeLineDistance() * 2;

    ImageMatrix noLines = this.getNoLineImage();
    for (MeasureLine measure : sorted) {
      if (measure.xBeginCoordinate == startX + 1) {
        startX++;
        continue;
      }
      else if (this.staff.contains(measure.getxBeginCoordinate(), measure.getyBeginCoordinate(),
          this.info, 0)
          || this.staff.contains(measure.getxEndCoordinate(), measure.getyEndCoordinate(),
              this.info, 4)) {
        MeasureMatrix measureImage =
            new MeasureMatrix(removeLines ? noLines.submat(0, endY, startX,
                (int) measure.xBeginCoordinate) : this.submat(0, endY, startX,
                (int) measure.xBeginCoordinate), this.staff, this.info, startX);
        if (measureImage.cols() >= noteWidth && Core.countNonZero(measureImage) > 0) {
          measureImages.add(measureImage);
        }
        startX = (int) Math.ceil(measure.xEndCoordinate);
      }
    }
    measureImages.add(new MeasureMatrix(removeLines ? noLines.submat(0, endY, startX, endCol)
        : this.submat(0, endY, startX, endCol), this.staff, this.info, startX));
    return measureImages;
  }

  public MeasureMatrix toMeasureMatrix(boolean removeLines) {
    return new MeasureMatrix(removeLines ? this.getNoLineImage() : this, this.staff, this.info, 0);
  }

}
