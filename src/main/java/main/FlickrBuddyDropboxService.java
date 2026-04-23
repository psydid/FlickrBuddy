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

	private static final String EMPTY_PATH_COMPONENT = "_";

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
		
		return getCollectionList().contains(pathComponentForCollection(coll));
	}
	
	public boolean checkForSet(FlickrCollection coll, FlickrSet set) throws Exception {
		String collPath = pathComponentForCollection(coll);
		String setPath = pathComponentForSet(set);
		
		// See if the collection exists
		if(!getCollectionList().contains(collPath)) {
			return false;
		}
		
		// Fetch the sets for this collection, initializing if needed
		ArrayList<String> setList = getCollectionSetLists().get(coll.getTitle());
		if(setList == null) {
			setList = new ArrayList<String>();
			
			// Fetch sets from the collection
			String path = getSinkRoot() + "/" + collPath; 
			
			ListFolderResult result = filesReq.listFolder(path);
			List<Metadata> folderList = result.getEntries();
			for(Metadata m : folderList) {
				setList.add(m.getName());				
			}
			
			
			getCollectionSetLists().put(coll.getTitle(), setList);
		}
		
		if(!setList.contains(setPath)) {
			return false;
		}
		
		// Fetch photo names for this set
		if(!getSetPhotoLists().containsKey(set.getId())) {
			ArrayList<String> photos = new ArrayList<String>();
			
			String path = getSinkRoot() + "/" + collPath + "/" + setPath; 			
			ListFolderResult result = filesReq.listFolder(path);
			List<Metadata> photoList = result.getEntries();
			for(Metadata m : photoList) {
				photos.add(m.getName());
			}
			
			getSetPhotoLists().put(set.getId(), photos);
		}
		
		return setList.contains(setPath);
	}
	
	public int countFilesInSet(FlickrCollection coll, FlickrSet set) throws Exception{
		String path = getSinkRoot() + "/" + pathComponentForCollection(coll) + "/" + pathComponentForSet(set);
		
		ListFolderResult result = filesReq.listFolder(path);
		List<Metadata> fileList = result.getEntries();
		return fileList.size();
	}
	
	
	protected void writeDirectories(FlickrCollection coll, FlickrSet set, String photoParentPath) throws Exception {
		String collPath = pathComponentForCollection(coll);
		String setPath = pathComponentForSet(set);
	
		// Create collection folder if necessary
		if(!getCollectionList().contains(collPath)) {
			createCollectionFolder(collPath);
			getCollectionList().add(collPath);
		}
		
		// Create set folder if necessary
		ArrayList<String> collSetList = getCollectionSetLists().get(coll.getTitle());
		if(collSetList == null) {
			collSetList = new ArrayList<String>();
			getCollectionSetLists().put(coll.getTitle(), collSetList);
		}
		
		if(!collSetList.contains(setPath)) {
			createSetFolder(collPath, setPath);
			collSetList.add(setPath);
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

	@Override
	protected String pathComponentForCollection(FlickrCollection coll) {
		return sanitizePathComponent(coll.getTitle());
	}

	@Override
	protected String pathComponentForSet(FlickrSet set) {
		return sanitizePathComponent(set.getTitle());
	}

	@Override
	protected String fileNameForPhoto(FlickrPhoto photo, String suffix) {
		return sanitizePathComponent(photo.getTitle()) + suffix;
	}

	static String sanitizePathComponent(String pathComponent) {
		if(pathComponent == null) {
			return EMPTY_PATH_COMPONENT;
		}

		String sanitized = pathComponent
				.replace('\\', '-')
				.replace('/', '-')
				.replaceAll("[\\p{Cntrl}]+", " ")
				.replaceAll("\\s+", " ")
				.trim()
				.replaceAll("\\.+$", "");

		if(sanitized.isEmpty()) {
			return EMPTY_PATH_COMPONENT;
		}

		return sanitized;
	}
}
