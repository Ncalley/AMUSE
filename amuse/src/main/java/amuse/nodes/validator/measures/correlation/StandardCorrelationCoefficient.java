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
 *  Creation date: 25.11.2009
 */ 
package amuse.nodes.validator.measures.correlation;

import java.util.ArrayList;

import amuse.interfaces.nodes.NodeException;
import amuse.nodes.classifier.interfaces.ClassifiedSongPartitions;
import amuse.nodes.validator.interfaces.ClassificationQualityDoubleMeasureCalculator;
import amuse.nodes.validator.interfaces.ValidationMeasureDouble;

/**
 * Correlation coefficient calculates the correlation between labeled and predicted relationships.
 *  
 * @author Igor Vatolkin
 * @version $Id: StandardCorrelationCoefficient.java 243 2018-09-07 14:18:30Z frederik-h $
 */
public class StandardCorrelationCoefficient extends ClassificationQualityDoubleMeasureCalculator {

	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#setParameters(java.lang.String)
	 */
	public void setParameters(String parameterString) throws NodeException {
		// Does nothing
	}
	
	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateOneClassMeasureOnSongLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateOneClassMeasureOnSongLevel(ArrayList<Double> groundTruthRelationships, ArrayList<ClassifiedSongPartitions> predictedRelationships) throws NodeException {
		
		// Calculate the mean values for predicted and labeled instances
		double meanPredictedValue = 0.0d;
		double meanLabeledValue = 0.0d;
		for(int i=0;i<groundTruthRelationships.size();i++) {
			
			// Calculate the predicted value for this song (averaging among all partitions)
			Double currentPredictedValue = 0.0d;
			for(int j=0;j<predictedRelationships.get(i).getRelationships().length;j++) {
				currentPredictedValue += predictedRelationships.get(i).getRelationships()[j];
			}
			currentPredictedValue /= predictedRelationships.get(i).getRelationships().length;
			
			meanPredictedValue += currentPredictedValue;
			meanLabeledValue += groundTruthRelationships.get(i);
		}
		meanPredictedValue /= groundTruthRelationships.size();
		meanLabeledValue /= groundTruthRelationships.size();
		
		// Calculate the covariance and variance for predicted and labeled instances
		double covariance = 0.0d;
		double variancePredicted = 0.0d;
		double varianceLabeled = 0.0d;
		for(int i=0;i<groundTruthRelationships.size();i++) {
			
			// Calculate the predicted value for this song (averaging among all partitions)
			Double currentPredictedValue = 0.0d;
			for(int j=0;j<predictedRelationships.get(i).getRelationships().length;j++) {
				currentPredictedValue += predictedRelationships.get(i).getRelationships()[j];
			}
			currentPredictedValue /= predictedRelationships.get(i).getRelationships().length;
			
			covariance += (currentPredictedValue - meanPredictedValue)*(groundTruthRelationships.get(i) - meanLabeledValue);
			variancePredicted += (currentPredictedValue - meanPredictedValue)*(currentPredictedValue - meanPredictedValue);
			varianceLabeled += (groundTruthRelationships.get(i) - meanLabeledValue)*(groundTruthRelationships.get(i) - meanLabeledValue);
		}	
		covariance /= (groundTruthRelationships.size() - 1);
		variancePredicted /= (groundTruthRelationships.size() - 1);
		varianceLabeled /= (groundTruthRelationships.size() - 1);
		
		// Calculate the correlation coefficient
		double corrCoef = covariance / Math.sqrt(variancePredicted * varianceLabeled);
		
		// Prepare the result
		ValidationMeasureDouble[] correlationMeasure = new ValidationMeasureDouble[1];
		correlationMeasure[0] = new ValidationMeasureDouble(false);
		correlationMeasure[0].setId(300);
		correlationMeasure[0].setName("Standard correlation coefficient on song level");
		correlationMeasure[0].setValue(corrCoef);
		return correlationMeasure;
	}
	
	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateOneClassMeasureOnPartitionLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateOneClassMeasureOnPartitionLevel(ArrayList<Double> groundTruthRelationships, ArrayList<ClassifiedSongPartitions> predictedRelationships) throws NodeException {
		
		// Calculate the number of all partitions
		int overallPartitionNumber = 0;
		for(int i=0;i<groundTruthRelationships.size();i++) {
			overallPartitionNumber += predictedRelationships.get(i).getRelationships().length;
		}
		
		// Calculate the mean values for predicted and labeled instances
		double meanPredictedValue = 0.0d;
		double meanLabeledValue = 0.0d;
		for(int i=0;i<groundTruthRelationships.size();i++) {
			for(int j=0;j<predictedRelationships.get(i).getRelationships().length;j++) {
				meanPredictedValue += predictedRelationships.get(i).getRelationships()[j];
				meanLabeledValue += groundTruthRelationships.get(i);
			}
		}
		meanPredictedValue /= overallPartitionNumber;
		meanLabeledValue /= overallPartitionNumber;
		
		// Calculate the covariance and variance for predicted and labeled instances
		double covariance = 0.0d;
		double variancePredicted = 0.0d;
		double varianceLabeled = 0.0d;
		for(int i=0;i<groundTruthRelationships.size();i++) {
			for(int j=0;j<predictedRelationships.get(i).getRelationships().length;j++) {
				covariance += (predictedRelationships.get(i).getRelationships()[j] - meanPredictedValue)*(groundTruthRelationships.get(i) - meanLabeledValue);
				variancePredicted += (predictedRelationships.get(i).getRelationships()[j] - meanPredictedValue)*(predictedRelationships.get(i).getRelationships()[j] - meanPredictedValue);
				varianceLabeled += (groundTruthRelationships.get(i) - meanLabeledValue)*(groundTruthRelationships.get(i) - meanLabeledValue);
			}
		}	
		covariance /= (overallPartitionNumber - 1);
		variancePredicted /= (overallPartitionNumber - 1);
		varianceLabeled /= (overallPartitionNumber - 1);
		
		// Calculate the correlation coefficient
		double corrCoef = covariance / Math.sqrt(variancePredicted * varianceLabeled);
		
		// Prepare the result
		ValidationMeasureDouble[] correlationMeasure = new ValidationMeasureDouble[1];
		correlationMeasure[0] = new ValidationMeasureDouble(false);
		correlationMeasure[0].setId(300);
		correlationMeasure[0].setName("Standard correlation coefficient on partition level");
		correlationMeasure[0].setValue(corrCoef);
		return correlationMeasure;
	}

	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateMulticlassMeasureOnSongLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateMultiClassMeasureOnSongLevel(ArrayList<ClassifiedSongPartitions> groundTruthRelationships, ArrayList<ClassifiedSongPartitions> predictedRelationships) throws NodeException {
		throw new NodeException(this.getClass().getName() + " can be calculated only for binary classification tasks");
	}


	/**
	 * @see amuse.nodes.validator.interfaces.ClassificationQualityMeasureCalculatorInterface#calculateMulticlassMeasureOnPartitionLevel(java.util.ArrayList, java.util.ArrayList)
	 */
	public ValidationMeasureDouble[] calculateMultiClassMeasureOnPartitionLevel(ArrayList<ClassifiedSongPartitions> groundTruthRelationships, ArrayList<ClassifiedSongPartitions> predictedRelationships) throws NodeException {
		throw new NodeException(this.getClass().getName() + " can be calculated only for binary classification tasks");
	}


}

