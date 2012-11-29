package feups;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;

import feups.city.City;
import feups.parcel.Parcel;
import feups.map.Cell;
import feups.map.Position;
import feups.truck.Truck;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

public class Parser {
	
	/** JSON com os objectos */
	JSONObject json = null;
	
	/** Onde se guarda o mundo */
	World world;
	
	public Parser(World world){
		
		this.world = world;
		FileInputStream is;
		try {
			is = new FileInputStream("maps/map01.json");
		} catch (FileNotFoundException e1) {
			System.err.println("File not found.");
			return;
		}
		
		String jsonTxt = null;
		try{
			jsonTxt = IOUtils.toString(is);
		} catch(Exception e){
			System.err.println("File error.");
			return;
		}
		
		json = (JSONObject) JSONSerializer.toJSON( jsonTxt );
		
	}
	
	
	/**
	 * Obtem os detalhes de um mapa a partir dos ficheiros de configuração.
	 * 
	 * @return true se tudo correu bem, false se houve erros.
	 */
	public boolean getDetails(){
		if(json==null)
			return false;
		
		// Getting the map of Roads
	    String mapName = json.getString( "name" );
	    String mapFileName = json.getString("map");
	    world.addMap(mapName, mapFileName);
	    
	    
	    // Getting cities
	    System.out.println("\n##### CITIES #####");
	    JSONArray cities = json.getJSONArray("cities");
	    
	    for(int n = 0; n < cities.size(); n++)
	    {
	        JSONObject city = cities.getJSONObject(n);
	        String cityName = city.getString("name"); //City Name
	        System.out.println("City: " + cityName);
	        
	        JSONObject position = city.getJSONObject("position"); //City position
	        Integer pos_x = position.getInt("x");
	        Integer pos_y = position.getInt("y");
	        System.out.println("\t x=" + pos_x + " y=" + pos_y);
	        
	        // Adiciona a cidade ao mapa.
	        world.getMap().addCity(cityName, new Position(pos_x, pos_y));
	    }
	    
	    // Getting parcels
	    System.out.println("\n##### PARCELS #####");
	    JSONArray parcels = json.getJSONArray("parcels");
	    
	    for(int n = 0; n < parcels.size(); n++)
	    {
	        JSONObject parcel = parcels.getJSONObject(n); //Parcel Name
	        String parcelName = parcel.getString("name");
	        System.out.println("Parcel: " + parcelName);
	        
	        String parcelPosition = parcel.getString("position"); //Parcel Position (city )
	        System.out.println("\tPosition: " + parcelPosition);
	        
	        String parcelDestination = parcel.getString("destination"); //Parcel Destination
	        System.out.println("\tDestination: " + parcelDestination);
	        
	        City posCity = world.getMap().getCity(parcelPosition);
	        City destCity = world.getMap().getCity(parcelDestination);
	        
	        // verifica se ambas as cidades existem
	        if(posCity == null || destCity == null)
	        	return false;
	        
	        // adiciona a cidade ao world
	        boolean result = world.addParcel(parcelName, posCity.getPosition(), destCity);
	        
	        if(!result)
	        	return false;
	    }
	    
	    

	    // Getting trucks
	    System.out.println("\n##### TRUCKS #####");
	    JSONArray trucks = json.getJSONArray("trucks");
	    
	    for(int n= 0; n<trucks.size(); n++){
	    	
	    	
	        JSONObject truckJSON = trucks.getJSONObject(n);
	        
	        String truckName = truckJSON.getString("name");
	        String truckPosition = truckJSON.getString("position");
	        
	        System.out.println("Truck: " + truckName);
	        
	        JSONArray truckParcels = truckJSON.getJSONArray("parcels");
	        
	        City truckCity = world.getMap().getCity(truckPosition);
	        if(truckCity == null) // se cidade nao existir
	        	return false;
	        
	        
	        Truck truck = new Truck(truckCity.getPosition());
	        
	        // Getting parcels on truck
	        for(int i= 0; i<truckParcels.size(); i++){
	        	JSONObject truckParcel = truckParcels.getJSONObject(i);
	        	String truckParcelName = truckParcel.getString("name");
	        	System.out.println("\tTruck " + truckName + " has parcel " + truckParcelName);
	        	
	        	Parcel p = world.getParcel(truckParcelName);
	        	if(p==null) // retorna falso caso parcel nao exista
	        		return false;
	        	
	        	// I add this parcel to truck.
	        	truck.addParcel(p);
	        }
	        world.addTruck(truckName, truck);
	    }
	    
	    System.out.println(world.printRoads(world.roads));
	   
	    System.out.println("Name is '" + mapName + "' and map file is '" + mapFileName + "'");
	    return true;
	}
}
