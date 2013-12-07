package edu.hawaii.omr;

import java.util.SortedSet;
import java.util.TreeSet;
import org.opencv.core.Mat;

public class MeasureMatrix extends ImageMatrix {

  private final Staff staff;
  private final StaffInfo info;
  private final int offset;
  private SortedSet<Point> noteCenters = null;
  
  public MeasureMatrix(Mat matrix, Staff staff, StaffInfo info, int offset) {
    super(matrix);
    this.staff = staff;
    this.info = info;
    this.offset = offset;
  }
  
  public SortedSet<Point> findNoteCenters() {
    SortedSet<Point> centers = new TreeSet<>(new Point.XComparator());
    
    this.noteCenters = centers;
    return centers;
  }
  
  public void getPitches(StringBuilder builder) {
    if(this.noteCenters == null) {
      this.findNoteCenters();
    }
    
    for(Point center : this.noteCenters) {
      builder.append(' ');
      builder.append(this.staff.getPitchTreble(center.getX() - this.offset, center.getY(), this.info));
    }
    builder.append(" |");
  }
  
}
