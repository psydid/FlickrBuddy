/* Implements a sync for photos */

package main;


import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import flickrest.*;

public class FlickrBuddyDiskService extends FlickrBuddySinkService {

	private String localRoot;
	
	public FlickrBuddyDiskService(String localRoot) {
		
		this.localRoot = localRoot;
	}
	
	public String getSinkRoot() {
		return this.localRoot;
	}
	
	public boolean checkForCollection(FlickrCollection coll) {
		String path = localRoot + "/" + coll.getTitle();
		
		File dir = new File(path);
		boolean exists = dir.exists();
		return exists;
	}
	
	public boolean checkForSet(FlickrCollection coll, FlickrSet set) {
		String path = localRoot + "/" + coll.getTitle() + "/" + set.getTitle();
		
		File dir = new File(path);
		return dir.exists();
	}
	
	public int countFilesInSet(FlickrCollection coll, FlickrSet set) {
		String path = localRoot + "/" + coll.getTitle() + "/" + set.getTitle();
		
		File dir = new File(path);
		return dir.listFiles().length;
	}
	
	protected void writeDirectories(FlickrCollection coll, FlickrSet set, String photoParentPath) throws Exception {
		File photoParentDirs = new File(photoParentPath);
		photoParentDirs.mkdirs();	
	}

	
	protected boolean writeFile(FlickrCollection coll, FlickrSet set, String photoName, InputStream photoStream, String photoPath) throws Exception {
		
		File photoFile = new File(photoPath);
		if(photoFile.exists()) {
			photoStream.close();
			return false;
		}
		
		photoFile.createNewFile();
		FileOutputStream writer = new FileOutputStream(photoPath);
		
		byte[] buffer = new byte[100000];
		int bytesRead = 0;
		while((bytesRead = photoStream.read(buffer)) > 0) {
			writer.write(buffer, 0, bytesRead);
		}
		writer.close();
		photoStream.close();

		return true;
	}
}
