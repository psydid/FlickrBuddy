package flickrest;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="rsp")
public class FlickrRestResponse {

	private String status;

	public String getStatus() {
		return status;
	}

	@XmlAttribute(name="stat")
	public void setStatus(String status) {
		this.status = status;
	}
	
	
}
