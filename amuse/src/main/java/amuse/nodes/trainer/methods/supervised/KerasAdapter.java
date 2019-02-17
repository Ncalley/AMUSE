package amuse.nodes.trainer.methods.supervised;

import java.io.File;
import java.io.IOException;

import org.deeplearning4j.nn.modelimport.keras.InvalidKerasConfigurationException;
import org.deeplearning4j.nn.modelimport.keras.KerasModelImport;
import org.deeplearning4j.nn.modelimport.keras.UnsupportedKerasConfigurationException;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.io.ClassPathResource;

import amuse.data.io.DataSet;
import amuse.data.io.DataSetInput;
import amuse.interfaces.nodes.NodeException;
import amuse.interfaces.nodes.methods.AmuseTask;
import amuse.nodes.trainer.TrainingConfiguration;
import amuse.nodes.trainer.interfaces.TrainerInterface;

public class KerasAdapter extends AmuseTask implements TrainerInterface {

	private String pathToModel = "";
	

	/**
	 * @see amuse.nodes.trainer.interfaces.TrainerInterface#setParameters(String)
	 */
	@Override
	public void setParameters(String parameterString) throws NodeException {
		if(parameterString == "" || parameterString == null) {
			pathToModel = "Keras/Models/model.h5";
		} else {
			pathToModel = parameterString;
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
		
		try {
			MultiLayerNetwork model = KerasModelImport.importKerasSequentialModelAndWeights(pathToModel, false);
			
			// Test basic inference on the model.
	        INDArray input = Nd4j.create(256, 100);
	        INDArray output = model.output(input);

	        // Test basic model training.
	        model.fit(input, output);
			
			//Remove this line
			dataSet.saveToArffFile(new File(outputModel));
		} catch(IOException e) {
			throw new NodeException("Could not save the data: " + e.getMessage());
		} catch (InvalidKerasConfigurationException e) {
			// TODO Auto-generated catch block
			throw new NodeException("Invalid Keras configuration : "+ e.getMessage());
		} catch (UnsupportedKerasConfigurationException e) {
			// TODO Auto-generated catch block
			throw new NodeException("Unsupported Keras configuration : "+ e.getMessage());
		}
	}

}
