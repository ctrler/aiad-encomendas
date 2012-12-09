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
	    PARCELDELIVERY, 
	    AGENTLOCATIONRECEIVED, 
	    AUTOPILOT, 
	    DEBUGEVALROUTE, 
	    SETUP, GETNEXTPARCEL, PARCELNEGOTIATION
	}
	
	public static void print(PrintType level, String msg){
		
		if(printLevel(level))
			System.out.println(msg);
	}
	
	@Deprecated
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
			case PARCELDELIVERY:
				return true;
			case GETNEXTPARCEL:
				return true;
			case PARCELNEGOTIATION:
				return true;
			case DEBUGEVALROUTE:
				return true;
			case AGENTLOCATION:
				return false;
			default:
				return false;
		
		}

		
	}
}
