package amuse.nodes.trainer.methods.supervised;

import java.io.IOException;

import amuse.interfaces.nodes.NodeException;
import amuse.interfaces.nodes.methods.AmuseTask;
import amuse.nodes.trainer.interfaces.TrainerInterface;

public class KerasAdapter extends AmuseTask implements TrainerInterface {

	private String pathToModel = "";
	

	/**
	 * @see amuse.nodes.trainer.interfaces.TrainerInterface#setParameters(String)
	 */
	@Override
	public void setParameters(String parameterString) throws NodeException {
		if(parameterString == "" || parameterString == null) {
			pathToModel = "Keras/";
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
		/*
		 *We execute the python model so it generates the file 
		 */
		try {
			String command = "py "+pathToModel+outputModel;
			Process p = Runtime.getRuntime().exec(command);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			String command = "python "+pathToModel+outputModel;
			try {
				Process p = Runtime.getRuntime().exec(command);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

}
