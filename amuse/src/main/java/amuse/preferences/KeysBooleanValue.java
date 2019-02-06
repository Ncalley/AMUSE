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
 * Creation date: 01.09.2008
 */
package amuse.preferences;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Level;

import amuse.util.AmuseLogger;

/**
 * This enumeration lists all available keys to get presistent Booleans from <code>AmusePreferences</code>.
 * Unlike his related enumerations this enum has no <code>isValid()</code> method.
 * @author Clemens Waeltken
 * @version $Id: KeysBooleanValue.java 230 2018-04-27 12:14:47Z frederik-h $
 */
public enum KeysBooleanValue {

    REDUCE_TO_MONO,
    USE_DOWNSAMPLING,
    SPLIT_WAVE,
    USE_GRID_CLASSIFIER,
    USE_GRID_EXTRACTOR,
    USE_GRID_TRAINER,
    USE_GRID_PROCESSOR,
    USE_GRID_VALIDATOR,
    USE_GRID_OPTIMIZER,
    MARK_CURRENT_TIME_IN_ANNOTATION_AUDIOSPECTRUM;

    /**
     * This method is used to determine and get default values for any key.
     * @return the default value for this key
     */
    protected Boolean getDefaultValue() {
        switch (this) {
            case USE_GRID_CLASSIFIER:
            case USE_GRID_EXTRACTOR:
            case USE_GRID_TRAINER:
            case USE_GRID_PROCESSOR:
            case USE_GRID_VALIDATOR:
            case USE_GRID_OPTIMIZER:
            case MARK_CURRENT_TIME_IN_ANNOTATION_AUDIOSPECTRUM:
                return false;
            case REDUCE_TO_MONO:
                return true;
            case USE_DOWNSAMPLING:
                return true;
            case SPLIT_WAVE:
                return true;
            default:
                AmuseLogger.write(this.getClass().getName(), Level.DEBUG,
                        this.toString() + ": no default value set!");
                return false;
        }
    }

    @Override
    public String toString() {
        return "BOOLEAN_" + super.toString();
    }
    
	/**
	 * @param key String representation of a KeysStringValue element.
	 * @return The comment for this key.
	 */
	public static String getCommentFor(String key) {
		return commentsMap.get(key);
	}
	
	private static Map<String, String> commentsMap = getCommentsMap();
	
	/**
	 * This map holds all comments for each Key.
	 * @return
	 */
	private static Map<String, String> getCommentsMap() {
		Map<String, String> map = new HashMap<String, String>();
		map.put(REDUCE_TO_MONO.toString(), "TRUE to reduce music files from stereo to mono:");
		map.put(SPLIT_WAVE.toString(), "TRUE to enable splitting of large wave files:");
		map.put(USE_DOWNSAMPLING.toString(), "TRUE to reduce sampling quality of music files:");
		map.put(USE_GRID_EXTRACTOR.toString(), "TRUE to use grid for feature extraction:");
		map.put(USE_GRID_PROCESSOR.toString(), "TRUE to use grid for feature processing:");
		map.put(USE_GRID_TRAINER.toString(), "TRUE to use grid for classification training:");
		map.put(USE_GRID_CLASSIFIER.toString(), "TRUE to use grid for classification:");
		map.put(USE_GRID_VALIDATOR.toString(), "TRUE to use grid for validation:");
		map.put(USE_GRID_OPTIMIZER.toString(), "TRUE to use grid for optimization:");
		map.put(MARK_CURRENT_TIME_IN_ANNOTATION_AUDIOSPECTRUM.toString(), "TRUE to display a beam on the audio spectrum in the annotation editor that tracks the time:");
		return map;
	}
}
