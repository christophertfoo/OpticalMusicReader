package edu.hawaii.omr;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StaffLineFinder {

  private BufferedImage image;

  public StaffLineFinder(BufferedImage image) throws NotGrayscaleException {
    OtsuRunner thresholder = new OtsuRunner(image);
    this.image = Helpers.createBinaryImage(image, thresholder.getThreshold());
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
      if ((current > 0 && next < 0) || (current < 0 && next > 0)) {
        if (histogram[i] > histogram[i + 1]) {
          maxArray[i]= histogram[i];
        }
        else {
          maxArray[i + 1] = histogram[i + 1];
        }
      }
    }
    

    return maxArray;
  }

}
