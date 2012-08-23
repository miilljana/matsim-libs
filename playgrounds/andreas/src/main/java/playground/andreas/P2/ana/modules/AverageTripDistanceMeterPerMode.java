/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2012 by the members listed in the COPYING,        *
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

package playground.andreas.P2.ana.modules;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.api.experimental.events.LinkEnterEvent;
import org.matsim.core.api.experimental.events.handler.LinkEnterEventHandler;
import org.matsim.core.events.PersonEntersVehicleEvent;
import org.matsim.core.events.PersonLeavesVehicleEvent;
import org.matsim.core.events.TransitDriverStartsEvent;
import org.matsim.core.events.handler.PersonEntersVehicleEventHandler;
import org.matsim.core.events.handler.PersonLeavesVehicleEventHandler;
import org.matsim.core.events.handler.TransitDriverStartsEventHandler;


/**
 * Calculates the average trip distance per ptModes specified. A trip starts by entering a vehicle and end by leaving one.
 * 
 * @author aneumann
 *
 */
public class AverageTripDistanceMeterPerMode extends AbstractPAnalyisModule implements TransitDriverStartsEventHandler, PersonEntersVehicleEventHandler, PersonLeavesVehicleEventHandler, LinkEnterEventHandler{
	
	private final static Logger log = Logger.getLogger(AverageTripDistanceMeterPerMode.class);
	
	private Network network;
	private HashMap<Id, String> vehId2ptModeMap;
	private HashMap<String, Double> ptMode2MeterTravelledMap;
	private HashMap<String, Integer> ptMode2TripCountMap;
	private HashMap<Id,HashMap<Id,Double>> vehId2AgentId2DistanceTravelledInMeterMap = new HashMap<Id, HashMap<Id, Double>>();

	
	public AverageTripDistanceMeterPerMode(String ptDriverPrefix, Network network){
		super("AverageTripDistanceMeterPerMode",ptDriverPrefix);
		this.network = network;
		log.info("enabled");
	}

	@Override
	public String getResult() {
		StringBuffer strB = new StringBuffer();
		for (String ptMode : this.ptModes) {
			strB.append(", " + (this.ptMode2MeterTravelledMap.get(ptMode) / this.ptMode2TripCountMap.get(ptMode)));
		}
		return strB.toString();
	}
	
	@Override
	public void reset(int iteration) {
		this.vehId2ptModeMap = new HashMap<Id, String>();
		this.ptMode2MeterTravelledMap = new HashMap<String, Double>();
		this.ptMode2TripCountMap = new HashMap<String, Integer>();
		this.vehId2AgentId2DistanceTravelledInMeterMap = new HashMap<Id, HashMap<Id,Double>>();
	}

	@Override
	public void handleEvent(TransitDriverStartsEvent event) {
		String ptMode = this.lineIds2ptModeMap.get(event.getTransitLineId());
		if (ptMode == null) {
			log.warn("Should not happen");
			ptMode = "no valid pt mode found";
		}
		this.vehId2ptModeMap.put(event.getVehicleId(), ptMode);
	}

	@Override
	public void handleEvent(PersonEntersVehicleEvent event) {
		if (this.vehId2AgentId2DistanceTravelledInMeterMap.get(event.getVehicleId()) == null) {
			this.vehId2AgentId2DistanceTravelledInMeterMap.put(event.getVehicleId(), new HashMap<Id, Double>());
		}
		
		if(!event.getPersonId().toString().startsWith(ptDriverPrefix)){
			this.vehId2AgentId2DistanceTravelledInMeterMap.get(event.getVehicleId()).put(event.getPersonId(), new Double(0.0));
		}
	}
	
	@Override
	public void handleEvent(PersonLeavesVehicleEvent event) {
		if(!event.getPersonId().toString().startsWith(ptDriverPrefix)){
			
			String ptMode = this.vehId2ptModeMap.get(event.getVehicleId());
			if (ptMode == null) {
				ptMode = "nonPtMode";
			}
			if (ptMode2MeterTravelledMap.get(ptMode) == null) {
				ptMode2MeterTravelledMap.put(ptMode, new Double(0.0));
			}
			if (ptMode2TripCountMap.get(ptMode) == null) {
				ptMode2TripCountMap.put(ptMode, new Integer(0));
			}
			
			this.ptMode2MeterTravelledMap.put(ptMode, new Double(this.ptMode2MeterTravelledMap.get(ptMode) + this.vehId2AgentId2DistanceTravelledInMeterMap.get(event.getVehicleId()).get(event.getPersonId()).doubleValue()));
			this.ptMode2TripCountMap.put(ptMode, new Integer(this.ptMode2TripCountMap.get(ptMode) + 1));
		}
	}

	@Override
	public void handleEvent(LinkEnterEvent event) {
		double newValue = this.network.getLinks().get(event.getLinkId()).getLength();
		
		for (Id agentId : this.vehId2AgentId2DistanceTravelledInMeterMap.get(event.getVehicleId()).keySet()) {
			double oldValue = this.vehId2AgentId2DistanceTravelledInMeterMap.get(event.getVehicleId()).get(agentId).doubleValue();
			this.vehId2AgentId2DistanceTravelledInMeterMap.get(event.getVehicleId()).put(agentId, new Double(oldValue + newValue));
		}
	}

}
