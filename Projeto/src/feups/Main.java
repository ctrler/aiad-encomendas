package feups;
import java.awt.Point;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import feups.city.City;
import feups.communication.TruckPathCommunication;
import feups.ia.AutoPilot;
import feups.map.EndOfMapException;
import feups.map.Roads;
import feups.map.Path;
import feups.parcel.Parcel;


public class Main {
	
	//Change here to load another file
	public static final String INPUT_FILE = "maps/example1.map";

	//Initialization of the map
	private static Roads map;
	
	//Initialization of the world
	private static World world;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		world = new World();
		
		System.out.println("The file will now be loaded!\n");
		
		Parser parser = new Parser(world);
		if (parser.getDetails())
			System.out.println("Parsing OK");
		else
			System.out.println("Parsing FAIL");
		
		
		AutoPilot autoPilot = new AutoPilot(world.getMap());
		
		Point pontoOrigemA = new Point(4,3);
		Point pontoOrigemB = new Point(15,3);
		Point pontoDestinoA = new Point(11,11);
		Point pontoDestinoB =  new Point(7,11);
		
		Path pathA = autoPilot.getPath(pontoOrigemA, pontoDestinoA);
		Path pathB = autoPilot.getPath(pontoOrigemB, pontoDestinoB);
		
		double custoA = pathA.calculateLenght();
		double custoB = pathB.calculateLenght();
		System.out.println("custo A " + custoA);
		System.out.println("custo B " + custoB);
		
		
		System.out.println(pathA.toString());
		System.out.println(world.getMap().printRoute(pathA));
		System.out.println(pathB.toString());
		System.out.println(world.getMap().printRoute(pathB));
		
		
		// Determinar o ponto de encontro em path1 e path2
		Point pontoEncontroA = pathA.getFirstCommon(pathB);
		Point pontoEncontroB = pathB.getFirstCommon(pathA);
		
		System.out.println("Ponto de encontro A é " + pontoEncontroA);
		System.out.println("Ponto de encontro B é " + pontoEncontroB);
		
		
		Point pontoEncontro = pontoEncontroB;
		
		// Custo de entregar ponto de encontro + minha +  dele;
		Path pathA_mais_B = autoPilot.getPath(pontoOrigemA, pontoEncontro);
		
		List<Point> pointsParcels = new LinkedList<Point>();
		pointsParcels.add(pontoDestinoA);
		pointsParcels.add(pontoDestinoB);
		
		pathA_mais_B.add(autoPilot.getPath(pontoEncontro, pointsParcels));
		
		/* Como o AutoPilot.getPath() devolve apenas o path ate ao primeiro ponto
		 * temos que ver qual dos pontos é que ele encontrou primeiro e depois 
		 * adicionar o path até ao segundo ponto.
		 */
		if(pathA_mais_B.getPath().getLast().equals(pontoDestinoA)){
			pathA_mais_B.add(autoPilot.getPath(pontoDestinoA, pontoDestinoB));
		}
		else{
			pathA_mais_B.add(autoPilot.getPath(pontoDestinoB, pontoDestinoA));
		}
		
		System.out.println(pathA_mais_B.toString());
		System.out.println(world.getMap().printRoute(pathA_mais_B));
		
		/* Agora que temos todas as hipoteses verificamos qual é a melhor opção
		 */
		double custoPathA_mais_B = pathA_mais_B.calculateLenght();
		
		double hipoteseSeparados = custoA+custoB;
		double hipoteseJuntos = custoPathA_mais_B + autoPilot.getPath(pontoOrigemB, pontoEncontro).calculateLenght();
		
		System.out.println("custo separados: " + hipoteseSeparados +"\ncusto juntos: " + hipoteseJuntos);
			
	}
	
	
	

}
