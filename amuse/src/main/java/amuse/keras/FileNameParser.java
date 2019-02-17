package amuse.keras;

import java.io.File;
import java.util.ArrayList;

/**
 * This class contains the generic parser for a fileName
 * @author NICOLAS
 *
 */
public class FileNameParser {

	/**
	 * This function takes an array of files and a regular expression then parses the filenames to 
	 * filter the array and retrieve only the files containing the right extension.
	 * @param files A table that contains the files which needs to be filtered
	 * @param extension A regular expression that matches the kind of extension needed
	 * @return A table of files containing only the files with the right extension
	 */
	public static File[] parse(File[] files, String extension) {
		ArrayList<File> res = new ArrayList<>();
		for(File elt : files) {
			
			String[] s = elt.getName().split("\\.");
			if(s.length>1) {
				if(s[s.length-1].matches(extension)) {
					res.add(elt);
				}
			}
		}
		File[] t = {};
		return res.toArray(t);
	}
}
