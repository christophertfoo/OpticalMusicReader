package edu.hawaii.omr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StaffInfo {
  private List<Integer> lineDistances;
  private List<Integer> lineHeights;
  private List<Integer> staffHeights;

  private Map<Integer, Integer> lineDistanceFreq;
  private Map<Integer, Integer> lineHeightFreq;
  private Map<Integer, Integer> staffHeightFreq;

  private int lineDistanceMode;
  private int lineHeightMode;
  private int staffHeightMode;

  private Map<Double, Range> lineDistanceRanges;
  private Map<Double, Range> lineHeightRanges;

  public StaffInfo() {
    this.lineDistances = new ArrayList<>();
    this.lineHeights = new ArrayList<>();
    this.staffHeights = new ArrayList<>();

    this.lineDistanceFreq = null;
    this.lineHeightFreq = null;
    this.staffHeightFreq = null;

    this.lineDistanceMode = -1;
    this.lineHeightMode = -1;
    this.staffHeightMode = -1;

    this.lineDistanceRanges = new HashMap<>();
    this.lineHeightRanges = new HashMap<>();
  }

  public void addLineDistance(int distance) {
    this.lineDistances.add(distance);
    this.lineDistanceFreq = null;
    this.lineDistanceMode = -1;
    this.lineDistanceRanges.clear();
  }

  public void addLineHeight(int height) {
    this.lineHeights.add(height);
    this.lineHeightFreq = null;
    this.lineHeightMode = -1;
    this.lineHeightRanges.clear();
  }

  public void addStaffHeight(int height) {
    this.staffHeights.add(height);
    this.staffHeightFreq = null;
    this.staffHeightMode = -1;
  }

  public int getModeLineDistance() {
    if (this.lineDistanceMode == -1) {
      if (this.lineDistanceFreq == null) {
        this.lineDistanceFreq = this.getFrequencyMap(this.lineDistances);
      }
      this.lineDistanceMode = this.getMode(this.lineDistanceFreq);
    }
    return this.lineDistanceMode;
  }

  public Range getModeLineDistance(double thresholdPercent) {
    if (!this.lineDistanceRanges.containsKey(thresholdPercent)) {
      if (this.lineDistanceFreq == null) {
        this.lineDistanceFreq = this.getFrequencyMap(this.lineDistances);
      }
      this.lineDistanceRanges.put(thresholdPercent,
          this.getModeRange(this.lineDistanceFreq, thresholdPercent));
    }
    return this.lineDistanceRanges.get(thresholdPercent);
  }

  public int getModeLineHeight() {
    if (this.lineHeightMode == -1) {
      if (this.lineHeightFreq == null) {
        this.lineHeightFreq = this.getFrequencyMap(this.lineHeights);
      }
      this.lineHeightMode = this.getMode(this.lineHeightFreq);
    }
    return this.lineHeightMode;
  }

  public Range getModeLineHeight(double thresholdPercent) {
    if (!this.lineHeightRanges.containsKey(thresholdPercent)) {
      if (this.lineHeightFreq == null) {
        this.lineHeightFreq = this.getFrequencyMap(this.lineHeights);
      }
      this.lineHeightRanges.put(thresholdPercent,
          this.getModeRange(this.lineHeightFreq, thresholdPercent));
    }
    return this.lineHeightRanges.get(thresholdPercent);
  }

  public int getModeStaffHeight() {
    if(this.staffHeightMode == -1) {
      if(this.staffHeightFreq == null) {
        this.staffHeightFreq = this.getFrequencyMap(this.staffHeights);
      }
      this.staffHeightMode = this.getMode(this.staffHeightFreq);
    }
    return this.staffHeightMode;
  }
  
  public void addStaffInfo(StaffInfo info) {
    for (int distance : info.lineDistances) {
      this.addLineDistance(distance);
    }
    for (int height : info.lineHeights) {
      this.addLineHeight(height);
    }
    
    for(int height : info.staffHeights) {
      this.addStaffHeight(height);
    }
  }

  private Map<Integer, Integer> getFrequencyMap(List<Integer> list) {
    Map<Integer, Integer> frequencyMap = new HashMap<Integer, Integer>();
    for (int value : list) {
      if (frequencyMap.containsKey(value)) {
        frequencyMap.put(value, frequencyMap.get(value) + 1);
      }
      else {
        frequencyMap.put(value, 1);
      }
    }
    return frequencyMap;
  }

  private int getMode(Map<Integer, Integer> frequencyMap) {
    int mode = 0;
    int highestFrequency = 0;

    for (int key : frequencyMap.keySet()) {
      if (mode == 0) {
        mode = key;
        highestFrequency = frequencyMap.get(key);
      }
      else if (frequencyMap.get(key) > highestFrequency) {
        mode = key;
        highestFrequency = frequencyMap.get(key);
      }
    }
    return mode;
  }

  private Range getModeRange(Map<Integer, Integer> frequencyMap, double thresholdPercent) {
    List<Integer> survived = new ArrayList<>();
    int modeFrequency = frequencyMap.get(this.getMode(frequencyMap));
    double threshold = modeFrequency * thresholdPercent;
    for (int key : frequencyMap.keySet()) {
      if (frequencyMap.get(key) > threshold) {
        survived.add(key);
      }
    }

    int lowBound = 0;
    int highBound = 0;

    for (int key : survived) {
      if (lowBound == 0) {
        lowBound = key;
      }
      else if (key < lowBound) {
        lowBound = key;
      }
      if (highBound == 0) {
        highBound = key;
      }
      else if (key > highBound) {
        highBound = key;
      }
    }

    return new Range(lowBound, highBound);
  }
}