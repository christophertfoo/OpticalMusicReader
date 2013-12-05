package edu.hawaii.omr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class StaffMatrix extends ImageMatrix {

  private static MeasureLine.BeginYComparator measureYComparator =
      new MeasureLine.BeginYComparator();
  private static MeasureLine.BeginXComparator measureXComparator =
      new MeasureLine.BeginXComparator();

  private StaffInfo info = null;
  private Staff staff = null;
  private SortedSet<MeasureLine> measureLines = null;

  public StaffMatrix(Mat matrix) {
    super(matrix.rows(), matrix.cols(), matrix.type());
    matrix.copyTo(this);
  }

  public StaffMatrix(int rows, int cols, int type) {
    this(Mat.zeros(rows, cols, type));
  }

  public Staff getStaff() {
    return this.staff;
  }

  public void setStaff(Staff staff) {
    this.staff = staff;
  }

  public StaffInfo getStaffInfo() {
    return this.info;
  }

  public void setStaffInfo(StaffInfo info) {
    this.info = info;
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
    return this.subtractImagePreserve(this.getStaffLineImage(), false);
  }

  public void findMeasureLines() {
    this.measureLines = new TreeSet<>(measureYComparator);
    Mat lines = new Mat();
    Imgproc.HoughLinesP(this.getNoLineImage(), lines, 1, Math.PI / 10, 10,
        this.info.getModeStaffHeight() - 5, 10);

    for (int i = 0; i < lines.cols(); i++) {
      double[] vec = lines.get(0, i);
      if (vec[0] == vec[2]) {
        MeasureLine line = new MeasureLine(vec[0], vec[1], vec[2], vec[3]);
        this.measureLines.add(line);
      }
    }
  }

  public List<ImageMatrix> splitIntoMeasures() {

    if (this.measureLines == null) {
      this.findMeasureLines();
    }

    List<ImageMatrix> measureImages = new ArrayList<>();
    List<MeasureLine> sorted = new ArrayList<>(this.measureLines);
    Collections.sort(sorted, measureXComparator);

    int endCol = this.cols() - 1;
    int startX = 0;
    int endY = this.rows() - 1;
    for (MeasureLine measure : sorted) {
      if (measure.xBeginCoordinate == startX + 1) {
        startX++;
        continue;
      }

      ImageMatrix measureImage =
          new ImageMatrix(this.submat(0, endY, startX, (int) measure.xBeginCoordinate));
      if (Core.countNonZero(measureImage) > 0) {
        measureImages.add(measureImage);
      }
      startX = (int) Math.ceil(measure.xEndCoordinate);
    }
    measureImages.add(new ImageMatrix(this.submat(0, endY, startX, endCol)));
    return measureImages;
  }

}
