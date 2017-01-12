package metricyard.flickrbuddy.flickrest;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="photoset")
public class FlickrPhotoList {
	
	@XmlElement(name="photo")
	private ArrayList<FlickrPhoto> photoz;

	public ArrayList<FlickrPhoto> getPhotos() {
		return photoz;
	}

	public void setPhotos(ArrayList<FlickrPhoto> photos) {
		this.photoz = photos;
	}
	
}
