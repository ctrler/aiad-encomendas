package feups;

public class Debug {
	
	/** Nível de log:
	 * 		0 - Apenas erros
	 * 		1 - Info
	 * 		2 - Verbose
	 */
	static final int level = 1;
	
	public static void print(int level, String msg){
		if(level<=Debug.level)
			System.out.println(msg);
	}
}
