/** 
 * This file is part of AMUSE framework (Advanced MUsic Explorer).
 * 
 * Copyright 2006-2012 by code authors
 * 
 * Created at TU Dortmund, Chair of Algorithm Engineering
 * (Contact: <http://ls11-www.cs.tu-dortmund.de>) 
 *
 * AMUSE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AMUSE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with AMUSE. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Creation date: 14.04.2012
 */
package amuse.nodes.extractor.methods;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Level;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import amuse.data.Feature;
import amuse.data.FeatureTable;
import amuse.interfaces.nodes.NodeException;
import amuse.interfaces.nodes.methods.AmuseTask;
import amuse.nodes.extractor.interfaces.ExtractorInterface;
import amuse.util.AmuseLogger;
import amuse.util.ExternalProcessBuilder;

/**
 * Adapter to NNLS Chroma SonicAnnotator feature extractor. 
 * For further details of SonicAnnotator see <a href="http://www.omras2.org/SonicAnnotator">http://www.omras2.org/SonicAnnotator</a>
 * For further details of NNLS see <a href="http://www.isophonics.net/nnls-chroma">http://www.isophonics.net/nnls-chroma</a>
 *  
 * @author Daniel Stoller
 * @version $Id: $
 */
public class SonicAnnotatorAdapter extends AmuseTask implements ExtractorInterface {

	/** Input music file */
	private String musicFile;
	
	/** If the input music file was splitted, here is the number of current part */
	private Integer currentPart;
	
	/** Path to the desired arff output from Sonic Annotator */
	private String outputFeatureFile;
	
