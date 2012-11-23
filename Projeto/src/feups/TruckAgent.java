package feups;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import java.io.Serializable;

import feups.map.Path;


public class TruckAgent extends Agent{
	private static final long serialVersionUID = -4023017875029640114L;

	private int id;  //truck ID
	private AID aid; //AID for the comunication with JADE
	private Path path;
	
	public TruckAgent(AID aid, int id){
		this.setAid(aid);
		this.setId(id);
	}
	
	

	protected void setup() {
		System.out.println("########## TRUCK AGENT: Trying to setup truck");
		// regista agente no DF
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setName(getName());
		sd.setType("Agente Truck");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		} catch (FIPAException e) {
			e.printStackTrace();
		}
	}
	
	
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public AID getAid() {
		return aid;
	}

	public void setAid(AID aid) {
		this.aid = aid;
	}

	public Path getPath() {
		return path;
	}

	public void setPath(Path path) {
		this.path = path;
	}

	
}
