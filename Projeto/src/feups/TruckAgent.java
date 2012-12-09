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
import java.util.Random;
import java.util.Set;

import feups.communication.ParcelCommunication;
import feups.communication.TruckPathAnswer;
import feups.communication.TruckPathCommunication;
import feups.communication.TruckWorldPosCommunication;
import feups.ia.AutoPilot;
import feups.map.EndOfMapException;
import feups.map.Path;
import feups.map.Roads;
import feups.parcel.Parcel;
import feups.truck.Truck;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

public class TruckAgent extends Agent {
	
	public enum Modo {PARCEL, ENCONTRO_OFERECE, ENCONTRO_RECEBE, NEGOCIACAO, ENCONTRO_OFERECE_ESPERA, SPECIALDELIVERY};
	
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
	
	Random rnd;
	
	String talkingTo;

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
		rnd = new Random( hashCode() + System.currentTimeMillis());
		Debug.print(Debug.PrintType.SETUP,"########## TRUCK AGENT " + getLocalName()
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

		ParallelBehaviour par = new ParallelBehaviour(
				ParallelBehaviour.WHEN_ALL);
		par.addSubBehaviour(new ColaborateBehaviour(this));
		par.addSubBehaviour(new DeliveryParcelsBehaviour(this));
		//par.addSubBehaviour(new BehaviourPrintStuff(this));

		addBehaviour(par);

	}
	
	

	/**
	 * Behaviour que responde a mensagens
	 * 
	 */
	class ColaborateBehaviour extends CyclicBehaviour {
		private static final long serialVersionUID = 1837679922616403427L;

		// construtor do behaviour
		public ColaborateBehaviour(Agent a) {
			super(a);
		}

