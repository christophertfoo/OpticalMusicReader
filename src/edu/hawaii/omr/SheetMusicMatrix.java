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
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

public class SheetMusicMatrix extends ImageMatrix {

  private static final double modeThreshold = 0.33;
  private static final double staffWidthThreshold = 0.5;

  private static final Staff.TopComparator staffComparator = new Staff.TopComparator();
  private static final Staff.BottomComparator bottomComparator = new Staff.BottomComparator();
  private final int minStaffWidth;
  
  private SortedSet<Staff> staffs = null;
  private StaffInfo info = null;

  public SheetMusicMatrix(Mat matrix) {
    super(matrix.rows(), matrix.cols(), matrix.type());
    matrix.copyTo(this);
    this.staffs = new TreeSet<>(staffComparator);
    this.minStaffWidth = (int) Math.round(this.cols() * staffWidthThreshold);
  }

  public SheetMusicMatrix(int rows, int cols, int type) {
    this(Mat.zeros(rows, cols, type));
  }

  public StaffInfo getStaffInfo() {
    ExecutorService threadPool =
        Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    StaffInfo info = this.getStaffInfo(threadPool);
    threadPool.shutdown();
    return info;
  }

  public StaffInfo getStaffInfo(ExecutorService threadPool) {
    
    if(!this.isBinary) {
      this.makeBinary();
    }
    
    if(!this.hasWhiteForeground) {
      this.invert();
    }
    
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

  public void findStaffLines() {
    if (this.info == null) {
      this.getStaffInfo();
    }
    this.staffs = new TreeSet<>(staffComparator);
    Range distanceRange = this.info.getModeLineDistance(modeThreshold);
    Range heightRange = this.info.getModeLineHeight(modeThreshold);

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
            this.info.addStaffHeight(staff.getBottomBound() - staff.getTopBound());
          }
        }
      }
    }
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
    if (this.staffs == null) {
      this.findStaffLines();
    }
    ImageMatrix image = new ImageMatrix(this.rows(), this.cols(), this.type());
    for (Staff staff : this.staffs) {
      if (this.info != null) {
        staff.addToImage(image, this.info);
      }
      else {
        staff.addToImage(image);
      }
    }
    image.isBinary = this.isBinary;
    image.hasWhiteForeground = this.hasWhiteForeground;
    return image;
  }

  public void mergeConnectedStaffs() {
    if(this.staffs == null) {
      this.findStaffLines();
    }
    ConnectedComponentFinder finder = new ConnectedComponentFinder();
    finder.findConnectedComponents(this.getStaffLineImage(), this.hasWhiteForeground ? 255 : 0);
    Map<Integer, SortedSet<Point>> labelMap = finder.makeLabelMap();
    Set<Staff> matched = new HashSet<>();
    for (SortedSet<Point> component : labelMap.values()) {
      this.associateComponent(component, matched);
    }
    this.staffs.clear();
    this.staffs.addAll(matched);
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
    if(this.staffs == null) {
      this.findStaffLines();
    }
    
    List<Staff> results = new ArrayList<>();
    List<Staff> checked = new ArrayList<>();
    SortedSet<Staff> bottomStaffs = new TreeSet<>(bottomComparator);
    bottomStaffs.addAll(this.staffs);
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
            bottomStaffs.subSet(new Staff(0, bottom - margin, 0, 0), new Staff(
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
  }
  
  public void mergeStaffs() {
    this.mergeConnectedStaffs();
    this.mergeSeparatedStaffs();
  }

  public List<StaffMatrix> splitImage() {
    int numStaffs = this.staffs.size();
    List<StaffMatrix> split = new ArrayList<>();
    int[] splitPoints = new int[numStaffs - 1];
    Iterator<Staff> iterator = this.staffs.iterator();
    Staff previous = null;
    int i = 0;
    
    // Find split points
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

    // Find average distance between splits for the first and last staff
    int numSplits = numStaffs - 1;
    int distance = 0;
    for (i = 1; i < numSplits; i++) {
      distance += splitPoints[i] - splitPoints[i - 1];
    }
    distance = (int) Math.ceil(((double) distance) / (numSplits - 1));

    // Split the sheet
    StaffMatrix staffMat;
    Staff containedStaff;
    int endCol = this.cols() - 1;
    
    iterator = this.staffs.iterator();
    for (i = 0; i < numSplits; i++) {
      int startRow;
      if (i == 0) {
        startRow = splitPoints[i] - distance;
      }
      else {
        startRow = splitPoints[i - 1];
      }

      // TODO Extract into method
      staffMat = new StaffMatrix(this.submat(startRow, splitPoints[i], 0, endCol));
      staffMat.setStaffInfo(this.info);
      containedStaff = iterator.next().clone();
      containedStaff.translateVertically(-startRow);
      staffMat.setStaff(containedStaff);
      staffMat.isBinary = this.isBinary;
      staffMat.hasWhiteForeground = this.hasWhiteForeground;
      split.add(staffMat);
    }
    
    // Split the last staff
    int endRow = splitPoints[i - 1] + distance;
    if (endRow >= this.rows()) {
      endRow = this.rows() - 1;
    }
    
    staffMat = new StaffMatrix(this.submat(splitPoints[i - 1], endRow, 0, endCol));
    staffMat.setStaffInfo(this.info);
    containedStaff = iterator.next().clone();
    containedStaff.translateVertically(-splitPoints[i - 1]);
    staffMat.setStaff(containedStaff);
    staffMat.isBinary = this.isBinary;
    staffMat.hasWhiteForeground = this.hasWhiteForeground;
    split.add(staffMat);
    return split;
  }


  public static SheetMusicMatrix readImage(String filePath, int flags, boolean whiteForeground) {
    Mat matrix = Highgui.imread(filePath, flags);
    SheetMusicMatrix sheet = new SheetMusicMatrix(matrix);
    sheet.hasWhiteForeground = whiteForeground;
    return sheet;
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
      int foregroundColor = hasWhiteForeground ? 255 : 0;

      StaffInfo info = new StaffInfo();

      final int notSet = -1;
      int lineEnd = notSet;
      int lineBegin = notSet;

      for (int y = 0, height = this.sheet.rows(); y < height; y++) {
        int rgb = (int) this.sheet.get(y, column)[0];
        if (rgb == foregroundColor) {
          // Start of a new foreground section
          if (lineBegin == notSet) {
            lineBegin = y;
            if (y != 0) {
              // There was a background section between first foreground pixel and top of sheet
              if (lineEnd == notSet) {
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
        else if (lineBegin != notSet) {
          // One pixel high
          if (lineEnd == notSet || lineBegin > lineEnd) {
            info.addLineHeight(1);
          }

          // Foreground section was more than 1 pixel high
          else {
            info.addLineHeight(lineEnd - lineBegin + 1);
          }
          lineBegin = notSet;
        }
      }
      return info;
    }

  }
}
