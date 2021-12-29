package dropdown;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import org.scribe.model.Token;

import com.dropbox.core.v2.files.DbxUserFilesRequests;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;

import droprest.DropboxRestService;
import flickrest.*;
import javafx.util.Pair;
import main.FlickrBuddyConfig;
import main.FlickrBuddyDiskService;
import main.FlickrBuddyDiskSynchro;
import main.FlickrBuddyDropboxSynchro;
import main.FlickrBuddySinkService;


public class DropDownMain {

	private static DropboxRestService dropboxService;
	private static FlickrBuddyDropboxSynchro dbox;		
	private static FlickrBuddyDiskSynchro disk;	

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		
		if(args.length < 2) {
			printUsage();
			return;
		}
		
		String dropRoot = "/" + args[0];
		String localRoot = args[1];
		
		dropboxService = DropboxRestService.get();
		dropboxService.authorize(FlickrBuddyConfig.get().getDropboxAuthFileName());

		dbox = new FlickrBuddyDropboxSynchro(dropboxService);		
		disk = new FlickrBuddyDiskSynchro(localRoot);	

		// Loop over folders starting at dropbox root
		// 
			
		for(String dir : dbox.getFolders(dropRoot)) {
			syncDir(dropRoot, "", dir);
		}
				
		for (Pair<String,Long> p : dbox.getFiles(dropRoot)) {
			syncFile(dropRoot, "", p.getKey(), p.getValue());
		}
		
		
		System.out.println("Done with dropdown!");
	}
	
	
	private static void syncDir(String dropPath, String diskPath, String dir) throws Exception
	{
		System.out.println("syncing dir " + dropPath + "/" + dir);

		
		disk.writeDir(diskPath, dir);
		
		String newDropPath = dropPath + "/" + dir;
		String newDiskPath = diskPath + "/" + dir;
		for(String subDir : dbox.getFolders(newDropPath)) {
			syncDir(newDropPath, newDiskPath, subDir);
		}

		for (Pair<String,Long> p : dbox.getFiles(newDropPath)) {
			syncFile(newDropPath, newDiskPath, p.getKey(), p.getValue());
		}

	}
	

	private static void syncFile(String dropPath, String diskPath, String fileName, Long size) throws Exception
	{	
		//System.out.println("found file " + fileName);

		if(disk.checkFile(diskPath, fileName, size)) {
			System.out.println("file  exists " + diskPath + "/" + fileName);
			return;
		}
		
		System.out.println("syncing file " + diskPath + "/" + fileName + " from dropbox location " + dropPath);
		
		
		InputStream fileData = null;
		try {
			fileData = dbox.getFile(dropPath, fileName);
			disk.writeFile(diskPath, fileName, fileData);
		} finally {
			if(fileData != null)
				fileData.close();
		}
	}

	/*
	private static void syncCollection(FlickrCollection coll) throws Exception {

		ArrayList<FlickrSet> orderedList = new ArrayList<FlickrSet>();
		ArrayList<FlickrSet> onDiskList = new ArrayList<FlickrSet>();
		
		for(FlickrSet set: coll.getSets()) {
			if(sinkService.checkForSet(coll, set)) {
				onDiskList.add(set);
			} else {
				orderedList.add(set);
			}
		}
		
		orderedList.addAll(onDiskList);
		
		for(FlickrSet set : orderedList) {
			syncSet(coll, set);
		}	
	}
	
	*/
	
	/*
	
	private static void syncSet(FlickrCollection coll, FlickrSet set) throws Exception {
		FlickrPhotoList photoList = service.getPhotosInSet(set);
		
		if(DropDownMain.fastMode && sinkService.checkForCollection(coll) && sinkService.checkForSet(coll, set)) {
			if(photoList.getPhotos().size() <= sinkService.countFilesInSet(coll, set)) {
				System.out.println(String.format("Collection %s,  Set %s already synced", coll.getTitle(), set.getTitle()));
				return;
			} else {
				System.out.println(String.format("Collection %s,  Set %s has %d photos on service, %d on disk", coll.getTitle(), set.getTitle(), photoList.getPhotos().size(), sinkService.countFilesInSet(coll, set)));				
			}
		}
		
		HashMap<String, Integer> photoNameCounts = new HashMap<String, Integer>();
		
		for(FlickrPhoto photo : photoList.getPhotos()) {
			String name = photo.getTitle().toLowerCase();
			if(!photoNameCounts.containsKey(name)) {
				photoNameCounts.put(name, 1);
			} else {
				// Mark the second incarnation of this name and adjust title
				int newCount = photoNameCounts.get(name) + 1;
				photoNameCounts.put(name, newCount);
				name = String.format("%s (%d)", name, newCount);
				// Save the newly formatted name too in case _it_ collides with a photo that comes with (2) or something on the end.
				photo.setTitle(name);
				photoNameCounts.put(name, 1);
			}
			
			FlickrPhotoSizeList sizeList = service.getPhotoSizes(photo);
			for(FlickrPhotoSize size : sizeList.getSizes()) {
				if(size.getLabel().equals("Original")) {
					sinkService.syncPhoto(coll, set, photo, size.getSource());
				}
			}
		}
	} */
	
	private static void printUsage() {
		System.out.println("usage:\n FlickrBuddy <Dropbox path> <localRoot>");
	}
}
