package feups.city;

import java.awt.Point;

/**
 * Stores a city
 * @author Ricardo Teixeira
 *
 */
public class City  implements java.io.Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2189028557230825109L;
	String nome;
	Point position;
	
	public City(String nome, Point p){
		
		this.position = p;
		this.nome =nome;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public Point getPosition() {
		return position;
	}

	public void setPosition(Point position) {
		this.position = position;
	}
	
	
	public String print() {
		return "X";
	}
	
	

}
