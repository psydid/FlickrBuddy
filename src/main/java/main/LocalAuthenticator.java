package main;

import java.io.IOException;

import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.auth.Auth;

import droprest.DropboxRestService;

public class LocalAuthenticator {
    public static void main(String[] args) throws FlickrException, IOException {
       
    	// Uncomment and run this locally to get tokens for Flickr. Make sure to set the FLICKR_API_KEY and FLICKR_API_SECRET env vars
    	
       //FlickrAuthenticator auth = new FlickrAuthenticator();
       //Auth accessToken = auth.authenticate(); // Interactive flow
       //System.out.println("Access Token: " + accessToken.getToken());
       //System.out.println("Access Token Secret: " + accessToken.getTokenSecret());

    	
    	// Uncomment and run this part to get a refresh token for Dropbox. Make sure to set the DROPBOX_API_KEY and DROPBOX_API_SECRET env vars
    	String token = DropboxRestService.get().authorizeNew();
    	System.out.println("Dropbox Access Token: " + token);
           
        
        }
}