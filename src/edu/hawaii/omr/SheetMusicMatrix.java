package edu.hawaii.omr;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

public class SheetMusicMatrix extends ImageMatrix {

  private static final double modeThreshold = 0.33;
  private static final double staffWidthThreshold = 0.5;

  private static final Staff.TopComparator staffComparator = new Staff.TopComparator();
  private static final Staff.BottomComparator bottomComparator = new Staff.BottomComparator();
  private final int minStaffWidth;
  private SortedSet<Staff> staffs;
  private SortedSet<Staff> bottomStaffs;
  private StaffInfo info;

  public SheetMusicMatrix(Mat matrix) {
    this(matrix.rows(), matrix.cols(), matrix.type());
    matrix.copyTo(this);
  }

  public SheetMusicMatrix(int rows, int cols, int type) {
    super(rows, cols, type);
    this.staffs = new TreeSet<>(staffComparator);
    this.bottomStaffs = new TreeSet<>(bottomComparator);
    this.minStaffWidth = (int) Math.round(cols * staffWidthThreshold);
  }

  public StaffInfo getStaffInfo() {
    ExecutorService threadPool =
        Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    StaffInfo info = this.getStaffInfo(threadPool);
    threadPool.shutdown();
    return info;
  }

  public StaffInfo getStaffInfo(ExecutorService threadPool) {
    StaffInfo info = new StaffInfo();

    List<FindInfoCallable> callables = new ArrayList<>();
    for (int x = 0, width = this.cols(); x < width; x++) {
      callables.add(new FindInfoCallable(this, x));
    }
    try {
      List<Future<StaffInfo>> results = threadPool.invokeAll(callables);
      for (Future<StaffInfo> future : results) {
        info.addStaffInfo(future.get());
      }
    }
    catch (InterruptedException e) {
      // Do nothing for now...
    }
    catch (ExecutionException e) {
      // Do nothing, it should not happen...
    }
    this.info = info;
    return info;
  }

