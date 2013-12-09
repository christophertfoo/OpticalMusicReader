package edu.hawaii.omr;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class WholeNote extends NoteHead {

	private double angle = 0;
	private double height;
	public final double threshold = 0.50;
	
	public WholeNote(int xCoordinate, int yCoordinate){
		super.xCoordinate = xCoordinate;
		super.yCoordinate = yCoordinate;
		super.type = "Whole";

	}

	public WholeNote(int height){
		this.height = height;
			}
	
	
	public Mat makeNoteHeadTemplate() {
		super.angleRotation = angle;
		
		
		template = new Mat((int)(2*height),  (int) (2*height), CvType.CV_8UC1);
		
		for(int y=0; y > (2*height*-1);y--){
			for(int x=0; x < (2*height); x++){
			
				double value = (Math.pow((x-height), 2)/Math.pow(height,2)) + 
						(Math.pow((y+height),2)/Math.pow(height/2.0, 2));
				if (value < 1 && value > 0.2){
					double data[] = {255};
					template.put(Math.abs(y), x, data);
				}
				else{
					double data[] = {0};
					template.put(Math.abs(y),x,data);
				}
			}
		}	
		return template;
	}

}
