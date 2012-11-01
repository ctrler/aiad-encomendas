package feups.city;

import feups.map.Position;

/**
 * Stores a city
 * @author Ricardo Teixeira
 *
 */
public class City {
	
	String nome;
	Position position;
	
	public City(String nome, Position p){
		
		this.position = p;
		this.nome =nome;
	}

}
