package feups.city;

import feups.map.Cell;
import feups.map.Position;

/**
 * Stores a city
 * @author Ricardo Teixeira
 *
 */
public class City implements Cell {
	
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

	@Override
	public String print() {
		return "X";
	}
	
	

}