	private static HashMap<String,Feature> filename2feature = new HashMap<String,Feature>();
	
	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.extractor.interfaces.ExtractorInterface#setFilenames(java.lang.String, java.lang.String, java.lang.Integer)
	 */
	public void setFilenames(String musicFile, String outputFeatureFile, Integer currentPart) throws NodeException {
		this.musicFile = musicFile;
		this.currentPart = currentPart;
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.extractor.interfaces.ExtractorInterface#convertBaseScript(java.util.HashMap, amuse.data.FeatureTable)
	 */
	public void convertBaseScript(HashMap<Integer, Integer> feature2Tool,
			FeatureTable featureTable) throws NodeException {
		
		// Load Sonic Annotator base script
		Document currentBaseScript = null;
		try {
			currentBaseScript = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
					properties.getProperty("extractorFolder") + "/" + 
					properties.getProperty("inputExtractorBaseBatch"));
		} catch(java.io.IOException e) {
			throw new NodeException("Cannot open Sonic Annotator base script: " + e.getMessage());
		} catch(javax.xml.parsers.ParserConfigurationException e) {
			throw new NodeException("Cannot create DocumentBuilder which satisfies the configuration: " + e.getMessage());
		} catch(org.xml.sax.SAXException e) {
			throw new NodeException("Cannot create DocumentBuilder which satisfies the configuration: " + e.getMessage());
		}
		
		// Save the modified script as xml file
		filename2feature.clear();
		NodeList nList = currentBaseScript.getElementsByTagName("amuseEnableFeature");
		try {
		        BufferedWriter out = new BufferedWriter(new FileWriter(properties.getProperty("extractorFolder") + "/" + 
						   properties.getProperty("inputExtractorBatch")));
		        
				for(int i=0;i<nList.getLength();i++) {
					Node node = nList.item(i);
					NamedNodeMap attr = node.getAttributes();
					Integer id = new Integer(attr.getNamedItem("id").getNodeValue());
					if(feature2Tool.containsKey(id) ) {
						String pluginCmd = node.getTextContent().substring(1); // Plugin command parameter for sonic-annotator
						out.write(pluginCmd); // Write the plugin command parameter
						Feature ft = featureTable.getFeatureByID(id); //Look up the feature through the featureTable
						filename2feature.put(pluginCmd.replace("\n", ""), ft); // Add the pair of plugin command and feature object into the data structure for the following conversion
						AmuseLogger.write(this.getClass().getName(), Level.DEBUG, "Feature with ID '" + id + 
								"' will be extracted with " + properties.getProperty("extractorName"));
					}
				}
		        out.close();
		} catch (IOException e) {
		    	e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.extractor.interfaces.ExtractorInterface#convertOutput()
	 */
	public void convertOutput() throws NodeException {
		String musicFileName = this.musicFile.substring(musicFile.lastIndexOf(File.separator) + 1, musicFile.lastIndexOf("."));
		Set<String> pluginCommands = filename2feature.keySet();
		Iterator<String> loop = pluginCommands.iterator();
		
		// Create a folder for Amuse feature files
		File folder = new File(this.correspondingScheduler.getHomeFolder() + "/input/task_" + this.correspondingScheduler.getTaskId() + 
				"/" + this.currentPart + "/" + properties.getProperty("extractorFolderName"));
		if(!folder.exists() && !folder.mkdirs()) {
			throw new NodeException("Extraction with Sonic Annotator failed: could not create temp folder " + 
					folder.toString());
		}
		
		// Go through all extracted feature files
		while(loop.hasNext()) {
			String pluginCmd = (String) loop.next();
			Feature currentFeature = filename2feature.get(pluginCmd);
			String outputPath = outputFeatureFile + File.separator + musicFileName + "_" + pluginCmd.replace(':','_') + ".csv";
			
			// For AMUSE feature file (TMP in the folder name is omitted)
			String outputAmuseFeature = outputFeatureFile.substring(0,outputFeatureFile.length()-3) + 
				File.separator + musicFileName + "_" + currentFeature.getId() + ".arff";
			FileReader featuresInput = null;
			BufferedReader featuresReader = null;
			int columns;
			String[] attributeTypes;
			try {
				columns = count(outputPath);
				attributeTypes = getAttributeTypes(outputPath);
				featuresInput = new FileReader(new File(outputPath));
				featuresReader = new BufferedReader(featuresInput);
			} catch (FileNotFoundException e) {
				throw new NodeException("Could not find the file with extracted features: " + e.getMessage());
			} catch (IOException e) {
				throw new NodeException("Could not read the file with extracted features: " + e.getMessage());
			}
			
			boolean hasWindows = (currentFeature.getSourceFrameSize() > 0); // Check if the current feature has a correct window size (not -1) and thus needs an additional Attribute //TODO: Fix this!
			int rows = currentFeature.getDimension();
			
			// Start writing the arff file
			try {
				FileOutputStream values_to = new FileOutputStream(new File(outputAmuseFeature));
				DataOutputStream writer = new DataOutputStream(values_to);
				String sep = System.getProperty("line.separator");
								
				// Start off by describing the feature (name, rows, columns, sample_rate, optionally window_size)
				writer.writeBytes("@RELATION 'Music feature'" + sep + sep +
						"%rows=" + rows + sep +
						"%columns=" + columns + sep +
						// TODO currently always set to 22050!
						"%sample_rate=" + "22050" + /*currentFeature.getSampleRate()*/ sep +  
						"%window_size=" + currentFeature.getSourceFrameSize() + sep + sep);
				
				// Write attribute declarations
				if(!hasWindows) {
					writer.writeBytes("@ATTRIBUTE Time NUMERIC" + sep);
				}
				
				for(int i=0;i<attributeTypes.length;i++) {
					writer.writeBytes("@ATTRIBUTE '" + currentFeature.getDescription() + "' " + attributeTypes[i] + sep); 
				}
				
				if(hasWindows) {
					writer.writeBytes("@ATTRIBUTE WindowNumber NUMERIC" + sep);
				}
				writer.writeBytes(sep);
				
				// Write the data
				writer.writeBytes("@DATA\n");
				String input = null;
				int i = 1;
				while((input = featuresReader.readLine()) != null) {
					StringTokenizer str = new StringTokenizer(input, ",");
					if(hasWindows) {
						str.nextToken();
						while(str.hasMoreElements()) {
							writer.writeBytes(str.nextToken()+",");
						}
						writer.writeBytes(new Integer(i).toString());
					}
					else {
						writer.writeBytes(input);
					}
					writer.writeBytes(sep);
					i++;
				}
				writer.close();
			}
			catch(IOException e) {
				throw new NodeException("Could not read the csv output from Sonic Annotator: " + e.getMessage());
			}
		}
	}

	// TODO comment
	private String[] getAttributeTypes(String csvFile) throws NodeException {
		FileReader featuresInput = null;
		BufferedReader featuresReader = null;
		try {
			featuresInput = new FileReader(new File(csvFile));
			featuresReader = new BufferedReader(featuresInput);
			String line = featuresReader.readLine();
			StringTokenizer strTok = new StringTokenizer(line, ",");
			int columns = strTok.countTokens() - 1; //Decrement, because the time column is being handled in convertOutput
			strTok.nextToken(); //Skip the corresponding attribute type of the time column
			String[] attributeTypes = new String[columns];
			for(int i=0;i<columns;i++)
			{
				attributeTypes[i] = strTok.nextToken().contains("\"") ? "STRING" : "NUMERIC";
			}
			return attributeTypes;
		} catch (FileNotFoundException e) {
			throw new NodeException("Could not find the file with extracted features: " + e.getMessage());
		} catch (IOException e) {
			throw new NodeException("Could not read the file with extracted features: " + e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.extractor.interfaces.ExtractorInterface#extractFeatures()
	 */
	public void extractFeatures() throws NodeException {
		AmuseLogger.write(this.getClass().getName(), Level.DEBUG, "Starting feature extraction...");
		
		// Create a folder for Sonic Annotator temporary feature files
		File folder = new File(this.correspondingScheduler.getHomeFolder() + "/input/task_" + this.correspondingScheduler.getTaskId() + 
				"/" + this.currentPart + "/" + properties.getProperty("extractorFolderName") + "TMP");
		if(!folder.exists() && !folder.mkdirs()) {
			throw new NodeException("Extraction with Sonic Annotator failed: could not create temp folder " + 
					folder.toString());
		}
				
		// Go through the modified base script and start Sonic Annotator for each feature
		try {
			BufferedReader in = new BufferedReader(new FileReader(properties.getProperty("extractorFolder") + File.separator + 
					   properties.getProperty("inputExtractorBatch")));
			String input = null;
			while((input = in.readLine()) != null)
			{
				List<String> commands = new ArrayList<String>();
				commands.add(properties.getProperty("extractorFolder") + File.separator + properties.getProperty("extractorStartScript"));
				commands.add("-d");
				commands.add(input);
				commands.add(this.musicFile);
				commands.add("-w");
				commands.add("csv");
				commands.add("--csv-basedir");
				commands.add(folder.getAbsolutePath());
				ExternalProcessBuilder sonic = new ExternalProcessBuilder(commands);
				sonic.setWorkingDirectory(new File(properties.getProperty("extractorFolder")));
				sonic.setEnv("VAMP_PATH", properties.getProperty("extractorFolder")+File.separator+"Plugins");
				Process pc = sonic.start();
				
			    pc.waitFor();
			}
		} catch (IOException e) {
        	throw new NodeException("Extraction with Sonic Annotator failed: " + e.getMessage());
        } catch (InterruptedException e) {
            throw new NodeException("Extraction with Sonnic Annotator interrupted! " + e.getMessage());
        }
        outputFeatureFile = folder.getAbsolutePath();
        convertOutput();
	}

	/*
	 * (non-Javadoc)
	 * @see amuse.interfaces.nodes.methods.AmuseTaskInterface#initialize()
	 */
	public void initialize() throws NodeException {
		// Nothing to initialize
		
	}

	/*
	 * (non-Javadoc)
	 * @see amuse.interfaces.nodes.methods.AmuseTaskInterface#setParameters(java.lang.String)
	 */
	public void setParameters(String parameterString) throws NodeException {
		// Nothing to set
		
	}
	
	// TODO comment
	private int count(String filename) throws IOException {
	    InputStream is = new BufferedInputStream(new FileInputStream(filename));
	    try {
	        byte[] c = new byte[1024];
	        int count = 0;
	        int readChars = 0;
	        while ((readChars = is.read(c)) != -1) {
	            for (int i = 0; i < readChars; ++i) {
	                if (c[i] == '\n')
	                    ++count;
	            }
	        }
	        return count;
	    } finally {
	        is.close();
	    }
	}

}