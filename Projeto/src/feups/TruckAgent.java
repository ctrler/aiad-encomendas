package feups;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

import java.io.Serializable;

import feups.map.Path;


public class TruckAgent extends Agent{
	private static final long serialVersionUID = -4023017875029640114L;


	protected void setup() {
		System.out.println("########## TRUCK AGENT " + getLocalName() + ": Acordado");
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
		
		// Envia a mensagem ao world
		try {
			DFAgentDescription[] result = DFService.search(this, template);
			// envia mensagem "pong" inicial a todos os agentes "ping"
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			for (int i = 0; i < result.length; ++i)
				msg.addReceiver(result[i].getName());
			msg.setContent(this.getLocalName() + " pronto para o serviço!");

			send(msg);
		} catch (FIPAException e) {
			e.printStackTrace();
		}
		
		// regista behaviour
//		TruckAgentBehaviour b = new TruckAgentBehaviour(this);
//		addBehaviour(b);
		
	}
	
	// classe do behaviour
	class TruckAgentBehaviour extends SimpleBehaviour {
		private static final long serialVersionUID = 1837679922616403427L;
		private int n = 0;

		// construtor do behaviour
		public TruckAgentBehaviour(Agent a) {
			super(a);
		}

		// método action
		public void action() {
			
			// Espera por mensagens recebidas
			ACLMessage msg = blockingReceive();
			if (msg.getPerformative() == ACLMessage.INFORM) {
				System.out.println(++n + " " + getLocalName() + ": recebi "
						+ msg.getContent());
				// cria resposta
				ACLMessage reply = msg.createReply();
				// preenche conteúdo da mensagem
				reply.setContent("Hello there");
				// envia mensagem
				send(reply);
			}
		}

		// método done
		public boolean done() {
			return false;
		}
	} 
}
