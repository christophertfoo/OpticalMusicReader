package edu.hawaii.omr;

public class Range {
  private int lowerBound;
  private int upperBound;
  
  public Range(int lowerBound, int upperBound) {
    this.lowerBound = upperBound < lowerBound ? upperBound : lowerBound;
    this.upperBound = lowerBound > upperBound ? lowerBound : upperBound;
  }
  
  public int getLowerBound() {
    return this.lowerBound;
  }
  
  public int getUpperBound() {
    return this.upperBound;
  }
  
  public int[] getAllValues() {
    int[] values = new int[this.upperBound - this.lowerBound + 1];
    for(int i = 0, value = this.lowerBound; value <= this.upperBound; value++) {
      values[i] = value;
    }
    return values;
  }
}
