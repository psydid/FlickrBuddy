package metricyard.flickrbuddy.flickrest;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class FlickrCollection {

	
	private ArrayList<FlickrSet> sets;
	
	
	public ArrayList<FlickrSet> getSets() {
		return sets;
	}

	@XmlElement(name="set")
	public void setSets(ArrayList<FlickrSet> sets) {
		this.sets = sets;
	}

	String id;
	String title;
	
	public String getId() {
		return id;
	}
	
	@XmlAttribute
	public void setId(String id) {
		this.id = id;
	}
	
	public String getTitle() {
		return title;
	}
	
	@XmlAttribute
	public void setTitle(String title) {
		this.title = title;
	}
	
	
	
}
