/* *********************************************************************** *
 * project: org.matsim.*
 * CadytsController.java
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

package playground.dziemke.cadyts;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.contrib.cadyts.car.CadytsCarScoring;
import org.matsim.contrib.cadyts.car.CadytsContext;
import org.matsim.contrib.cadyts.car.CadytsPlanChanger;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.StrategyConfigGroup.StrategySettings;
import org.matsim.core.controler.Controler;
import org.matsim.core.replanning.PlanStrategy;
import org.matsim.core.replanning.PlanStrategyFactory;
import org.matsim.core.replanning.PlanStrategyImpl;
import org.matsim.core.scoring.ScoringFunction;
import org.matsim.core.scoring.ScoringFunctionAccumulator;
import org.matsim.core.scoring.ScoringFunctionFactory;
import org.matsim.core.scoring.functions.CharyparNagelActivityScoring;
import org.matsim.core.scoring.functions.CharyparNagelAgentStuckScoring;
import org.matsim.core.scoring.functions.CharyparNagelLegScoring;
import org.matsim.core.scoring.functions.CharyparNagelScoringParameters;

public class CadytsControllerWithScoring {
	private final static Logger log = Logger.getLogger(CadytsControllerWithScoring.class);
	
	public static void main(String[] args) {
		final Config config = ConfigUtils.loadConfig(args[0]) ;
		
		StrategySettings stratSets = new StrategySettings(new IdImpl(2));
		stratSets.setModuleName("ccc");
		stratSets.setProbability(1.0);
		config.strategy().addStrategySettings(stratSets);
		
		final Controler controler = new Controler(config);
		
		final CadytsContext cContext = new CadytsContext(controler.getConfig());
		
		controler.addControlerListener(cContext);
		
		controler.addPlanStrategyFactory("ccc", new PlanStrategyFactory() {
			@Override
			public PlanStrategy createPlanStrategy(Scenario scenario2, EventsManager events2) {
				final CadytsPlanChanger planSelector = new CadytsPlanChanger(cContext);

				planSelector.setCadytsWeight(30.*scenario2.getConfig().planCalcScore().getBrainExpBeta() ) ;
				// set cadyts weight very high = close to brute force
				
				return new PlanStrategyImpl(planSelector);
			}
		});
		
		
		final CharyparNagelScoringParameters params = new CharyparNagelScoringParameters(config.planCalcScore());
		
		controler.setScoringFunctionFactory(new ScoringFunctionFactory() {
			@Override
			public ScoringFunction createNewScoringFunction(Plan plan) {
				
				ScoringFunctionAccumulator scoringFunctionAccumulator = new ScoringFunctionAccumulator();
				scoringFunctionAccumulator.addScoringFunction(new CharyparNagelLegScoring(params, controler.getScenario().getNetwork()));
				scoringFunctionAccumulator.addScoringFunction(new CharyparNagelActivityScoring(params)) ;
				scoringFunctionAccumulator.addScoringFunction(new CharyparNagelAgentStuckScoring(params));

				final CadytsCarScoring scoringFunction = new CadytsCarScoring(plan, config, cContext);
				final double cadytsScoringWeight = 1.0;
				scoringFunction.setWeightOfCadytsCorrection(cadytsScoringWeight) ;
				scoringFunctionAccumulator.addScoringFunction(scoringFunction );

				return scoringFunctionAccumulator;
			}
		}) ;
				
		
//		controler.addControlerListener(new KaiAnalysisListener());
		
		controler.run();
	}
}
