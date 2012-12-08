package feups.communication;

import java.util.Set;

import feups.map.Path;
import feups.parcel.Parcel;

public class TruckPathCommunication implements java.io.Serializable {
	
	Path currentRoute;
	Set<Parcel> cargo;
	
	public TruckPathCommunication(Path currentRoute, Set<Parcel> cargo){
		this.currentRoute = currentRoute;
		this.cargo = cargo;
	}
	
	public String toString() {
		return ("CurrentRoute: "+this.getCurrentRoute() + " Current Parcels" + this.cargo.toString());
	}

	public Path getCurrentRoute() {
		return this.currentRoute;
	}
	
	public Set<Parcel> getParcels(){
		return this.cargo;
	}

}
