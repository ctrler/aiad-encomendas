package feups;

public class Debug {
	
	/** Nível de log:
	 * 		0 - Apenas erros
	 * 		1 - Info
	 * 		2 - Verbose
	 */
	
	public enum PrintType {
	    AGENTLOCATION, 
	    DEBUG,
	    TRUCKMSG,
	    PARCELDELIVERY, AGENTLOCATIONRECEIVED, AUTOPILOT
	}
	
	public static void print(PrintType level, String msg){
		
		if(printLevel(level))
			System.out.println(msg);
	}
	
	public static void print(int level, String msg){
			System.out.println(msg);
	}
	
	private static boolean printLevel(PrintType level){
		
		switch(level){
			case DEBUG :
				return false;
			case AGENTLOCATIONRECEIVED:
				return false;
			case AUTOPILOT:
				return false;
			default:
				return true;
		
		}

		
	}
}
