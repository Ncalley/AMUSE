package amuse.data.datasets;

import java.util.LinkedHashMap;
import java.util.Map;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

/**
 * A simple object that will keep both the data and the name of a DataSet when it will be exploited with Keras
 * As Keras can't exploit non numeric Data we ensure to properly save all the data to rebuild our DataSet later.
 * @author NCalley
 *
 */
public class KerasSet {

	
	private String name = "";
	
	//A map that will contain either the name and the Attribute or the name and an Integer or Double
	//This map is used to rebuild our DataSet as Keras doesn't handle String values, it's probably not optimal though
	private Map<String,Object> hierarchy = new LinkedHashMap<>();
	
	//An ensemble of Numeric values that Keras can exploit and train on
	private INDArray content = Nd4j.create();
	
	public KerasSet(String name, INDArray content) {
		this.name = name;
		this.content = content;
	}
	
	public KerasSet(String name, INDArray content, Map<String,Object> hierarchy) {
		this.name = name;
		this.content = content;
		this.hierarchy = hierarchy;
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

	public Map<String,Object> getHierarchy() {
		return hierarchy;
	}

	public void setHierarchy(Map<String,Object> hierarchy) {
		this.hierarchy = hierarchy;
	}
	
}
