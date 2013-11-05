package edu.hawaii.omr;

import java.awt.image.BufferedImage;

public class StaffLine implements Comparable<StaffLine> {
  private int startY;
  private int endY;

  public StaffLine(int startY, int endY) {
    this.startY = startY;
    this.endY = endY;
  }

  public boolean isOnLine(int y) {
    return y >= startY && y <= endY;
  }

  public int getStartY() {
    return this.startY;
  }

  public int getEndY() {
    return this.endY;
  }

  public void subtract(BufferedImage image, int foregroundColor, int backgroundColor) {
    for (int x = 0, width = image.getWidth(); x < width; x++) {
      boolean isWholeColumn = false;
      for (int y = this.startY; y <= this.endY; y++) {
        if (image.getRGB(x, y) != foregroundColor) {
          break;
        }
        else if (y == this.endY) {
          isWholeColumn = true;
        }
      }

      boolean keepColumn = false;
      if (isWholeColumn) {
        int above = this.startY - 1;
        int below = this.endY + 1;

        if (above > 0 && image.getRGB(x, above) == foregroundColor) {
          keepColumn = true;
        }
        if (below >= image.getHeight() && image.getRGB(x, below) == foregroundColor) {
          keepColumn = true;
        }

        if (!keepColumn) {
          for (int y = this.startY; y <= this.endY; y++) {
            image.setRGB(x, y, backgroundColor);
          }
        }
      }
      if (!keepColumn) {
        for (int y = this.startY; y <= this.endY; y++) {
          image.setRGB(x, y, backgroundColor);
        }
      }
    }
  }

  public void writeToImage(BufferedImage image, int foregroundColor) {
    int width = image.getWidth();
    for (int y = startY; y <= endY; y++) {
      for (int x = 0; x < width; x++) {
        image.setRGB(x, y, foregroundColor);
      }
    }
  }

  @Override
  public int compareTo(StaffLine o) {
    int compareValue = 0;
    if (this.startY > o.startY) {
      compareValue = 1;
    }
    else if (this.startY < o.startY) {
      compareValue = -1;
    }
    return compareValue;
  }

}
