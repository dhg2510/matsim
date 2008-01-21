/* *********************************************************************** *
 * project: org.matsim.*
 * NewPlansControler.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007 by the members listed in the COPYING,        *
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

package playground.yu.ivtch;

import org.matsim.config.Config;
import org.matsim.gbl.Gbl;
import org.matsim.mobsim.QueueNetworkLayer;
import org.matsim.network.MatsimNetworkReader;
import org.matsim.plans.MatsimPlansReader;
import org.matsim.plans.Plans;
import org.matsim.plans.PlansReaderI;
import org.matsim.world.World;

import playground.marcel.MyRuns;

/**
 * test of NewAgentPtPlan
 * 
 * @author ychen
 * 
 */
public class NewPlansControler {

	public static void main(final String[] args) {
		final String netFilename = "./test/yu/ivtch/input/network.xml";
		final String plansFilename = "./test/yu/ivtch/input/all_plans.xml.gz";

		World world = Gbl.getWorld();
		@SuppressWarnings("unused")
		Config config = Gbl
				.createConfig(new String[] { "./test/yu/ivtch/config_for_make10pctSample.xml" });

		QueueNetworkLayer network = new QueueNetworkLayer();
		new MatsimNetworkReader(network).readFile(netFilename);
		world.setNetworkLayer(network);

		Plans population = new Plans();
		NewAgentPtPlan nap = new NewAgentPtPlan(population);
		population.addAlgorithm(nap);
		PlansReaderI plansReader = new MatsimPlansReader(population);
		plansReader.readFile(plansFilename);
		world.setPopulation(population);
		population.runAlgorithms();
		nap.writeEndPlans();
	}
}
