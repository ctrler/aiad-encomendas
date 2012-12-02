package feups.map;

import java.awt.Point;
import java.util.LinkedList;

public class Path {
	private LinkedList<Point> path;
	
	public Path(){
		this.path = new LinkedList<Point>();
	}
	
	public Path(LinkedList<Point> path){
		this.path = path;
	}
	
	public void setPath(LinkedList<Point> path){
		this.path = path;
	}
	
	public LinkedList<Point> getPath(){
		return this.path;
	}
	
}
