package amuse.nodes.classifier.methods.supervised;

import java.io.File;
import java.io.IOException;

import org.deeplearning4j.nn.modelimport.keras.KerasModelImport;
import org.deeplearning4j.nn.modelimport.keras.exceptions.InvalidKerasConfigurationException;
import org.deeplearning4j.nn.modelimport.keras.exceptions.UnsupportedKerasConfigurationException;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import amuse.data.io.DataSet;
import amuse.data.io.DataSetInput;
import amuse.interfaces.nodes.NodeException;
import amuse.interfaces.nodes.methods.AmuseTask;
import amuse.nodes.classifier.ClassificationConfiguration;
import amuse.nodes.classifier.interfaces.ClassifierInterface;
import amuse.nodes.trainer.TrainingConfiguration;

public class KerasModelLoader extends AmuseTask implements ClassifierInterface {

	
	@Override
	public void setParameters(String parameterString) throws NodeException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initialize() throws NodeException {
		// TODO Auto-generated method stub

	}

	@Override
	public void classify(String pathToModelFile) throws NodeException {
		DataSet dataSetToClassify = ((DataSetInput)((ClassificationConfiguration)this.correspondingScheduler.
				getConfiguration()).getInputToClassify()).getDataSet();
		
		try {
			
			
			//(1) Convert to keras set
			//dataSetToClassify.convertToKerasSet();
			
			//(2) Load the model
			MultiLayerNetwork model = KerasModelImport.importKerasSequentialModelAndWeights(pathToModelFile, false);
			
			//(3) Connect the ports
			// Test basic inference on the model.
	        INDArray input = Nd4j.create(256, 100);
	        INDArray output = model.output(input);

	        //(4)Apply the model
	        // Test basic model training.
	        model.fit(input, output);
	        
	        // (6) Convert the results to AMUSE EditableDataSet
	     	/*exampleSet.getAttributes().getPredictedLabel().setName("PredictedCategory");
	     	((ClassificationConfiguration)(this.correspondingScheduler.getConfiguration())).setInputToClassify(new DataSetInput(
	     		new DataSet(exampleSet)));*/
			
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
