package edu.hawaii.omr;

public class MeasureLines {

	double yBeginCoordinate;
	double xBeginCoordinate;
	double yEndCoordinate;
	double xEndCoordinate;
	
	public MeasureLines(double xBeginCoordinate, double yBeginCoordinate, double xEndCoordinate, double yEndCoordinate) {
		this.yBeginCoordinate = yBeginCoordinate;
		this.xBeginCoordinate = xBeginCoordinate;
		this.yEndCoordinate = yEndCoordinate;
		this.xEndCoordinate = xEndCoordinate;
	}

	public double getyBeginCoordinate() {
		return yBeginCoordinate;
	}

	public double getxBeginCoordinate() {
		return xBeginCoordinate;
	}

	public double getyEndCoordinate() {
		return yEndCoordinate;
	}

	public double getxEndCoordinate() {
		return xEndCoordinate;
	}
	
	

}
