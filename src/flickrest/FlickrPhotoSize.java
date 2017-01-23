package flickrest;

import javax.xml.bind.annotation.XmlAttribute;

public class FlickrPhotoSize {

	private String label;
	private String source;
	public String getLabel() {
		return label;
	}
	@XmlAttribute
	public void setLabel(String label) {
		this.label = label;
	}
	public String getSource() {
		return source;
	}
	@XmlAttribute
	public void setSource(String source) {
		this.source = source;
	}
	
	
}
