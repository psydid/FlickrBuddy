package metricyard.flickrbuddy.flickrest;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="sizes")
public class FlickrPhotoSizeList {
	
	@XmlElement(name="size")
	private ArrayList<FlickrPhotoSize> sizez;

	public ArrayList<FlickrPhotoSize> getSizes() {
		return sizez;
	}

	public void setSizes(ArrayList<FlickrPhotoSize> sizez) {
		this.sizez = sizez;
	}
	
}
