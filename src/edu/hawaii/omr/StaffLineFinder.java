package edu.hawaii.omr;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class StaffLineFinder {

  private BufferedImage image;

  public static int foregroundColor = 0xFF000000;
  public static int backgroundColor = 0xFFFFFFFF;

  public StaffLineFinder(BufferedImage image) throws NotGrayscaleException {
    OtsuRunner thresholder = new OtsuRunner(image);
    this.image = Helpers.createBinaryImage(image, thresholder.getThreshold());
    Helpers.writeGifImage(this.image, "binary.gif");
  }

  public void getConnectedHistogram(double widthPercent) {
    ConnectedComponentFinder finder = new ConnectedComponentFinder();
    finder.findConnectedComponents(this.image, foregroundColor);
    Helpers.labelsToCsv(finder.getLabels(), "labels.csv");
    double widthThreshold = this.image.getWidth() * widthPercent;
    Map<Integer, SortedSet<Point<Object>>> labels = finder.getLabelPoints();
    List<Integer> keysToRemove = new ArrayList<>();
    for (Integer label : labels.keySet()) {
      SortedSet<Point<Object>> labelPoints = labels.get(label);
      if (labelPoints.last().getX() - labelPoints.first().getX() < widthThreshold) {
        keysToRemove.add(label);
      }
    }

    for (Integer key : keysToRemove) {
      labels.remove(key);
    }

    BufferedImage modified =
        new BufferedImage(this.image.getWidth(), this.image.getHeight(), BufferedImage.TYPE_INT_ARGB);
    for (int i = 0, height = this.image.getHeight(); i < height; i++) {
      for (int j = 0, width = this.image.getWidth(); j < width; j++) {
        modified.setRGB(j, i, backgroundColor);
      }
    }

    for (Integer label : labels.keySet()) {
      SortedSet<Point<Object>> labelPoints = labels.get(label);
      for (Point<Object> point : labelPoints) {
        modified.setRGB((int) point.getX(), (int) point.getY(), foregroundColor);
      }
    }
    Helpers.writeGifImage(modified, "test.gif");
    
    BufferedImage extended =
        Morph.extend(modified, foregroundColor, Runtime.getRuntime().availableProcessors());
    Helpers.writeGifImage(extended, "extended.gif");
    BufferedImage closed =
        Morph.closing(2, extended, foregroundColor, backgroundColor, Runtime.getRuntime()
            .availableProcessors());
    Helpers.writeGifImage(closed, "closed.gif");
    
    BufferedImage filtered = this.filterStaffLines(closed, 15);
    Helpers.writeGifImage(filtered, "filtered.gif");
  }

  public int[] getVerticalHistogram() {
    int[] histogram = new int[this.image.getHeight()];
    ColorModel model = this.image.getColorModel();
    for (int i = 0, width = this.image.getWidth(); i < width; i++) {
      for (int j = 0, height = this.image.getHeight(); j < height; j++) {
        if (model.getRed(this.image.getRGB(i, j)) == 0) {
          histogram[j]++;
        }
      }
    }
    return histogram;
  }

  public int maxValue(int[] histogram) {
    int maxValue = 0;
    for (int count : histogram) {
      if (count > maxValue) {
        maxValue = count;
      }
    }
    return maxValue;
  }

  public int[] thresholdHistogram(int[] histogram, double percentage) {
    int[] thresholded = histogram.clone();
    double threshold = percentage * this.maxValue(histogram);
    for (int i = 0, length = histogram.length; i < length; i++) {
      if (histogram[i] < threshold) {
        thresholded[i] = 0;
      }
      else {
        thresholded[i] = histogram[i];
      }
    }
    return thresholded;
  }
  
  public int[] reverseThresholdHistogram(int[] histogram, double percentage) {
    int[] thresholded = histogram.clone();
    double threshold = percentage * this.maxValue(histogram);
    for (int i = 0, length = histogram.length; i < length; i++) {
      if (histogram[i] < threshold) {
        thresholded[i] = histogram[i];
      }
      else {
        thresholded[i] = 0;
      }
    }
    return thresholded;
  }

  public int[] findMaxima(int[] histogram) {
    Map<Integer, Integer> map = new HashMap<>();

    int[] firstDerivative = new int[histogram.length];

    for (int i = 0, length = histogram.length; i < length; i++) {
      if (i == 0) {
        firstDerivative[i] = histogram[i];
      }
      else if (i == length - 1) {
        firstDerivative[i] = 0 - histogram[i];
      }
      else {
        firstDerivative[i] = histogram[i + 1] - histogram[i];
      }
    }

    int[] maxArray = new int[histogram.length];
    for (int i = 0, length = firstDerivative.length - 1; i < length; i++) {
      int current, next;
      current = firstDerivative[i];
      next = firstDerivative[i + 1];
      if (current > 0 && next < 0) {
        if (histogram[i] > histogram[i + 1]) {
          maxArray[i] = histogram[i];
        }
        else {
          maxArray[i + 1] = histogram[i + 1];
        }
      }
    }

    return maxArray;
  }

  public SortedSet<StaffLine> getStaffLines(BufferedImage sheet) {
    SortedSet<StaffLine> lines = new TreeSet<>();
    int startY = -1;
    int endY = -1;
    for(int y = 0, height = sheet.getHeight(); y < height; y++) {
      if(sheet.getRGB(0, y) == foregroundColor) {
        if(startY == -1) {
          startY = y;
          endY = y;
        }
        else {
          endY = y;
        }
      }
      else if(endY != -1) {
        lines.add(new StaffLine(startY, endY));
        startY = -1;
        endY = -1;
      }
    }
    return lines;
  }
  
  public List<Integer> getDistances(SortedSet<StaffLine> lines) {
    List<Integer> distances = new ArrayList<>();
    int previousEnd = -1;
    for(StaffLine line : lines) {
      if(previousEnd == -1) {
        previousEnd = line.getEndY();
      }
      else {
        distances.add(line.getStartY() - previousEnd);
        previousEnd = line.getEndY();
      }
    }
    return distances;
  }
  
  public int getMedianDistance(List<Integer> distances) {

    
    int median;
    int middleIndex = distances.size() / 2;
    Collections.sort(distances);
    if(distances.size() % 2 == 1) {
      median = distances.get(middleIndex);
    }
    else {
      median = Math.round((distances.get(middleIndex) + distances.get(middleIndex) - 1 / 2.0f));
    }
    
    return median;
  }
  
  public BufferedImage filterStaffLines(BufferedImage sheet, int errorMargin) {
    BufferedImage filtered = new BufferedImage(sheet.getWidth(), sheet.getHeight(), BufferedImage.TYPE_INT_ARGB);
    SortedSet<StaffLine> lineSet = this.getStaffLines(sheet);
    List<StaffLine> lines = new ArrayList<>(lineSet);
    List<StaffLine> rejects = new ArrayList<>();
    List<Integer> distances = this.getDistances(lineSet);
    int medianDistance = this.getMedianDistance(distances);
    
    errorMargin = medianDistance / 2; //Math.round(Helpers.getStdDev(distances));
    
    for(int i = 0, length = lines.size(); i < length; i++) {
      
      StaffLine current = lines.get(i);
      StaffLine previous = (i == 0) ? null : lines.get(i - 1);
      StaffLine next = (i == length - 1) ? null : lines.get(i + 1);
      boolean found = false;

      int currentTop = current.getStartY();
      int currentBottom = current.getEndY();
      for(int j = 0; j <= errorMargin && !found; j++) {
        if(previous != null) {
          found = found || previous.isOnLine(currentTop - medianDistance + j);
          found = found || previous.isOnLine(currentTop - medianDistance - j);
        }
        
        if(next != null) {
          found = found || next.isOnLine(currentBottom + medianDistance + j);
          found = found || next.isOnLine(currentBottom + medianDistance - j);
        }
      }
      
      if(found) {
        current.writeToImage(filtered, foregroundColor);
      }
      else {
        rejects.add(current);
      }
    }
    
    lines.removeAll(rejects);
    
    BufferedImage subtracted = Helpers.copyImage(this.image);
    for(StaffLine line : lines) {
      line.subtract(subtracted, foregroundColor, backgroundColor);
    }
    Helpers.writeGifImage(subtracted, "subtracted.gif");
    
    return filtered;
  }
}
