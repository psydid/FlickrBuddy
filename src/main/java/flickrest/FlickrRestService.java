package flickrest;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.FlickrApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class FlickrRestService {

	public String API_KEY;
	public String API_SECRET;
	public static String REST_ENDPOINT = "https://api.flickr.com/services/rest/";
	
	private OAuthService oAuthService;

	private static FlickrRestService _INSTANCE;
	private Token accessToken;
	private Token requestToken;
	
	public FlickrRestService()  throws FileNotFoundException, IOException {
		
		//FileInputStream cfgFile = new FileInputStream("FlickrAppSecrets.cfg");
		
		//BufferedReader cfgReader = new BufferedReader(new InputStreamReader(cfgFile));
		
		/*
		try {
			API_KEY = cfgReader.readLine();
			API_SECRET = cfgReader.readLine();
		} finally {
			cfgReader.close();
		}
		*/
		
		//TODO: just pull this from FlickrAuthenticator
		API_KEY = System.getProperty("flickr.api.key", 
		            System.getenv("FLICKR_API_KEY"));
		API_SECRET = System.getProperty("flickr.api.secret", 
		            System.getenv("FLICKR_API_SECRET"));
		        
		
		
		oAuthService = new ServiceBuilder().
				apiKey(API_KEY).
				apiSecret(API_SECRET).
				provider(FlickrApi.class).
				build();
		
	}

	public String getAuthUrl() {
		requestToken = oAuthService.getRequestToken();
		return oAuthService.getAuthorizationUrl(requestToken);
	}
	
	public void authorize(String verifierString) {
		Verifier verifier = new Verifier(verifierString);
		accessToken = oAuthService.getAccessToken(requestToken, verifier);
	}
	
	public Token getAccessToken() {
		return accessToken;
	}
	
	public void setAccessToken(Token token) {
		accessToken = token;
	}
	
	public static FlickrRestService get() {
		try {
		
			if(_INSTANCE == null) {
				_INSTANCE = new FlickrRestService();
			}
			return _INSTANCE;
		}
		catch(FileNotFoundException f) {
			System.out.println("Could not find config file FlickrAppSecrets.cfg");
			return null;
		}
		catch(IOException e) {
			System.out.println("FlickrAppSecrets.cfg must contain two lines. First line is your API key, second line is your API secret");
			return null;			
		}
	}
	
	private Document getResponse(String method, Map<String, String> params) throws Exception {
		OAuthRequest request = new OAuthRequest(Verb.GET, REST_ENDPOINT);
		request.addQuerystringParameter("method", method);
		if(params != null) {
			for(Entry<String,String> param : params.entrySet()) {
				request.addQuerystringParameter(param.getKey(), param.getValue());
			}
		}
		oAuthService.signRequest(accessToken, request);
		Response response = request.send();
		
		String responseXml = response.getBody();
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(new ByteArrayInputStream(responseXml.getBytes("UTF-8")));
		
		Node statusNode = doc.getElementsByTagName("rsp").item(0);
		String status = statusNode.getAttributes().getNamedItem("stat").getNodeValue();
		if(!status.equals("ok")) {
			throw new Exception(status.toString());
		}
		
		return doc;
	}
	
	
	public FlickrCollectionList getColls() throws Exception {
		
		Document doc = getResponse("flickr.collections.getTree", null);
		Node collections = doc.getElementsByTagName("collections").item(0);
		
		FlickrCollectionList list = (FlickrCollectionList) unmarshal(collections, FlickrCollectionList.class);
		return list;
	}
	
	public FlickrPhotoList getPhotosInSet(FlickrSet set) throws Exception {
		
		HashMap<String, String> params = new HashMap<String,String>();
		params.put("photoset_id", set.getId());
		
		Document doc = getResponse("flickr.photosets.getPhotos", params);
		Node collections = doc.getElementsByTagName("photoset").item(0);
		
		FlickrPhotoList list = (FlickrPhotoList) unmarshal(collections, FlickrPhotoList.class);
		return list;
	}

	public FlickrPhotoSizeList getPhotoSizes(FlickrPhoto photo) throws Exception {
		
		HashMap<String, String> params = new HashMap<String,String>();
		params.put("photo_id", photo.getId());
		
		Document doc = getResponse("flickr.photos.getsizes", params);
		Node collections = doc.getElementsByTagName("sizes").item(0);
		
		FlickrPhotoSizeList list = (FlickrPhotoSizeList) unmarshal(collections, FlickrPhotoSizeList.class);
		return list;
	}
	
	
	
	private Object unmarshal(Node responseNode, Class<?> rootClass) throws JAXBException {
		
		JAXBContext context = JAXBContext.newInstance(rootClass);
		Unmarshaller um = context.createUnmarshaller();
		Object o = um.unmarshal(responseNode);

		return o;
	}
	
	/*
	public String test() {
		String call = REST_ENDPOINT + "?method=flickr.test.echo&api_key=" + API_KEY;
		
		try {
			ClientResource client = new ClientResource(call);
			return client.get().getText();
		} catch(IOException e) {
			return "error: " + e.getMessage();
		}
			
	}*/
	
	
}
