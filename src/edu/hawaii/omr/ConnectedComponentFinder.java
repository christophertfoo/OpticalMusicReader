package edu.hawaii.omr;

import java.awt.image.BufferedImage;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Finds the connected components in the image using a 8-neighbor based classic component finding
 * algorithm. Modified to connect edge strengths instead of pixel intensities.
 * 
 * @author Christopher Foo
 * 
 */
public class ConnectedComponentFinder {

  /**
   * The union-find data structure that keeps track of the equivalences.
   */
  private Map<Integer, Integer> unionFind;

  /**
   * The labels assigned to the edges.
   */
  private int[][] labels;

  /**
   * Finds the number of connected components in the given {@link Plane}.
   * 
   * @param image
   *          TODO UPDATE The Plane containing the edge strengths of the image.
   * @param foregroundColor
   *          TODO UPDATE The low threshold used to determine weak edges. Edges below this threshold
   *          are considered to be background.
   * @return The number of connected components in the image.
   */
  public int findConnectedComponents(BufferedImage image, int foregroundColor) {
    setupDataStructures(image, foregroundColor);
    firstPass();
    secondPass();
    int count = countLabels();
    return count;
  }

  /**
   * Gets the labels of {@link Plane} in a 2D array.
   * 
   * @return The labels.
   */
  public int[][] getLabels() {
    return this.labels;
  }

  /**
   * Gets a {@link Set} of labels used.
   * 
   * @return A set of labels used.
   */
  public Set<Integer> getLabelValues() {
    Map<Integer, Integer> found = new HashMap<>();

    for (int i = 0; i < this.labels.length; i++) {
      for (int j = 0; j < this.labels[i].length; j++) {

        if (this.labels[i][j] != 0 && !found.containsKey(this.labels[i][j])) {
          found.put(this.labels[i][j], 1);
        }
      }
    }
    return found.keySet();
  }

  public Map<Integer, SortedSet<Point<Object>>> getLabelPoints() {
    Map<Integer, SortedSet<Point<Object>>> points = new HashMap<>();
    Comparator<Point<Object>> xComparator = new XComparator<Object>();
    for (int i = 0; i < this.labels.length; i++) {
      for (int j = 0; j < this.labels[i].length; j++) {
        int label = this.labels[i][j];
        if (label != 0) {
          if (!points.containsKey(label)) {
            points.put(label, new TreeSet<Point<Object>>(xComparator));
          }

          points.get(label).add(new Point<Object>(j, i));
        }
      }
    }
    return points;
  }

  /**
   * Sets up the unionFind and labels data structures.
   * 
   * @param image
   *          TODO UPDATE The Plane containing the edge strengths of the image.
   * @param foregroundColor
   *          TODO UPDATE The low threshold used to determine weak edges. Edges below this threshold
   *          are considered to be background.
   */
  private void setupDataStructures(BufferedImage image, int foregroundColor) {
    this.unionFind = new HashMap<>();

    int height = image.getHeight();
    int width = image.getWidth();
    this.labels = new int[height][width];

    for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
        if (image.getRGB(j, i) == foregroundColor) {
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
    SortedSet<Integer> adjacentLabels = new TreeSet<>();

    for (int i = 0; i < this.labels.length; i++) {
      int rowLength = this.labels[i].length;
      for (int j = 0; j < rowLength; j++) {

        // Check foreground pixels.
        if (this.labels[i][j] == 1) {
          int label = 0;
          adjacentLabels.clear();

          // Check neighbor to the top.
//          if (i > 0 && this.labels[i - 1][j] != 0) {
//            adjacentLabels.add(this.labels[i - 1][j]);
//          }

          // Check neighbor to the left.
          if (j > 0 && this.labels[i][j - 1] != 0) {
            adjacentLabels.add(this.labels[i][j - 1]);
          }

//          if (i > 0 && j > 0 && this.labels[i - 1][j - 1] != 0) {
//            adjacentLabels.add(this.labels[i - 1][j - 1]);
//          }
//
//          if (i > 0 && j < rowLength - 1 && this.labels[i - 1][j + 1] != 0) {
//            adjacentLabels.add(this.labels[i - 1][j + 1]);
//          }

          if (adjacentLabels.size() > 0) {
            int minLabel = adjacentLabels.first();
            if (adjacentLabels.size() > 1) {
              for (Integer conflictLabel : adjacentLabels.tailSet(minLabel + 1)) {
                this.unionFind.put(conflictLabel, minLabel);
              }
            }
            label = minLabel;
          }
          else {
            // Assign new label if neighbors do not have labels.
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
  private int countLabels() {
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
