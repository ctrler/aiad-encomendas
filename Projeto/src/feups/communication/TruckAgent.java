package feups.communication;

import jade.core.AID;

import java.io.Serializable;

import feups.map.Path;


public class TruckAgent implements Serializable{
	
	private int id;  //truck ID
	private AID aid; //AID for the comunication with JADE
	private Path path;
	
	public TruckAgent(AID aid, int id){
		this.setAid(aid);
		this.setId(id);
	}

	private void setId(int id) {
		this.id = id;
	}

	private void setAid(AID aid) {
		this.aid = aid;
	}
	
	private void setPath(Path path){
		this.path = path;
	}
	
	private int getId(){
		return this.id;
	}
	
	private AID getAID(){
		return this.aid;
	}
	
	private Path getPath(){
		return this.path;
	}
	
}
