package edu.hawaii.omr;

import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Morph {
  private Morph() {
    // Make class static.
  }

  public static BufferedImage dilate(int radius, BufferedImage original, int foregroundColor,
      int numThreads) {
    BufferedImage dilated = Helpers.copyImage(original);
    ExecutorService pool = Executors.newFixedThreadPool(numThreads);
    for (int y = 0, height = original.getHeight(); y < height; y++) {
      pool.submit(new DilationRunnable(radius, y, original, dilated, foregroundColor));
    }

    pool.shutdown();
    boolean stopped = true;
    while (stopped) {
      try {
        stopped = !pool.awaitTermination(60, TimeUnit.SECONDS);
      }
      catch (InterruptedException e) {
        // Do nothing...
      }
    }
    return dilated;
  }

  public static BufferedImage erode(int radius, BufferedImage original, int foregroundColor,
      int backgroundColor, int numThreads) {
    BufferedImage eroded = Helpers.copyImage(original);
    ExecutorService pool = Executors.newFixedThreadPool(numThreads);
    for (int y = 0, height = original.getHeight(); y < height; y++) {
      pool.submit(new ErodeRunnable(radius, y, original, eroded, foregroundColor, backgroundColor));
    }

    pool.shutdown();
    boolean stopped = true;
    while (stopped) {
      try {
        stopped = !pool.awaitTermination(60, TimeUnit.SECONDS);
      }
      catch (InterruptedException e) {
        // Do nothing...
      }
    }
    return eroded;
  }

  public static BufferedImage closing(int radius, BufferedImage original, int foregroundColor,
      int backgroundColor, int numThreads) {
    BufferedImage dilated = Morph.dilate(radius, original, foregroundColor, numThreads);
    return Morph.erode(radius, dilated, foregroundColor, backgroundColor, numThreads);
  }

  public static BufferedImage opening(int radius, BufferedImage original, int foregroundColor,
      int backgroundColor, int numThreads) {
    BufferedImage eroded =
        Morph.erode(radius, original, foregroundColor, backgroundColor, numThreads);
    return Morph.dilate(radius, eroded, foregroundColor, numThreads);
  }

  public static BufferedImage extend(BufferedImage original, int foregroundColor, int numThreads) {
    BufferedImage extended = Helpers.copyImage(original);
    ExecutorService pool = Executors.newFixedThreadPool(numThreads);
    for (int y = 0, height = original.getHeight(); y < height; y++) {
      pool.submit(new ExtenderRunnable(y, original, extended, foregroundColor));
    }

    pool.shutdown();
    boolean stopped = true;
    while (stopped) {
      try {
        stopped = !pool.awaitTermination(60, TimeUnit.SECONDS);
      }
      catch (InterruptedException e) {
        // Do nothing...
      }
    }
    return extended;
  }
  
  public static BufferedImage subtract(BufferedImage first, BufferedImage second, int foregroundColor, int backgroundColor, int numThreads) {
    BufferedImage result = Helpers.copyImage(first);
    return result;
  }
}

class DilationRunnable implements Runnable {

  private int radius;
  private int row;
  private BufferedImage original;
  private BufferedImage dilated;
  private int foregroundColor;

  public DilationRunnable(int radius, int row, BufferedImage original, BufferedImage dilated,
      int foregroundColor) {
    this.radius = radius;
    this.row = row;
    this.original = original;
    this.dilated = dilated;
    this.foregroundColor = foregroundColor;
  }

  @Override
  public void run() {
    for (int x = 0, width = this.original.getWidth(); x < width; x++) {
      dilatePoint(this.radius, x, this.row, this.original, this.dilated, this.foregroundColor);
    }
  }

  private static void dilatePoint(int radius, int x, int y, BufferedImage original,
      BufferedImage dilated, int foregroundColor) {
    boolean isForeground = false;
    int height = original.getHeight();
    int width = original.getWidth();
    for (int i = -radius; i <= radius && !isForeground; i++) {
      for (int j = -radius; j <= radius && !isForeground; j++) {
        int targetX = x + j;
        int targetY = y + i;
        if (targetY < 0 || targetY >= height || targetX >= width) {
          break;
        }
        else if (targetX < 0) {
          continue;
        }
        else if (original.getRGB(targetX, targetY) == foregroundColor) {
          isForeground = true;
        }
      }
    }
    if (isForeground) {
      dilated.setRGB(x, y, foregroundColor);
    }
  }
}

class ErodeRunnable implements Runnable {

  private int radius;
  private int row;
  private BufferedImage original;
  private BufferedImage dilated;
  private int foregroundColor;
  private int backgroundColor;

  public ErodeRunnable(int radius, int row, BufferedImage original, BufferedImage dilated,
      int foregroundColor, int backgroundColor) {
    this.radius = radius;
    this.row = row;
    this.original = original;
    this.dilated = dilated;
    this.foregroundColor = foregroundColor;
    this.backgroundColor = backgroundColor;
  }

  @Override
  public void run() {
    for (int x = 0, width = this.original.getWidth(); x < width; x++) {
      this.erodePoint(this.radius, x, this.row, this.original, this.dilated, this.foregroundColor,
          this.backgroundColor);
    }
  }

  private void erodePoint(int radius, int x, int y, BufferedImage original, BufferedImage eroded,
      int foregroundColor, int backgroundColor) {
    boolean isForeground = true;
    int height = original.getHeight();
    int width = original.getWidth();
    for (int i = -radius; i <= radius; i++) {
      for (int j = -radius; j <= radius; j++) {
        int targetX = x + j;
        int targetY = y + i;
        if (targetY < 0 || targetY >= height || targetX >= width) {
          break;
        }
        else if (targetX < 0) {
          continue;
        }
        else if (original.getRGB(targetX, targetY) != foregroundColor) {
          isForeground = false;
        }
      }
    }
    eroded.setRGB(x, y, isForeground ? foregroundColor : backgroundColor);
  }
}

class ExtenderRunnable implements Runnable {

  private int row;
  private BufferedImage original;
  private BufferedImage extended;
  private int foregroundColor;

  public ExtenderRunnable(int row, BufferedImage original, BufferedImage extended,
      int foregroundColor) {
    this.row = row;
    this.original = original;
    this.extended = extended;
    this.foregroundColor = foregroundColor;
  }

  @Override
  public void run() {
    boolean isForeground = false;
    for (int x = 0, width = this.original.getWidth(); x < width && !isForeground; x++) {
      if (this.original.getRGB(x, this.row) == this.foregroundColor) {
        isForeground = true;
      }
    }

    if (isForeground) {
      for (int x = 0, width = this.original.getWidth(); x < width; x++) {
        this.extended.setRGB(x, this.row, this.foregroundColor);
      }
    }
  }
}
