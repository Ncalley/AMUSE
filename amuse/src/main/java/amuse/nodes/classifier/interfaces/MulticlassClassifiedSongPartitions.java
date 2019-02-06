/** 
 * This file is part of AMUSE framework (Advanced MUsic Explorer).
 * 
 * Copyright 2006-2011 by code authors
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
 * Creation date: 04.01.2011
 */
package amuse.nodes.classifier.interfaces;

/**
 * This class saves the multiclass results of a classification task
 * 
 * @author Igor Vatolkin
 * @version $Id: MulticlassClassifiedSongPartitions.java 197 2017-08-11 12:15:34Z frederik-h $
 */
public class MulticlassClassifiedSongPartitions extends ClassifiedSongPartitions {
	
	/** Assigned labels/categories */
	final String[] labels;
	
	/**
	 * Constructor
	 * @param pathToMusicSong Music song which was classified
	 * @param startMs Starts of the partitions in ms
	 * @param endMs Ends of the partitions in ms
	 * @param labels Assigned label/category
	 * @param relationships Corresponding relationships for the partitions
	 */
	public MulticlassClassifiedSongPartitions(String pathToMusicSong, int songId, Double[] startMs, Double[] endMs, String[] labels,
			Double[] relationships) {
		super(pathToMusicSong, songId, startMs, endMs, relationships);
		this.labels = labels;
	}

	/**
	 * @return the label
	 */
	public String[] getLabels() {
		return labels;
	}

}
