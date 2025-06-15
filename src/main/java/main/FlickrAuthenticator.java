package main;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.REST;
import com.flickr4java.flickr.auth.Auth;
import com.flickr4java.flickr.auth.AuthInterface;
import com.flickr4java.flickr.auth.Permission;
import com.flickr4java.flickr.util.AuthStore;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.model.OAuth1Token;

import org.scribe.model.Token;
import org.scribe.model.Verifier;

import java.util.Scanner;

public class FlickrAuthenticator {
    private Flickr flickr;
    
    
    private static final String API_KEY = System.getProperty("flickr.api.key", 
            System.getenv("FLICKR_API_KEY"));
        private static final String API_SECRET = System.getProperty("flickr.api.secret", 
            System.getenv("FLICKR_API_SECRET"));
        
        public static String getApiKey() {
            if (API_KEY == null || API_KEY.isEmpty()) {
                throw new IllegalStateException("Flickr API key not configured");
            }
            return API_KEY;
        }
        
        public static String getApiSecret() {
            if (API_SECRET == null || API_SECRET.isEmpty()) {
                throw new IllegalStateException("Flickr API secret not configured");
            }
            return API_SECRET;
        }
    
    public FlickrAuthenticator() {
        this.flickr = new Flickr(getApiKey(), getApiSecret(), new REST());
    }
    
    public Auth authenticate() throws FlickrException {
        AuthInterface authInterface = flickr.getAuthInterface();
        
        // Get request token
        OAuth1RequestToken requestToken = authInterface.getRequestToken();
        
        // Get authorization URL
        String authUrl = authInterface.getAuthorizationUrl(requestToken, Permission.DELETE);
        System.out.println("Please visit this URL to authorize the application:");
        System.out.println(authUrl);
        System.out.println();
        System.out.println("After authorization, paste the verifier code here:");
        
        Scanner scanner = new Scanner(System.in);
        String verifierCode = scanner.nextLine().trim();
        
        OAuth1Token accessToken = authInterface.getAccessToken(requestToken, verifierCode);
        
        // Get Auth object
        Auth auth = authInterface.checkToken(accessToken);
        
        return auth;
    }
    
    public Flickr getFlickr() {
        return flickr;
    }
}