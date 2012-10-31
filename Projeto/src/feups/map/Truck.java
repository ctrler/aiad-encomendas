package feups.map;

public class Truck implements Cell{
	
	public static char IDENTIFIER = 'T';
	
	private int steps = 0;
	
	public void addStep(){
		steps++;
	}
	
	@Override
	public String print() {
		return "T";
	}

	public int getFinalScore() {
		return (steps) ;
	}

}