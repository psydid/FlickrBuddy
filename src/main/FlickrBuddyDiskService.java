package main;


import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import flickrest.*;

public class FlickrBuddyDiskService {

	private String localRoot;
	
	public FlickrBuddyDiskService(String localRoot) {
		
		this.localRoot = localRoot;
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
	

	
	public boolean syncPhoto(FlickrCollection coll, FlickrSet set, FlickrPhoto photo, String source) throws Exception {
		
		String s = String.format("Syncing %s/%s/%s/%s from %s", localRoot, coll.getTitle(), set.getTitle(), photo.getTitle(), source);
		System.out.println(s);

		String suffix = source.substring(source.lastIndexOf('.'));
		
		String photoParentPath = localRoot + "/" + coll.getTitle() + "/" + set.getTitle();
		File photoParentDirs = new File(photoParentPath);
		photoParentDirs.mkdirs();
		
		String photoPath = photoParentPath + "/" + photo.getTitle() + suffix;
		File photoFile = new File(photoPath);
		if(photoFile.exists()) {
			return false;
		}
		
		URL url = new URL(source);
		url.openConnection();
		InputStream reader = url.openStream();
		
		photoFile.createNewFile();
		FileOutputStream writer = new FileOutputStream(photoPath);
		
		byte[] buffer = new byte[100000];
		int bytesRead = 0;
		while((bytesRead = reader.read(buffer)) > 0) {
			writer.write(buffer, 0, bytesRead);
		}
		writer.close();
		reader.close();

		return true;
	}
}
