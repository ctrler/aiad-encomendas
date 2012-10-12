package Map;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Map {
	
	private ArrayList<ArrayList<Cell>> map;
	private int cities = 0;
	private int trucks = 0;
	
	/**
	 * Constructor
	 * @param path Path to filename
	 * @throws FileNotFoundException 
	 */
	public Map(String path) throws FileNotFoundException {
		map = new ArrayList<ArrayList<Cell>>();
		this.load(path);
	}
	
	public Map(ArrayList<String> lines) {
		map = new ArrayList<ArrayList<Cell>>();
		this.load(lines);
	}
	
	public Cell getXY(int x, int y){
		return map.get(y - 1).get(x - 1);
	}
	
	public Cell setXY(int x, int y, Cell cell){
		return map.get(y - 1).set(x - 1, cell);
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
			map.add(new ArrayList<Cell>());
			
			char[] lineChars = maporig.get(i).toCharArray();
			
			for (char c : lineChars) {
				switch (c) {
				case '#':
					map.get(x).add(new Road());
					break;
				case 'T':
					map.get(x).add(new Truck());
					trucks++;
					break;
				case 'X':
					map.get(x).add(new City());
					cities++;
					break;
				case ' ':
					map.get(x).add(new Empty());
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
				map.get(l).add(new Empty());
		}
	}
	
	public String print() {
		String output = "";
		for(int y = 1; y <= getHeight(); y++) {
			String output_line = "";
			for(int x = 1; x <= getWidth(); x++) {
				Cell cell = getXY(x, y);
				output_line += cell.print();
			}
			output = output_line + "\n" + output;
		}
			
		return output;
	}


}
