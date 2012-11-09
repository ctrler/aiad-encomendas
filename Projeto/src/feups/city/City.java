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

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}
	
	

}
