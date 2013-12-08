package edu.hawaii.omr;

import java.util.ArrayList;
import java.util.Collections;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

public class NoteHeadRunner {

	private double height;
	private double wholeNoteThreshold = 0.50;
	private double halfNoteThreshold = 0.60;
	private double quarterNoteThreshold = 0.67;
	
	public NoteHeadRunner(double height) {
		this.height = height;
	    
	}
	
	public ArrayList<NoteHead> findAllNotes(Mat image){
		Mat image_rgb = new Mat();
		Imgproc.cvtColor(image, image_rgb, Imgproc.COLOR_GRAY2RGB);
		
		NoteHeadDetection matchWholeNoteHead = new WholeNoteDetection(height, wholeNoteThreshold);
		NoteHeadDetection matchHalfNoteHead = new HalfNoteDetection(height, halfNoteThreshold);
		NoteHeadDetection matchQuarterNoteHead = new QuarterNoteDetection(height, quarterNoteThreshold);
		
		
	    matchWholeNoteHead.findNotes(image);
	    matchHalfNoteHead.findNotes(image);
	    matchQuarterNoteHead.findNotes(image);

	    ArrayList<NoteHead> wholeNotes = matchWholeNoteHead.getDetectedNotes();
	    ArrayList<NoteHead> halfNotes = matchHalfNoteHead.getDetectedNotes();
	    ArrayList<NoteHead> quarterNotes = matchQuarterNoteHead.getDetectedNotes();

	    ArrayList<NoteHead> detectedNotes = new ArrayList<NoteHead>();
	    
	    detectedNotes.addAll(wholeNotes);
	    detectedNotes.addAll(halfNotes);
	    detectedNotes.addAll(quarterNotes);
	    
	   	Collections.sort(detectedNotes);
	   	
	   	
	   	Point matchLoc;
	   	for(int i=0; i<detectedNotes.size(); i++){
			matchLoc = new Point(detectedNotes.get(i).getXCoordinate(), detectedNotes.get(i).getYCoordinate());
			//Make a box
			Point boxPoint = new Point(matchLoc.x + 2*height, matchLoc.y + 2*height);
			
			//Makes a point
			//Point boxPoint = new Point(matchLoc.x, matchLoc.y);
			
			//Write to image
			if(detectedNotes.get(i).getType().equals("Whole")){
				Core.rectangle(image_rgb, matchLoc , boxPoint, new Scalar(255,0,0)); //blue
			}
			else if(detectedNotes.get(i).getType().equals("Half")){
				Core.rectangle(image_rgb, matchLoc , boxPoint, new Scalar(0,255,0)); //green
			}
			else if(detectedNotes.get(i).getType().equals("Quarter")){
				Core.rectangle(image_rgb, matchLoc , boxPoint, new Scalar(0,0,255));  //red
			}
		}
			
		Highgui.imwrite("templateALLMatchTest.png", image_rgb);

		return detectedNotes;
		
	}

}
