package feups.communication;

import java.util.List;

import feups.parcel.Parcel;


public class CentralAgent {
	
	//TODO: lista de trucks e parcels
	//Number of agents in a game
	private int nTruckAgents = 0;
	private int nAgentsRegistered=0;
	
	private int nParcels = 0;
	
	public List<TruckAgent> truckAgentList;
	public List<Parcel> parcelList;
	
	public void setNTruckAgents(int nTruckAgents){
		this.nTruckAgents = nTruckAgents;
	}
	
	public void setNAgentsRegistered(int nAgentsRegistered){
		this.nAgentsRegistered = nAgentsRegistered;
	}
	
	public void addAgent(TruckAgent agent){
		truckAgentList.add(agent);
		nTruckAgents++;
	}
	
	public void addParcel(Parcel parcel){
		parcelList.add(parcel);
		nParcels++;
	}

}
