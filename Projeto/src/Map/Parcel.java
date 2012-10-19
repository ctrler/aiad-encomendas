package Map;

public class Parcel implements Cell {
	
public static char IDENTIFIER = 'P';
public City destination;
	
	@Override
	public String print() {
		return "P";
	}
	
	public void setDestination(City destination){
		this.destination = destination;
	}

	public City getDestination(){
		return this.destination;
	}

}
