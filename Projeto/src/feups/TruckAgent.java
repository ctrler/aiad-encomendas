package feups;


import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import feups.communication.TruckWorldCommunication;
import feups.ia.AutoPilot;
import feups.map.EndOfMapException;
import feups.map.Path;
import feups.map.Roads;
import feups.parcel.Parcel;
import feups.truck.Truck;
import jade.core.AID;
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
	
	public enum Modo {PARCEL, ENCONTRO};
	
	/** Parcels a entregar que nao estao na minha carga*/
	Set<Parcel> parcels;
	
	/** Parcels na minha carga (viajam comigo e sao actualizados sempre que me movo) */
	Set<Parcel> cargo;
	
	LinkedList<Parcel> delivered;
	
	private Roads roads; //Used so Truck can navigate in the map
	Point currentPosition;
	Parcel currentParcel;
	AutoPilot autoPilot;
	
	/** Rota até ao destino	 */
	Path currentRoute;
	
	/** Destino */
	Point destination;
	
	/** Modo de funcionamento */
	Modo modoF;
	
	/** Distancia percorrida */
	double km;
	
	private static final long serialVersionUID = -4023017875029640114L;

	public TruckAgent(Truck truck, Roads map, Set<Parcel> cargo) {
		this.currentPosition = truck.getCurrentPosition();
		this.roads = map;
		this.parcels = new HashSet<Parcel>();
		this.currentParcel = null;
		this.cargo = cargo;
		this.km = (double) 0.0;
		this.delivered = new LinkedList<Parcel>();
		
		this.modoF=Modo.PARCEL;
		autoPilot = new AutoPilot(roads);
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
//		DFAgentDescription template = new DFAgentDescription();
//		ServiceDescription sd1 = new ServiceDescription();
//		sd1.setType("Agente World");
//		template.addServices(sd1);
		
		
		ParallelBehaviour par = new ParallelBehaviour(
				ParallelBehaviour.WHEN_ALL);
		//par.addSubBehaviour(new TruckAgentBehaviour(this));
		par.addSubBehaviour(new DeliveryParcelsBehaviour(this));
		//par.addSubBehaviour(new BehaviourPrintStuff(this));

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
			Debug.print(1, this.myAgent.getLocalName() + " \tGPS: " + currentPosition.getX() + " " + currentPosition.getY() + " Destination is " + destination);


			moveCargo(); //Actualizamos a localização dos parcels dentro do camião 
			checkDelivery(); // Verificamos se existe parcels para entregar

			/* Caso chegada ao destino 
			 * Limpamos a rota e obtemos uma nova parcel
			 * caso se esteja em modo Parcel
			 */
			if(currentPosition.equals(destination)){
				
				// Limpamos a rota
				currentRoute = null;
				destination = null;
			}
			
			/* Caso não exista destino */
			if(destination==null){
				
				// Se estivermos em modo parcel criamos nova rota
				if(modoF == Modo.PARCEL){
					Parcel nextP = getNextCargoParcel();
					if( nextP!=null){			// Apenas se houver parcel para entregar
						destination = nextP.getDestination().getPosition();
						currentRoute = autoPilot.getPath(currentPosition, destination);
					}
				}
			}
			
			/* Caso exista uma rota a percorrer, 
			 * comemos a cada tick um bocadinho dessa rota
			 */
			if(currentRoute!=null){

				Point next = null;
				try{
					next = currentRoute.getPath().pop(); // remove o primeiro elemento da lista
				}catch (NoSuchElementException e){ // lista vazia, nao ha mais pontos
					Debug.print(0, this.myAgent.getLocalName() + "LISTA VAZIA");
					//reset();	// isto volta a executar o onTick() sem fazer o resto.				
				}
				
				if(next!=null){ // caso exista póximo ponto
					
					// TODO porque é que precisamos de fazer isto e depois fazer um make move?
					// estamos a usar o curentPosition aqui e depois o make move volta a utilizar...
					// Se a Path que temos é um conjunto de pontos SEQUENCIAIS nao basta apenas consumir esses pontos
					// um a um? Cada um dos pontos a cada tick passa a ser a current position.
					String input = AutoPilot.getDirection(currentPosition, next);
					try {
						makeMove(input);
						informWorld();
					} catch (EndOfMapException | IOException e) {
						Debug.print(0,e.getMessage());
					}
				}
			}
		}

		/**
		 * Creates the object to be sent to the World with:
		 * Truck Position, Truck km
		 */
		private void informWorld() throws IOException {
			// pesquisa DF por agentes "Agente Truck"
			DFAgentDescription template = new DFAgentDescription();
			ServiceDescription sd1 = new ServiceDescription();
			sd1.setType("Agente World"); // Vai procurar por todos os agentes
											// deste tipo
			template.addServices(sd1);
			
			TruckWorldCommunication reg = new TruckWorldCommunication (currentPosition);
			
			try {
				DFAgentDescription[] result = DFService.search(this.myAgent,
						template);
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				for (int i = 0; i < result.length; ++i)
					msg.addReceiver(result[i].getName());
				
				msg.setContentObject(reg);
				msg.setLanguage("JavaSerialization");
				send(msg);
			}catch (FIPAException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Behaviour que envia posição actual ao World
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
	
	/** Próxima Parcel a ser entregue;
	 * @return A parcela mais próxima ou null se já tiverem sido todas entregues;
	 */
	Parcel getNextCargoParcel(){
		/* TODO Isto está muito complicado para algo que devia ser mais simples...
		 */
		
		if(cargo==null)
			return null;
		if(cargo.isEmpty())
			return null;
		
		List<Parcel> cargoParcels = new LinkedList<Parcel>(cargo);

		Parcel closest = null;
		long dist = Long.MAX_VALUE;
		
		for(Parcel p : cargoParcels){
			Path tempPath = autoPilot.getPath(getCurrentPosition(),p.getDestination().getPosition());
			long tempDist = tempPath.calculateLenght();
			if(tempDist < dist){
				closest = p;
				dist = tempDist;
			}
		}
		
		Debug.print(2,this.getLocalName() + ": Proxima parcel is "+ closest);
		return closest;
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
		Point destination = this.getCurrentPosition();  // Para o caso de cair num "w" ou num "a"?!
														// nao queremos colocar a currentPosition a null.
		
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
			case "w": //TODO ?
				return true;
			case "a": // TODO ?
				//throw new EndOfMapException("You aborted the city-finding activity. Score: " + truck.getFinalScore());
			default:
				return false;
		}
		
		km = km+currentPosition.distance(destination);
		// Corrigi em cima, isto podia colocar a current position a null!
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
	
	/**
	 * Move todas as Parcels no Cargo do Truck para a posição actual
	 */
	private void moveCargo() {
		Iterator<Parcel> iter = this.cargo.iterator();
	    while (iter.hasNext()) {
		    Parcel parc = iter.next();
		    parc.setCurrentPosition(currentPosition);
	    }
	}
	
	/**
	 * Verifica a posição actual e caso exista uma Parcel a ser entregue para
	 * essa posição entrega essa Parcel.
	 */
	private void checkDelivery() {
		Iterator<Parcel> iter = this.cargo.iterator();
	    while (iter.hasNext()) {
			Parcel parcel = iter.next();
			/* Entregamos a parcel */
			if(parcel.getDestination().getPosition().equals(this.getCurrentPosition())){
				Debug.print(1,this.getLocalName() + " ENTREGOU " + parcel.getNome() + " Km: " + km + " a "
						+ currentPosition.getX() + " " + currentPosition.getY());
				delivered.add(parcel);
				iter.remove();
			}
		}
	}
	
	
	
}
