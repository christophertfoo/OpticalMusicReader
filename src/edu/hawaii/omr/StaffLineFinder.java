package edu.hawaii.omr;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class StaffLineFinder {

  private BufferedImage image;

  public StaffLineFinder(BufferedImage image) throws NotGrayscaleException {
    OtsuRunner thresholder = new OtsuRunner(image);
    this.image = Helpers.createBinaryImage(image, thresholder.getThreshold());
    Helpers.writeGifImage(this.image, "binary.gif");
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

  public int maxValue(int[] histogram) {
    int maxValue = 0;
    for(int count : histogram) {
      if(count > maxValue) {
        maxValue = count;
      }
    }
    return maxValue;
  }
  
  public int[] thresholdHistogram(int[] histogram, double percentage) {
    int[] thresholded = histogram.clone();
    double threshold = percentage * this.maxValue(histogram);
    for(int i = 0, length = histogram.length; i < length; i++) {
      if(histogram[i] < threshold) {
        thresholded[i] = 0;
      }
      else {
        thresholded[i] = histogram[i];
      }
    }
    return thresholded;
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
      if (current > 0 && next < 0) {
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
  
  /**
	 * Gets the Image
	 * 
	 * @return - BufferedImage image
	 */
	public BufferedImage getImage() {
		return image;
	}
	
	/**
	 * Concert image to a integer 2D array
	 * @return - 2D integer array of the image
	 */
	public int[][] imageTo2DArray(){
		int height = image.getHeight();
		int width = image.getWidth();
		
		int[][] ret = new int[height][width];
		for(int y=0; y<height; y++){
			for(int x =0; x<width;x++){
				if(image.getRGB(x,y) == 0xFF000000){
					ret[y][x] = 1;
				}
				else{
					ret[y][x] =0;
				}
				
			}
		}
		return ret;
	}
	

	
	/**
	 * Find the size of the staffline
	 * 
	 * @param threshold
	 * @return
	 */
	public int findLineSize(int[] threshold){
		ArrayList<Integer> sizes = new ArrayList<Integer>();
		for(int i =0; i<threshold.length;i++){
			if(threshold[i] != 0){
				int size = 1;
				int j=1;
				while(true){ //look up to see if there are lines
					if(threshold[i-j]!= 0){
						size++;
						j++;
					}
					else{
						break;
					}
				}
				j=1;
				while(true){ //look down to see if there are lines
					if(threshold[i+j] != 0 ){
						size++;
						j++;
					}
					else{
						break;
					}
				}
				sizes.add(size);
			}
		}
		int ret =0;
		for(int i =0; i<sizes.size();i++){
			ret += sizes.get(i);
		}
		ret = (int) Math.ceil(ret/sizes.size());
		return ret;
 	}
	
	public int[][] staffLineRemoval(int[] maximaThreshold,int[] threshold, int lineSize){
		int[][] img = imageTo2DArray();
		int[][] ret = img.clone();
		int height = img.length;
		int width = img[0].length;
		for(int y = 0; y<height;y++){
			if(maximaThreshold[y] !=0){
				
				ArrayList<Integer> yLines = new ArrayList<Integer>();
				int tempY = y-lineSize;
				int tempYEnd = y+lineSize;
				int thresholdValue = width/4;
				while(tempY < tempYEnd){
					if(threshold[tempY] > thresholdValue){
						yLines.add(tempY);
					}
					tempY++;
				}
				
				for(int i =0; i < yLines.size(); i++){
					for(int x =0; x< width; x++){
						ret[yLines.get(i)][x] = 0;
					}
					
				}
				
				//Old code, will probably delete when the above works.
//				int tempy = y - lineSize/2;
//				for(int i=0;i<lineSize/2;i++){
//					for(int x =0; x<width; x++){
//						img[tempy][x] = 0;
//					}
//					tempy++;
//				}
//				tempy = y;
//				for(int i=0;i<lineSize/2;i++){
//					for(int x =0; x<width; x++){
//						img[tempy][x] = 0;
//					}
//					tempy++;
//				}
			
			}
		}
		return ret;
	}  
	
}
