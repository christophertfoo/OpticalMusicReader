package edu.hawaii.omr;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

public class LineMatrix extends Mat {
  
  public LineMatrix(int rows, int cols, int type) {
    super(rows, cols, type);
  }
  
  public LineMatrix extendLines(int endX) {
    return this.extendLines(0, endX);
  }

  public LineMatrix extendLines(int startX, int endX) {
    int[] points = new int[4];
    for (int i = 0, numLines = (int) this.size().width; i < numLines; i++) {
      this.get(0, i, points);
      int x0 = points[0];
      int y0 = points[1];
      int x1 = points[2];
      int y1 = points[3];

      LineEquation currentLine = new LineEquation(x0, y0, x1, y1);
      int[] newPoints = new int[4];
      newPoints[0] = startX;
      newPoints[1] = (int) Math.round(currentLine.calculateY(startX));
      newPoints[2] = endX;
      newPoints[3] = (int) Math.round(currentLine.calculateY(endX));
      this.put(0, i, newPoints);
    }
    return this;
  }
  
  /**
   * NOTE: Does NOT modify the template.
   * @param template
   * @return
   */
  public ImageMatrix toImageMatrix(ImageMatrix template) {
    return this.toImageMatrix(template.rows(), template.cols(), template.type());
  }
  
  public ImageMatrix toImageMatrix(int rows, int cols, int type) {
    ImageMatrix image = new ImageMatrix(rows, cols, type);
    int[] points = new int[4];
    for (int i = 0, size = (int) this.size().width; i < size; i++) {
      this.get(0, i, points);
      int x0 = points[0], y0 = points[1], x1 = points[2], y1 = points[3];
      Core.line(image, new org.opencv.core.Point(x0, y0), new org.opencv.core.Point(x1, y1),
          new Scalar(255));
    }
    return image;
  }
}
