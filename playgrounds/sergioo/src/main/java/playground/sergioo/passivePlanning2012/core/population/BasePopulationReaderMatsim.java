/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2011 by the members listed in the COPYING,        *
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

package playground.sergioo.passivePlanning2012.core.population;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.Route;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.population.*;
import org.matsim.core.population.routes.GenericRoute;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.utils.io.MatsimXmlParser;
import org.matsim.core.utils.io.UncheckedIOException;
import org.matsim.core.utils.misc.Time;
import org.xml.sax.Attributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Stack;

/**
 * A reader for plans files of MATSim according to <code>population_v5.dtd</code>.
 *
 * @author mrieser
 * @author balmermi
 */
public class BasePopulationReaderMatsim extends MatsimXmlParser implements PopulationReader {

	private final static String POPULATION = "population";
	private final static String PERSON = "person";
	private final static String PLAN = "plan";
	private final static String ACT = "act";
	private final static String LEG = "leg";
	private final static String ROUTE = "route";

	private final static String ATTR_POPULATION_DESC = "desc";
	private final static String ATTR_PERSON_ID = "id";
	private final static String ATTR_PERSON_SEX = "sex";
	private final static String ATTR_PERSON_AGE = "age";
	private final static String ATTR_PERSON_LICENSE = "license";
	private final static String ATTR_PERSON_CARAVAIL = "car_avail";
	private final static String ATTR_PERSON_EMPLOYED = "employed";
	private final static String ATTR_PLAN_SCORE = "score";
	private final static String ATTR_PLAN_TYPE = "type";
	private final static String ATTR_PLAN_SELECTED = "selected";
	private final static String ATTR_ACT_TYPE = "type";
	private final static String ATTR_ACT_X = "x";
	private final static String ATTR_ACT_Y = "y";
	private final static String ATTR_ACT_LINK = "link";
	private final static String ATTR_ACT_FACILITY = "facility";
	private final static String ATTR_ACT_STARTTIME = "start_time";
	private final static String ATTR_ACT_ENDTIME = "end_time";
	private final static String ATTR_ACT_MAXDUR = "max_dur";
	private final static String ATTR_LEG_MODE = "mode";
	private final static String ATTR_LEG_DEPTIME = "dep_time";
	private final static String ATTR_LEG_TRAVTIME = "trav_time";
	private final static String ATTR_LEG_ARRTIME = "arr_time";

	private final static String VALUE_YES = "yes";
	private final static String VALUE_NO = "no";
	private final static String VALUE_UNDEF = "undef";

	private final Scenario scenario;
	private final Population plans;

	private BasePersonImpl currperson = null;
	private BasePlanImpl currplan = null;
	private ActivityImpl curract = null;
	private LegImpl currleg = null;
	private Route currRoute = null;
	private String routeDescription = null;

	private Activity prevAct = null;

	public BasePopulationReaderMatsim(final Scenario scenario) {
		this.scenario = scenario;
		this.plans = scenario.getPopulation();
	}

	@Override
	public void startTag(final String name, final Attributes atts, final Stack<String> context) {
		if (POPULATION.equals(name)) {
			startPopulation(atts);
		} else if (PERSON.equals(name)) {
			startPerson(atts);
		} else if (PLAN.equals(name)) {
			startPlan(atts);
		} else if (ACT.equals(name)) {
			startAct(atts);
		} else if (LEG.equals(name)) {
			startLeg(atts);
		} else if (ROUTE.equals(name)) {
			startRoute(atts);
		} else {
			throw new RuntimeException(this + "[tag=" + name + " not known or not supported]");
		}
	}

	@Override
	public void endTag(final String name, final String content, final Stack<String> context) {
		if (PERSON.equals(name)) {
			if (this.plans instanceof PopulationImpl) {
				this.plans.addPerson(this.currperson);
			} else {
				this.plans.addPerson(this.currperson);
			}
			this.currperson = null;
		} else if (PLAN.equals(name)) {
			if (this.currplan.getPlanElements() instanceof ArrayList<?>) {
				((ArrayList<?>) this.currplan.getPlanElements()).trimToSize();
			}
			this.currplan = null;
		} else if (ACT.equals(name)) {
			this.prevAct = this.curract;
			this.curract = null;
		} else if (ROUTE.equals(name)) {
			this.routeDescription = content;
		}
	}

	/**
	 * Parses the specified plans file. This method calls {@link #parse(String)}.
	 *
	 * @param filename The name of the file to parse.
	 */
	@Override
	public void readFile(final String filename) throws UncheckedIOException {
		parse(filename);
	}

	private void startPopulation(final Attributes atts) {
		this.plans.setName(atts.getValue(ATTR_POPULATION_DESC));
	}

	private void startPerson(final Attributes atts) {
		String ageString = atts.getValue(ATTR_PERSON_AGE);
		int age = Integer.MIN_VALUE;
		if (ageString != null)
			age = Integer.parseInt(ageString);
		this.currperson = new BasePersonImpl(this.scenario.createId(atts.getValue(ATTR_PERSON_ID)));
		this.currperson.setSex(atts.getValue(ATTR_PERSON_SEX));
		this.currperson.setAge(age);
		this.currperson.setLicence(atts.getValue(ATTR_PERSON_LICENSE));
		this.currperson.setCarAvail(atts.getValue(ATTR_PERSON_CARAVAIL));
		String employed = atts.getValue(ATTR_PERSON_EMPLOYED);
		if (employed == null) {
			this.currperson.setEmployed(null);
		} else {
			this.currperson.setEmployed(VALUE_YES.equals(employed));
		}
	}

