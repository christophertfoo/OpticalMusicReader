package edu.hawaii.omr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StaffInfo {
  private List<Integer> distances;
  private List<Integer> heights;

  private Map<Integer, Integer> distanceFreq;
  private Map<Integer, Integer> heightFreq;

  private int distanceMode;
  private int heightMode;

  private Map<Double, Range> distanceRanges;
  private Map<Double, Range> heightRanges;

  public StaffInfo() {
    this.distances = new ArrayList<>();
    this.heights = new ArrayList<>();
    this.distanceFreq = null;
    this.heightFreq = null;
    this.distanceMode = -1;
    this.heightMode = -1;
    this.distanceRanges = new HashMap<>();
    this.heightRanges = new HashMap<>();
  }

  public void addDistance(int distance) {
    this.distances.add(distance);
    this.distanceFreq = null;
    this.distanceMode = -1;
    this.distanceRanges.clear();
  }

  public void addHeight(int height) {
    this.heights.add(height);
    this.heightFreq = null;
    this.heightMode = -1;
    this.heightRanges.clear();
  }

  public int getModeDistance() {
    if (this.distanceMode == -1) {
      if (this.distanceFreq == null) {
        this.distanceFreq = this.getFrequencyMap(this.distances);
      }
      this.distanceMode = this.getMode(this.distanceFreq);
    }
    return this.distanceMode;
  }

  public Range getModeRangeDistance(double thresholdPercent) {
    if (!this.distanceRanges.containsKey(thresholdPercent)) {
      if (this.distanceFreq == null) {
        this.distanceFreq = this.getFrequencyMap(this.distances);
      }
      this.distanceRanges.put(thresholdPercent,
          this.getModeRange(this.distanceFreq, thresholdPercent));
    }
    return this.distanceRanges.get(thresholdPercent);
  }

  public int getModeHeight() {
    if (this.heightMode == -1) {
      if (this.heightFreq == null) {
        this.heightFreq = this.getFrequencyMap(this.heights);
      }
      this.heightMode = this.getMode(this.heightFreq);
    }
    return this.heightMode;
  }

  public Range getModeRangeHeight(double thresholdPercent) {
    if (!this.heightRanges.containsKey(thresholdPercent)) {
      if (this.heightFreq == null) {
        this.heightFreq = this.getFrequencyMap(this.heights);
      }
      this.heightRanges.put(thresholdPercent,
          this.getModeRange(this.heightFreq, thresholdPercent));
    }
    return this.heightRanges.get(thresholdPercent);
  }
  
  public void addStaffInfo(StaffInfo info) {
    for(int distance : info.distances) {
      this.addDistance(distance);
    }
    for(int height : info.heights) {
      this.addHeight(height);
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