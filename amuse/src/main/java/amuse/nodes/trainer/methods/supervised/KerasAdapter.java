package amuse.nodes.trainer.methods.supervised;

import java.io.File;
import java.io.IOException;

import org.deeplearning4j.nn.modelimport.keras.KerasModelImport;
import org.deeplearning4j.nn.modelimport.keras.exceptions.InvalidKerasConfigurationException;
import org.deeplearning4j.nn.modelimport.keras.exceptions.UnsupportedKerasConfigurationException;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
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

public class KerasAdapter extends AmuseTask implements TrainerInterface {

	private String pathToModel = AmusePreferences.get(KeysStringValue.KERAS_DATABASE);
	

	/**
	 * @see amuse.nodes.trainer.interfaces.TrainerInterface#setParameters(String)
	 */
	@Override
	public void setParameters(String parameterString) throws NodeException {
		if(parameterString == "" || parameterString == null) {
			pathToModel = pathToModel + File.separator + "GenerateModel.py";
		} else {
			//It should never be used as I couldn't define any way to correctly set this (it should be in classifierAlgorithmTable.arff)
			pathToModel = pathToModel + File.separator + parameterString;
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
			
			String command = "py "+pathToModel;
			Process p = Runtime.getRuntime().exec(command);
			
			
		} catch (IOException e) {
			e.printStackTrace();
			
			//The command isn't the same on Windows and Linux so we must try differently if it fails
			String command = "python "+pathToModel;
			try {
				Process p = Runtime.getRuntime().exec(command);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
		}
		
		try {
			//We import the model with deepLearning4j
			MultiLayerNetwork model = KerasModelImport.importKerasSequentialModelAndWeights(pathToModel);
			
			KerasSet trainingSet = dataSet.convertToKerasSet();
			//Mapping the the inputs and outputs
			INDArray input = trainingSet.getContent();
			INDArray output = model.output(input);

			//Training
			model.fit(input, output);
			
			//Setting the new content of the KerasSet
			trainingSet.setContent(output);
			
			try {
				//Saving
				dataSet = new DataSet(trainingSet);
				dataSet.saveToArffFile(new File(outputModel));
			} catch(IOException e) {
				throw new NodeException("Could not save the data: " + e.getMessage());
			}
			
		} catch (IOException | InvalidKerasConfigurationException | UnsupportedKerasConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
