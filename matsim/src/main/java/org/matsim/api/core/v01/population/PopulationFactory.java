/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
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

package org.matsim.api.core.v01.population;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.core.api.internal.MatsimFactory;

/**
 * @author dgrether
 */
public interface PopulationFactory extends MatsimFactory {

	Person createPerson(Id id);

	Plan createPlan();

	Activity createActivityFromCoord(String actType, Coord coord);

//	Activity createActivityFromFacilityId(String actType, Id facilityId);
	// disabled until everything else is figured out

	Activity createActivityFromLinkId(String actType, Id linkId);

	Leg createLeg(String legMode);

	/**
	 * Creates a new Route object
	 * @param currentRouteLinkIds List of Ids including the start and the end Link Id of the route's links
	 * @return a BasicRoute Object with the links set accordingly
	 * @deprecated needs to be verified // TODO [MR] verify
	 */
//	Route createRoute(Id startLinkId, Id endLinkId, final List<Id> currentRouteLinkIds);
	// disabled until everything else is figured out

}
