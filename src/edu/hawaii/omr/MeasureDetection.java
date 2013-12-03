package edu.hawaii.omr;

import java.util.ArrayList;
import java.util.List;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

public class MeasureDetection {

  private Mat image;
  private int stafflineHeight;
  private List<MeasureLines> measureLines = new ArrayList<MeasureLines>();

  public MeasureDetection(Mat image, StaffInfo info) {
    this.image = image;
    this.stafflineHeight = info.getModeStaffHeight();
  }

  /**
   * Detect the measure lines in the image. Will create an arraylist to store the coordinate of the
   * measure lines and can be pulled out using getMeasureLines getStaffLineHight needs to run prior
   * to running this method to find the height of the staff lines
   * 
   */
  public void detectMeasure() {

    Mat edges = new Mat();

    // DELETE LATER WHEN CANNY NOT NEEDED
    // Imgproc.Canny(image,edges, 80, 120);
    // String filename3 = "OpenCVCanny.png";
    // Highgui.imwrite(filename3, edges);

    // Convert to binary and invert colors
    Imgproc.threshold(image, edges, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
    Core.bitwise_not(edges, edges);

    Mat verticalLineOutput = new Mat();
    Imgproc.cvtColor(edges, verticalLineOutput, Imgproc.COLOR_GRAY2BGR);

    Mat lines = new Mat();
    Imgproc.HoughLinesP(edges, lines, 1, Math.PI / 10, 10, stafflineHeight - 5, 10);

    for (int i = 0; i < lines.cols(); i++) {
      double[] vec = lines.get(0, i);
      if (vec[0] == vec[2]) {
        Core.line(verticalLineOutput, new Point(vec[0], vec[1]), new Point(vec[2], vec[3]),
            new Scalar(150, 150, 0), 2);
        measureLines.add(new MeasureLines(vec[0], vec[1], vec[2], vec[3]));
      }
    }
    String filename = "OpenCVMeasureLines.png";
    Highgui.imwrite(filename, verticalLineOutput);
  }

  public List<MeasureLines> getMeasureLines() {
    return measureLines;
  }
}
