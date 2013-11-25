package edu.hawaii.omr;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class StaffLineFinder {

  private BufferedImage image;

  public static int foregroundColor = 0xFF000000;
  public static int backgroundColor = 0xFFFFFFFF;

  public StaffLineFinder(BufferedImage image) throws NotGrayscaleException {
    OtsuRunner thresholder = new OtsuRunner(image);
    this.image = Helpers.createBinaryImage(image, thresholder.getThreshold());
    this.image =
        Morph.closing(1, this.image, foregroundColor, backgroundColor, Runtime.getRuntime()
            .availableProcessors());
  }

  public BufferedImage getImage() {
    return this.image;
  }

  public StaffInfo getStaffInfo() {
    StaffInfo info = new StaffInfo();

    for (int x = 0, width = this.image.getWidth(); x < width; x++) {
      int lineEnd = -1;
      int lineBegin = -1;
      for (int y = 0, height = this.image.getHeight(); y < height; y++) {
        int rgb = this.image.getRGB(x, y);
        if (rgb == foregroundColor) {
          // Start of a new black section
          if (lineBegin == -1) {
            lineBegin = y;
            if (y != 0) {
              // There was a white section between first black pixel and top of sheet
              if (lineEnd == -1) {
                info.addDistance(lineBegin);
              }

              // There was a white section between black sections
              else {
                info.addDistance(lineBegin - lineEnd - 1);
              }
            }
          }

          // Continue the black section
          else {
            lineEnd = y;
          }
        }

        // Encountered white pixel after going through a black section
        else if (lineBegin != -1) {
          // One pixel high
          if (lineEnd == -1 || lineBegin > lineEnd) {
            info.addHeight(1);
          }

          // Black section was more than 1 pixel high
          else {
            info.addHeight(lineEnd - lineBegin + 1);
          }
          lineBegin = -1;
        }
      }
    }
    return info;
  }

  public BufferedImage findStaffLines(StaffInfo info) {
    BufferedImage copy =
        new BufferedImage(this.image.getWidth(), this.image.getHeight(),
            BufferedImage.TYPE_INT_ARGB);
    Range distanceRange = info.getModeRangeDistance(0.15);
    Range heightRange = info.getModeRangeHeight(0.15);

    heightRange.setLowerBound(heightRange.getLowerBound() - 1);
    distanceRange.setUpperBound(distanceRange.getUpperBound() + 1);

    List<Integer> potentialValues = new ArrayList<>();
    for (int x = 0, width = this.image.getWidth(); x < width; x++) {
      for (int y = 0, height = this.image.getHeight(); y < height; y++) {
        potentialValues.clear();
        if (this.image.getRGB(x, y) == foregroundColor) {
          potentialValues.add(y);

          int temp = y;
          // Check first line
          int lineHeight = checkHeight(x, y, heightRange);
          if (lineHeight == -1) {
            continue;
          }
          else {
            for (int j = 1; j < lineHeight; j++) {
              potentialValues.add(++temp);
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
            for (int j = 1; j < lineHeight; j++) {
              potentialValues.add(++temp);
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
            for (int j = 1; j < lineHeight; j++) {
              potentialValues.add(++temp);
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
            for (int j = 1; j < lineHeight; j++) {
              potentialValues.add(++temp);
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
            for (int j = 1; j < lineHeight; j++) {
              potentialValues.add(++temp);
            }

            for (int staffPixels : potentialValues) {
              copy.setRGB(x, staffPixels, foregroundColor);
            }
          }
        }
      }
    }
    return copy;
  }

  private int checkHeight(int x, int y, Range heightRange) {
    for (int i = 1; i < heightRange.getUpperBound(); i++) {
      if (y + i >= this.image.getHeight() || this.image.getRGB(x, y + i) != foregroundColor) {
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
      if (y + i < this.image.getHeight() && this.image.getRGB(x, y + i) == foregroundColor) {
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
}
