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

    if (NoteHead.template == null) {
      NoteHead.makeNoteHeadTemplate(height);
    }

    Mat template = NoteHead.template;

    int result_cols = this.cols() - NoteHead.template.cols() + 1;
    int result_rows = this.rows() - template.rows() + 1;
    Mat result = new Mat(result_cols, result_rows, CvType.CV_32F);

    List<NoteHead> falseNotes = new ArrayList<>();
    this.noteCenters = new ArrayList<>();

    Imgproc.matchTemplate(this, template, result, Imgproc.TM_CCOEFF_NORMED);
    // Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat()); //use if need to
    // normalize

    NoteHead.setNoteWidth((int) height);

    for (int y = 0; y < result_rows; y++) {
      for (int x = 0; x < result_cols; x++) {
        if (result.get(y, x)[0] > NoteHead.templateThreshold) {

          // Detected point at topleft of the template, need to move to center of the square
          int midpoint = (int) (template.cols() / 2.0);
          addNoteToList(new NoteHead(x + midpoint, y + midpoint), falseNotes);
        }
      }
    }

    Collections.sort(this.noteCenters);
  }

  private void addNoteToList(NoteHead input, List<NoteHead> falseNotes) {
    if (this.noteCenters.size() == 0) {
      this.noteCenters.add(input);
    }

    else {
      boolean checkFalseNotes = true;
      boolean addToDetectedNotes = true;

      for (int i = 0; i < this.noteCenters.size(); i++) {
        if (input.adjacentTo(this.noteCenters.get(i))) {
          falseNotes.add(input);
          checkFalseNotes = false;
          addToDetectedNotes = false;
        }
      }

      if (checkFalseNotes == true) {
        int counter = falseNotes.size();
        for (int i = 0; i < counter; i++) {
          if (input.adjacentTo(falseNotes.get(i))) {
            falseNotes.add(input);
            addToDetectedNotes = false;
          }
        }
      }

      if (addToDetectedNotes == true) {
        this.noteCenters.add(input);
      }
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

    for (NoteHead center : this.noteCenters) {

      matchLoc = new Point(center.getXCoordinate(), center.getYCoordinate());
      // Make a box
      // Point boxPoint = new Point(matchLoc.x + template.cols(), matchLoc.y + template.rows());

      // Makes a point
      Point boxPoint = new Point(matchLoc.x, matchLoc.y);
      Core.rectangle(image, matchLoc, boxPoint, new Scalar(255, 0, 0));
    }
    
    return image;
  }

}
