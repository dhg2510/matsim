/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2016 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

/**
 * 
 */
package playground.jbischoff.ffcs.data;

import java.util.LinkedHashMap;
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.vehicles.Vehicle;

/**
 * @author  jbischoff
 *
 */
/**
 *
 */
public class CarsharingData {
    private final Map<Id<Vehicle>, Id<Link>> vehiclesStartLocations = new LinkedHashMap<>();

    /**
	 * @return the vehiclesStartLocations
	 */
	public Map<Id<Vehicle>, Id<Link>> getVehiclesStartLocations() {
		return vehiclesStartLocations;
	}
	
	public void addVehicle(Id<Vehicle> vehicle, Id<Link> startLocationLinkId){
		this.vehiclesStartLocations.put(vehicle, startLocationLinkId);
	}
}
