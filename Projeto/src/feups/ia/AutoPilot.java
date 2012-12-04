package feups.ia;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;

import feups.Debug;
import feups.city.City;
import feups.map.Roads;
import feups.map.Path;


public class AutoPilot {
	
	private Roads map;
	
	public AutoPilot(Roads map){
		this.map = map;
	}
	
	private boolean isWalkable(String left){
		return (left == "#" || left =="X" || left == "T" || left == "P");
	}
	
	/**
	 * Get the best path between the Truck (origin) and a list of destinations (destination is chosen by minDistance)
	 * 
	 * @param origin
	 * @param destinations
	 * @return List of points (path)
	 */
	public Path getPath(Point origin, LinkedList<Point> destinations){
		
		ArrayList<Double> tempPoints = new ArrayList<Double>(destinations.size());
		for (Point point : destinations) {
			tempPoints.add(point.distance(origin));
		}
		
		double minDistance = Collections.min(tempPoints);
		ArrayList<Integer> nearPointsIndex = new ArrayList<Integer>();

		for(int i = 0; i < tempPoints.size(); i++){
			if(tempPoints.get(i) == minDistance){
				nearPointsIndex.add(i);
			}
		}
		
		Random r = new Random();
		int minDistancePointIndex = nearPointsIndex.get(r.nextInt(nearPointsIndex.size()));
		Point destination = destinations.get(minDistancePointIndex);
		
		return getPath(origin, destination);
	}
	
	/**
	 * Calculates a path between 2 points.
	 * Avoids obstacles.
	 * @param origin
	 * @param destination
	 * @return list of points
	 */
	public Path getPath(Point origin, Point destination) {
		Path path = new Path();
				
		/*
		 * Open and closed points list
		 */
		Path openPoints = new Path();
		Path closedPoints = new Path();
		
		/*
		 * Add to openPoints origin point 
		 */
		openPoints.getPath().add(origin);
		
		/*
		 * Get the path from origin to destination
		 */
		//System.out.println("\t\t(" + origin.getX() + ", " + origin.getY() + ") - (" + destination.getX() + ", " + destination.getY() + ")");
		path = this.getPathAux(openPoints, closedPoints, origin, destination);
		
		/* Print */
		for (Point point : path.getPath()) {
			Debug.print(2,"\t\tPATH ("+origin.x+","+origin.y+":"+destination.x+","+destination.y+"): " + point.getX() + ":" + point.getY());
		}
		
		if (path.getPath().size() >= 2) {
			path.getPath().pop();
		}
		
		return path;
	}
	
	/**
	 * 
	 * @param openPoints
	 * @param closedPoints
	 * @param origin
	 * @param destination
	 * @return A list of ordered points to follow
	 */
	private Path getPathAux(Path openPoints, Path closedPoints, Point origin, Point destination) {
		
		/*
		 * Search for the nearest point on openPoints
		 */
		Point currentPoint = (Point) origin.clone();
		//System.out.println("currentPoint.distance(destination) = " + currentPoint.distance(destination));
		while(currentPoint.distance(destination) != 0){
			
			if (openPoints.getPath().isEmpty()) {
				Debug.print(0,"[ERROR] Impossible to calculate route due to inexistent Paths.");
				return new Path();
			}
			
			Point minDistancePoint;
			
			if(openPoints.getPath().size() == 1) {
				minDistancePoint = openPoints.getPath().pop();
			}else {
				ArrayList<Double> tempPoints = new ArrayList<Double>(openPoints.getPath().size());
				for (Point point : openPoints.getPath()) {
					tempPoints.add(point.distance(destination));
					//tempPoints.add(MathUtils.calcWorldDistance(getSpace().getSizeX(), getSpace().getSizeY(), point, destination));
				}
				
				double minDistance = Collections.min(tempPoints);
				ArrayList<Integer> nearPointsIndex = new ArrayList<Integer>();

				for(int i = 0; i < tempPoints.size(); i++){
					if(tempPoints.get(i) == minDistance){
						nearPointsIndex.add(i);
					}
				}
				
				Random r = new Random();
				int minDistancePointIndex = nearPointsIndex.get(r.nextInt(nearPointsIndex.size()));
				minDistancePoint = openPoints.getPath().get(minDistancePointIndex);
				openPoints.getPath().remove(minDistancePointIndex);
			}
			
			closedPoints.getPath().add(minDistancePoint);
			
			openPoints.getPath().clear();

			
			String left = null, down = null, up = null, right = null; 
			try{
				left = map.getXY(minDistancePoint.x - 1, minDistancePoint.y);
				left = isWalkable(left) ? left : null;
			}catch(IndexOutOfBoundsException e){
				left = null;
			}
			
			try{
				up = map.getXY(minDistancePoint.x, minDistancePoint.y + 1);
				up = isWalkable(up) ? up : null;
			}catch(IndexOutOfBoundsException e){
				up = null;
			}
			
			try{
				down = map.getXY(minDistancePoint.x, minDistancePoint.y - 1);
				down = isWalkable(down) ? down : null;
			}catch(IndexOutOfBoundsException e){
				down = null;
			}
			
			try{
				right = map.getXY(minDistancePoint.x + 1, minDistancePoint.y);
				right = isWalkable(right) ? right : null;
			}catch(IndexOutOfBoundsException e){
				right = null;
			}
			
			Point leftPoint = new Point(minDistancePoint.x - 1, minDistancePoint.y);
			if(left != null && !closedPoints.getPath().contains(leftPoint) && !openPoints.getPath().contains(leftPoint)){
				openPoints.getPath().add(leftPoint);
			}
			
			Point upPoint = new Point(minDistancePoint.x, minDistancePoint.y + 1);
			if(up != null && !closedPoints.getPath().contains(upPoint) && !openPoints.getPath().contains(upPoint)){
				openPoints.getPath().add(upPoint);
			}
			
			Point downPoint = new Point(minDistancePoint.x, minDistancePoint.y - 1);
			if(down != null && !closedPoints.getPath().contains(downPoint) && !openPoints.getPath().contains(downPoint)){
				openPoints.getPath().add(downPoint);
			}
			
			Point rightPoint = new Point(minDistancePoint.x + 1, minDistancePoint.y);
			if(right != null && !closedPoints.getPath().contains(rightPoint) && !openPoints.getPath().contains(rightPoint)){
				openPoints.getPath().add(rightPoint);
			}
			
			currentPoint = minDistancePoint;
		}
		
		return closedPoints;
		
	}
	
	public static String getDirection(Point current, Point destination){
		if(current.getX() == destination.getX()){
			if(current.getY() > destination.getY()){
				return "D";
			}else{
				return "U";
			}
		}else{
			if(current.getX() > destination.getX()){
				return "L";
			}else{
				return "R";
			}
		}
	}

}
