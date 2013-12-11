package edu.hawaii.omr;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class HalfNote extends NoteHead {
  private double angle = Math.PI / 6; // 30 degrees
  private double height;
  public final double threshold = 0.60;

  public HalfNote(int xCoordinate, int yCoordinate) {
    super.xCoordinate = xCoordinate;
    super.yCoordinate = yCoordinate;
    super.type = "Half";
  }

  public HalfNote(int height) {
    this.height = height;
  }

  public Mat makeNoteHeadTemplate() {
    super.angleRotation = angle;

    template = new Mat((int) (2 * height), (int) (2 * height), CvType.CV_8UC1);

    for (int y = 0; y > (2 * height * -1); y--) {
      for (int x = 0; x < (2 * height); x++) {
        double value =
            (Math.pow(
                (x - height) * Math.cos(angleRotation) + (y + height) * Math.sin(angleRotation), 2) / Math
                .pow(height, 2))
                + (Math
                    .pow(
                        (x - height) * Math.sin(angleRotation) - (y + height)
                            * Math.cos(angleRotation), 2) / Math.pow(height / 2.0, 2));
        if (value < 1 && value > 0.3) {
          double data[] = { 255 };
          template.put(Math.abs(y), x, data);
        }
        else {
          double data[] = { 0 };
          template.put(Math.abs(y), x, data);
        }
      }
    }
    return template;
  }

}
