package main;


import java.io.InputStream;
import java.net.URL;
import flickrest.*;

public abstract class FlickrBuddySinkService {

	public abstract boolean checkForCollection(FlickrCollection coll) throws Exception;
	
	public abstract boolean checkForSet(FlickrCollection coll, FlickrSet set) throws Exception;
	
	public abstract int countFilesInSet(FlickrCollection coll, FlickrSet set) throws Exception;
	
	public abstract String getSinkRoot();

	
	public boolean syncPhoto(FlickrCollection coll, FlickrSet set, FlickrPhoto photo, String source) throws Exception {
		
		String sinkRoot = getSinkRoot();
		
		String s = String.format("Syncing %s/%s/%s/%s from %s", sinkRoot, coll.getTitle(), set.getTitle(), photo.getTitle(), source);
		System.out.println(s);

		String suffix = source.substring(source.lastIndexOf('.'));
		
		String photoParentPath = getSinkRoot()  + "/" + coll.getTitle() + "/" + set.getTitle();
		String photoName = photo.getTitle() + suffix;
		String photoPath = photoParentPath + "/" + photoName;

		writeDirectories(coll, set, photoParentPath);

		URL url = new URL(source);
		url.openConnection();
		InputStream reader = url.openStream();

		return writeFile(coll, set, photoName, reader, photoPath);
	}
	
	protected abstract boolean writeFile(FlickrCollection coll, FlickrSet set, String photoName, InputStream photoStream, String fullPath) throws Exception;

	protected abstract void writeDirectories(FlickrCollection coll, FlickrSet set, String photoParentPath) throws Exception;

}
