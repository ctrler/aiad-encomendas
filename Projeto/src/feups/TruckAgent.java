package feups;


import java.awt.Point;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
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
	
	public enum Modo {PARCEL, ENCONTRO};
	
	/** Parcels a entregar que nao estao na minha carga*/
	Set<Parcel> parcels;
	
	/** Parcels na minha carga (viajam comigo e sao actualizados sempre que me movo) */
	Set<Parcel> cargo;
	
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
			

			/* Enviamos uma mensagem ao World a dizer onde estamos
			 */
			//TODO
			
			/* Actualizamos a localização dos parcels dentro do camião */
			moveCargo();
			
			/* Tentamos obter uma parcel para entregar e caso
			 * exista carregamos o destino do truck para a origem desse parcel
			 * Caso já estejamos com o parcel em carga
			 * ou destinatario dessa parcel 
			 * e traçamos uma rota até a parcel;
			 */
			if(currentParcel==null && modoF == Modo.PARCEL){
				currentParcel=getNextParcel();
				
				if(currentParcel!=null){ // Havia parcel para entregar
					
					if(currentParcel.getPosition().equals(currentPosition)){ //Parcel já dentro do camião
						destination = currentParcel.getDestination().getPosition();
						currentRoute = autoPilot.getPath(currentPosition, destination);
					}
					else{ // Vamos buscar parcel
						//FIXME Implementar esta parte
					}
				}
				
			}
			
			/* Caso chegada ao destino */
			if(currentPosition.equals(destination)){
				
				/* Se estamos a entregar um Parcel que está na Cargo do Truck
				 */
				if(destination.equals(currentParcel.getDestination().getPosition())){ // Entregamos o parcel
					Debug.print(1,this.myAgent.getLocalName() + " ENTREGOU " + currentParcel.getNome() + " Km: " + km);
					cargo.remove(currentParcel);
					// Colocamos tudo a null, ele vai tratar de carregar mais um parcel no próximo tick
					currentRoute = null;
					destination = null;
					currentParcel = null;
				}
				// FIXME Verificar para situações em que nao estamos a entregar parcels.
			}
			
			/* Caso exista uma parcel para entregar
			 * ou seja, uma rota a percorrer, 
			 * comemos a cada tick um bocadinho dessa rota
			 */
			if(currentRoute!=null){
				Debug.print(1, this.myAgent.getLocalName() + " GPS: " + currentPosition.getX() + " " + currentPosition.getY() );
				
				Point next = null;
				try{
					next = currentRoute.getPath().pop(); // remove o primeiro elemento da lista
				}catch (NoSuchElementException e){ // lista vazia, nao ha mais pontos
					Debug.print(0,"LISTA VAZIA");
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
					} catch (EndOfMapException e) {
						Debug.print(0,e.getMessage());
					}
				}
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
	Parcel getNextParcel(){
		if(cargo==null)
			return null;
		if(cargo.isEmpty())
			return null;
		// FIXME Retornar a Parcel mais próxima e não a primeira;
		return (Parcel) cargo.toArray()[0];
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
	 * Moves the Parcels inside the truck with it
	 */
	private void moveCargo() {
		Iterator<Parcel> iter = this.cargo.iterator();
	    while (iter.hasNext()) {
		    Parcel parc = iter.next();
		    parc.setCurrentPosition(currentPosition);
	    }
	}
	
}
