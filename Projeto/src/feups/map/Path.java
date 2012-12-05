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

	@Override
	public String toString() {
		String str = "Path: ";
		for(Point p : path){
			str = str.concat(" > " + p.getX() +" " +  p.getY());
		}
		str.concat("|");
		return str;
		
	}
	
	
	
}
