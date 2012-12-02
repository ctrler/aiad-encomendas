package feups;

import jade.core.Agent;

import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.TickerBehaviour;
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
import feups.map.Roads;
import feups.parcel.Parcel;
import feups.city.City;
import feups.truck.Truck;

/**
 * Stores world with map, trucks, etc..
 * 
 * @author Ricardo Teixeiera
 * 
 */

public class World extends Agent {
	private static final long serialVersionUID = -6572093365452115398L;

	/*
	 * This is used so we can get a state of the system at any given time.
	 */
	static Roads roads;
	HashMap<String, Truck> trucks;
	HashMap<String, Parcel> parcels;

	/**
	 * Default constructor
	 */
	public World() {
		this.trucks = new HashMap<String, Truck>();
		this.parcels = new HashMap<String, Parcel>();
	}

	/**
	 * Adds the map to the world
	 * 
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

	/**
	 * Defines the behaviour of our agent
	 */
	class WorldBehaviour extends SimpleBehaviour {
		private static final long serialVersionUID = 1837679922616403427L;
		private int n = 0;

		/** Default constructor */
		public WorldBehaviour(Agent a) {
			super(a);
		}

		/**
		 * Envia e recebe as mensagens dos trucks.
		 */
		public void action() {
			ACLMessage msg = blockingReceive();
			if (msg.getPerformative() == ACLMessage.INFORM) {
				System.out.println(++n + " " + getLocalName() + ": recebi "
						+ msg.getContent());
				// // cria resposta
				// ACLMessage reply = msg.createReply();
				// // preenche conte�do da mensagem
				// reply.setContent("Mensagem recebida. Toca a trabalhar!");
				// send(reply);
			}
		}

		/**
		 * Controls the termination of the agent When this returns true, the
		 * agent stops.
		 */
		public boolean done() {
			return false;
		}
	}

	/**
	 * Para enviar mensagens de tempos a tempos de forma a testar os trucks Mais
	 * em: http://www.iro.umontreal.ca/~vaucher/Agents/Jade/primer6.html#6.6.4
	 */
	class SendMsgBehaviour extends TickerBehaviour {
		private static final long serialVersionUID = 1837679922616403427L;
		private int n = 0;

		/** Default constructor */
		public SendMsgBehaviour(Agent a) {
			super(a, 5000); // 5000 � o tempo entre cada tick
		}

		/**
		 * Envia mensagem ao truck
		 */
		public void onTick() {
			// pesquisa DF por agentes "Agente Truck"
			DFAgentDescription template = new DFAgentDescription();
			ServiceDescription sd1 = new ServiceDescription();
			sd1.setType("Agente Truck"); // Vai procurar por todos os agentes
											// deste tipo
			template.addServices(sd1);

			// Envia a mensagem ao world
			try {
				DFAgentDescription[] result = DFService.search(this.myAgent,
						template);
				// envia mensagem "pong" inicial a todos os agentes "ping"
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				for (int i = 0; i < result.length; ++i)
					msg.addReceiver(result[i].getName()); // Envia uma mensagem
															// para multiplos
															// destinos
				msg.setContent("Ol� Sr. camionista.");

				send(msg);
			} catch (FIPAException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * Sets up the world and launches other agents.
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

		// WorldBehaviour b = new WorldBehaviour(this);
		// addBehaviour(b);
		addBehaviour(new SendMsgBehaviour(this));

		/* Loads the world into this agent */
		Parser parser = new Parser(this);
		if (parser.getDetails())
			System.out.println("Parsing OK");
		else
			System.out.println("Parsing FAIL");

		/*
		 * LOAD TRUCKS From here on we load the trucks spawning an truck agent
		 * for every truck. We add the truck to the controller and start it.
		 */
		for (String truckName : trucks.keySet()) {

			TruckAgent t = new TruckAgent();

			AgentController agentController;
			try {
				agentController = this.getContainerController().acceptNewAgent(
						truckName, t);
				agentController.start();

			} catch (StaleProxyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

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
	 * 
	 * @param name
	 * @return Map of Roads
	 */
	public Roads getMap() {
		return this.roads;
	}

	public String printRoads() {

		String output = "";
		String temp = "";
		for (int y = 1; y <= this.roads.getHeight(); y++) {
			String output_line = "";
			for (int x = 1; x <= this.roads.getWidth(); x++) {
				String cell = this.roads.getXY(x, y);
				output_line += cell;
			}
			output = output_line + "\n" + output;
		}

		return output;

	}

	/**
	 * Gets a truck by name.
	 * 
	 * @param name
	 *            Truck name
	 * @return Truck if truck exits, null otherwise
	 */
	public Truck getTruck(String name) {
		return trucks.get(name);
	}

	/**
	 * Adds a truck
	 * 
	 * @param name
	 * @param truck
	 * @return
	 */
	public boolean addTruck(String name, Truck truck) {

		if (!trucks.containsKey(name)) {
			trucks.put(name, truck);
			this.getMap().setXY(truck.getCurrentPosition().getX(),
					truck.getCurrentPosition().getY(), "T");
			return true;
		} else
			return false; // truck already exists
	}

	/**
	 * Gets a parcel by name.
	 * 
	 * @param name
	 *            Parcel name
	 * @return Parcel if parcel exits, null otherwise
	 */
	public Parcel getParcel(String name) {
		return parcels.get(name);
	}

	/**
	 * Adds a parcel.
	 * 
	 * @param name
	 * @param p
	 * @param destination
	 * @return
	 */
	public boolean addParcel(String name, Point p, City destination) {
		Parcel parcel = new Parcel(name, p, destination);

		if (!parcels.containsKey(name)) {
			parcels.put(name, parcel);
			this.getMap().setXY(destination.getPosition().getX(),
					destination.getPosition().getY(), "P");
			return true;
		} else
			return false;
	}

	/**
	 * Returns the collection of Trucks
	 * 
	 * @return HashMap<String,Truck> trucks;
	 */
	public HashMap<String, Truck> getTrucks() {
		return this.trucks;
	}

}
