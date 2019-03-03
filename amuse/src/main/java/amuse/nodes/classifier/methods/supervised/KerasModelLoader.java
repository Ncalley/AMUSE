package amuse.nodes.classifier.methods.supervised;

import java.io.File;
import java.io.IOException;

import javax.swing.JPanel;

import org.apache.log4j.Level;
import org.deeplearning4j.nn.modelimport.keras.KerasModelImport;
import org.deeplearning4j.nn.modelimport.keras.exceptions.InvalidKerasConfigurationException;
import org.deeplearning4j.nn.modelimport.keras.exceptions.UnsupportedKerasConfigurationException;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import amuse.data.datasets.KerasSet;
import amuse.data.io.DataSet;
import amuse.data.io.DataSetInput;
import amuse.interfaces.nodes.NodeException;
import amuse.interfaces.nodes.methods.AmuseTask;
import amuse.nodes.classifier.ClassificationConfiguration;
import amuse.nodes.classifier.interfaces.ClassifierInterface;
import amuse.nodes.trainer.methods.supervised.KerasAdapter;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;
import amuse.util.AmuseLogger;

/**
 * Uses Keras model to classify Amuse's inputs
 * 
 * inspired by : https://github.com/deeplearning4j/dl4j-examples/blob/master/dl4j-examples/src/main/java/org/deeplearning4j/examples/feedforward/classification/MLPClassifierLinear.java
 * 
 * @author Nicolas Calley
 *
 */
public class KerasModelLoader extends AmuseTask implements ClassifierInterface {

	private String pathToModel = this.correspondingScheduler.getHomeFolder();
	private String h5File = "Models" + File.separator + "model.h5";
	
    int numOutputs = 1;
	
	@Override
	public void setParameters(String parameterString) throws NodeException {
		
	}

	@Override
	public void initialize() throws NodeException {

	}

	@Override
	public void classify(String pathToModelFile) throws NodeException {
		DataSet dataSetToClassify = ((DataSetInput)((ClassificationConfiguration)this.correspondingScheduler.
				getConfiguration()).getInputToClassify()).getDataSet();
		
		try {
			
			
			//(1) Convert to keras set
			//dataSetToClassify.convertToKerasSet();
			
			//(2) Load the model
			MultiLayerNetwork model = KerasModelImport.importKerasSequentialModelAndWeights(pathToModel + File.separator + h5File, false);
			AmuseLogger.write(KerasAdapter.class.getName(), Level.INFO, "Keras model loaded successfully !");
			
			KerasSet kerasSet = dataSetToClassify.convertToKerasSet();
			

	        //(3)Initializing the model
	        model.init();
	        
	        //(4) Classifying
	        Evaluation eval = new Evaluation(numOutputs);
	        INDArray features = kerasSet.getContent();
	        INDArray labels = kerasSet.getLabels();
	        INDArray predicted = model.output(features,false);

	        eval.eval(labels, predicted);
	        
	        double xMin = 0;
	        double xMax = 1.0;
	        double yMin = -0.2;
	        double yMax = 0.8;

	        //(5 Evaluate the predictions at every point in the x/y input space
	        int nPointsPerAxis = 100;
	        double[][] evalPoints = new double[nPointsPerAxis*nPointsPerAxis][2];
	        int count = 0;
	        for( int i=0; i<nPointsPerAxis; i++ ){
	            for( int j=0; j<nPointsPerAxis; j++ ){
	                double x = i * (xMax-xMin)/(nPointsPerAxis-1) + xMin;
	                double y = j * (yMax-yMin)/(nPointsPerAxis-1) + yMin;

	                evalPoints[count][0] = x;
	                evalPoints[count][1] = y;

	                count++;
	            }
	        }

	        INDArray allXYPoints = Nd4j.create(evalPoints);
	        INDArray predictionsAtXYPoints = model.output(allXYPoints);

	        INDArray testPredicted = model.output(kerasSet.getContent());

	        
	        // (6 Convert the results to AMUSE EditableDataSet
	     	((ClassificationConfiguration)(this.correspondingScheduler.getConfiguration())).setInputToClassify(new DataSetInput(
	     		new DataSet(kerasSet)));
			
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
