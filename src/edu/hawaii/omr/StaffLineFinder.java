package edu.hawaii.omr;

import java.awt.image.BufferedImage;

public class StaffLineFinder {

  private BufferedImage image;

  public static int foregroundColor = 0xFF000000;
  public static int backgroundColor = 0xFFFFFFFF;

  public StaffLineFinder(BufferedImage image) throws NotGrayscaleException {
    OtsuRunner thresholder = new OtsuRunner(image);
    this.image = Helpers.createBinaryImage(image, thresholder.getThreshold());
    this.image = Morph.closing(3, this.image, foregroundColor, backgroundColor, Runtime.getRuntime().availableProcessors());
    Helpers.writeGifImage(this.image, "binary.gif");
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
        else if(lineBegin != -1) {
          // One pixel high
          if(lineEnd == -1 || lineBegin > lineEnd) {
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
}
