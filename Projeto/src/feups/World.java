package feups;

import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

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

public class World extends Agent{
	
	/* This is used so we can get a state of the system at any
	 * given time. */
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
	
	
	/** Defines the behaviour of our agent
	 */
	class WorldBehaviour extends SimpleBehaviour {
		private static final long serialVersionUID = 1837679922616403427L;
		private int n = 0;

		/** Default constructor */
		public WorldBehaviour(Agent a) {
			super(a);
		}

		/** Sends and receives messages from trucks
		 */
		public void action() {
			System.out.println("World � escuta");
			ACLMessage msg = blockingReceive();
			if (msg.getPerformative() == ACLMessage.INFORM) {
				System.out.println(++n + " " + getLocalName() + ": recebi "
						+ msg.getContent());
				// cria resposta
				ACLMessage reply = msg.createReply();
				// preenche conte�do da mensagem
				if (msg.getContent().equals("ping"))
					reply.setContent("pong");
				else
					reply.setContent("ping");
				// envia mensagem
				send(reply);
			}
		}
		
		/** Controls the termination of the agent
		 * When this returns true, the agent stops.
		 */
		public boolean done() {
			return false;
		}
	}
	
		/** Sets up the world and launches other agents.
		 */
		protected void setup() {
			// regista agente no DF
			DFAgentDescription dfd = new DFAgentDescription();
			dfd.setName(getAID());
			ServiceDescription sd = new ServiceDescription();
			sd.setName(getName());
			sd.setType("Agente World");
			dfd.addServices(sd);
			try {
				DFService.register(this, dfd);
			} catch (FIPAException e) {
				e.printStackTrace();
			}

			// defines the behaviour
			WorldBehaviour b = new WorldBehaviour(this);
			addBehaviour(b);
			
			/* Loads the world into this agent */
			Parser parser = new Parser(this);
			if (parser.getDetails())
				System.out.println("Parsing OK");
			else
				System.out.println("Parsing FAIL");
			
			//FIXME Load each and every truck
			/* From here on we load the trucks spawning an truck agent for every truck. */
			
		} 

		/** So we can take down our agent */
		protected void takeDown() {
			// retira registo no DF
			try {
				DFService.deregister(this);
			} catch (FIPAException e) {
				e.printStackTrace();
			}
		}
	
	
	/**
	 * Adds a city to the world.
	 * @param name The name of the city
	 * @param p Position
	 * @return true if insert ok, false otherwise
	 * FIXME change to add a city as Roads.addCity();
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
	 * FIXME change to add a get a city Roads.addCity();
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
