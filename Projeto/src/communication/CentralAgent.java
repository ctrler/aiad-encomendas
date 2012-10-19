package communication;

import Map.Parcel;

public class CentralAgent {
	
	//TODO: lista de trucks e parcels
	//Number of agents in a game
	private int nTruckAgents = 0;
	private int nAgentsRegistered=0;
	public TruckAgent[] truckAgents = null;
	public Parcel[] parcelList = null;
	
	public void setNTruckAgents(int nTruckAgents){
		this.nTruckAgents = nTruckAgents;
	}
	
	public void setNAgentsRegistered(int nAgentsRegistered){
		this.nAgentsRegistered = nAgentsRegistered;
	}

}
