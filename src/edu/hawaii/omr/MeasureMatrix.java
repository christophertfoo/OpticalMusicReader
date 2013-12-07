package edu.hawaii.omr;

import org.opencv.core.Mat;

public class MeasureMatrix extends ImageMatrix {

  private final Staff staff;
  private final StaffInfo info;
  private final int offset;
  
  public MeasureMatrix(Mat matrix, Staff staff, StaffInfo info, int offset) {
    super(matrix);
    this.staff = staff;
    this.info = info;
    this.offset = offset;
  }
  
}
