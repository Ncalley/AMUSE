/** 
 * This file is part of AMUSE framework (Advanced MUsic Explorer).
 * 
 * Copyright 2006-2010 by code authors
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
 * Creation date: 05.10.2007
 */ 
package amuse.nodes.trainer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.log4j.Level;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.converters.ArffLoader;
import amuse.data.GroundTruthSourceType;
import amuse.data.io.ArffDataSet;
import amuse.data.io.DataSet;
import amuse.data.io.DataSetAbstract;
import amuse.data.io.DataSetInput;
import amuse.data.io.FileInput;
import amuse.data.io.attributes.NumericAttribute;
import amuse.data.io.attributes.StringAttribute;
import amuse.interfaces.nodes.NodeEvent;
import amuse.interfaces.nodes.NodeException;
import amuse.interfaces.nodes.NodeScheduler;
import amuse.interfaces.nodes.TaskConfiguration;
import amuse.interfaces.nodes.methods.AmuseTask;
import amuse.nodes.trainer.interfaces.ClassificationPreprocessingInterface;
import amuse.nodes.trainer.interfaces.TrainerInterface;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;
import amuse.util.AmuseLogger;

/**
 * TrainerNodeScheduler is responsible for the trainer node. The labeled music files
 * are used for the training of classification model. 
 * 
 * @author Igor Vatolkin
 * @version $Id$
 */
public class TrainerNodeScheduler extends NodeScheduler { 

	/** Classification trainer adapter */
	TrainerInterface ctad = null;
	
	/** Parameters for classification algorithm if required */
	private String requiredParameters = null;
	
	/** Music category id and name separated by '-', e.g. "15-Music_for_Inspiration" */
	private String categoryDescription = null;
	
	private String pathToFileWithLabeledInstances = null;
	
	/** Path to the output model(s) */
	private String outputModel = null;
	
	/**
	 * Constructor
	 */
	public TrainerNodeScheduler(String folderForResults) throws NodeException {
		super(folderForResults);
		requiredParameters = new String();
		categoryDescription = new String();
		outputModel = new String();
	}
	
