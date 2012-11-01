package feups;

import java.util.HashMap;

import feups.map.Position;
import feups.map.Roads;
import feups.parcel.Parcel;
import feups.city.City;
import feups.truck.Truck;

/**
 * Stores world with map, trucks, etc..
 * @author Ricardo Teixeiera
 *
 */

public class World {
	
	Roads roads;
	HashMap<String,Truck> trucks;
	HashMap<String,City> cities;
	HashMap<String,Parcel> parcels;
	
	/**
	 * Default constructor
	 */
	public World(){
		this.trucks = new HashMap<String,Truck>();
		this.cities = new HashMap<String,City>();
		this.parcels = new HashMap<String,Parcel>();
	}
	
	
	/**
	 * Adds a city to the world.
	 * @param name The name of the city
	 * @param p Position
	 * @return true if insert ok, false otherwise
	 */
	public boolean addCity(String name, Position p){
		
		City city = new City(name, p);
		
		if(!cities.containsKey(name)){
			cities.put(name, city);
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
	
	
	/**
	 * Gets a truck by name.
	 * @param name Truck name
	 * @return Truck if truck exits, null otherwise
	 */
	public Truck getTruck(String name){
		return trucks.get(name);
	}
	
	public boolean addTruck(String name, Truck truck){
			
		if(!trucks.containsKey(name)){
			trucks.put(name, truck);
			return true;
		}
		else
			return false; // truck already exists
		
	}
	
	/**
	 * Gets a parcel by name.
	 * @param name Parcel name
	 * @return Parcel if parcel exits, null otherwise
	 */
	public Parcel getParcel(String name){
		return parcels.get(name);
	}
	
	/**
	 * Adds a parcel.
	 * @param name
	 * @param p
	 * @param destination
	 * @return
	 */
	public boolean addParcel(String name, Position p, City destination){
		Parcel parcel = new Parcel(name,p,destination);
		
		if(!parcels.containsKey(name)){
			parcels.put(name, parcel);
			return true;
		}
		else
			return false;
	}
	

}