  public ImageMatrix findStaffLines(StaffInfo info) {
    ImageMatrix lines = new ImageMatrix(this.rows(), this.cols(), this.type());
    Range distanceRange = info.getModeLineDistance(modeThreshold);
    Range heightRange = info.getModeLineHeight(modeThreshold);

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

          int temp = y;
          // Check first line
          int lineHeight = checkHeight(x, y, heightRange);
          if (lineHeight == -1) {
            continue;
          }
          else {
            for (int j = 0; j < lineHeight; j++) {
              line1.addPoint(new Point(x, temp++));
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
            for (int j = 0; j < lineHeight; j++) {
              line2.addPoint(new Point(x, temp++));
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
            for (int j = 0; j < lineHeight; j++) {
              line3.addPoint(new Point(x, temp++));
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
            for (int j = 0; j < lineHeight; j++) {
              line4.addPoint(new Point(x, temp++));
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
            for (int j = 0; j < lineHeight; j++) {
              line5.addPoint(new Point(x, temp++));
            }

            Staff staff = new Staff(line1, line2, line3, line4, line5);
            this.staffs.add(staff);
            this.bottomStaffs.add(staff);
            info.addStaffHeight(staff.getBottomBound() - staff.getTopBound());
            staff.addToImage(lines);
          }
        }
      }
    }
    return lines;
  }

  private int checkHeight(int x, int y, Range heightRange) {
    for (int i = 1; i <= heightRange.getUpperBound(); i++) {
      if (y + i >= this.rows() || this.get(y + i, x)[0] != 255) {
        if (i < heightRange.getLowerBound()) {
          return -1;
        }
        else {
          return i;
        }
      }
    }
    return -1;
  }

  private int checkDistance(int x, int y, Range distanceRange) {
    for (int i = distanceRange.getLowerBound(); i <= distanceRange.getUpperBound(); i++) {
      if (y + i < this.rows() && this.get(y + i, x)[0] == 255) {
        if (i >= distanceRange.getLowerBound()) {
          return i;
        }
        else {
          return -1;
        }
      }
    }
    return -1;
  }

  public ImageMatrix getStaffLineImage() {
    ImageMatrix merged = new ImageMatrix(this.rows(), this.cols(), this.type());
    for (Staff staff : this.staffs) {
      if (this.info != null) {
        staff.addToImage(merged, this.info);
      }
      else {
        staff.addToImage(merged);
      }
    }
    return merged;
  }

  public void mergeConnectedStaffs(Map<Integer, SortedSet<Point>> labelMap) {
    Set<Staff> matched = new HashSet<>();
    for (SortedSet<Point> component : labelMap.values()) {
      this.associateComponent(component, matched);
    }
    this.staffs.clear();
    this.staffs.addAll(matched);
    this.bottomStaffs.clear();
    this.bottomStaffs.addAll(matched);
  }

  private boolean associateComponent(SortedSet<Point> component, Set<Staff> matched) {
    boolean found = false;
    Point topLeft = component.first();
    for (Staff staff : this.staffs) {
      StaffLine line = staff.contains(topLeft);
      if (line != null) {
        for (Point point : component) {
          line.addPoint(point);
        }
        // I don't like this... find better way...
        staff.setBounds();
        matched.add(staff);
        found = true;
      }
    }
    return found;
  }

  public void mergeSeparatedStaffs() {
    List<Staff> results = new ArrayList<>();
    List<Staff> checked = new ArrayList<>();
    int margin =
        (int) Math.ceil(this.info.getModeLineDistance(modeThreshold).getUpperBound() / 2.0);
    for (Staff staff : this.staffs) {
      if (!checked.contains(staff)) {
        int top = staff.getTopBound();
        int bottom = staff.getBottomBound();
        Set<Staff> inRange =
            new HashSet<Staff>(this.staffs.subSet(new Staff(top - margin, 0, 0, 0), new Staff(top
                + margin + 1, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE)));
        Set<Staff> bottomRange =
            this.bottomStaffs.subSet(new Staff(0, bottom - margin, 0, 0), new Staff(
                Integer.MAX_VALUE, bottom + margin + 1, Integer.MAX_VALUE, Integer.MAX_VALUE));
        inRange.addAll(bottomRange);
        inRange.removeAll(checked);
        Iterator<Staff> iterator = inRange.iterator();
        if (iterator.hasNext()) {
          Staff seed = iterator.next();
          int horizontalCoverage = seed.getRightBound() - seed.getLeftBound() + 1;
          while (iterator.hasNext()) {
            Staff next = iterator.next();
            horizontalCoverage += next.getRightBound() - next.getLeftBound() + 1;
            seed.addStaff(next);
          }
          if (horizontalCoverage >= minStaffWidth) {
            results.add(seed);
          }
        }
        checked.addAll(inRange);
      }
    }
    this.staffs.clear();
    this.staffs.addAll(results);
    int i = 1;
    for (Staff staff : this.staffs) {
      ImageMatrix matrix = new ImageMatrix(this.rows(), this.cols(), this.type());
      staff.addToImage(matrix, this.info);
      matrix.writeImage("staff_" + i + ".png");
      i++;
    }
  }

  public ImageMatrix[] splitImage() {
    int numStaffs = this.staffs.size();
    ImageMatrix[] split = new ImageMatrix[numStaffs];
    int[] splitPoints = new int[numStaffs - 1];
    Iterator<Staff> iterator = this.staffs.iterator();
    Staff previous = null;
    int i = 0;
    while (iterator.hasNext()) {
      Staff next = iterator.next();
      if (previous == null) {
        previous = next;
      }
      else {
        splitPoints[i] = (int) Math.round((next.getTopBound() + previous.getBottomBound()) / 2.0);
        previous = next;
        i++;
      }
    }

    int numSplits = numStaffs - 1;
    int endCol = this.cols() - 1;
    for (i = 0; i < numSplits; i++) {
      int startRow;
      if (i == 0) {
        startRow = 0;
      }
      else {
        startRow = splitPoints[i - 1];
      }
      split[i] = new ImageMatrix(this.submat(startRow, splitPoints[i], 0, endCol));
    }
    split[i] = new ImageMatrix(this.submat(splitPoints[i - 1], this.rows() - 1, 0, endCol));
    return split;
  }

  public static SheetMusicMatrix readImage(String filePath, int flags) {
    Mat matrix = Highgui.imread(filePath, flags);
    return new SheetMusicMatrix(matrix);
  }

  private class FindInfoCallable implements Callable<StaffInfo> {

    private SheetMusicMatrix sheet;
    private int column;

    public FindInfoCallable(SheetMusicMatrix sheet, int column) {
      this.sheet = sheet;
      this.column = column;
    }

    @Override
    public StaffInfo call() throws Exception {
      StaffInfo info = new StaffInfo();
      int lineEnd = -1;
      int lineBegin = -1;
      for (int y = 0, height = this.sheet.rows(); y < height; y++) {
        int rgb = (int) this.sheet.get(y, column)[0];
        if (rgb == 255) {
          // Start of a new foreground section
          if (lineBegin == -1) {
            lineBegin = y;
            if (y != 0) {
              // There was a background section between first foreground pixel and top of sheet
              if (lineEnd == -1) {
                info.addLineDistance(lineBegin);
              }

              // There was a background section between foreground sections
              else {
                info.addLineDistance(lineBegin - lineEnd - 1);
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
            info.addLineHeight(1);
          }

          // Foreground section was more than 1 pixel high
          else {
            info.addLineHeight(lineEnd - lineBegin + 1);
          }
          lineBegin = -1;
        }
      }
      return info;
    }

  }
}
