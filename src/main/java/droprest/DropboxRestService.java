package droprest;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxAuthFinish;
import com.dropbox.core.DbxAuthInfo;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxWebAuth;
import com.dropbox.core.TokenAccessType;
import com.dropbox.core.oauth.DbxCredential;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.DbxUserFilesRequests;

public class DropboxRestService {
	
	private static DropboxRestService _INSTANCE;
	
    private static final String API_KEY = System.getProperty("dropbox.api.key", 
            System.getenv("DROPBOX_API_KEY"));
        private static final String API_SECRET = System.getProperty("dropbox.api.secret", 
            System.getenv("DROPBOX_API_SECRET"));
        private static final String REFRESH_TOKEN = System.getProperty("dropbox.api.refreshtoken", 
                System.getenv("DROPBOX_REFRESH_TOKEN"));
        
        
	private DbxClientV2 client;
	
	public DropboxRestService() {
		;
	}
	
	public static DropboxRestService get() {
		if(_INSTANCE == null) {
			_INSTANCE = new DropboxRestService();
		}
		
		return _INSTANCE;
	}
	
	public void authorize(String authFile) throws IOException, DbxException {

		/*
		String accessToken;
		File file = new File(authFile);
		if(file.exists()) {
			accessToken = authorizeFromFile(authFile);
		} else {
			accessToken = authorizeNew(authFile);
		}
		*/
		
		DbxRequestConfig requestConfig = new DbxRequestConfig("FlickrBuddy");
	    DbxCredential credential = new DbxCredential(
	             "", // access token (will be auto-refreshed)
	             System.currentTimeMillis() + 3600000, // expires soon, forcing refresh
	             REFRESH_TOKEN,
	             API_KEY,
	             API_SECRET
	         );
	    
	    credential.refresh(requestConfig);
	    
	    System.out.println("credential access token: " + credential.getAccessToken());
	            
		
		client = new DbxClientV2(requestConfig, credential);
		
		try {
            // Test the connection
            System.out.println("Connected to Dropbox as: " + 
                client.users().getCurrentAccount().getName().getDisplayName());
            
            // Your app logic here
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
	}
	

	public String authorizeNew(String authFile) throws IOException {

        // Read app info file (contains app key and app secret)
        DbxAppInfo appInfo = new DbxAppInfo(API_KEY, API_SECRET);
        

        // Run through Dropbox API authorization process
        DbxRequestConfig requestConfig = new DbxRequestConfig("FlickrBuddy");
        DbxWebAuth webAuth = new DbxWebAuth(requestConfig, appInfo);
        DbxWebAuth.Request webAuthRequest = DbxWebAuth.newRequestBuilder()
        	.withTokenAccessType(TokenAccessType.OFFLINE)
            .withNoRedirect()
            .build();

        String authorizeUrl = webAuth.authorize(webAuthRequest);
        System.out.println("1. Go to " + authorizeUrl);
        System.out.println("2. Click \"Allow\" (you might have to log in first).");
        System.out.println("3. Copy the authorization code.");
        System.out.print("Enter the authorization code here: ");

        String code = new BufferedReader(new InputStreamReader(System.in)).readLine();
        if (code == null) {
            System.exit(1); return null;
        }
        code = code.trim();

        DbxAuthFinish authFinish;
        try {
            authFinish = webAuth.finishFromCode(code);
        } catch (DbxException ex) {
            System.err.println("Error in DbxWebAuth.authorize: " + ex.getMessage());
            System.exit(1); return null;
        }

        System.out.println("Authorization complete.");
        System.out.println("- User ID: " + authFinish.getUserId());
        System.out.println("- Access Token: " + authFinish.getAccessToken());

        // Save auth information to output file.
        DbxAuthInfo authInfo = new DbxAuthInfo(authFinish.getAccessToken(), appInfo.getHost());
        File output = new File(authFile);
        try {
            DbxAuthInfo.Writer.writeToFile(authInfo, output);
            System.out.println("Saved authorization information to \"" + output.getCanonicalPath() + "\".");
        } catch (IOException ex) { 
            System.err.println("Error saving to <auth-file-out>: " + ex.getMessage());
            System.exit(1); return null;
        }
        
        return authFinish.getAccessToken();
    }
	
	public String authorizeNew() throws IOException {

        // Read app info file (contains app key and app secret)
        DbxAppInfo appInfo = new DbxAppInfo(API_KEY, API_SECRET);
        

        // Run through Dropbox API authorization process
        DbxRequestConfig requestConfig = new DbxRequestConfig("FlickrBuddy");
        DbxWebAuth webAuth = new DbxWebAuth(requestConfig, appInfo);
        DbxWebAuth.Request webAuthRequest = DbxWebAuth.newRequestBuilder()
        	.withTokenAccessType(TokenAccessType.OFFLINE)
        	.withNoRedirect()
            .build();

        String authorizeUrl = webAuth.authorize(webAuthRequest);
        System.out.println("1. Go to " + authorizeUrl);
        System.out.println("2. Click \"Allow\" (you might have to log in first).");
        System.out.println("3. Copy the authorization code.");
        System.out.print("Enter the authorization code here: ");

        String code = new BufferedReader(new InputStreamReader(System.in)).readLine();
        if (code == null) {
            System.exit(1); return null;
        }
        code = code.trim();

        DbxAuthFinish authFinish;
        try {
            authFinish = webAuth.finishFromCode(code);
        } catch (DbxException ex) {
            System.err.println("Error in DbxWebAuth.authorize: " + ex.getMessage());
            System.exit(1); return null;
        }

        System.out.println("Authorization complete.");
        System.out.println("\n=== SUCCESS ===");
        System.out.println("Access Token: " + authFinish.getAccessToken());
        System.out.println("Refresh Token: " + authFinish.getRefreshToken());
        System.out.println("Expires At: " + authFinish.getExpiresAt());
        System.out.println();
        System.out.println("Add these to your .env file:");
        System.out.println("DROPBOX_ACCESS_TOKEN=" + authFinish.getAccessToken());
        System.out.println("DROPBOX_REFRESH_TOKEN=" + authFinish.getRefreshToken());        
 
        return authFinish.getAccessToken();
    }
	
	
	public DbxUserFilesRequests getFiles() {
		return client.files();
	}
	
	/*
	 * 	public void test() throws IOException{
		try {
			DbxUserFilesRequests filesReq = client.files();
			ListFolderResult result = filesReq.listFolder("");
			List<Metadata> fileList = result.getEntries();
			boolean found = false;
			for(Metadata m : fileList) {
				if(m.getName().equals("test.txt")) {
					found = true;
					System.out.println("found test file woot woot");
				}				
			}
			if(!found) {
				System.out.println("NO TEST FILE FOOL");
				
				String testString = "FOOL!";
				InputStream in = new ByteArrayInputStream(testString.getBytes());
				filesReq.uploadBuilder("/test.txt").withMode(WriteMode.ADD).uploadAndFinish(in);
			}

		} catch (DbxException ex) {
			System.out.println(ex.getMessage());
		}
		
	}
	*/
	 
	
}