	private void startPlan(final Attributes atts) {
		String sel = atts.getValue(ATTR_PLAN_SELECTED);
		boolean selected;
		if (VALUE_YES.equals(sel)) {
			selected = true;
		}
		else if (VALUE_NO.equals(sel)) {
			selected = false;
		}
		else {
			throw new IllegalArgumentException(
					"Attribute 'selected' of Element 'Plan' is neither 'yes' nor 'no'.");
		}
		this.routeDescription = null;
		this.currplan = new BasePlanImpl(currperson); 
		if(selected)
			currperson.setSelectedPlan(currplan);
		String scoreString = atts.getValue(ATTR_PLAN_SCORE);
		if (scoreString != null) {
			double score = Double.parseDouble(scoreString);
			this.currplan.setScore(score);
		}

		atts.getValue(ATTR_PLAN_TYPE);
	}

	private void startAct(final Attributes atts) {
		Coord coord = null;
		if (atts.getValue(ATTR_ACT_LINK) != null) {
			Id linkId = this.scenario.createId(atts.getValue(ATTR_ACT_LINK));
			if ((atts.getValue(ATTR_ACT_X) != null) && (atts.getValue(ATTR_ACT_Y) != null)) {
				coord = this.scenario.createCoord(Double.parseDouble(atts.getValue(ATTR_ACT_X)), Double.parseDouble(atts.getValue(ATTR_ACT_Y)));
			}
			this.curract = new ActivityImpl(atts.getValue(ATTR_ACT_TYPE), coord, linkId);
			this.currplan.addActivity(curract);
		} else if ((atts.getValue(ATTR_ACT_X) != null) && (atts.getValue(ATTR_ACT_Y) != null)) {
			coord = this.scenario.createCoord(Double.parseDouble(atts.getValue(ATTR_ACT_X)), Double.parseDouble(atts.getValue(ATTR_ACT_Y)));
			this.curract = new ActivityImpl(atts.getValue(ATTR_ACT_TYPE), coord);
			this.currplan.addActivity(curract);
		} else {
			throw new IllegalArgumentException("In this version of MATSim either the coords or the link must be specified for an Act.");
		}
		this.curract.setStartTime(Time.parseTime(atts.getValue(ATTR_ACT_STARTTIME)));
		this.curract.setMaximumDuration(Time.parseTime(atts.getValue(ATTR_ACT_MAXDUR)));
		this.curract.setEndTime(Time.parseTime(atts.getValue(ATTR_ACT_ENDTIME)));
		String fId = atts.getValue(ATTR_ACT_FACILITY);
		if (fId != null) {
			this.curract.setFacilityId(this.scenario.createId(fId));
		}
		if (this.routeDescription != null) {
			Id startLinkId = null;
			if (this.prevAct.getLinkId() != null) {
				startLinkId = this.prevAct.getLinkId();
			}
			Id endLinkId = null;
			if (this.curract.getLinkId() != null) {
				endLinkId = this.curract.getLinkId();
			}
			if (this.currRoute instanceof GenericRoute) {
				((GenericRoute) this.currRoute).setRouteDescription(startLinkId, this.routeDescription.trim(), endLinkId);
			} else if (this.currRoute instanceof NetworkRoute) {
				List<Id> linkIds = NetworkUtils.getLinkIds(this.routeDescription, this.scenario);
				if (linkIds.size() > 0) {
					linkIds.remove(0);
				}
				if (linkIds.size() > 0) {
					linkIds.remove(linkIds.size() - 1);
				}
				((NetworkRoute) this.currRoute).setLinkIds(startLinkId, linkIds, endLinkId);
			} else {
				throw new RuntimeException("unknown route type: " + this.currRoute.getClass().getName());
			}
			this.routeDescription = null;
			this.currRoute = null;
		}
	}

	private void startLeg(final Attributes atts) {
		String mode = atts.getValue(ATTR_LEG_MODE).toLowerCase(Locale.ROOT);
		if (VALUE_UNDEF.equals(mode)) {
			mode = "undefined";
		}
		this.currleg = new LegImpl(mode.intern());
		this.currleg.setDepartureTime(Time.parseTime(atts.getValue(ATTR_LEG_DEPTIME)));
		this.currleg.setTravelTime(Time.parseTime(atts.getValue(ATTR_LEG_TRAVTIME)));
		this.currleg.setArrivalTime(Time.parseTime(atts.getValue(ATTR_LEG_ARRTIME)));
		this.currplan.addLeg(currleg);
	}

	private void startRoute(final Attributes atts) {
		this.currRoute = ((PopulationFactoryImpl) this.scenario.getPopulation().getFactory()).createRoute(this.currleg.getMode(), null, null);
		this.currleg.setRoute(this.currRoute);

		if (atts.getValue("vehicleRefId") != null && this.currRoute instanceof NetworkRoute ) {
			((NetworkRoute)this.currRoute).setVehicleId(this.scenario.createId(atts.getValue("vehicleRefId")));
		}

	}

}
