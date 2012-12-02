package feups;


import java.awt.Point;
import java.util.Iterator;
import java.util.Set;

import feups.ia.AutoPilot;
import feups.map.EndOfMapException;
import feups.map.Path;
import feups.map.Roads;
import feups.parcel.Parcel;
import feups.truck.Truck;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class TruckAgent extends Agent {
	
	Set<Parcel> parcels;
	private Roads roads; //Used so Truck can navigate in the map
	Point currentPosition;
	
	private static final long serialVersionUID = -4023017875029640114L;

	public TruckAgent(Truck truck, Roads map, Set<Parcel> parcels) {
		this.currentPosition = truck.getCurrentPosition();
		this.roads = map;
		this.parcels = parcels;
	}

	protected void setup() {
		System.out.println("########## TRUCK AGENT " + getLocalName()
				+ ": Acordado");
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

		// pesquisa DF por agentes "Agente World"
		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription sd1 = new ServiceDescription();
		sd1.setType("Agente World");
		template.addServices(sd1);

		ParallelBehaviour par = new ParallelBehaviour(
				ParallelBehaviour.WHEN_ALL);
		par.addSubBehaviour(new TruckAgentBehaviour(this));
		par.addSubBehaviour(new DeliveryParcelsBehaviour(this));
		par.addSubBehaviour(new BehaviourPrintStuff(this));

		addBehaviour(par);

	}

	/**
	 * Behaviour que responde a mensagens
	 * 
	 */
	class TruckAgentBehaviour extends CyclicBehaviour {
		private static final long serialVersionUID = 1837679922616403427L;

		// construtor do behaviour
		public TruckAgentBehaviour(Agent a) {
			super(a);
		}

		// método action
		public void action() {

			// Espera por mensagens recebidas
			// ACLMessage msg = blockingReceive(); // Isto bloqueava todo o
			// agente, o que fazia o outro behaviour nao imprimir coisas
			ACLMessage msg = receive();
			if (msg != null) {
				if (msg.getPerformative() == ACLMessage.INFORM) {
					System.out.println("<" + getLocalName() + "> [RECEIVED] "
							+ msg.getContent());
					// cria resposta
					// ACLMessage reply = msg.createReply();
					// preenche conteúdo da mensagem
					// reply.setContent("Hello there");
					// envia mensagem
					// send(reply);
				}
			}
			block(); // Bloqueia até a proxima mensagem chegar
		}

	}
	
	/**
	 * Behaviour que move o truck de posicao
	 * 
	 */
	class DeliveryParcelsBehaviour extends TickerBehaviour {
		private static final long serialVersionUID = 1837679922616403427L;

		// construtor do behaviour
		public DeliveryParcelsBehaviour(Agent a) {
			super(a, 2000); // Imprime a mensagem a cada 1 segundo
		}

		// método action
		public void onTick() {
			while(!getParcels().isEmpty()){ //While there's parcels, deliver them
				
				AutoPilot autoPilot = new AutoPilot(roads);
				Point truckPosition = getCurrentPosition();
				Iterator<Parcel> iter = getParcels().iterator();
				while (iter.hasNext()) {
					   Parcel parc = iter.next();
					   
					   Point pickupPoint = parc.getPosition();
					   Point deliveryPoint = parc.getDestination().getPosition();
					   Path path = autoPilot.getPath(truckPosition, pickupPoint);
					   Path path2 = autoPilot.getPath(pickupPoint, deliveryPoint);
					   
					   String path_str = ""; 
					   
					   //Make the Trucks Move from one position to a destination
					   for (Point point : path.getPath()) {
						    String input = AutoPilot.getDirection(truckPosition, point);
							path_str += input + "";
							
							try {
								makeMove(input);
								//map.update();
							} catch (EndOfMapException e) {
								System.out.println(e.getMessage());
								
							}
							truckPosition = point;
							//System.out.println(roads.print());
					   }
					   
					   //System.out.println("Truck Final Position: " + getCurrentPosition());
					   //System.out.println("Parcel Final Position: " + pickupPoint);
					   if(getCurrentPosition().equals(pickupPoint)){
							System.out.println("Parcel Picked up with sucess at: " + pickupPoint);
					   }
					   
					   //Make the Trucks Move from one position to a destination
					   for (Point point : path2.getPath()) {
						    String input = AutoPilot.getDirection(truckPosition, point);
							path_str += input + "";
							try {
								makeMove(input);
								//map.update();
							} catch (EndOfMapException e) {
								System.out.println(e.getMessage());
								
							}
							truckPosition = point;
							//System.out.println(roads.print());
					   }
					   
					   //System.out.println("Truck Final Position: " + getCurrentPosition());
					   //System.out.println("Parcel Final Position: " + deliveryPoint);
					   if(getCurrentPosition().equals(deliveryPoint)){
						   	//Removes the Parcel from the truck
						   	iter.remove();
							System.out.println("Parcel Delivered with sucess at: " + deliveryPoint);
					   }
				}
			}
			//printAllInfo();
		}

	}

	/**
	 * Behaviour que imprime coisas
	 * 
	 */
	class BehaviourPrintStuff extends TickerBehaviour {
		private static final long serialVersionUID = 1837679922616403427L;

		// construtor do behaviour
		public BehaviourPrintStuff(Agent a) {
			super(a, 2000); // Imprime a mensagem a cada 1 segundo
		}

		// método action
		public void onTick() {
			System.out.println("<" + this.myAgent.getLocalName()
					+ "> Printing some stuff");
		}

	}
	
	public Set<Parcel> getParcels() {
		return this.parcels;
	}
	
	public Point getCurrentPosition() {
		return this.currentPosition;
	}
	
	/**
	 * Sets the current position (x, y) for the Truck
	 * @param destination
	 */
	public void setCurrentPosition(Point currentPosition) {
		this.currentPosition = currentPosition;
	}
	
	/**
	 * Moves the Truck to one Specific direction: Left, Right, Up or Down
	 * @param direction
	 * @return
	 * @throws EndOfMapException
	 */
	public boolean makeMove(String direction) throws EndOfMapException{
		
		Point truckPosition = this.getCurrentPosition();
		Point destination = null;
		
		switch (direction.toLowerCase()){
			case "u":
				destination =  new Point(truckPosition.x, truckPosition.y+1);
				break;
			case "l":
				destination =  new Point(truckPosition.x-1, truckPosition.y);
				break;
			case "d":
				destination =  new Point(truckPosition.x, truckPosition.y-1);
				break;
			case "r":
				destination =  new Point(truckPosition.x+1, truckPosition.y);
				break;
			case "w":
				return true;
			case "a":
				//throw new EndOfMapException("You aborted the city-finding activity. Score: " + truck.getFinalScore());
			default:
				return false;
		}
		
		this.setCurrentPosition(destination);
		
		//Moves every parcel inside the truck with it
		//this.makeParcelsInsideMove(destination);
		
		return true;
	}
	
	/**
	 * Prints in screen a representation of the Truck and its Parcels
	 */
	public void printAllInfo(){
		System.out.println("------------------------------------------------");
	    System.out.println("[TruckInfo]");
	    //System.out.println("TruckName: " + this.);
	    System.out.println("Position: " + this.getCurrentPosition().toString());
	    
	    //Print parcels information
	    Iterator<Parcel> iter = this.getParcels().iterator();
	    while (iter.hasNext()) {
	    	Parcel parc = iter.next();
	    	System.out.println("\n\t[ParcelInfo]");
	    	System.out.println("\tName: " + parc.getNome());
	    	System.out.println("\tOrigin: " + parc.getPosition().toString());
	    	System.out.println("\tDestination: " + parc.getDestination().getPosition().toString());
	    }	
	}
}
