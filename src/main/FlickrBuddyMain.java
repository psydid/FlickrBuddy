package main;


import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import org.scribe.model.Token;

import droprest.DropboxRestService;
import flickrest.*;


public class FlickrBuddyMain {

	private static FlickrRestService service;
	private static FlickrBuddySinkService sinkService;
	private static boolean fastMode = true;
	
	private static DropboxRestService dropboxService;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		/*
		if(args.length < 1) {
			System.out.println("usage: FlickrBuddy <local root for photo stash>");
			return;
		}
		
		*/
		dropboxService = DropboxRestService.get();
		dropboxService.authorize(FlickrBuddyConfig.get().getDropboxAuthFileName());
		//dropboxService.test();

		//sinkService = new FlickrBuddyDiskService(args[0]);
		sinkService = new FlickrBuddyDropboxService(dropboxService);

		
		service = FlickrRestService.get();
		Token accessToken = FlickrBuddyConfig.get().getAccessToken();
		
		if(accessToken == null) {
			// Need to authenticate
			String url = service.getAuthUrl();
			System.out.println("Visit this URL:");
			System.out.println(url);
			System.out.println("And copy and paste the verifier string here, then hit Enter:");
					
			
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
			String verifierString;
			try {
				verifierString = bufferedReader.readLine();
			} catch(IOException e) {
				System.out.println(e.getMessage());
				return;
			}
			
			service.authorize(verifierString);
			
			accessToken = service.getAccessToken();
			FlickrBuddyConfig.get().saveAccessToken(accessToken);
		} else {
			service.setAccessToken(accessToken);
		}
		
		FlickrCollectionList colls = service.getColls();
	
		ArrayList<FlickrCollection> orderedList = new ArrayList<FlickrCollection>();
		ArrayList<FlickrCollection> onDiskList = new ArrayList<FlickrCollection>();		
		for(FlickrCollection coll : colls.getCollections()) {
			if(sinkService.checkForCollection(coll)) {
				onDiskList.add(coll);
			} else {
				orderedList.add(coll);
			}
		}
		
		orderedList.addAll(onDiskList);
		for(FlickrCollection coll : orderedList) {
			syncCollection(coll);
		}
		
		System.out.println("Done!");
	}
	
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
	
	private static void syncSet(FlickrCollection coll, FlickrSet set) throws Exception {
		FlickrPhotoList photoList = service.getPhotosInSet(set);
		
		if(FlickrBuddyMain.fastMode && sinkService.checkForCollection(coll) && sinkService.checkForSet(coll, set)) {
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
	}
}
