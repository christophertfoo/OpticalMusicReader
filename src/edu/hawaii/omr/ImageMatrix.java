package edu.hawaii.omr;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

public class ImageMatrix extends Mat {

  protected boolean isBinary = false;
  protected boolean hasWhiteForeground = false;

  public ImageMatrix(Mat matrix) {
    super(matrix.rows(), matrix.cols(), matrix.type());
    matrix.copyTo(this);
  }

  public ImageMatrix(int rows, int cols, int type) {
    this(Mat.zeros(rows, cols, type));
  }

  public ImageMatrix makeBinary() {
    Mat temp = new Mat(this.rows(), this.cols(), this.type());
    Imgproc.threshold(this, temp, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
    temp.copyTo(this);
    this.isBinary = true;
    return this;
  }

  public ImageMatrix close(int elementType, int width, int height) {
    if (!this.isBinary) {
      this.makeBinary();
    }
    if (!this.hasWhiteForeground) {
      this.invert();
    }
    Mat structuringElement = Imgproc.getStructuringElement(elementType, new Size(width, height));
    Mat dilated = new Mat(this.rows(), this.cols(), this.type());
    Imgproc.dilate(this, dilated, structuringElement);
    Imgproc.erode(dilated, this, structuringElement);
    return this;
  }

  public ImageMatrix open(int elementType, int width, int height) {
    if (!this.isBinary) {
      this.makeBinary();
    }
    if (!this.hasWhiteForeground) {
      this.invert();
    }
    Mat structuringElement = Imgproc.getStructuringElement(elementType, new Size(width, height));
    Mat eroded = new Mat(this.rows(), this.cols(), this.type());
    Imgproc.erode(this, eroded, structuringElement);
    Imgproc.dilate(eroded, this, structuringElement);
    return this;
  }

  public ImageMatrix invert() {
    Core.bitwise_not(this, this);
    this.hasWhiteForeground = !this.hasWhiteForeground;
    return this;
  }

  public ImageMatrix subtractImage(ImageMatrix other) {
    ImageMatrix difference = new ImageMatrix(this.rows(), this.cols(), this.type());

    int numChannels = this.channels();

    double[] backgroundValues = new double[numChannels];
    for (int i = 0; i < numChannels; i++) {
      backgroundValues[i] = 0;
    }

    for (int y = 0, height = this.rows(); y < height; y++) {
      for (int x = 0, width = this.width(); x < width; x++) {
        double[] thisValues = this.get(y, x);
        double[] otherValues = other.get(y, x);

        boolean isMatch = true;
        for (int i = 0; i < numChannels; i++) {
          if (Double.compare(thisValues[i], otherValues[i]) != 0) {
            isMatch = false;
            break;
          }
        }
        if (isMatch) {
          difference.put(y, x, backgroundValues);
        }
        else {
          difference.put(y, x, thisValues);
        }
      }
    }
    difference.writeImage("diffTest.png");
    return difference;
  }

  /**
   * TODO Clean up, this is kind of long...
   * 
   * @param other
   * @param eightWay
   * @return
   */
  public ImageMatrix subtractImagePreserve(Mat other, boolean eightWay) {
    ImageMatrix difference = new ImageMatrix(this.rows(), this.cols(), this.type());

    int numChannels = this.channels();

    double[] backgroundValues = new double[numChannels];
    for (int i = 0; i < numChannels; i++) {
      backgroundValues[i] = 0;
    }

    for (int y = 0, height = this.rows(); y < height; y++) {
      for (int x = 0, width = this.width(); x < width; x++) {
        double[] thisValues = this.get(y, x);
        double[] otherValues = other.get(y, x);
        boolean preserve = true;
        boolean isMatch = true;
        for (int i = 0; i < numChannels; i++) {
          if (Double.compare(thisValues[i], otherValues[i]) != 0) {
            isMatch = false;
            break;
          }
        }
        if (isMatch) {

          // Check the next pixel below the match
          int yOffset = 0;
          preserve = false;
          while (y + yOffset < other.rows() - 1 && other.get(y + yOffset, x)[0] == 255) {
            yOffset++;
          }
          if (this.get(y + yOffset, x)[0] == 255) {
            preserve = true;
          }
          else if (eightWay && yOffset > 0) {

            // Check the pixel below and to the left
            if (x > 0 && this.get(y + yOffset, x - 1)[0] == 255) {
              preserve = true;
            }

            // Check the pixel below and to the right
            else if (x < this.cols() - 1 && this.get(y + yOffset, x + 1)[0] == 255) {
              preserve = true;
            }
          }

          // If the pixels below do not save this pixel, check above
          if (!preserve) {
            yOffset = 0;
            while (y + yOffset > 0 && other.get(y + yOffset, x)[0] == 255) {
              yOffset--;
            }
            if (this.get(y + yOffset, x)[0] == 255) {
              preserve = true;
            }
            else if (eightWay && yOffset < 0) {

              // Check above and to the left
              if (x > 0 && this.get(y + yOffset, x - 1)[0] == 255) {
                preserve = true;
              }

              // Check above and to the right
              else if (x < this.cols() - 1 && this.get(y + yOffset, x + 1)[0] == 255) {
                preserve = true;
              }
            }
          }
        }
        if (!preserve) {
          difference.put(y, x, backgroundValues);
        }
        else {
          difference.put(y, x, thisValues);
        }
      }
    }
    return difference;
  }

  public void writeImage(String filePath) {
    Highgui.imwrite(filePath, this);
  }

  public static ImageMatrix readImage(String filePath, int flags, boolean whiteForeground) {
    Mat matrix = Highgui.imread(filePath, flags);
    ImageMatrix image = new ImageMatrix(matrix);
    image.hasWhiteForeground = whiteForeground;
    return image;
  }

}
