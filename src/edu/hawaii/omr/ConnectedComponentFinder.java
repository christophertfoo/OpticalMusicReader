package edu.hawaii.omr;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Finds the connected components in the image using a 4-neighbor based classic component finding
 * algorithm.
 * 
 * @author Christopher Foo
 * 
 */
public class ConnectedComponentFinder {
  
  private static Point.XComparator xComparator = new Point.XComparator();

  /**
   * The union-find data structure that keeps track of the equivalences.
   */
  private Map<Integer, Integer> unionFind;

  /**
   * The labels assigned to the image's foreground.
   */
  private int[][] labels;

  /**
   * Finds the number of connected components in the given image.
   * 
   * @param image TODO UPDATE COMMENT The {@link BufferedImage} to search.
   * @param foreground The color of the foreground.
   * @return The number of connected components in the image.
   */
  public int[][] findConnectedComponents(ImageMatrix image, int foreground) {
    setupDataStructures(image, foreground);
    firstPass();
    secondPass();
    return this.labels;
  }
  
  public Map<Integer, SortedSet<Point>> makeLabelMap() {
    Map<Integer, SortedSet<Point>> map = new HashMap<>();
    if(this.labels != null) {
      for(int y = 0, height = this.labels.length; y < height; y++) {
        for(int x = 0, width = this.labels[y].length; x < width; x++) {
          int label = this.labels[y][x];
          if(label != 0) {
            if(!map.containsKey(label)) {
              map.put(label, new TreeSet<>(xComparator));
            }
            map.get(label).add(new Point(x, y));
          }
        }
      }
    }
    return map;
  }

  /**
   * Sets up the unionFind and labels data structures.
   * 
   * @param image TODO UPDATE COMMENT The image that will be searched for connected components.
   * @param foreground The color of the foreground in the image.
   */
  private void setupDataStructures(ImageMatrix image, int foreground) {
    this.unionFind = new HashMap<>();
    this.labels = new int[image.rows()][image.cols()];

    for (int i = 0; i < image.rows(); i++) {
      for (int j = 0; j < image.cols(); j++) {
        if (image.get(i, j)[0] == foreground) {
          this.labels[i][j] = 1;
        }
      }
    }
  }

  /**
   * Performs the first pass of the classic component finding algorithm and finds the temporary
   * labels.
   */
  private void firstPass() {
    int nextLabel = 1;

    for (int i = 0; i < this.labels.length; i++) {
      for (int j = 0; j < this.labels[i].length; j++) {

        // Check foreground pixels.
        if (this.labels[i][j] == 1) {
          int label = 0;

          // Check neighbor directly above.
          if (i > 0 && this.labels[i - 1][j] != 0) {
            label = this.labels[i - 1][j];
          }

          // Check neighbor to the left.
          if (j > 0 && this.labels[i][j - 1] != 0) {

            // If conflict, choose the smaller label and add
            // other label to the union-find structure.
            if (label != 0 && label != this.labels[i][j - 1]) {
              if (this.labels[i][j - 1] < label) {
                this.unionFind.put(label, this.labels[i][j - 1]);
                label = this.labels[i][j - 1];
              }
              else {
                this.unionFind.put(this.labels[i][j - 1], label);
              }
            }

            // No conflict
            else {
              label = this.labels[i][j - 1];
            }
          }

          // Assign new label if neighbors do not have labels.
          if (label == 0) {
            label = nextLabel;
            nextLabel++;
          }

          this.labels[i][j] = label;
        }
      }
    }
  }

  /**
   * Performs the second pass of the classic component finding algorithm and replaces equivalent
   * labels to find the final labeling.
   */
  private void secondPass() {
    for (int i = 0; i < this.labels.length; i++) {
      for (int j = 0; j < this.labels[i].length; j++) {
        if (this.labels[i][j] != 0) {
          while (this.unionFind.containsKey(this.labels[i][j])) {
            this.labels[i][j] = this.unionFind.get(this.labels[i][j]);
          }
        }
      }
    }
  }

  /**
   * Counts the number of different labels in the labels data structure.
   * 
   * @return The number of labels in the labels data structure.
   */
  public int countLabels() {
    Map<Integer, Integer> found = new HashMap<>();

    for (int i = 0; i < this.labels.length; i++) {
      for (int j = 0; j < this.labels[i].length; j++) {

        if (this.labels[i][j] != 0 && !found.containsKey(this.labels[i][j])) {
          found.put(this.labels[i][j], 1);
        }
      }
    }

    return found.keySet().size();
  }
}
