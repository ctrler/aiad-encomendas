package feups;

import jade.core.Agent;


import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.ThreadedBehaviourFactory;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;

import jade.domain.FIPAException;

import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

import java.awt.Point;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import feups.map.Roads;
import feups.parcel.Parcel;
import feups.city.City;
import feups.communication.ParcelCommunication;
import feups.communication.TruckPathCommunication;
import feups.communication.TruckWorldPosCommunication;
import feups.truck.Truck;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
//import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;	
import org.eclipse.swt.widgets.*;





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

	Text mapText;
	
	/**
	 * Default constructor
	 */
	public World() {
		this.trucks = new HashMap<String, Truck>();
		this.parcels = new HashMap<String, Parcel>();
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
				
		addBehaviour(new CreateParcel(this));
		addBehaviour(new ReceiveTruckMovement(this));
		addBehaviour(new PrintStatus(this));
		
		// Cria uma thread e chama o behaviour do GUI la dentro (senão bloqueava)
		ThreadedBehaviourFactory tbf = new
				ThreadedBehaviourFactory();
		addBehaviour(tbf.wrap(new GuiBehaviour()));
		

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
		//TODO: Puts this in his own behaviour
		for (String truckName : trucks.keySet()) {

			//Builds the TruckAgent
			TruckAgent t = new TruckAgent(getTruck(truckName), getMap(), getTruck(truckName).getParcels());

			AgentController agentController;
			try {
				agentController = getContainerController().acceptNewAgent(
						truckName, t);
				agentController.start();

			} catch (StaleProxyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	/** Behaviour que implementa o GUI */
	public class GuiBehaviour extends OneShotBehaviour{

		/**
		 * 
		 */
		private static final long serialVersionUID = 419316773680070435L;

		@Override
		public void action() {
			  Display myDisplay = new Display();
			  final Shell myShell = new Shell(myDisplay);
			  myShell.setText("Map");
			  myShell.setBounds(100, 100, 500, 300);
			  myShell.setLayout(new FillLayout());
			  
			  Font terminalFont = JFaceResources.getFont(JFaceResources.TEXT_FONT);
			  
			  mapText = new Text(myShell, SWT.MULTI);
			  mapText.setFont(terminalFont);
			  mapText.setText("...");
			  //mapText.pack();
			  
			  //myShell.pack();
			  myShell.open();
			  while (!myShell.isDisposed()) {
			   if (!myDisplay.readAndDispatch())
			    myDisplay.sleep();
			  }
			  myDisplay.dispose();
			
		}
		
	}
	
	/**
	 * Envia novas parcels aos camioões
	 */
	public class CreateParcel extends TickerBehaviour {
		// TODO Ir buscar parcels a lista de parcels
		// TODO verificar qual é o agent mais proximo da parcel
		
		private static final long serialVersionUID = 1L;

		public CreateParcel(Agent a) {
			super(a, 5000);
		}

		@Override
		public void onTick() {
			// pesquisa DF por agentes "Agente Truck"
			DFAgentDescription template = new DFAgentDescription();
			ServiceDescription sd1 = new ServiceDescription();
			sd1.setType("Agente Truck"); // Vai procurar por todos os agentes
											// deste tipo
			template.addServices(sd1);
			
			Parcel p = parcels.get("parcelEspinhoBraga"); // FIXME Fazer isto para todas as parcels ainda nao assigned.
			//Debug.print(Debug.PrintType.PARCELDELIVERY, "<" + getLocalName() + "> Vou tentar atribuir a parcel " + p.isAssigned());
			
			if(!p.isAssigned()){
				
				ParcelCommunication reg = new ParcelCommunication();
				
				
				reg.setParcel(p);
				
				// Envia a mensagem para os trucks
				try {
					DFAgentDescription[] result = DFService.search(this.myAgent,
							template);
					
					ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
					
					// FIXME Obter o camião mais próximo!
					for (int i = 0; i < result.length; ++i){
						if(result[i].getName().getLocalName().equals("1")){
							msg.addReceiver(result[i].getName());
							Debug.print(1, "<" + getLocalName() + "> Enviada parcel" + reg.getParcel().getNome() +" para: " + result[i].getName());
						}
					}
					msg.setContentObject(reg);
					msg.setLanguage("JavaSerialization");
					send(msg);
					
					p.setAssigned(true);
					
					
				} catch (FIPAException | IOException e) {
					e.printStackTrace();
					p.setAssigned(false);
				}
			}

		}

	}

	/**
	 * Used to Receive Truck Movements and updating the 
	 * general state of the World
	 * @author Joca
	 *
	 */
	public class ReceiveTruckMovement extends CyclicBehaviour {

		private static final long serialVersionUID = -8422485146766422510L;

		public ReceiveTruckMovement() {
		}

		public ReceiveTruckMovement(Agent a) {
			super(a);
		}

		@Override
		public void action() {
			/**
			 * Recebe dos trucks uma mensagem com a posiçao e actualiza;
			 */
			ACLMessage msg = receive();
			if (msg != null) {
				try {
					Object obj = msg.getContentObject();
					if(obj instanceof TruckWorldPosCommunication){ 						// Verifica o tipo de objecto
						TruckWorldPosCommunication reg =  (TruckWorldPosCommunication) obj;
						Debug.print(Debug.PrintType.AGENTLOCATIONRECEIVED,"<" + getLocalName() + "> Received Message From <" + msg.getSender().getLocalName() + "> | Content: " + reg);
						
						// Preenche o truckBeacon com os dados recebidos e
						// constroi lista de pontos percorridos
						Truck truckBeacon = getTruck(msg.getSender().getLocalName());	//Retorna o truck correspondente
						Point oldPosition = truckBeacon.getCurrentPosition();
						truckBeacon.setCurrentPosition(reg.getCurrentPosition().getLocation());		//Atualiza a posicao do truck
						if(!oldPosition.equals(truckBeacon.getCurrentPosition())){
							truckBeacon.addKM(); //Incrementa 1km percorrido
						}
										
						truckBeacon.getPositionHistory().add(reg.getCurrentPosition()); //Adiciona ponto percorrido ao histórico
					
					}
				}
				catch (UnreadableException ex) { ex.printStackTrace();}
				
			}
			block();
		}
	}
	
	/**
	 * Imprime o mapa e o estado de todos os camioes
	 * em: http://www.iro.umontreal.ca/~vaucher/Agents/Jade/primer6.html#6.6.4
	 */
	class PrintStatus extends TickerBehaviour {
		private static final long serialVersionUID = 1837679922616403427L;

		public PrintStatus(Agent a) {
			super(a, 500); 
		}

		public void onTick() {
			
			String line = "";
			double totalKm  = 0.0;
			for(Map.Entry<String, Truck> cursor : trucks.entrySet()) {
				line = line.concat(cursor.getKey() + ": " + cursor.getValue().getCurrentPosition().getX() + "," + cursor.getValue().getCurrentPosition().getY()+"\t");
				totalKm += cursor.getValue().getKM();
			}

			guiText(printRoads()+"\n##### Trucks ####\n"+line + "\nTotal Km: "+totalKm);
			
		}

	}

	protected void takeDown() {
		// retira registo no DF
		try {
			DFService.deregister(this);
		} catch (FIPAException e) {
			e.printStackTrace();
		}
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
	 * Gets the map by name
	 * 
	 * @param name
	 * @return Map of Roads
	 */
	public Roads getMap() {
		return this.roads;
	}

	
	/** Retorna a visualização do mapa com os trucks em cima
	 * 
	 */
	public String printRoads() {
		Roads tempRoads = new Roads(roads); // Construtor de cópia
		
		for(Map.Entry<String, Truck> cursor : trucks.entrySet()) {
			Truck t = cursor.getValue();
			tempRoads.setXY(t.getCurrentPosition().getX(), t.getCurrentPosition().getY(), cursor.getKey());
		}
		
		String output = tempRoads.print();
		
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
			//this.getMap().setXY(truck.getCurrentPosition().getX(),
			//		truck.getCurrentPosition().getY(), "T");
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
	
	/**
	 * Imprime na janela GUI um texto
	 * Feito para imprimir o mapa.
	 */
	public void guiText(final String text){
		Display.getDefault().asyncExec(new Runnable() {
		    public void run() {
		        mapText.setText(text);
		    }
		});
		
	}
	
	

}
