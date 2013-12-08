package edu.hawaii.omr;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import org.opencv.core.Mat;

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
  
  public List<NoteHead> findNoteCenters(NoteHeadDetection detector) {
    detector.findNotes(this);
    List<NoteHead> centers = detector.getDetectedNotes();    
    this.noteCenters = centers;
    return centers;
  }
  
  public void getPitches(StringBuilder builder, NoteHeadDetection detector) {
    if(this.noteCenters == null) {
      this.findNoteCenters(detector);
    }
    
    for(NoteHead center : this.noteCenters) {
      builder.append(' ');
      builder.append(this.staff.getPitchTreble(center.getXCoordinate() - this.offset, center.getYCoordinate(), this.info));
    }
    builder.append(" |");
  }
  
}
