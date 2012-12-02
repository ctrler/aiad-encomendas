package feups;

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
	private static final long serialVersionUID = -4023017875029640114L;

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

		ParallelBehaviour par = new ParallelBehaviour(
				ParallelBehaviour.WHEN_ALL);
		par.addSubBehaviour(new TruckAgentBehaviour(this));
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
					System.out.println("<" + getLocalName() + "> recebi "
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
	 * Behaviour que imprime coisas
	 * 
	 */
	class BehaviourPrintStuff extends TickerBehaviour {
		private static final long serialVersionUID = 1837679922616403427L;

		// construtor do behaviour
		public BehaviourPrintStuff(Agent a) {
			super(a, 1000); // Imprime a mensagem a cada 1 segundo
		}

		// método action
		public void onTick() {
			System.out.println("<" + this.myAgent.getLocalName()
					+ "> Printing some stuff");
		}

	}
}
