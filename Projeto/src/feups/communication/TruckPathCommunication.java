package feups.communication;

import feups.map.Path;

public class TruckPathCommunication implements java.io.Serializable {
	
	Path currentRoute;
	
	public TruckPathCommunication(Path currentRoute){
		this.currentRoute = currentRoute;
	}
	
	public String toString() {
		return ("CurrentRoute: "+this.getCurrentRoute());
	}

	public Path getCurrentRoute() {
		return this.currentRoute;
	}

}