		// método action
		public void action() {
			ACLMessage msg = receive();
			if (msg != null) {

				/* ######## recebeu INFORM ##########
				 * 
				 */
				if (msg.getPerformative() == ACLMessage.INFORM && modoF==Modo.PARCEL) { // So responde se estiver em modo parcel
					
					try {
						Object obj = msg.getContentObject();
						
						// Caso a mensagem seja um Path vindo de outros trucks
						if(obj!=null && obj instanceof TruckPathCommunication){
							TruckPathCommunication reg =  (TruckPathCommunication) obj;
							Debug.print(Debug.PrintType.PARCELNEGOTIATION,"<" + getLocalName() + "> INFORM: TruckPathCommunication from truck " + msg.getSender().getLocalName());
							
							TruckPathAnswer ans = evaluateRoute(reg.getCurrentRoute());
							
							
							if(ans!=null){
								ans.setParcelTheirs(reg.getParcel());
								modoF=Modo.NEGOCIACAO; // Para parar com as coisas
								Debug.print(Debug.PrintType.PARCELNEGOTIATION,this.myAgent.getLocalName()+"> Posso ajudar " + msg.getSender().getLocalName() +
										" encontro em " + ans.getMeeting());
								
								ACLMessage reply = msg.createReply();
								
								talkingTo = ""+rnd.nextLong(); // unique hash;
								reply.setConversationId(talkingTo);
								
								reply.setContentObject(ans);
								reply.setLanguage("JavaSerialization");
								reply.setPerformative(ACLMessage.PROPOSE);
								
								send(reply);
							}
							else{
								Debug.print(Debug.PrintType.PARCELNEGOTIATION,this.myAgent.getLocalName()+"> ANS null");
								 modoF=Modo.PARCEL;
							}
							
						}
						else{
							 modoF=Modo.PARCEL;
						}
					} catch (UnreadableException e) {
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					
				}
				
				/* ######## recebeu um PROPOSE ##########
				 * 
				 */
				if(msg.getPerformative() == ACLMessage.PROPOSE && modoF==Modo.PARCEL){
					//Caso seja REQUEST do path
					Debug.print(Debug.PrintType.PARCELNEGOTIATION,"<" + getLocalName() + "> Received PROPOSE From <" + msg.getSender().getLocalName() + ">");
					try {
						
						Object obj = msg.getContentObject();
						
						// FIXME Colocar isto a responder a fazer uma lista de prioridades de propostas
						if(obj instanceof TruckPathAnswer){
							talkingTo = msg.getConversationId();
							
							TruckPathAnswer reg = (TruckPathAnswer) obj;
							
							// Coloco me em modo de recebe e vou ate ao destino
							// onde espero por uma mensagem.
							modoF = Modo.ENCONTRO_OFERECE;
							Debug.print(Debug.PrintType.PARCELNEGOTIATION,"<" + getLocalName() + "> modeF = ENCONTRO_OFERECE");
							
							destination = reg.getMeeting();
							currentRoute = autoPilot.getPath(currentPosition, destination);
							Debug.print(Debug.PrintType.PARCELNEGOTIATION,"<" + getLocalName() + "> Rota até ponto oferta é " + currentRoute);
							
							ACLMessage reply = msg.createReply();
							
							reply.setContentObject(reg);
							reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
							reply.setLanguage("JavaSerialization");
							send(reply);
							addBehaviour(new SendsParcelBehaviour(this.myAgent, reg, msg));
						}
//						else{
//							// Caso a mensagem seja a resposta ao REQUEST
//							// de envio do Path
//							Object obj2 = msg.getContentObject();
//							if(obj!=null){
//								if(obj2 instanceof TruckPathCommunication){ 						// Verifica o tipo de objecto
//									TruckPathCommunication reg =  (TruckPathCommunication) obj2;
//									Debug.print(Debug.PrintType.PARCELNEGOTIATION,"<" + getLocalName() + "> Received Message From <" + msg.getSender().getLocalName() + "> | Content: " + reg);
//								}
//							}
//							
//							/* Adiciona novas parcels ao cargo */
//							if(obj2 instanceof ParcelCommunication){
//								ParcelCommunication reg =  (ParcelCommunication) obj2;
//								cargo.add(reg.getParcel()); // FIXME Mudar para parcels
//								Debug.print(Debug.PrintType.PARCELDELIVERY, this.myAgent.getLocalName() + " > RECEBIDA UMA PARCEL");
//							}
//						}
					}catch (IOException | UnreadableException ex) { ex.printStackTrace();}
				}
				
				/* ######## recebeu ACCEPT_PROPOSAL ##########
				 * 
				 */
				if(msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL && modoF==Modo.NEGOCIACAO && msg.getConversationId().equals(talkingTo) ){
					
					Debug.print(Debug.PrintType.PARCELNEGOTIATION,"<" + getLocalName() + "> Received ACCEPT_PROPOSAL From <" + msg.getSender().getLocalName() + ">");
					try {
						
						Object obj = msg.getContentObject();
						
						if(obj instanceof TruckPathAnswer){
							TruckPathAnswer reg = (TruckPathAnswer) obj;
							destination = reg.getMeeting();
							currentRoute = autoPilot.getPath(currentPosition, destination);
							Debug.print(Debug.PrintType.PARCELNEGOTIATION,"<" + getLocalName() + "> Rota até ponto oferta é " + currentRoute);
							modoF=Modo.ENCONTRO_RECEBE;
							
							addBehaviour(new ReceivesParcelBehaviour());
						}
					}catch (UnreadableException ex) { ex.printStackTrace();}
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
			super(a, 2000); // Imprime a mensagem a cada 2 segundo
		}

		// método action
		public void onTick() {
			Debug.print(Debug.PrintType.AGENTLOCATION, "<" + this.myAgent.getLocalName() + "> \tGPS: " + currentPosition.getX() + " " + currentPosition.getY() + " Destination is " + destination);
			try {
				informWorld();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}



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
					currentParcel = getNextCargoParcel();
					//currentRoute = getFullRoute();
					if(currentParcel!=null){			// Apenas se houver parcel para entregar
						//destination = currentRoute.getPath().getLast();
						destination = currentParcel.getDestination().getPosition();
						currentRoute = autoPilot.getPath(currentPosition, destination );
						//informOtherTrucks();
						
						/*
						 * Envia as mensagens ligueiramente em diferido para nao haver race conditions
						 */
		                addBehaviour( 
		                        new DelayBehaviour( myAgent, rnd.nextInt( 200 )+100) //FIXME Isto pode dar erro em computadores mais lentos
		                        {
		                        	private static final long serialVersionUID = -8022836574000583133L;

								public void handleElapsedTimeout() { 
		                        	   informOtherTrucks(); 
									}
		                        });
					}
					if(modoF==Modo.SPECIALDELIVERY){
						modoF = Modo.PARCEL;
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
						Debug.print(Debug.PrintType.PARCELDELIVERY,e.getMessage());
					}
				}
			}
		}

		/**
		 * Envia INFORM com Path + Parcels num objeto serializável
		 * para todos os restantes trucks do sistema
		 */
		private void informOtherTrucks() {
			/**
			 * Prepares message to be sent to other trucks in broadcast way
			 */
			// pesquisa DF por agentes "Agente Truck"
			DFAgentDescription template = new DFAgentDescription();
			ServiceDescription sd1 = new ServiceDescription();
			sd1.setType("Agente Truck"); // Vai procurar por todos os agentes
											// deste tipo
			template.addServices(sd1);
			
			// Envia a mensagem para os trucks
			try {
				DFAgentDescription[] result = DFService.search(this.myAgent,
						template);
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				
				for (int i = 0; i < result.length; ++i){
					if(!result[i].getName().getLocalName().equals(getLocalName())){
						// Envia uma mensagem para multiplos destinos
						// Excepto o proprio truck
						msg.addReceiver(result[i].getName()); 
					}
				}
				
				TruckPathCommunication reg = new TruckPathCommunication(currentRoute, currentParcel);
				msg.setContentObject(reg);
				msg.setLanguage("JavaSerialization");
				//msg.setContent("sendPath");

				send(msg);
			} catch (FIPAException | IOException e) {
				e.printStackTrace();
			}
		}

		private Path getFullRoute() {
			Path fullRoute = new Path();
			for(Parcel parcel : cargo){
				Path tempPath = autoPilot.getPath(parcel.getPosition().getLocation(),parcel.getDestination().getPosition());
				Path connectionPath = autoPilot.getPath(parcel.getDestination().getPosition(),parcel.getDestination().getPosition());
				//System.out.println("PATH de " + getLocalName() + " " + tempPath);
				fullRoute.add(tempPath);
			}
			if(fullRoute.getPath().isEmpty()){
				return null;
			}else return fullRoute;
		}

		/**
		 * Creates the object to be sent to the World with:
		 * Truck Position, Truck km
		 */
		private void informWorld() throws IOException {
			// pesquisa DF por agentes "Agente World"
			DFAgentDescription template = new DFAgentDescription();
			ServiceDescription sd1 = new ServiceDescription();
			sd1.setType("Agente World"); // Vai procurar por todos os agentes
											// deste tipo
			template.addServices(sd1);
			
			TruckWorldPosCommunication reg = new TruckWorldPosCommunication (currentPosition);
			
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
	 * Recebe uma parcel aquando da troca de parcels 
	 */
	class ReceivesParcelBehaviour extends Behaviour {
		private static final long serialVersionUID = -4295298617819796761L;
		TruckPathAnswer answerTPA = null;
		ACLMessage answerMsg = null;
		@Override
		public void action() {
			
			//Debug.print(Debug.PrintType.PARCELNEGOTIATION,"<" + getLocalName() + "> Receiver action() msg==null" );
			ACLMessage msg = receive();
			if (msg != null) {
				//Debug.print(Debug.PrintType.PARCELNEGOTIATION,"<" + getLocalName() + "> Receiver action() msg!=null" );
				/* se é uma mensagem do nosso interlocutor
				 * guardamos a parcel */
				if(msg.getConversationId().equals(talkingTo)){
					Debug.print(Debug.PrintType.PARCELNEGOTIATION,"<" + getLocalName() + "> Receiver action() Recebemos uma parcel!" );
					try {
						answerMsg = msg; //Guardamos a mensagem para podermos responder
						answerTPA = (TruckPathAnswer) msg.getContentObject();
					} catch (UnreadableException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			/* se já estivermos no destino
			 * definimos destino e enviamos msg accept
			 */
			if(destination==null && answerTPA!=null){
				cargo.add(answerTPA.getParcelTheirs());
				currentRoute = answerTPA.getPathMine();
				destination = answerTPA.getPathMine().getDestination();
				
				ACLMessage reply = answerMsg.createReply();
				try {
					reply.setContentObject(answerTPA);
				} catch (IOException e) { e.printStackTrace(); }
				reply.setLanguage("JavaSerialization");
				reply.setPerformative(ACLMessage.CONFIRM);
				
				for(Iterator iterator = reply.getAllReceiver();
						 iterator.hasNext();){
						             AID r = (AID) iterator.next();
						             System.out.println(getLocalName() + "> Receiver action() Destinatário: " + r.getLocalName());
					         }
				Debug.print(Debug.PrintType.PARCELNEGOTIATION,"<" + getLocalName() + "> Receiver action() " + reply);
				send(reply);
				send(reply); // FIXME Sim, isto é muito estranho mas se nao enviar duas vezes o receptor por vezes nao recebe.
				// Colocamos tudo nos valores normais
				talkingTo = "";
				Debug.print(Debug.PrintType.PARCELNEGOTIATION,"<" + getLocalName() + "> Receiver action() Fim, vamos entregar as parcels" + currentRoute.toString());
				modoF=Modo.SPECIALDELIVERY;
			}
			block(1000);
		}

		@Override
		public boolean done() {
			// Sai quando estiver no modo parcel;
			return modoF == Modo.SPECIALDELIVERY;
		}
		
	}
	
	/**
	 * Envia uma parcel aquando da troca de parcels 
	 */
	class SendsParcelBehaviour extends Behaviour {
		/**
		 * 
		 */
		private static final long serialVersionUID = -7259717416420472938L;
		TruckPathAnswer answerTPA = null;
		ACLMessage answerMsg = null;
		
		public SendsParcelBehaviour(Agent a, TruckPathAnswer answerTPA, ACLMessage answerMsg) {
			super(a);
			this.answerTPA = answerTPA;
			this.answerMsg = answerMsg;
			Debug.print(Debug.PrintType.PARCELNEGOTIATION,"<" + getLocalName() + "> Sender constructed" );
		}
		
		public void action() {
			ACLMessage msg = receive();
			if (msg != null) {
				Debug.print(Debug.PrintType.PARCELNEGOTIATION,"<" + getLocalName() + "> Sender action() msg!=null talkingTo="+talkingTo );
				/* se é uma mensagem do nosso interlocutor
				 * é pq este esta a confirmar a recepção da a parcel
				 * e nesse caso repomos os valores normais e acabamos a conversa
				 */
				 if(msg.getConversationId().equals(talkingTo) && msg.getPerformative() == ACLMessage.CONFIRM){
					Debug.print(Debug.PrintType.PARCELNEGOTIATION,"<" + getLocalName() + "> Sender action() Fim!");
					currentRoute = null;
					destination = null;
					talkingTo= "";
					modoF = Modo.PARCEL;
				}
			}
			
			/* se já estivermos no destino
			 * enviamos parcel
			 */
			if(destination==null && modoF == Modo.ENCONTRO_OFERECE){
				
				//modoF = Modo.ENCONTRO_OFERECE_ESPERA; // Ficamos em modo de espera.
				
				
				// Retiro parcel da minha cargo.
				List<Parcel> cargoParcels = new LinkedList<Parcel>(cargo);
				Parcel parcelRemover = null;
				for(Parcel p : cargoParcels){
					if(p.getNome().equals(answerTPA.getParcelTheirs().getNome()))
						parcelRemover = p;
				}
				cargo.remove(parcelRemover); //FIXME isto nao vai funcionar!!!
								
				// Envio mensagem ao destinatario com a parcel;
				ACLMessage reply = answerMsg.createReply();
				try {
					reply.setContentObject(answerTPA);
				} catch (IOException e) { e.printStackTrace(); }
				
				reply.setLanguage("JavaSerialization");
				reply.setPerformative(ACLMessage.INFORM); //TODO outro tipo?
				
				try {
					reply.setContentObject(answerTPA);
				} catch (IOException e) { e.printStackTrace();}
				 
				for(Iterator iterator = reply.getAllReceiver();
						 iterator.hasNext();){
						             AID r = (AID) iterator.next();
						             System.out.println(getLocalName()+ "> Sender action() Destinatário: " + r.getLocalName());
					         }
					Debug.print(Debug.PrintType.PARCELNEGOTIATION,"<" + getLocalName() + 
							"> Sender action() chegamos ao destino e enviamos a parcel");
				send(reply);
			}
			block(1000);
			
		}

		@Override
		public boolean done() {
			// Sai quando estiver no modo parcel;
			return modoF == Modo.PARCEL;
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
		double dist = Double.MAX_VALUE;
		
		for(Parcel p : cargoParcels){
			Path tempPath = autoPilot.getPath(getCurrentPosition(),p.getDestination().getPosition());
			double tempDist = tempPath.calculateLenght();
			if(tempDist < dist){
				closest = p;
				dist = tempDist;
			}
		}
		
		Debug.print(Debug.PrintType.GETNEXTPARCEL,this.getLocalName() + ": Proxima parcel is "+ closest);
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
				Debug.print(Debug.PrintType.PARCELDELIVERY,this.getLocalName() + " ENTREGOU " + parcel.getNome() + " Km: " + km + " a "
						+ currentPosition.getX() + " " + currentPosition.getY());
				delivered.add(parcel);
				iter.remove();
			}
		}
	}
	
	
	/**
	 * Retorna a melhor rota de entre duas possíveis
	 * 
	 * @return 
	 * 
	 */
	public TruckPathAnswer evaluateRoute(Path pathB){
		
		/* modo de troca
		 * por agora deixo no modo 2 
		 * (é sempre o agente que propoe que leva as parcels)
		 * ver comentario sobre modo no TruckPathanswer
		 */
		int modo = 2;
		
		Point pontoOrigemA = this.currentPosition;
		Point pontoDestinoA = this.destination;
		
		Point pontoOrigemB = pathB.getOrigin();
		Point pontoDestinoB = pathB.getDestination();
		
		Path pathA = this.currentRoute; //TODO Será que isto pode mudar a meio da execução?
		
		if(pathA!=null && pontoDestinoA!=null){
			
			double custoA = pathA.calculateLenght();
			double custoB = pathB.calculateLenght();
			
			Debug.print(Debug.PrintType.DEBUGEVALROUTE,this.getLocalName()+ "> PATH A: "+pathA.toString());
			Debug.print(Debug.PrintType.DEBUGEVALROUTE,this.getLocalName()+ "> ROADS A ####\n"+this.roads.printRoute(pathA));
			Debug.print(Debug.PrintType.DEBUGEVALROUTE,this.getLocalName()+ "> PATH B: "+pathB.toString());
			Debug.print(Debug.PrintType.DEBUGEVALROUTE,this.getLocalName()+ "> ROADS B ####\n"+this.roads.printRoute(pathB));
			
			
			// Determinar o ponto de encontro em path1 e path2
			Point pontoEncontroA = pathA.getFirstCommon(pathB);
			Point pontoEncontroB = pathB.getFirstCommon(pathA);
			
			Debug.print(Debug.PrintType.DEBUGEVALROUTE,this.getLocalName()+ "> Ponto de encontro A é " + pontoEncontroA);
			Debug.print(Debug.PrintType.DEBUGEVALROUTE,this.getLocalName()+ "> Ponto de encontro B é " + pontoEncontroB);
			
			// FIXME Fazer isto para os dois pontos... 
			Point pontoEncontro = pontoEncontroB;
			
			if(pontoEncontro==null)
				return null;
			
			Path pathA_encontro = autoPilot.getPath(pontoOrigemA, pontoEncontro);
			// Custo de entregar ponto de encontro + minha +  dele;
			if(pontoOrigemA.equals(pontoEncontro)){
				Debug.print(Debug.PrintType.DEBUGEVALROUTE,this.getLocalName()+ "> WARNING: Ponto de encontro A é igual a ponto Origem");
			}
			Debug.print(Debug.PrintType.DEBUGEVALROUTE,this.getLocalName()+ "> Path A a encontro " + pathA_encontro.toString());
			
			Path pathA_mais_B = pathA_encontro;
			
			
			List<Point> pointsParcels = new LinkedList<Point>();
			pointsParcels.add(pontoDestinoA); 
			pointsParcels.add(pontoDestinoB);
			
			Path novaPathA = autoPilot.getPath(pontoEncontro, pointsParcels);
			pathA_mais_B.add(autoPilot.getPath(pontoEncontro, pointsParcels));
			
			Debug.print(Debug.PrintType.DEBUGEVALROUTE,this.getLocalName()+ "> PontoDestino A " + pontoDestinoA);
			Debug.print(Debug.PrintType.DEBUGEVALROUTE,this.getLocalName()+ "> PontoDestino B " + pontoDestinoB);
			Debug.print(Debug.PrintType.DEBUGEVALROUTE,this.getLocalName()+ "> Path pontoEncontro a P1 " + novaPathA.toString());
			
			/* Como o AutoPilot.getPath() devolve apenas o path ate ao primeiro ponto
			 * temos que ver qual dos pontos é que ele encontrou primeiro e depois 
			 * adicionar o path até ao segundo ponto.
			 */

			Path ultima = null;
			if(pathA_mais_B.getPath().getLast().equals(pontoDestinoA)){
				ultima = autoPilot.getPath(pontoDestinoA, pontoDestinoB);
				pathA_mais_B.add(ultima);
				novaPathA.add(ultima);
			}
			else{
				ultima = autoPilot.getPath(pontoDestinoB, pontoDestinoA);
				pathA_mais_B.add(ultima);
				novaPathA.add(ultima);
			}
			
			Debug.print(Debug.PrintType.DEBUGEVALROUTE,this.getLocalName()+ "> Path A+B: "+ pathA_mais_B.toString());
//			Debug.print(Debug.PrintType.DEBUGEVALROUTE,this.getLocalName()+ "> "+ this.roads.printRoute(pathA_mais_B));
			
			/* Agora que temos todas as hipoteses verificamos qual é a melhor opção
			 */
			double custoPathA_mais_B = pathA_mais_B.calculateLenght();
			
			
			Path novaPathB = autoPilot.getPath(pontoOrigemB, pontoEncontro);
			

			double hipoteseJuntos = custoPathA_mais_B + novaPathB.calculateLenght();
			double hipoteseSeparados = custoA+custoB;
			
			Debug.print(Debug.PrintType.DEBUGEVALROUTE,"custo separados: " + hipoteseSeparados +"\ncusto juntos: " + hipoteseJuntos);
			
			if(hipoteseSeparados > hipoteseJuntos + 1){ // FIXME Mais um porque.... sim
				Debug.print(Debug.PrintType.DEBUGEVALROUTE,this.getLocalName()+ "> ENCONTRADA PATH MELHOR");
				TruckPathAnswer tpa;
				tpa = new TruckPathAnswer( novaPathA, novaPathB, pontoEncontro, hipoteseJuntos, modo);
				
				return tpa;
			}
			else{
				return null;
			}
		} // Fim do calculo para quando existe rota
		
		return null;

	}
	
	
}
