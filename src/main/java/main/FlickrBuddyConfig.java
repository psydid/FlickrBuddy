package main;



import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.scribe.model.Token;

public class FlickrBuddyConfig {
		
	private static FlickrBuddyConfig _INSTANCE;
	private String dropboxAuthFileName = "dropboxAuth";
	
	
	
	
    private static final String ACCESS_TOKEN = System.getProperty("flickr.api.accessToken", 
            System.getenv("FLICKR_ACCESS_TOKEN"));
        private static final String ACCESS_TOKEN_SECRET = System.getProperty("flickr.api.accessTokenSecret", 
            System.getenv("FLICKR_ACCESS_TOKEN_SECRET"));
	
	public static FlickrBuddyConfig get() {
		if(_INSTANCE == null) {
			_INSTANCE = new FlickrBuddyConfig();
		}
		
		return _INSTANCE;
	}
	
	public Token getAccessToken() throws IOException{
		
		/*
		BufferedReader input;
		try {
			input = new BufferedReader(new FileReader("credentials2"));
		} catch(FileNotFoundException x) {
			return null;
		}
		
		try {
			String tokenString = input.readLine();
			String secretString = input.readLine();
			return new Token(tokenString, secretString);
		} catch(IOException e) {
			throw e;
		} finally {
			input.close();
		}
		
		*/
		String tokenString = ACCESS_TOKEN;
		String secretString = ACCESS_TOKEN_SECRET;
		return new Token(tokenString, secretString);
		
	}
	
	public void saveAccessToken(Token accessToken) throws IOException {
		BufferedWriter output = null;
		try {
			output = new BufferedWriter(new FileWriter("credentials2"));
			output.write(accessToken.getToken());
			output.newLine();
			output.write(accessToken.getSecret());
			output.newLine();
		} finally {
			if(output != null) {
				output.close();
			}
		}
	}
	
	public String getDropboxAuthFileName() {
		return dropboxAuthFileName;
	}
}
