package edu.hawaii.omr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StaffInfo {
  private List<Integer> distances;
  private List<Integer> heights;
  
  public StaffInfo() {
    this.distances = new ArrayList<>();
    this.heights = new ArrayList<>();
  }
  
  public void addDistance(int distance) {
    this.distances.add(distance);
  }
  
  public void addHeight(int height) {
    this.heights.add(height);
  }
  
  public int getModeDistance() {
    Map<Integer, Integer> frequencyMap = this.getFrequencyMap(this.distances);
    int maxValue = 0;
    for(int key : frequencyMap.keySet()) {
      if(key > maxValue) {
        maxValue = key;
      }
    }
    
    int[] array = new int[maxValue + 1];
    for(int key : frequencyMap.keySet()) {
      array[key] = frequencyMap.get(key);
    }
    return this.getMode(frequencyMap);
  }
  
  public Range getModeRangeDistance(double thresholdPercent) {
    return this.getModeRange(this.getFrequencyMap(this.distances), thresholdPercent);
  }
  
  public int getModeHeight() {
    Map<Integer, Integer> frequencyMap = this.getFrequencyMap(this.heights);
    int maxValue = 0;
    for(int key : frequencyMap.keySet()) {
      if(key > maxValue) {
        maxValue = key;
      }
    }
    
    int[] array = new int[maxValue + 1];
    for(int key : frequencyMap.keySet()) {
      array[key] = frequencyMap.get(key);
    }
    return this.getMode(frequencyMap);
  }
  
  public Range getModeRangeHeight(double thresholdPercent) {

    return this.getModeRange(this.getFrequencyMap(this.heights), thresholdPercent);
  }
  
  private Map<Integer, Integer> getFrequencyMap(List<Integer> list) {
    Map<Integer, Integer> frequencyMap = new HashMap<Integer, Integer>();
    for(int value : list) {
      if(frequencyMap.containsKey(value)) {
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

    for(int key : frequencyMap.keySet()) {
      if(mode == 0) {
        mode = key;
        highestFrequency = frequencyMap.get(key);
      }
      else if(frequencyMap.get(key) > highestFrequency) {
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
    for(int key : frequencyMap.keySet()) {
      if(frequencyMap.get(key) > threshold) {
        survived.add(key);
      }
    }
    
    int lowBound = 0;
    int highBound = 0;
    
    for(int key : survived) {
      if(lowBound == 0) {
        lowBound = key;
      }
      else if(key < lowBound) {
        lowBound = key;
      }
      if(highBound == 0) {
        highBound = key;
      }
      else if(key > highBound) {
        highBound = key;
      }
    }
    
    return new Range(lowBound, highBound);
  }
}
