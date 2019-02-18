package amuse.keras;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.datavec.api.util.ClassPathResource;
import org.deeplearning4j.nn.modelimport.keras.KerasModelImport;
import org.deeplearning4j.nn.modelimport.keras.exceptions.InvalidKerasConfigurationException;
import org.deeplearning4j.nn.modelimport.keras.exceptions.UnsupportedKerasConfigurationException;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;

/**
 * Singleton that will control everything regarding keras
 * @author NICOLAS
 *
 */
public class KerasController {
	
	private File[] files = {};
	private File[] pyFiles = {};
	private File[] h5Files = {};
	
	//Start of singleton package
	private KerasController() {}
	
	public static synchronized KerasController getInstance() {
		return LazyHolder.INSTANCE; 
	}
	
	private static class LazyHolder{
		private static volatile KerasController INSTANCE = new KerasController(); 
	}
	//End of singleton package
	
	/**
	 * Search through the directory and retrieve every file in it then store it in the global variable "files"
	 * by default, the research is made in "Keras/Models" directory
	 */
	public void init() {
		init("Keras");
	}
	
	/**
	 * Search through the directory and retrieve every file in it then store it in the global variable "files"
	 * Then retrieves the python files and h5 files and initializes two corresponding lists.
	 * @param filePath
	 */
	public void init(String filePath) {
		files = new File(filePath).listFiles();
		pyFiles = FileNameParser.parse(files, "py");
		h5Files = FileNameParser.parse(new File(filePath+"/Models").listFiles(),"h5");
	}
	
	/**
	 * Shows every files that we found.
	 * @return true if there was any files in the directory, false otherwise
	 */
	public boolean showFiles() {
		return showFiles(files);
	}
	
	/**
	 * Shows every files that we found.
	 * @param files
	 * @return true if there was any files in the directory, false otherwise
	 */
	public boolean showFiles(File[] files) {
		if(files != null && files.length==0) {
			return false;
		}
	    for (File file : files) {
	        if (file.isDirectory()) {
	            System.out.println("Directory: " + file.getName());
	            //showFiles(file.listFiles()); // Calls same method again.
	        } else {
	            System.out.println("File: " + file.getName());
	        }
	    }
	    return true;
	}
	
	public void importFiles() {
		for(File elt : h5Files) {
			importFile("Keras/Models/"+elt.getName());
		}
	}
	
	public MultiLayerNetwork importFile(String filePath) {
		MultiLayerNetwork model = null;
		try {
			//String simpleMlp = new ClassPathResource(filePath).getFile().getPath();
			model = KerasModelImport.importKerasSequentialModelAndWeights(filePath,true);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKerasConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedKerasConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return model;
	}
	
	
	/**
	 * Only here for the tests since JUnit doesn't want to function properly, it's dirty and must be erased in the newest version
	 * @param args
	 */
	public static void main(String... args) {
		KerasController k = KerasController.getInstance();
		k.init();
		k.importFiles();
	}
}