	/**
	 * Main method for classification training
	 * @param args Training configuration
	 */
	public static void main(String[] args) {
		
		// Create the node scheduler
		TrainerNodeScheduler thisScheduler = null;
		try {
			thisScheduler = new TrainerNodeScheduler(args[0] + File.separator + "input" + File.separator + "task_" + args[1]);
		} catch(NodeException e) {
			AmuseLogger.write(TrainerNodeScheduler.class.getName(), Level.ERROR,
					"Could not create folder for trainer node intermediate results: " + e.getMessage());
			return;
		}
		
		// Proceed the task
		thisScheduler.proceedTask(args);
		
		// Remove the folder for input and intermediate results
		try {
			thisScheduler.removeInputFolder();
		} catch(NodeException e) {
				AmuseLogger.write(TrainerNodeScheduler.class.getClass().getName(), Level.WARN,
					"Could not remove properly the folder with intermediate results '" + 
					thisScheduler.nodeHome + File.separator + "input" + File.separator + "task_'" + thisScheduler.jobId + 
					"; please delete it manually! (Exception: "+ e.getMessage() + ")");
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.interfaces.nodes.NodeSchedulerInterface#proceedTask(java.lang.String, long, amuse.interfaces.nodes.TaskConfiguration)
	 */
	public void proceedTask(String nodeHome, long jobId, TaskConfiguration trainingConfiguration) {
		
		// -------------------------------------
		// (I): Configure trainer node scheduler
		// -------------------------------------
		this.nodeHome = nodeHome;
		if(this.nodeHome.startsWith(AmusePreferences.get(KeysStringValue.AMUSE_PATH))) {
			this.directStart = true;
		}
		this.jobId = new Long(jobId);
		this.taskConfiguration = ((TrainingConfiguration) trainingConfiguration).clone();
		
		// If this node is started directly, the properties are loaded from AMUSEHOME folder;
		// if this node is started via command line (e.g. in a grid, the properties are loaded from
		// %trainer home folder%/input
		if(!this.directStart) {
			File preferencesFile = new File(this.nodeHome + File.separator + "config" + File.separator + "amuse.properties");
			AmusePreferences.restoreFromFile(preferencesFile);
		}
		
		AmuseLogger.write(this.getClass().getName(), Level.INFO, "Classification trainer node scheduler for ground truth source " + 
				((TrainingConfiguration)this.taskConfiguration).getGroundTruthSource() + " started");
		
		
		// Set the i/o files
		pathToFileWithLabeledInstances = new String();		
		if(((TrainingConfiguration)this.taskConfiguration).getGroundTruthSourceType().
				equals(GroundTruthSourceType.FILE_LIST)) {
			pathToFileWithLabeledInstances = ((TrainingConfiguration)this.taskConfiguration).getGroundTruthSource().toString();
			this.outputModel = ((TrainingConfiguration)this.taskConfiguration).getPathToOutputModel();
			if(this.outputModel.equals(new String("-1")))  {
				AmuseLogger.write(this.getClass().getName(), Level.ERROR, "Path to output model is not set!");
				return;
			}
					
		} else if(((TrainingConfiguration)this.taskConfiguration).getGroundTruthSourceType().
				equals(GroundTruthSourceType.READY_INPUT)) {
			pathToFileWithLabeledInstances = new String("-1"); // Input is already labeled!
			this.outputModel = ((TrainingConfiguration)this.taskConfiguration).getPathToOutputModel();
			if(this.outputModel.equals(new String("-1")))  {
				AmuseLogger.write(this.getClass().getName(), Level.ERROR, "Path to output model is not set!");
				return;
			}
		} else {
			DataSetAbstract categoryList = null;
			try {
				categoryList = new ArffDataSet(new File(AmusePreferences.getMultipleTracksAnnotationTablePath()));
			} catch (IOException e) {
				AmuseLogger.write(this.getClass().getName(), Level.ERROR,  
						"Could not load the category table: " + e.getMessage()); 
				return;
			}
			int i=0;
			while(i < categoryList.getValueCount()) {
				Integer id = new Double(categoryList.getAttribute("Id").getValueAt(i).toString()).intValue();
				if(id.toString().equals(
						((TrainingConfiguration)this.taskConfiguration).getGroundTruthSource().toString())) {
					this.categoryDescription = ((TrainingConfiguration)this.taskConfiguration).getGroundTruthSource().toString() + 
						"-" + categoryList.getAttribute("CategoryName").getValueAt(i).toString();
					pathToFileWithLabeledInstances = categoryList.getAttribute("Path").getValueAt(i).toString();
					break;
				}
				i++;
			}
		}
		
		
		// -------------------------------------------------------------------------------------
		// (II): Convert feature vectors + descriptions to labeled classifier input for training
		// -------------------------------------------------------------------------------------
		try {
			this.prepareTrainerInput();
		} catch(NodeException e) {
			AmuseLogger.write(this.getClass().getName(), Level.ERROR,  
				"Could not prepare trainer input: " + e.getMessage()); 
			errorDescriptionBuilder.append(taskConfiguration.getDescription());
			this.fireEvent(new NodeEvent(NodeEvent.TRAINING_FAILED, this));
			return;
		}
		
		// ---------------------------------------------------------------------------------------
		// (III): Run data preprocessing (e.g. outlier removal) before classification. This is not
		// feature processing since all classification data must exist for preprocessing
		// ---------------------------------------------------------------------------------------
		try {
			this.runDataPreprocessing();
		} catch(NodeException e) {
			AmuseLogger.write(this.getClass().getName(), Level.ERROR,  
					"Could not run data preprocessing: " + e.getMessage()); 
			errorDescriptionBuilder.append(taskConfiguration.getDescription());
			this.fireEvent(new NodeEvent(NodeEvent.TRAINING_FAILED, this));
			return;
		}
		
		// -----------------------------------
		// (IV): Configure the training method
		// -----------------------------------
		try {
			this.configureTrainingMethod();
		} catch(NodeException e) {
			AmuseLogger.write(this.getClass().getName(), Level.ERROR,  
					"Configuration of trainer failed: " + e.getMessage()); 
			errorDescriptionBuilder.append(taskConfiguration.getDescription());
			this.fireEvent(new NodeEvent(NodeEvent.TRAINING_FAILED, this));
			return;
		}
		
		// ------------------------------
		// (V): Start the training method
		// ------------------------------
		try {
			this.trainModel();
		} catch(NodeException e) {
			AmuseLogger.write(this.getClass().getName(), Level.ERROR,  
					"Classification training failed: " + e.getMessage()); 
			errorDescriptionBuilder.append(taskConfiguration.getDescription());
			this.fireEvent(new NodeEvent(NodeEvent.TRAINING_FAILED, this));
			return;
		}
		
		// ---------------------------------------------------------------------------------
		// (V) If started directly, remove generated data and fire event for Amuse scheduler
		// ---------------------------------------------------------------------------------
		if(this.directStart) {
			try {
				this.cleanInputFolder();
			} catch(NodeException e) {
				AmuseLogger.write(this.getClass().getName(), Level.ERROR,
					"Could not remove properly the intermediate results '" + 
					this.nodeHome + File.separator + "input" + File.separator + "task_'" + this.jobId + "; please delete it manually! (Exception: "+ e.getMessage() + ")");
			}
			this.fireEvent(new NodeEvent(NodeEvent.TRAINING_COMPLETED, this));
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.interfaces.nodes.NodeSchedulerInterface#proceedTask(java.lang.String[])
	 */
	public void proceedTask(String[] args) {
		if(args.length < 2) {
			AmuseLogger.write(this.getClass().getName(), Level.FATAL, 2 - args.length + 
					" arguments are missing; The usage is 'TrainerNodeScheduler %1 %2', where: \n" +
					"%1 - Home folder of this node\n" +
					"%2 - Unique (for currently running Amuse instance) task Id\n"); 
			System.exit(1);
		}
		
		// Load the task configuration from %TRAINERHOME%/task.ser
		TrainingConfiguration[] trainerConfig = null;
		FileInputStream fis = null;
		ObjectInputStream in = null;
		try {
			fis = new FileInputStream(args[0] + File.separator + "task_" + args[1] + ".ser");
			in = new ObjectInputStream(fis);
			Object o = in.readObject();
			trainerConfig = (TrainingConfiguration[])o;
			in.close();
		} catch(IOException ex) {
		    ex.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		// Proceed the training task(s)
		for(int i=0;i<trainerConfig.length;i++) {
			proceedTask(args[0],new Long(args[1]),trainerConfig[i]);
			AmuseLogger.write(this.getClass().getName(), Level.INFO, "Trainer node is going to start job " + 
					(i+1) + File.separator + trainerConfig.length);
		}
	}
	
	/**
	 * Converts feature vectors + descriptions to labeled classifier input for training
	 * @throws NodeException
	 */
	private void prepareTrainerInput() throws NodeException {
		if(! (((TrainingConfiguration)this.getConfiguration()).getGroundTruthSource() instanceof DataSetInput)) {
			
			DataSet labeledInputForTraining = null;
			
			// If the ground truth has been previously prepared (e.g. from validator), the input is ready! 
			if(((TrainingConfiguration)this.getConfiguration()).getGroundTruthSourceType().
					equals(GroundTruthSourceType.READY_INPUT)) {
				try {
					labeledInputForTraining = new DataSet(new File(((TrainingConfiguration)this.getConfiguration()).getGroundTruthSource().toString()),
							"TrainingSet");
				} catch (IOException e) {
					throw new NodeException(e.getMessage());
				}
			} else {
				
				// Set the i/o files
				/*String pathToFileWithLabeledInstances = new String();
				if(((TrainingConfiguration)this.taskConfiguration).getGroundTruthSourceType().
						equals(TrainingConfiguration.GroundTruthSourceType.FILE_LIST)) {
					pathToFileWithLabeledInstances = ((TrainingConfiguration)this.taskConfiguration).getGroundTruthSource().toString();
					this.outputModel = ((TrainingConfiguration)this.taskConfiguration).getPathToOutputModel();
					if(this.outputModel.equals(new String("-1")))  {
						AmuseLogger.write(this.getClass().getName(), Level.ERROR, "Path to output model is not set!");
						return;
					}
							
				} else if(((TrainingConfiguration)this.taskConfiguration).getGroundTruthSourceType().
						equals(TrainingConfiguration.GroundTruthSourceType.READY_INPUT)) {
					pathToFileWithLabeledInstances = new String("-1"); // Input is already labeled!
					this.outputModel = ((TrainingConfiguration)this.taskConfiguration).getPathToOutputModel();
					if(this.outputModel.equals(new String("-1")))  {
						AmuseLogger.write(this.getClass().getName(), Level.ERROR, "Path to output model is not set!");
						return;
					}
				} else {
					DataSet categoryList = null;
					try {
						categoryList = new DataSet(new File(AmusePreferences.get(KeysStringValue.CATEGORY_DATABASE)));
					} catch (IOException e) {
						AmuseLogger.write(this.getClass().getName(), Level.FATAL,  
								"Could not load the category table: " + e.getMessage()); 
						return;
					}
					int i=0;
					while(i < categoryList.getValueCount()) {
						Integer id = new Double(categoryList.getAttribute("Id").getValueAt(i).toString()).intValue();
						if(id.toString().equals(
								((TrainingConfiguration)this.taskConfiguration).getGroundTruthSource().toString())) {
							this.categoryDescription = ((TrainingConfiguration)this.taskConfiguration).getGroundTruthSource().toString() + 
								"-" + categoryList.getAttribute("CategoryName").getValueAt(i).toString();
							pathToFileWithLabeledInstances = categoryList.getAttribute("Path").getValueAt(i).toString();
							break;
						}
						i++;
					}
				}
				*/
				try {
					labeledInputForTraining = new DataSet("TrainingSet");
					
					// Load the classifier description
					DataSetAbstract classifierGroundTruthSet = new ArffDataSet(new File(pathToFileWithLabeledInstances));
					
					// Load the first classifier input for attributes information
					String currentInputFile = classifierGroundTruthSet.getAttribute("Path").getValueAt(0).toString();
					
					if(currentInputFile.startsWith(AmusePreferences.get(KeysStringValue.MUSIC_DATABASE))) {
						String processedFeatureDatabase = ((TrainingConfiguration)this.getConfiguration()).getProcessedFeatureDatabase();

						if(processedFeatureDatabase.endsWith(File.separator)){
							processedFeatureDatabase = processedFeatureDatabase.substring(0, processedFeatureDatabase.length() - 1);
						}
						currentInputFile = 
							processedFeatureDatabase
							+ File.separator 
							+ currentInputFile.substring(AmusePreferences.get(KeysStringValue.MUSIC_DATABASE).length() + 1,
									currentInputFile.lastIndexOf("."))
							+ File.separator
							+ currentInputFile.substring(currentInputFile.lastIndexOf(File.separator) + 1,
									currentInputFile.lastIndexOf("."))
							+ "_" 
							+ ((TrainingConfiguration)this.taskConfiguration).getProcessedFeaturesModelName() + ".arff";
					}
					else{
						String processedFeatureDatabase = ((TrainingConfiguration)this.getConfiguration()).getProcessedFeatureDatabase();

						if(processedFeatureDatabase.endsWith(File.separator)){
							processedFeatureDatabase = processedFeatureDatabase.substring(0, processedFeatureDatabase.length() - 1);
						}
						currentInputFile = 
							processedFeatureDatabase
							+ File.separator 
							+ currentInputFile.substring(0,
									currentInputFile.lastIndexOf(".")) 
							+ File.separator 
							+ currentInputFile.substring(currentInputFile.lastIndexOf(File.separator) + 1,
									currentInputFile.lastIndexOf(".")) 
							+ "_" 
							+ ((TrainingConfiguration)this.taskConfiguration).getProcessedFeaturesModelName() + ".arff";
					}
					currentInputFile = currentInputFile.replaceAll(File.separator + "+", File.separator);
					
					ArffLoader classifierInputLoader = new ArffLoader();
					Instance inputInstance;
					AmuseLogger.write(this.getClass().getName(), Level.DEBUG, "Loading: " + currentInputFile);
					classifierInputLoader.setFile(new File(currentInputFile));
					inputInstance = classifierInputLoader.getNextInstance(classifierInputLoader.getStructure());
					
					// Create the attributes omitting UNIT, START and END attributes (they describe the partition for modeled features)
					for(int i=0;i<classifierInputLoader.getStructure().numAttributes()-3;i++) {
						labeledInputForTraining.addAttribute(new NumericAttribute(inputInstance.attribute(i).name(),
							new ArrayList<Double>()));
					}
					labeledInputForTraining.addAttribute(new NumericAttribute("Id",new ArrayList<Double>()));
					labeledInputForTraining.addAttribute(new StringAttribute("Category",new ArrayList<String>()));
					
					// Create the labeled data
					for(int i=0;i<classifierGroundTruthSet.getValueCount();i++) {
						String label = classifierGroundTruthSet.getAttribute("Category").getValueAt(i).toString();
						Double confidence = new Double(classifierGroundTruthSet.getAttribute("Relationship").getValueAt(i).toString());
						Integer end = new Double(classifierGroundTruthSet.getAttribute("End").getValueAt(i).toString()).intValue();
						
						// If the complete song should be read
						if(end == -1) {
							while(inputInstance != null) {
								for(int j=0;j<classifierInputLoader.getStructure().numAttributes()-3;j++) {
									Double val = inputInstance.value(j);
									labeledInputForTraining.getAttribute(j).addValue(val);
								}
								
								// Write the ID attribute (from what song the features are saved)
								// IMPORTANT: --------------------------------------------------- 
								// This attribute must not be used for classification model training! 
								// If any new classification algorithms are integrated into AMUSE, they must
								// handle this properly!!!
								Double id = new Double(classifierGroundTruthSet.getAttribute("Id").getValueAt(i).toString());
								labeledInputForTraining.getAttribute("Id").addValue(id);
								
								// TODO Parameter?
								if(confidence >= 0.5) {
									labeledInputForTraining.getAttribute("Category").addValue(label);
								} else {
									//FIXME
									//values_writer.writeBytes("PIANO");
									labeledInputForTraining.getAttribute("Category").addValue("NOT_" + label);
								}
								inputInstance = classifierInputLoader.getNextInstance(classifierInputLoader.getStructure());
							}
						} else {
							// TODO Consider Vocals/Piano-Recognition-Scenario!
							/*for (Enumeration attrs = classifierInputLoader.getStructure().enumerateAttributes() ; attrs.hasMoreElements() ;) {
								if(inputInstance == null) System.out.println("HARR!");
								Double val = inputInstance.value((Attribute)attrs.nextElement());
								values_writer.writeBytes(val.toString());
								values_writer.writeBytes(",");
				   	        }
							values_writer.writeBytes(label);
							values_writer.writeBytes(sep);*/
		
						}
						
						// Go to the next description
						String newInputFile = classifierGroundTruthSet.getAttribute("Path").getValueAt(i+1).toString();
						
						if(newInputFile.startsWith(AmusePreferences.get(KeysStringValue.MUSIC_DATABASE))) {
							newInputFile = 
								((TrainingConfiguration)this.getConfiguration()).getProcessedFeatureDatabase()
								+ File.separator 
								+ newInputFile.substring(AmusePreferences.get(KeysStringValue.MUSIC_DATABASE).length() + 1,
										newInputFile.lastIndexOf("."))
								+ File.separator
								+ newInputFile.substring(newInputFile.lastIndexOf(File.separator) + 1,
										newInputFile.lastIndexOf("."))
								+ "_" 
								+ ((TrainingConfiguration)this.taskConfiguration).getProcessedFeaturesModelName() + ".arff";
						}
						else{
							newInputFile = 
								((TrainingConfiguration)this.getConfiguration()).getProcessedFeatureDatabase()
								+ File.separator 
								+ newInputFile.substring(1,
										newInputFile.lastIndexOf(".")) 
								+ File.separator 
								+ newInputFile.substring(newInputFile.lastIndexOf(File.separator) + 1,
										newInputFile.lastIndexOf(".")) 
								+ "_" 
								+ ((TrainingConfiguration)this.taskConfiguration).getProcessedFeaturesModelName() + ".arff";
						}
						newInputFile = newInputFile.replaceAll(File.separator + "+", File.separator);
						
						
						
						// Go to the next music file?
						if(!newInputFile.equals(currentInputFile)) {
							currentInputFile = newInputFile;
							AmuseLogger.write(this.getClass().getName(), Level.DEBUG, "Loading: " + currentInputFile);
							classifierInputLoader = new ArffLoader();
							classifierInputLoader.setFile(new File(currentInputFile));
						} 
						
						// Load the next input vector
						inputInstance = classifierInputLoader.getNextInstance(classifierInputLoader.getStructure());
						
						// If the input vector and its description do not match 
						if(inputInstance == null) {
							AmuseLogger.write(this.getClass().getName(), Level.WARN,  
									"Descriptions from " + pathToFileWithLabeledInstances +
									"do not correspond to extractorNode.jar input vectors, the classifier will not be trained with" +
									"the complete data!");
							break;
						}
					}
				} catch(IOException e) {
					e.printStackTrace();
					throw new NodeException(e.getMessage());
				}
			}
			
			// Replace the ground truth source by the data set input loaded into memory
			((TrainingConfiguration)this.taskConfiguration).setGroundTruthSource(new DataSetInput(labeledInputForTraining));
		}
	}
	
	/**
	 * Run data preprocessing (e.g. outlier removal) before classification. This is not 
	 * feature processing since all classification data must exist for preprocessing
	 * @throws NodeException
	 */
	private void runDataPreprocessing() throws NodeException {
		
		// Is any preprocessing required?
		if(!((TrainingConfiguration)this.taskConfiguration).getPreprocessingAlgorithmDescription().equals(new String("-1"))) {
			
			ClassificationPreprocessingInterface cpi = null;
			Integer requiredAlgorithm; 
			// (1) If parameter string for this algorithm exists..
			if(((TrainingConfiguration)this.taskConfiguration).getPreprocessingAlgorithmDescription().contains("[") && 
					((TrainingConfiguration)this.taskConfiguration).getPreprocessingAlgorithmDescription().contains("]")) {
				requiredAlgorithm = new Integer(((TrainingConfiguration)this.taskConfiguration).
						getPreprocessingAlgorithmDescription().substring(0,((TrainingConfiguration)this.taskConfiguration).
						getPreprocessingAlgorithmDescription().indexOf("[")));
				this.requiredParameters = ((TrainingConfiguration)this.taskConfiguration).getPreprocessingAlgorithmDescription().
						substring(((TrainingConfiguration)this.taskConfiguration).getPreprocessingAlgorithmDescription().indexOf("[")+1,
						((TrainingConfiguration)this.taskConfiguration).getPreprocessingAlgorithmDescription().lastIndexOf("]")); 
			} else {
				requiredAlgorithm = new Integer(((TrainingConfiguration)this.taskConfiguration).getPreprocessingAlgorithmDescription());
				this.requiredParameters = null;
			}
			
			// (2) Search for class which runs preprocessing and configure it
			boolean algorithmFound = false;
			try {
		    	ArffLoader classificationTrainerTableLoader = new ArffLoader();
		    	if(this.directStart) {
		    		classificationTrainerTableLoader.setFile(new File(AmusePreferences.getClassifierPreprocessingAlgorithmTablePath()));
		    	} else {
		    		classificationTrainerTableLoader.setFile(new File(this.nodeHome + File.separator + "input" + File.separator + "task_" + this.jobId + File.separator + "classifierPreprocessingAlgorithmTable.arff"));
		    	}
		    	Instance currentInstance = classificationTrainerTableLoader.getNextInstance(classificationTrainerTableLoader.getStructure());
				Attribute idAttribute = classificationTrainerTableLoader.getStructure().attribute("Id");
				Attribute nameAttribute = classificationTrainerTableLoader.getStructure().attribute("Name");
				Attribute preprocessingAdapterClassAttribute = classificationTrainerTableLoader.getStructure().attribute("PreprocessingAdapterClass");
				Attribute homeFolderAttribute = classificationTrainerTableLoader.getStructure().attribute("HomeFolder");
				Attribute startScriptAttribute = classificationTrainerTableLoader.getStructure().attribute("StartScript");
				Attribute inputBasePreprocessingBatchAttribute = classificationTrainerTableLoader.getStructure().attribute("InputBasePreprocessingBatch");
				Attribute inputPreprocessingBatchAttribute = classificationTrainerTableLoader.getStructure().attribute("InputPreprocessingBatch");
				while(currentInstance != null) {
					Integer idOfCurrentAlgorithm = new Double(currentInstance.value(idAttribute)).intValue();
					if(idOfCurrentAlgorithm.equals(requiredAlgorithm)) {
						
						// Configure the adapter class
						try {
							Class<?> adapter = Class.forName(currentInstance.stringValue(preprocessingAdapterClassAttribute));
							cpi = (ClassificationPreprocessingInterface)adapter.newInstance();
							Properties preprocessingProperties = new Properties();
							Integer id = new Double(currentInstance.value(idAttribute)).intValue();
							preprocessingProperties.setProperty("id",id.toString());
							preprocessingProperties.setProperty("name",currentInstance.stringValue(nameAttribute));
							preprocessingProperties.setProperty("preprocessorFolderName",currentInstance.stringValue(homeFolderAttribute));
							if(directStart) {
								preprocessingProperties.setProperty("preprocessorFolder",AmusePreferences.get(KeysStringValue.AMUSE_PATH) +File.separator+ "tools" + File.separator + currentInstance.stringValue(homeFolderAttribute));
							} else {
								preprocessingProperties.setProperty("preprocessorFolder",nodeHome +File.separator+ "tools" + File.separator + currentInstance.stringValue(homeFolderAttribute));
							}
							preprocessingProperties.setProperty("startScript",currentInstance.stringValue(startScriptAttribute));
							preprocessingProperties.setProperty("inputBaseBatch",currentInstance.stringValue(inputBasePreprocessingBatchAttribute));
							preprocessingProperties.setProperty("inputBatch",currentInstance.stringValue(inputPreprocessingBatchAttribute));
							preprocessingProperties.setProperty("categoryName", this.categoryDescription);
							((AmuseTask)cpi).configure(preprocessingProperties,this,this.requiredParameters);
							((AmuseTask)cpi).initialize();
							
							AmuseLogger.write(this.getClass().getName(), Level.INFO, 
									"Preprocessing algorithm is configured: " + currentInstance.stringValue(preprocessingAdapterClassAttribute));
						} catch(ClassNotFoundException e) {
							AmuseLogger.write(this.getClass().getName(), Level.ERROR, 
									"Preprocessing algorithm class cannot be located: " + currentInstance.stringValue(preprocessingAdapterClassAttribute));
							System.exit(1);
						} catch(IllegalAccessException e) {
							AmuseLogger.write(this.getClass().getName(), Level.ERROR, 
									"Preprocessing algorithm class or its nullary constructor is not accessible: " + currentInstance.stringValue(preprocessingAdapterClassAttribute));
							System.exit(1);
						} catch(InstantiationException e) {
							AmuseLogger.write(this.getClass().getName(), Level.ERROR, 
									"Instantiation failed for preprocessing algorithm class: " + currentInstance.stringValue(preprocessingAdapterClassAttribute));
							System.exit(1);
						} catch(NodeException e) {
							AmuseLogger.write(this.getClass().getName(), Level.ERROR, 
									"Setting of parameters failed for preprocessing algorithm class: " + e.getMessage());
							System.exit(1);
						}
						
						algorithmFound = true;
						break;
					} 
					currentInstance = classificationTrainerTableLoader.getNextInstance(classificationTrainerTableLoader.getStructure());
				}
				
				if(!algorithmFound) {
					AmuseLogger.write(this.getClass().getName(), Level.ERROR, 
							"Algorithm with id " + ((TrainingConfiguration)this.taskConfiguration).getAlgorithmDescription() + 
							" was not found, task aborted");
					System.exit(1);
				}
			} catch(IOException e) {
		    	throw new NodeException(e.getMessage());
		    }
			
			// (3) Run the preprocessing method
			try {
				cpi.runPreprocessing();
			} catch(NodeException e) {
				AmuseLogger.write(this.getClass().getName(), Level.WARN, 
						"Could not run preprocessing, training will be started directly: " + e.getMessage());
			}
		}
	}	
	
	/**
	 * Configures the training method
	 * @throws NodeException
	 */
	private void configureTrainingMethod() throws NodeException {
		Integer requiredAlgorithm; 
		// If parameter string for this algorithm exists..
		if(((TrainingConfiguration)this.taskConfiguration).getAlgorithmDescription().contains("[") && 
				((TrainingConfiguration)this.taskConfiguration).getAlgorithmDescription().contains("]")) {
			requiredAlgorithm = new Integer(((TrainingConfiguration)this.taskConfiguration).
					getAlgorithmDescription().substring(0,((TrainingConfiguration)this.taskConfiguration).
					getAlgorithmDescription().indexOf("[")));
			this.requiredParameters = ((TrainingConfiguration)this.taskConfiguration).getAlgorithmDescription().
					substring(((TrainingConfiguration)this.taskConfiguration).getAlgorithmDescription().indexOf("[")+1,
					((TrainingConfiguration)this.taskConfiguration).getAlgorithmDescription().lastIndexOf("]")); 
		} else {
			requiredAlgorithm = new Integer(((TrainingConfiguration)this.taskConfiguration).getAlgorithmDescription());
			this.requiredParameters = null;
		}
		boolean algorithmFound = false;
		try {
	    	ArffLoader classificationTrainerTableLoader = new ArffLoader();
	    	if(this.directStart) {
	    		classificationTrainerTableLoader.setFile(new File(AmusePreferences.getClassifierAlgorithmTablePath()));
	    	} else {
	    		classificationTrainerTableLoader.setFile(new File(this.nodeHome + File.separator + "input" + File.separator + "task_" + this.jobId + File.separator + "classifierAlgorithmTable.arff"));
	    	}
	    	Instance currentInstance = classificationTrainerTableLoader.getNextInstance(classificationTrainerTableLoader.getStructure());
			Attribute idAttribute = classificationTrainerTableLoader.getStructure().attribute("Id");
			Attribute nameAttribute = classificationTrainerTableLoader.getStructure().attribute("Name");
			Attribute trainerAdapterClassAttribute = classificationTrainerTableLoader.getStructure().attribute("TrainerAdapterClass");
			Attribute homeFolderAttribute = classificationTrainerTableLoader.getStructure().attribute("HomeFolder");
			Attribute startScriptAttribute = classificationTrainerTableLoader.getStructure().attribute("StartScript");
			Attribute inputBaseTrainingBatchAttribute = classificationTrainerTableLoader.getStructure().attribute("InputBaseTrainingBatch");
			Attribute inputTrainingBatchAttribute = classificationTrainerTableLoader.getStructure().attribute("InputTrainingBatch");
			while(currentInstance != null) {
				Integer idOfCurrentAlgorithm = new Double(currentInstance.value(idAttribute)).intValue();
				if(idOfCurrentAlgorithm.equals(requiredAlgorithm)) {
					
					// Configure the adapter class
					try {
						Class<?> adapter = Class.forName(currentInstance.stringValue(trainerAdapterClassAttribute));
						this.ctad = (TrainerInterface)adapter.newInstance();
						Properties trainerProperties = new Properties();
						Integer id = new Double(currentInstance.value(idAttribute)).intValue();
						trainerProperties.setProperty("id",id.toString());
						trainerProperties.setProperty("name",currentInstance.stringValue(nameAttribute));
						if(this.directStart) {
							trainerProperties.setProperty("homeFolder",AmusePreferences.get(KeysStringValue.AMUSE_PATH) + File.separator + "tools" + File.separator + currentInstance.stringValue(homeFolderAttribute));
						} else {
							trainerProperties.setProperty("homeFolder",this.nodeHome + File.separator + "tools" + File.separator + currentInstance.stringValue(homeFolderAttribute));
						}
						trainerProperties.setProperty("startScript",currentInstance.stringValue(startScriptAttribute));
						trainerProperties.setProperty("inputBaseBatch",currentInstance.stringValue(inputBaseTrainingBatchAttribute));
						trainerProperties.setProperty("inputBatch",currentInstance.stringValue(inputTrainingBatchAttribute));
						trainerProperties.setProperty("categoryName", this.categoryDescription);
						((AmuseTask)this.ctad).configure(trainerProperties,this,this.requiredParameters);
						((AmuseTask)this.ctad).initialize();
						
						AmuseLogger.write(this.getClass().getName(), Level.INFO, 
								"Classification trainer is configured: " + currentInstance.stringValue(trainerAdapterClassAttribute));
					} catch(ClassNotFoundException e) {
						e.printStackTrace();
						AmuseLogger.write(this.getClass().getName(), Level.ERROR, 
								"Classification trainer class cannot be located: " + currentInstance.stringValue(trainerAdapterClassAttribute));
						System.exit(1);
					} catch(IllegalAccessException e) {
						AmuseLogger.write(this.getClass().getName(), Level.ERROR, 
								"Classification trainer class or its nullary constructor is not accessible: " + currentInstance.stringValue(trainerAdapterClassAttribute));
						System.exit(1);
					} catch(InstantiationException e) {
						AmuseLogger.write(this.getClass().getName(), Level.ERROR, 
								"Instantiation failed for classification trainer class: " + currentInstance.stringValue(trainerAdapterClassAttribute));
						System.exit(1);
					} catch(NodeException e) {
						AmuseLogger.write(this.getClass().getName(), Level.ERROR, 
								"Setting of parameters failed for classification trainer class: " + e.getMessage());
						System.exit(1);
					}
					
					algorithmFound = true;
					break;
				} 
				currentInstance = classificationTrainerTableLoader.getNextInstance(classificationTrainerTableLoader.getStructure());
			}
			
			if(!algorithmFound) {
				AmuseLogger.write(this.getClass().getName(), Level.ERROR, 
						"Algorithm with id " + ((TrainingConfiguration)this.taskConfiguration).getAlgorithmDescription() + 
						" was not found, task aborted");
				System.exit(1);
			}

	    } catch(IOException e) {
	    	throw new NodeException(e.getMessage());
	    }
	}

	
	/**
	 * Trains the model(s)
	 */
	private void trainModel() throws NodeException {
		// Check the folder for model file if it exists and if not create it
		if(requiredParameters != null) {
			requiredParameters = "[" + requiredParameters + "]";
		} else {
			requiredParameters = "";
		}
		
		// If the model(s) should be saved to the Amuse model database..
		if( ! (((TrainingConfiguration)this.taskConfiguration).getGroundTruthSource() instanceof FileInput) &&
				((TrainingConfiguration)this.taskConfiguration).getGroundTruthSourceType().equals(GroundTruthSourceType.CATEGORY_ID)) {
			File folderForModels;
			
			folderForModels = new File(
				((TrainingConfiguration)this.taskConfiguration).getModelDatabase() + File.separator + 
				this.categoryDescription + File.separator + ((AmuseTask)this.ctad).getProperties().getProperty("id") + 
				"-" + ((AmuseTask)this.ctad).getProperties().getProperty("name") +  
				requiredParameters + File.separator + 
				((TrainingConfiguration)this.taskConfiguration).getProcessedFeaturesModelName());
			
			if(!folderForModels.exists()) {
				if(!folderForModels.mkdirs()) {
					AmuseLogger.write(this.getClass().getName(), Level.ERROR, 
							"Could not create folder for output model: " + folderForModels);
					System.exit(1);
				}
			}
			this.outputModel = new String(folderForModels + File.separator + "model.mod");
		}
		AmuseLogger.write(this.getClass().getName(), Level.INFO, "Starting the classification training with " + 
				((AmuseTask)this.ctad).getProperties().getProperty("name") + "...");
		this.ctad.trainModel(this.outputModel);
		AmuseLogger.write(this.getClass().getName(), Level.INFO, "..classification training finished!");
	}
}
