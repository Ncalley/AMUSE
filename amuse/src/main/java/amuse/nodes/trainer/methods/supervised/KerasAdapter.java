package amuse.nodes.trainer.methods.supervised;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Level;
import org.deeplearning4j.nn.modelimport.keras.KerasModelImport;
import org.deeplearning4j.nn.modelimport.keras.exceptions.InvalidKerasConfigurationException;
import org.deeplearning4j.nn.modelimport.keras.exceptions.UnsupportedKerasConfigurationException;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.transferlearning.TransferLearning;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import amuse.data.datasets.KerasSet;
import amuse.data.io.DataSet;
import amuse.data.io.DataSetInput;
import amuse.interfaces.nodes.NodeException;
import amuse.interfaces.nodes.methods.AmuseTask;
import amuse.nodes.trainer.TrainingConfiguration;
import amuse.nodes.trainer.interfaces.TrainerInterface;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;
import amuse.util.AmuseLogger;

public class KerasAdapter extends AmuseTask implements TrainerInterface {

	private String pathToModel = AmusePreferences.get(KeysStringValue.KERAS_DATABASE);
	private String pythonFile = "";
	private String h5File = "Models" + File.separator + "model.h5";
	

	/**
	 * @see amuse.nodes.trainer.interfaces.TrainerInterface#setParameters(String)
	 */
	@Override
	public void setParameters(String parameterString) throws NodeException {
		if(parameterString == "" || parameterString == null) {
			pythonFile = pathToModel + File.separator + "GenerateModel.py";
		} else {
			//It should never be used as I couldn't define any way to correctly set this (it should be in classifierAlgorithmTable.arff)
			pythonFile = pathToModel + File.separator + parameterString;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see amuse.interfaces.AmuseTaskInterface#initialize()
	 */
	@Override
	public void initialize() throws NodeException {
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see amuse.nodes.trainer.interfaces.TrainerInterface#trainModel(java.lang.String, java.lang.String, long)
	 */
	@Override
	public void trainModel(String outputModel) throws NodeException {
		DataSet dataSet = ((DataSetInput)((TrainingConfiguration)this.correspondingScheduler.getConfiguration()).getGroundTruthSource()).getDataSet();
		
		//We execute the python model so it generates the file 
		 
		try {
			
			String command = "py "+pythonFile;
			Process p = Runtime.getRuntime().exec(command);
			AmuseLogger.write(KerasAdapter.class.getName(), Level.INFO, "Model created !");
			
		} catch (IOException e) {
			e.printStackTrace();
			
			//The command isn't the same on Windows and Linux so we must try differently if it fails
			String command = "python "+pythonFile;
			try {
				Process p = Runtime.getRuntime().exec(command);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
		}
		
		try {
			//We import the model with deepLearning4j
			MultiLayerNetwork model = KerasModelImport.importKerasSequentialModelAndWeights(pathToModel + File.separator + h5File, false);
			AmuseLogger.write(KerasAdapter.class.getName(), Level.INFO, "Keras model loaded successfully !");
			
			KerasSet trainingSet = dataSet.convertToKerasSet();
			//Mapping the the inputs and outputs
			INDArray input = trainingSet.getContent();
			INDArray output = model.output(input);
			
			

			AmuseLogger.write(KerasAdapter.class.getName(), Level.INFO, "Training...");
			//Training
			model.fit(input, output);
			
			AmuseLogger.write(KerasAdapter.class.getName(), Level.INFO, "Training complete.");
			//Setting the new content of the KerasSet
			trainingSet.setContent(output);
			
			AmuseLogger.write(KerasAdapter.class.getName(), Level.INFO, "Saving results");
			
			try {
				//Saving
				dataSet = new DataSet(trainingSet);
				dataSet.saveToArffFile(new File(outputModel));
			} catch(IOException e) {
				AmuseLogger.write(KerasAdapter.class.getName(), Level.ERROR,"Could not save the data: " + e.getMessage());
			}
			
		} catch (IOException | InvalidKerasConfigurationException | UnsupportedKerasConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
