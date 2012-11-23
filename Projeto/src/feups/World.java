package feups;

import jade.core.AID;
import jade.core.Agent;

import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;

import jade.domain.FIPAException;

import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

import java.awt.Point;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;

import feups.ia.AutoPilot;
import feups.map.Cell;
import feups.map.EndOfMapException;
import feups.map.Path;
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
	 * given time. 
	 */
	Roads roads;
	HashMap<String,Truck> trucks;
	HashMap<String,Parcel> parcels;
	
	/**
	 * Default constructor
	 */
	public World(){
		this.trucks = new HashMap<String,Truck>();
		this.parcels = new HashMap<String,Parcel>();
		//this.testAutopilot();
	}
	
	public void testAutopilot() {
		AutoPilot autoPilot = new AutoPilot(this.getMap());
		
		String path_str = "";
		while(!this.getMap().getDeliveries().isEmpty()){
			LinkedList<Point> destinations = this.getMap().getDeliveries();
			Point truckPosition = this.getMap().convert0BasedTo1Based(this.getMap().getTruckPosition());
			Path path = autoPilot.getPath(truckPosition, destinations);
			path_str += play_ia_walk(truckPosition, path);
		}
		
		System.out.println("[Action] " + path_str);
	}

	public String play_ia_walk(Point truckPosition, Path path) {
		String path_str = ""; 
		for (Point point : path.getPath()) {
			String input = AutoPilot.getDirection(truckPosition, point);
			System.out.println("Direction: " + input);
			System.out.println("TruckX: " + truckPosition.getX() + "TruckY: " + truckPosition.getY());
			System.out.println("DestinationX: " + point.getX() + "DestinationY: " + truckPosition.getY());
			path_str += input + "";
			
			try {
				this.getMap().makeMove(input);
				//map.update();
			} catch (EndOfMapException e) {
				System.out.println(e.getMessage());
				
			}
			truckPosition = point;
			System.out.println(this.getMap().print());
		}
		return path_str;
	}

	/**
	 * Adds the map to the world
	 * @param mapName
	 * @param mapFile
	 */
	public void addMap(String mapName, String mapFile) {
		try {
			this.roads = new Roads(mapFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
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
			System.out.println("World à escuta");
			ACLMessage msg = blockingReceive();
			if (msg.getPerformative() == ACLMessage.INFORM) {
				System.out.println(++n + " " + getLocalName() + ": recebi "
						+ msg.getContent());
				// cria resposta
				ACLMessage reply = msg.createReply();
				// preenche conteúdo da mensagem
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
			AID t1AID = new AID("teste",AID.ISLOCALNAME);
			TruckAgent t1 = new TruckAgent(t1AID,0);
			AgentController a;
			try {
				a = this.getContainerController().acceptNewAgent("teste123", t1);
				a.start();
				
			} catch (StaleProxyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
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
	 * Gets the map by name
	 * @param name
	 * @return Map of Roads
	 */
	public Roads getMap(){
		return this.roads;
	}
	
	public String printRoads(Roads roads){
		
		String output = "";
		String temp = "";
		for(int y = 1; y <= roads.getHeight(); y++) {
			String output_line = "";
			for(int x = 1; x <= roads.getWidth(); x++) {
				String cell = roads.getXY(x, y);
				output_line += cell;
			}
			output = output_line + "\n" + output;
		}
		
		return output;
		
	}
	
	
	/**
	 * Gets a truck by name.
	 * @param name Truck name
	 * @return Truck if truck exits, null otherwise
	 */
	public Truck getTruck(String name){
		return trucks.get(name);
	}
	
	/**
	 * Adds a truck
	 * @param name
	 * @param truck
	 * @return
	 */
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
