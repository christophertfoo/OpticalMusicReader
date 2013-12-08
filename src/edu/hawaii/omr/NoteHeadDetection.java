package edu.hawaii.omr;

import java.util.ArrayList;
import java.util.Collections;

import org.opencv.core.Core;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import com.sun.org.apache.xerces.internal.impl.xpath.regex.Match;

public abstract class NoteHeadDetection {

	protected double templateThreshold;
	protected double angleRotation;
	protected double height;
	protected Mat template;
	public String type;
	private ArrayList<NoteHead> detectedNotes  = new ArrayList<NoteHead>();
	private ArrayList<NoteHead> falseNotes = new ArrayList<NoteHead>();
	
	
	public NoteHeadDetection(double height, double templateThreshold) {
		this.height = height;
		this.templateThreshold = templateThreshold;
		makeNoteHeadTemplate();
		Highgui.imwrite("Testtemplate.png", template);
	}
	
	public String getType(){
		return type;
	}
	
	public abstract void makeNoteHeadTemplate();
	
	public void findNotes(Mat image){

		
		Mat image_rgb = new Mat();
		Imgproc.cvtColor(image, image_rgb, Imgproc.COLOR_GRAY2RGB);

		int result_cols = image.cols() - template.cols()+1;
		int result_rows = image.rows() - template.rows()+1;
		Mat result = new Mat(result_cols, result_rows, CvType.CV_32F);
		
		Imgproc.matchTemplate(image, template, result, Imgproc.TM_CCOEFF_NORMED);
		//Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat()); //use if need to normalize
		

		NoteHead.setNoteWidth((int) height);
		
		for(int y=0; y < result_rows; y++){
			for(int x=0; x < result_cols; x++){			
				if(result.get(y,x)[0] > templateThreshold){	
					
					//Detected point at topleft of the template, need to move to center of the square
					int midpoint = (int) (template.cols()/2.0);
					addNoteToList(new NoteHead(x+midpoint, y+midpoint, type));
				}
			}
			
		}
		
		Collections.sort(detectedNotes);

	}

	private void addNoteToList(NoteHead input){
		if(detectedNotes.size() == 0){
			detectedNotes.add(input);
		}
		
		else{
			boolean checkFalseNotes = true;
			boolean addToDetectedNotes = true;
			
			for(int i=0; i<detectedNotes.size(); i++){
				if(input.adjacentTo(detectedNotes.get(i))){
					falseNotes.add(input);
					checkFalseNotes = false;
					addToDetectedNotes = false;
				}
			}
			
			if(checkFalseNotes == true){
				int counter = falseNotes.size();
				for(int i=0; i<counter; i++){
					if(input.adjacentTo(falseNotes.get(i))){
						falseNotes.add(input);
						addToDetectedNotes = false;
					}
				}
			}
			
			if(addToDetectedNotes == true){
				detectedNotes.add(input);
			}
		}
	}

	public ArrayList<NoteHead> getDetectedNotes() {
		return detectedNotes;
	}
}
