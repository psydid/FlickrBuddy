package flickrest;


import javax.xml.bind.annotation.XmlAttribute;

public class FlickrPhoto {

	private String id;
	private String title;
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
