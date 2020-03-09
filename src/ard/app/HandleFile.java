package ard.app;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HandleFile {
	
	public static  void startSysWatchHandleFile() {
		 try {
			WatchService watchService  = FileSystems.getDefault().newWatchService();
			
			
			Path path = Paths.get(AppConstants.FLUID_FILE_PATH);
			 
	        path.register(
	          watchService, 
	            StandardWatchEventKinds.ENTRY_CREATE,
	            StandardWatchEventKinds.ENTRY_MODIFY);
	 
	        WatchKey key;
	        while ((key = watchService.take()) != null) {
	            for (WatchEvent<?> event : key.pollEvents()) {
	            	
	            	 WatchEvent<Path> ev = cast(event);
                     Path filename = ev.context();
	            	
	            	String  currFile =  filename.getFileName().toString();
	            	syncOtherFiles(currFile);
	                System.out.println("Event kind:" + event.kind() + ". File affected: " + currFile + ".");
	                
	            }
	            key.reset();
	        }
			
			
			
		} catch (Exception e) {
			System.out.println("system watch ran in to issues .....");
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
    private static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>)event;
    }

	private static void syncOtherFiles(String currFile) {
		
	}

}
