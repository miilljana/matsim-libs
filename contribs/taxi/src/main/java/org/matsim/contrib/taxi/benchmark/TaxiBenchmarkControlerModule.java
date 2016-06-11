/*
 *  *********************************************************************** *
 *  * project: org.matsim.*
 *  * DefaultControlerModules.java
 *  *                                                                         *
 *  * *********************************************************************** *
 *  *                                                                         *
 *  * copyright       : (C) 2014 by the members listed in the COPYING, *
 *  *                   LICENSE and WARRANTY file.                            *
 *  * email           : info at matsim dot org                                *
 *  *                                                                         *
 *  * *********************************************************************** *
 *  *                                                                         *
 *  *   This program is free software; you can redistribute it and/or modify  *
 *  *   it under the terms of the GNU General Public License as published by  *
 *  *   the Free Software Foundation; either version 2 of the License, or     *
 *  *   (at your option) any later version.                                   *
 *  *   See also COPYING, LICENSE and WARRANTY file                           *
 *  *                                                                         *
 *  * ***********************************************************************
 */

package org.matsim.contrib.taxi.benchmark;

import org.matsim.api.core.v01.TransportMode;
import org.matsim.contrib.dvrp.trafficmonitoring.VrpTravelTimeModules;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.events.EventsManagerModule;
import org.matsim.core.mobsim.DefaultMobsimModule;
import org.matsim.core.replanning.StrategyManagerModule;
import org.matsim.core.router.TripRouterModule;
import org.matsim.core.router.costcalculators.TravelDisutilityModule;
import org.matsim.core.scoring.ExperiencedPlansModule;
import org.matsim.core.scoring.functions.CharyparNagelScoringFunctionModule;
import org.matsim.core.trafficmonitoring.FreeSpeedTravelTime;


public class TaxiBenchmarkControlerModule
    extends AbstractModule
{
    @Override
    public void install()
    {
        install(new EventsManagerModule());
        install(new DefaultMobsimModule());
        //install(new TravelTimeCalculatorModule());
        install(new TravelDisutilityModule());
        install(new CharyparNagelScoringFunctionModule());
        install(new ExperiencedPlansModule());
        install(new TripRouterModule());
        install(new StrategyManagerModule());
        //install(new LinkStatsModule());
        //install(new VolumesAnalyzerModule());
        //install(new LegHistogramModule());
        //install(new LegTimesModule());
        //install(new TravelDistanceStatsModule());
        //install(new ScoreStatsModule());
        //install(new CountsModule());
        //install(new PtCountsModule());
        //install(new VspPlansCleanerModule());
        //install(new SnapshotWritersModule());

        addTravelTimeBinding(TransportMode.car).toInstance(new FreeSpeedTravelTime());
        addTravelTimeBinding(VrpTravelTimeModules.DVRP_ESTIMATED)
                .toInstance(new FreeSpeedTravelTime());
    }
}
