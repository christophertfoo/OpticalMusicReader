package edu.hawaii.omr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

public class SheetMusicMatrix extends ImageMatrix {

  private List<Staff> staffs;

  public SheetMusicMatrix(Mat matrix) {
    this(matrix.rows(), matrix.cols(), matrix.type());
    matrix.copyTo(this);
  }

  public SheetMusicMatrix(int rows, int cols, int type) {
    super(rows, cols, type);
    this.staffs = new ArrayList<>();
  }

  public StaffInfo getStaffInfo() {
    StaffInfo info = new StaffInfo();

    for (int x = 0, width = this.cols(); x < width; x++) {
      int lineEnd = -1;
      int lineBegin = -1;
      for (int y = 0, height = this.rows(); y < height; y++) {
        int rgb = (int) this.get(y, x)[0];
        if (rgb == 255) {
          // Start of a new foreground section
          if (lineBegin == -1) {
            lineBegin = y;
            if (y != 0) {
              // There was a background section between first foreground pixel and top of sheet
              if (lineEnd == -1) {
                info.addDistance(lineBegin);
              }

              // There was a background section between foreground sections
              else {
                info.addDistance(lineBegin - lineEnd - 1);
              }
            }
          }

          // Continue the foreground section
          else {
            lineEnd = y;
          }
        }

        // Encountered background pixel after going through a foreground section
        else if (lineBegin != -1) {
          // One pixel high
          if (lineEnd == -1 || lineBegin > lineEnd) {
            info.addHeight(1);
          }

          // Foreground section was more than 1 pixel high
          else {
            info.addHeight(lineEnd - lineBegin + 1);
          }
          lineBegin = -1;
        }
      }
    }
    return info;
  }

  public ImageMatrix findStaffLines(StaffInfo info) {
    ImageMatrix lines = new ImageMatrix(this.rows(), this.cols(), this.type());
    Range distanceRange = info.getModeRangeDistance(0.15);
    Range heightRange = info.getModeRangeHeight(0.15);

    heightRange.setLowerBound(heightRange.getLowerBound() - 1);
    distanceRange.setUpperBound(distanceRange.getUpperBound() + 1);

    for (int x = 0, width = this.cols(); x < width; x++) {
      for (int y = 0, height = this.rows(); y < height; y++) {
        StaffLine line1 = null;
        StaffLine line2 = null;
        StaffLine line3 = null;
        StaffLine line4 = null;
        StaffLine line5 = null;
        if (this.get(y, x)[0] == 255) {
          line1 = new StaffLine();
          line1.addPoint(new Point(x, y));

          int temp = y;
          // Check first line
          int lineHeight = checkHeight(x, y, heightRange);
          if (lineHeight == -1) {
            continue;
          }
          else {
            for (int j = 1; j < lineHeight; j++) {
              line1.addPoint(new Point(x, ++temp));
            }

            // Check first gap
            int gap = checkDistance(x, temp, distanceRange);
            if (gap == -1) {
              continue;
            }
            else {
              temp += gap;
            }
          }

          // Check second line
          lineHeight = checkHeight(x, temp, heightRange);
          if (lineHeight == -1) {
            continue;
          }
          else {
            line2 = new StaffLine();
            for (int j = 1; j < lineHeight; j++) {
              line2.addPoint(new Point(x, ++temp));
            }

            // Check second gap
            int gap = checkDistance(x, temp, distanceRange);
            if (gap == -1) {
              continue;
            }
            else {
              temp += gap;
            }
          }

          // Check third line
          lineHeight = checkHeight(x, temp, heightRange);
          if (lineHeight == -1) {
            continue;
          }
          else {
            line3 = new StaffLine();
            for (int j = 1; j < lineHeight; j++) {
              line3.addPoint(new Point(x, ++temp));
            }

            // Check third gap
            int gap = checkDistance(x, temp, distanceRange);
            if (gap == -1) {
              continue;
            }
            else {
              temp += gap;
            }
          }

          // Check fourth line
          lineHeight = checkHeight(x, temp, heightRange);
          if (lineHeight == -1) {
            continue;
          }
          else {
            line4 = new StaffLine();
            for (int j = 1; j < lineHeight; j++) {
              line4.addPoint(new Point(x, ++temp));
            }

            // Check fourth gap
            int gap = checkDistance(x, temp, distanceRange);
            if (gap == -1) {
              continue;
            }
            else {
              temp += gap;
            }
          }

          // Check fifth line
          lineHeight = checkHeight(x, temp, heightRange);
          if (lineHeight == -1) {
            continue;
          }
          else {
            line5 = new StaffLine();
            for (int j = 1; j < lineHeight; j++) {
              line5.addPoint(new Point(x, ++temp));
            }

            Staff staff = new Staff(line1, line2, line3, line4, line5);
            this.staffs.add(staff);
            staff.addToImage(lines);
          }
        }
      }
    }
    return lines;
  }

  private int checkHeight(int x, int y, Range heightRange) {
    for (int i = 1; i < heightRange.getUpperBound(); i++) {
      if (y + i >= this.rows() || this.get(y + i, x)[0] != 255) {
        if (i < heightRange.getLowerBound()) {
          return -1;
        }
        else {
          return i;
        }
      }
    }
    return heightRange.getUpperBound();
  }

  private int checkDistance(int x, int y, Range distanceRange) {
    for (int i = distanceRange.getLowerBound(); i <= distanceRange.getUpperBound(); i++) {
      if (y + i < this.rows() && this.get(y + i, x)[0] == 255) {
        if (i - 1 >= distanceRange.getLowerBound()) {
          return i;
        }
        else {
          return -1;
        }
      }
    }
    return -1;
  }

  public void mergeStaffs(Map<Integer, SortedSet<Point>> labelMap) {   
    Set<Staff> matched = new HashSet<>();
    for(SortedSet<Point> component : labelMap.values()) {
      this.associateComponent(component, matched);
    }
    this.staffs = new ArrayList<>();
    this.staffs.addAll(matched);
  }

  private boolean associateComponent(SortedSet<Point> component, Set<Staff> matched) {
    boolean found = false;
    Point topLeft = component.first();
    for (Staff staff : this.staffs) {
      StaffLine line = staff.contains(topLeft);
      if (line != null) {
        for(Point point : component) {
          line.addPoint(point);
        }
        matched.add(staff);
        found = true;
      }
    }
    return found;
  }

  public static SheetMusicMatrix readImage(String filePath, int flags) {
    Mat matrix = Highgui.imread(filePath, flags);
    return new SheetMusicMatrix(matrix);
  }
}
