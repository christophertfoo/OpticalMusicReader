package edu.hawaii.omr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class MeasureMatrix extends ImageMatrix {

  private final Staff staff;
  private final StaffInfo info;
  private final int offset;
  private List<NoteHead> noteCenters = null;

  public MeasureMatrix(Mat matrix, Staff staff, StaffInfo info, int offset) {
    super(matrix);
    this.staff = staff;
    this.info = info;
    this.offset = offset;
  }

  public void findNotes() {

    int height = this.info.getModeLineDistance();

    QuarterNote quarterNote = new QuarterNote(height);
    HalfNote halfNote = new HalfNote(height);
    WholeNote wholeNote = new WholeNote(height);

    Mat wholeTemplate = wholeNote.makeNoteHeadTemplate();
    Mat halfTemplate = halfNote.makeNoteHeadTemplate();
    Mat quarterTemplate = quarterNote.makeNoteHeadTemplate();

    int result_cols = this.cols() - wholeTemplate.cols() + 1;
    int result_rows = this.rows() - wholeTemplate.rows() + 1;
    Mat wholeResult = new Mat(result_cols, result_rows, CvType.CV_32F);
    Mat halfResult = new Mat(result_cols, result_rows, CvType.CV_32F);
    Mat quarterResult = new Mat(result_cols, result_rows, CvType.CV_32F);

    this.noteCenters = new ArrayList<>();

    Imgproc.matchTemplate(this, wholeTemplate, wholeResult, Imgproc.TM_SQDIFF_NORMED);
    Imgproc.matchTemplate(this, halfTemplate, halfResult, Imgproc.TM_SQDIFF_NORMED);
    Imgproc.matchTemplate(this, quarterTemplate, quarterResult, Imgproc.TM_SQDIFF_NORMED);
    // Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat()); //use if need to
    // normalize

    NoteHead.setNoteWidth((int) height);

    for (int y = 0; y < result_rows; y++) {
      for (int x = 0; x < result_cols; x++) {
        if (wholeResult.get(y, x)[0] < wholeNote.threshold) {

          // Detected point at topleft of the template, need to move to center of the square
          int midpoint = (int) (wholeTemplate.cols() / 2.0);
          addNoteToList(new WholeNote(x + midpoint, y + midpoint));
        }
      }
    }
    for (int y = 0; y < result_rows; y++) {
      for (int x = 0; x < result_cols; x++) {
        if (halfResult.get(y, x)[0] < halfNote.threshold) {

          // Detected point at topleft of the template, need to move to center of the square
          int midpoint = (int) (halfTemplate.cols() / 2.0);
          addNoteToList(new HalfNote(x + midpoint, y + midpoint));
        }
      }
    }
    for (int y = 0; y < result_rows; y++) {
      for (int x = 0; x < result_cols; x++) {
        if (quarterResult.get(y, x)[0] < quarterNote.threshold) {

          // Detected point at topleft of the template, need to move to center of the square
          int midpoint = (int) (quarterTemplate.cols() / 2.0);
          addNoteToList(new QuarterNote(x + midpoint, y + midpoint));
        }
      }
    }

    Collections.sort(this.noteCenters);
  }

  private void addNoteToList(NoteHead input) {

    boolean isAdjacent = false;
    for (int i = 0; i < this.noteCenters.size(); i++) {
      if (input.adjacentTo(this.noteCenters.get(i))) {
        isAdjacent = true;
      }
    }

    if (isAdjacent == false) {
      this.noteCenters.add(input);
    }

  }

  public void getPitches(StringBuilder builder) {
    if (this.noteCenters == null) {
      this.findNotes();
    }

    for (NoteHead center : this.noteCenters) {
      builder.append(' ');
      builder.append(this.staff.getPitchTreble(center.getXCoordinate() - this.offset,
          center.getYCoordinate(), this.info));
    }
    builder.append(" |");
  }

  public ImageMatrix getNoteLocationsImage() {

    if (this.noteCenters == null) {
      this.findNotes();
    }

    Mat image_rgb = new Mat();
    Imgproc.cvtColor(this, image_rgb, Imgproc.COLOR_GRAY2RGB);
    ImageMatrix image = new ImageMatrix(image_rgb);
    image_rgb = null;

    Point matchLoc;
    Point matchLocBox;

    int boxSize = this.info.getModeLineDistance();
    for (NoteHead center : this.noteCenters) {

      matchLoc = new Point(center.getXCoordinate(), center.getYCoordinate());
      matchLocBox = new Point(center.getXCoordinate() - boxSize, center.getYCoordinate() - boxSize);

      // Make a box
       Point boxPoint = new Point(matchLoc.x + boxSize, matchLoc.y + boxSize);

      // Makes a point
      Point midPoint = new Point(matchLoc.x, matchLoc.y);
      if (center.getType().equals("Whole")) {
        Core.rectangle(image, matchLoc, midPoint, new Scalar(255, 0, 0)); // blue
        Core.rectangle(image, matchLocBox, boxPoint, new Scalar(255, 0, 0));
      }
      else if (center.getType().equals("Half")) {
        Core.rectangle(image, matchLoc, midPoint, new Scalar(0, 255, 0)); // green
        Core.rectangle(image, matchLocBox, boxPoint, new Scalar(0, 255, 0));
      }
      else if (center.getType().equals("Quarter")) {
        Core.rectangle(image, matchLoc, midPoint, new Scalar(0, 0, 255)); // red
        Core.rectangle(image, matchLocBox, boxPoint, new Scalar(0, 0, 255));
      }

    }

    return image;
  }

}
