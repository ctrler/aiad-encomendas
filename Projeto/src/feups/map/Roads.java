package feups.map;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

import feups.city.City;
import feups.truck.Truck;

public class Roads {
	
	private ArrayList<ArrayList<String>> map;
	HashMap<String,City> cities;
	
	
	/**
	 * Constructor
	 * @param path Path to filename
	 * @throws FileNotFoundException 
	 */
	public Roads(String path) throws FileNotFoundException {
		this.cities = new HashMap<String,City>();
		map = new ArrayList<ArrayList<String>>();
		this.load(path);
	}
	
	public Roads(ArrayList<String> lines) {
		this.cities = new HashMap<String,City>();
		map = new ArrayList<ArrayList<String>>();
		this.load(lines);
	}
	
	public ArrayList<ArrayList<String>> getMap(){
		return this.map;
	}
	
	/** Construtor de cópia. 
	 * Para fazer uma cópia do objecto.
	 */
	@SuppressWarnings("unchecked")
	public Roads(Roads another) {
		this.map = (ArrayList<ArrayList<String>>) org.apache.commons.lang.SerializationUtils.clone(another.getMap());
		this.cities = (HashMap<String,City>) org.apache.commons.lang.SerializationUtils.clone(another.cities);
	}
	
	public String getXY(int x, int y){
		return map.get(y - 1).get(x - 1);
	}
	
	public String setXY(double d, double e, String cell){
		return map.get((int) (e - 1)).set((int) (d - 1), cell);
	}
	
	public int getWidth() {
		if(map.isEmpty()){
			return 0;
		}else
			return map.get(0).size();
	}
	
	public int getHeight() {
		return map.size();
	}
	
	
	private void load(String path) throws FileNotFoundException{
		BufferedReader in = new BufferedReader(new FileReader(path));
		
		ArrayList<String> maporig = new ArrayList<String>();
		
		String line = null;
		try {
			while((line = in.readLine()) != null){
				maporig.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		load(maporig);
	}
	
	public void load(ArrayList<String> maporig) {
		
		int x = 0;
		for(int i = maporig.size() - 1; i >= 0; i--){	
			map.add(new ArrayList<String>());
			
			char[] lineChars = maporig.get(i).toCharArray();
			
			for (char c : lineChars) {
				switch (c) {
				case '#':
					map.get(x).add("\u2591");
					break;
				case ' ':
					map.get(x).add(" ");
					break;
				default:
					break;
				}
			}
			x++;
		}
		
		normalizeMap();
	}
	
	public void normalizeMap() {
		int maxWidth = 0;
		
		// Find longest line
		for(int l = 0; l < map.size(); l++)
			if(map.get(l).size() > maxWidth)
				maxWidth = map.get(l).size();
		
		// Add empty cells to short lines
		for(int l = 0; l < map.size(); l++) {
			while(map.get(l).size() < maxWidth)
				map.get(l).add(" ");
		}
	}
	
	public String print() {
		
		String output = "";
		for(int y = 1; y <= getHeight(); y++) {
			String output_line = "";
			for(int x = 1; x <= getWidth(); x++) {
				String cell = getXY(x, y);
				output_line += cell;
			}
			output = output_line + "\n" + output;
		}
			
		return output;
	}
	
	
	public Point convert0BasedTo1Based(Point p){
		return new Point(p.x + 1, p.y + 1);
	}
	
	/**
	 * Adds a city to the world.
	 * @param name The name of the city
	 * @param p Position
	 * @return true if insert ok, false otherwise
	 */
	public boolean addCity(String cityName, Point position) {
		City city = new City(cityName, position);
		
		if(!cities.containsKey(cityName)){
			cities.put(cityName, city);
			this.setXY(position.getX(), position.getY(), "X");
			return true;
		}
		else
			return false; // city already exists
	}
	
	/**
	 * Gets a city by name.
	 * @param name City name
	 * @return City if city exits, null otherwise
	 */
	public City getCity(String name){
		return cities.get(name);
	}
	
	


}
