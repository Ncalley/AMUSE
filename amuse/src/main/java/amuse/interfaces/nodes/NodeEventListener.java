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
 * Creation date: 11.03.2008
 */ 
package amuse.interfaces.nodes;



/**
 * Listeners of NodeEvent should implement this interface
 * 
 * @author Igor Vatolkin
 * @version $Id: NodeEventListener.java 197 2017-08-11 12:15:34Z frederik-h $
 */
public interface NodeEventListener {
	
	/**
	 * Proceeds a node event
	 * @param event NodeEvent
	 */
	public void processEvent(NodeEvent event);
}
