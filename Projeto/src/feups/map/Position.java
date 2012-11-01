package feups.map;

/** 
 * Stores a position on the map.
 *  
 * @author Ricardo Teixeira
 *
 */
public class Position {
	Integer x;
	Integer y;
	
	public Position(Integer x, Integer y){
		this.x=x;
		this.y=y;
	}
	public Integer getX(){
		return this.x;
	}
	
	public Integer getY(){
		return this.y;
	}
}
