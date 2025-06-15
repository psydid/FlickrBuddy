package main;


import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.dropbox.core.v2.files.DbxUserFilesRequests;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.WriteMode;

import droprest.DropboxRestService;
import flickrest.FlickrCollection;
import flickrest.FlickrPhoto;
import flickrest.FlickrSet;

public class FlickrBuddyDropboxService extends FlickrBuddySinkService {

	//private DropboxRestService service;
	private DbxUserFilesRequests filesReq;
	
	public FlickrBuddyDropboxService(DropboxRestService service) {
		//this.service = service;
		filesReq = service.getFiles();
	}
	
	public String getSinkRoot() {
		return "/Apps/FlickrBuddy";
	}
	
	// Maps collections to sets 
	private ArrayList<String> collectionList;
	private HashMap<String, ArrayList<String>> collectionSetLists;
	private HashMap<String, ArrayList<String>> setPhotoLists;
	
	private ArrayList<String> getCollectionList() throws Exception {
		if(collectionList == null) {
			collectionList = new ArrayList<String>();
		
			ListFolderResult result = filesReq.listFolder(getSinkRoot());
			List<Metadata> fileList = result.getEntries();
			for(Metadata m : fileList) {
				collectionList.add(m.getName());		
			}
			
		}
		return collectionList;
	}
	
	private HashMap<String, ArrayList<String>> getCollectionSetLists() throws Exception {
		if(collectionSetLists == null) {
			collectionSetLists = new HashMap<String, ArrayList<String>>();
		}
		return collectionSetLists;
	}

	private HashMap<String, ArrayList<String>> getSetPhotoLists() throws Exception {
		if(setPhotoLists == null) {
			setPhotoLists = new HashMap<String, ArrayList<String>>();
		}
		return setPhotoLists;
	}

	
	public boolean checkForCollection(FlickrCollection coll) throws Exception {
		
		return getCollectionList().contains(coll.getTitle());
	}
	
	public boolean checkForSet(FlickrCollection coll, FlickrSet set) throws Exception {
		
		// See if the collection exists
		if(!getCollectionList().contains(coll.getTitle())) {
			return false;
		}
		
		// Fetch the sets for this collection, initializing if needed
		ArrayList<String> setList = getCollectionSetLists().get(coll.getTitle());
		if(setList == null) {
			setList = new ArrayList<String>();
			
			// Fetch sets from the collection
			String path = getSinkRoot() + "/" + coll.getTitle(); 
			
			ListFolderResult result = filesReq.listFolder(path);
			List<Metadata> folderList = result.getEntries();
			for(Metadata m : folderList) {
				setList.add(m.getName());				
			}
			
			
			getCollectionSetLists().put(coll.getTitle(), setList);
		}
		
		if(!setList.contains(set.getTitle())) {
			return false;
		}
		
		// Fetch photo names for this set
		if(!getSetPhotoLists().containsKey(set.getId())) {
			ArrayList<String> photos = new ArrayList<String>();
			
			String path = getSinkRoot() + "/" + coll.getTitle() + "/" + set.getTitle(); 			
			ListFolderResult result = filesReq.listFolder(path);
			List<Metadata> photoList = result.getEntries();
			for(Metadata m : photoList) {
				photos.add(m.getName());
			}
			
			getSetPhotoLists().put(set.getId(), photos);
		}
		
		return setList.contains(set.getTitle());
	}
	
	public int countFilesInSet(FlickrCollection coll, FlickrSet set) throws Exception{
		String path = getSinkRoot() + "/" + coll.getTitle() + "/" + set.getTitle();
		
		ListFolderResult result = filesReq.listFolder(path);
		List<Metadata> fileList = result.getEntries();
		return fileList.size();
	}
	
	
	protected void writeDirectories(FlickrCollection coll, FlickrSet set, String photoParentPath) throws Exception {
	
		// Create collection folder if necessary
		if(!getCollectionList().contains(coll.getTitle())) {
			createCollectionFolder(coll.getTitle());
			getCollectionList().add(coll.getTitle());
		}
		
		// Create set folder if necessary
		ArrayList<String> collSetList = getCollectionSetLists().get(coll.getTitle());
		if(collSetList == null) {
			collSetList = new ArrayList<String>();
			getCollectionSetLists().put(coll.getTitle(), collSetList);
		}
		
		if(!collSetList.contains(set.getTitle())) {
			createSetFolder(coll.getTitle(), set.getTitle());
			collSetList.add(set.getTitle());
		}

	}

	private void createCollectionFolder(String collName) throws Exception {
		
		String folderPath = getSinkRoot() + "/" + collName;
		filesReq.createFolder(folderPath);

		System.out.println("Creating directory for collection " + collName);

	}

	private void createSetFolder(String collName, String setName) throws Exception {
		
		String folderPath = getSinkRoot() + "/" + collName + "/" + setName;
		filesReq.createFolder(folderPath);

		System.out.println("Creating directory for set " + setName);
	}

	
	protected boolean writeFile(FlickrCollection coll, FlickrSet set, String photoName, InputStream photoStream, String photoPath) throws Exception {
		
		// Check if file exists
		ArrayList<String> photos = getSetPhotoLists().get(set.getId());
		if(photos != null && photos.contains(photoName)) {
			photoStream.close();
			return true;
		}
		
		System.out.println("Creating photo at " + photoPath);

		filesReq.uploadBuilder(photoPath).withMode(WriteMode.ADD).uploadAndFinish(photoStream);
		photoStream.close();
		return true;
	}
}
