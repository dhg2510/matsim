/* *********************************************************************** *
 * project: matsim
 * PlanAgent.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2010 by the members listed in the COPYING,        *
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

package org.matsim.core.mobsim.framework;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Identifiable;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;

/**Design decisions:<ul>
 * <li>The concept that I found was that, at the end of a PlanElement, the agent got control from the relevant engine 
 * (activity, net, teleportation, ...).
 * After advancing the Plan, the agent would not return its control to the calling method, but insert itself directly into the 
 * Mobsim.
 * <strike>
 * <li>When trying around with object composition, we found that this does not work, since the PlanAgent delegate would only
 * schedule the delegate back into the Mobsim.  Discussing a bit, we found that this is a problem in other places as well
 * (e.g. context switches in window-driven systems, where the calling method needs to know about the context switch).  The decision
 * was thus to modify the design such that control about the agent is always returned to the calling method.  This is, however,
 * not yet implemented (nov'10).
 * <li>I attempted this, but abandoned it eventually.  A major problem is that an agent that cannot insert him/herself into 
 * the next process but returns to where it was called from somehow needs to pass to that location what he/she wants to do next.
 * Since we do not want to assume that every agent is a PlanAgent, using the full PlanAgent interface really is too strong.
 * This, however, indicates that one would need some "return code", e.g. something like
 * <pre>
 * enum NextAction { StartActivity, StartLeg, ...}
 * </pre>
 * In the end, I considered this too much infrastructure, because if we have infrastructure, we can as well have a back pointer to
 * the "original agent" as part of the delegate.  kai, dec'10 
 * </strike>
 * </ul>
 * 
 * <strike>Towards a concept for status pointers:<ul>
 * <li> Let us start with the PlanElements Iterator.
 * </li><li> Re-using standard iterators does not make sense since those are always <i>between</i> elements,
 * but we need a "current" here.
 * </li><li> So we could say getPrev and getNext.
 * </li><li> How do we insert and remove?  In ArrayList, the Iterator fails if there is insert/remove
 * outside of the iterator.  But here, we cannot move the iterator away from its current position.
 * </li>
 * Some useful version is currently implemented, experimentally, into the experimental version of the WithinDayAgent.  If
 * necessary, disscuss there.  kai, dec'10
 * </strike>
 * </ul>
 * In terms of design, the assumption is that the plan remains unchanged in the mobsim, at least from the
 * perspective of the iterations: plan = genotype, execution = phenotype.  Therefore, <i>already Christoph's implementation
 * violates the specification</i>.
 * <br/>
 * That is, we need to copy the plan, or at least copy it before modification ("getModifiablePlan").  This, however,
 * would immensely simplify the design, since we could, at this point, also shorten the plan to where the agent currently is,
 * meaning that all replanning algos should also work much better.
 * <br/>
 * Memory considerations could be addressed by lazily delaying this plans copying to the point where the modifiable
 * plan is needed.
 * <p/>
 * <strike>Should add Identifiable to this interface; could then replace many PersonAgent by PlanAgent.  done.  kai, dec'10</strike>
 *
 * @author dgrether
 * @author nagel
 *
 */
public interface PlanAgent extends NetworkAgent, Identifiable, Initializable {

	/**
	 * The time the agent wants to depart from an Activity. If the agent is currently driving,
	 * the return value cannot be interpreted (e.g. it is not defined if it is the departure time
	 * from the previous activity, or from the next one).
	 *
	 * @return the time when the agent wants to depart from an activity.
	 */
	public double getActivityEndTime();
	/* there is no corresponding setter, as the implementation should set the the corresponding time
	 * internally, e.g. in legEnds().
	 */
	// yyyy getDepartureTimeFromActivity()  [[since there is also a linkDepartureTime of the
	// queue sim, and possibly a departure time of a leg]].  kai, jan'10
	// But the transit driver does not have an activity (?!). kai, apr'10
	// Re-named this into this weird method name since I got confused once more.  Would have been a lot easier if
	// bus drivers either would have activities, or would not be PersonAgents.  kai, oct'10

	/**
	 * Informs the agent that the activity has ended.  The agent is responsible for what comes next.
	 *
	 * @param now
	 */
	public void endActivityAndAssumeControl(final double now);

	/**
	 * Informs the agent that the leg has ended.  The agent is responsible for what comes next.
	 *
	 * @param now the current time in the simulation
	 */
	public void endLegAndAssumeControl(final double now);

	public PlanElement getCurrentPlanElement() ;
	// if this does not make sense for a class, then the class is maybe not a "Plan"Agent.  kai, may'10
	
	public PlanElement getNextPlanElement() ;

	/**
	 * @return "(Leg) getCurrentPlanElement()" if the current plan element is a leg, otherwise null.
	 */
	@Deprecated // try to use getCurrentPlanElement()
	public Leg getCurrentLeg();

	/**
	 * @return "(Activity) getCurrentPlanElement()" if the current plan element is an activity, otherwise null.
	 */
	@Deprecated // try to use getCurrentPlanElement()
	public Activity getCurrentActivity();

	/** Design thoughts:<ul>
	* <li>"Teleportation" certainly does NOT belong into a vehicle.  Also not into the driver.
	* Might go directly into the person, as some kind of minimal mobsim convenience method
	* (although I am not convinced).  kai, jan/apr'10
	* <li>zzzz Teleportation should from my point of view not be included in a data class like Person dg apr'10
	* <li>This is here since a in a normal leg, the driver moves from node to node and eventually is at the destination.
	* With teleportation, this does not work, and so another setting method needs to be found.
	* Can't say how this is done with transit.  kai, aug'10
	* <li>There needs to be some method that tells the agent that a teleportation has happened, similar to "moveOverNode".
	* Could be separated out to a "teleportation" agent, but can as well leave it here.
	* The name may still be improved.  kai, nov'10
	* </ul>
	*/
	public void notifyTeleportToLink(final Id linkId);

	/**
	 * Design thoughts:<ul>
	 * <li> yyyy Many methods call PersonAgent.getPerson.getSelectedPlan(). This method should replace
	 * all those calls. This might be useful, for multiple reasons.<br>
	 * E.g. an unmodifyable plan could be returned or a copy of the selected plan, which then could
	 * be edited by a within day replanning module.  cdobler, feb'11
	 * </ul>
	 */
	public Plan getExecutedPlan();
}
