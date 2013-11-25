package edu.hawaii.omr;

import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * Runs Otsu's method to find the optimal threshold of the image.
 * 
 * @author Christopher Foo
 * 
 */
public class OtsuRunner {

  /**
   * The histogram of intensity values.
   */
  private int[] histogram;

  /**
   * The probabilities for each intensity value.
   */
  private double[] probabilites;

  /**
   * The number of pixels in the image.
   */
  private int numPixels;

  /**
   * The average intensity value of the image.
   */
  private double mean;

  /**
   * Creates a new OstuRunner for the given image.
   * 
   * @param image The image that Otsu's method will be applied to.
   * @throws NotGrayscaleException If the given image is not a grayscale image.
   */
  public OtsuRunner(BufferedImage image) throws NotGrayscaleException {
    if (Helpers.isGrayscale(image)) {
      this.numPixels = image.getHeight() * image.getWidth();
      this.histogram = this.getHistogram(image);
      this.probabilites = getProbabilities();
      this.mean = getMean();
    }
    else {
      throw new NotGrayscaleException();
    }
  }

  /**
   * Gets the histogram of the intensity values in the image.
   * 
   * @param image The image.
   * @return The histogram as an array.
   */
  private int[] getHistogram(BufferedImage image) {
    int[] histogram = new int[256];

    for (int i = 0; i < image.getHeight(); i++) {
      for (int j = 0; j < image.getWidth(); j++) {
        histogram[new Color(image.getRGB(j, i)).getRed()]++;
      }
    }
    return histogram;
  }

  /**
   * Gets the probabilities for each intensity value in the image.
   * 
   * @return The probabilities in an array.
   */
  private double[] getProbabilities() {
    double[] probabilities = new double[256];

    for (int i = 0; i < this.histogram.length; i++) {
      probabilities[i] = ((double) this.histogram[i]) / this.numPixels;
    }
    return probabilities;
  }

  /**
   * Calculates the average intensity of the image.
   * 
   * @return The average intensity of the image.
   */
  private double getMean() {
    double mean = 0;
    for (int i = 0; i < this.probabilites.length; i++) {
      mean += i * this.probabilites[i];
    }
    return mean;
  }

  /**
   * The recursive relationship for calculating q1 at a given threshold.
   * 
   * @param t The threshold.
   * @return The q1 value.
   */
  private double getQ1(int t) {
    double q1;
    if (t == 0) {
      q1 = this.probabilites[t];
    }
    else {
      q1 = getQ1(t - 1) + this.probabilites[t];
    }
    return q1;
  }

  /**
   * The recursive relationship for computing the mean of the first group at a given threshold.
   * 
   * @param t The threshold.
   * @return The mean of the first group.
   */
  private double getMean1(int t) {
    double mean1;
    if (t == 0) {
      mean1 = 0;
    }
    else {
      mean1 = ((getQ1(t - 1) * getMean1(t - 1)) + (t * this.probabilites[t])) / getQ1(t);
      if(Double.isNaN(mean1)) {
        mean1 = 0;
      }
    }
    return mean1;
  }

  /**
   * The relationship for computing the mean of the second group at a given threshold.
   * 
   * @param t The threshold.
   * @return The mean of the second group.
   */
  private double getMean2(int t) {
    double mean2 = (this.mean - (getQ1(t) * getMean1(t))) / (1 - getQ1(t));
    return mean2;
  }

  /**
   * Calculates the between group variance for a given threshold.
   * 
   * @param t The threshold.
   * @return The between group variance for the given threshold.
   */
  private double getBetweenGroupVariance(int t) {
    double betweenGroupVariance =
        getQ1(t) * (1 - getQ1(t)) * Math.pow((getMean1(t) - getMean2(t)), 2);
    return betweenGroupVariance;
  }

  /**
   * Finds the optimal threshold using Otsu's method.
   * 
   * @return The optimal threshold.
   */
  public int getThreshold() {
    int threshold = 0;
    double maxBetweenGroupVariance = getBetweenGroupVariance(0);
    for (int i = 1; i < 256; i++) {
      double betweenGroupVariance = getBetweenGroupVariance(i);
      if (betweenGroupVariance > maxBetweenGroupVariance) {
        threshold = i;
        maxBetweenGroupVariance = betweenGroupVariance;
      }
    }
    return threshold == 0 ? 1 : threshold;
  }

}
