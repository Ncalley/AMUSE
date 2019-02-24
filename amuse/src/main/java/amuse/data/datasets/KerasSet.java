package amuse.data.datasets;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

/**
 * A simple object that will keep both the data and the name of a DataSet when it will be exploited with Keras
 * @author NCalley
 *
 */
public class KerasSet {

	private String name = "";
	private INDArray content = Nd4j.create();
	
	public KerasSet(String name, INDArray content) {
		this.name = name;
		this.content = content;
	}
	
	public KerasSet(INDArray content) {
		this.content = content;
	}
	
	public KerasSet(String name) {
		this.name = name;
	}
	
	public KerasSet() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public INDArray getContent() {
		return content;
	}

	public void setContent(INDArray content) {
		this.content = content;
	}
	
	
}
