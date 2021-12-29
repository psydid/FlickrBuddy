/* Implements a sync for photos */

package main;


import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class FlickrBuddyDiskSynchro {

	private String localRoot;
	
	public FlickrBuddyDiskSynchro(String localRoot) {
		
		this.localRoot = localRoot;
	}
	
	public String getSinkRoot() {
		return this.localRoot;
	}
	
	public boolean checkForPath(String path) {
		String fullPath = localRoot + "/" + path;
		
		File dir = new File(fullPath);
		boolean exists = dir.exists();
		return exists;
	}
	

	public int countFilesInDir(String path) {
		String fullPath = localRoot + "/" + path;
		
		File dir = new File(fullPath);
		return dir.listFiles().length;
	}
	
	public void writeDir(String rootPath, String dir) throws Exception {
		String fullPath = localRoot + "/" + rootPath + "/" + dir;
		
		File d = new File(fullPath);
		if(!d.exists()) {
			if(d.mkdir()) {
				System.out.println("creating" + fullPath);
			}
		}
	}

	public Boolean checkFile(String path, String fileName, long size) throws Exception {
		String fullPath = localRoot + "/" + path + "/" + fileName; 
		
		File newFile = new File(fullPath);
		if(!newFile.exists())
			return false;
		
		if(newFile.length() == size) {
			return true;
		} else {
			System.out.println("File on disk has size " + newFile.length() + " vs expected " + size);
			return false;
		}
	}
	
	public void writeFile(String path, String fileName, InputStream input) throws Exception {
		
		String fullPath = localRoot + "/" + path + "/" + fileName; 
		
		File newFile = new File(fullPath);
		if(!newFile.exists()) {		
			System.out.println("creating " + fullPath);
		} else {
			System.out.println("overwriting " + fullPath);
		}
		
		Path p = Paths.get(fullPath);
		long b = Files.copy(input, p, StandardCopyOption.REPLACE_EXISTING);
		System.out.println("copied " + b);
		
		
/*
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
		*/
	}
	
	
}
