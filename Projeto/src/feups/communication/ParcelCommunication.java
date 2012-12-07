package feups.communication;

import feups.map.Path;
import feups.parcel.Parcel;

public class ParcelCommunication implements java.io.Serializable {
	
	Parcel parcel;

	public Parcel getParcel() {
		return parcel;
	}

	public void setParcel(Parcel parcel) {
		this.parcel = parcel;
	}
	

}
