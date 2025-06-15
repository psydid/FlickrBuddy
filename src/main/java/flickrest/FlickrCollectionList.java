package flickrest;



import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="collections")
public class FlickrCollectionList {
	
	@XmlElement(name="collection")
	private ArrayList<FlickrCollection> colls;

	public ArrayList<FlickrCollection> getCollections() {
		return colls;
	}

	public void setCollections(ArrayList<FlickrCollection> collections) {
		this.colls = collections;
	}
	
	
}
