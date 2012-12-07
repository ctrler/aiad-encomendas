package feups.map;

import java.awt.Point;
import java.util.LinkedList;

public class Path implements java.io.Serializable {
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
	
	public long calculateLenght(){
		long len = 0;
		Point before = path.getFirst();
		for(Point p: path){
			p.distance(before);
			before = p;
		}
		return len;
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

	public void add(Path tempPath) {
		for(Point point : tempPath.getPath()){
			this.path.add(point);
		}
	}
	
	
	
}
