/* *********************************************************************** *
 * project: org.matsim.*
 * FacilitiesReaderMatsimV1.java
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

package org.matsim.facilities;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.IdentityTransformation;
import org.matsim.core.utils.io.MatsimXmlParser;
import org.matsim.core.utils.misc.Time;
import org.matsim.utils.objectattributes.AttributeConverter;
import org.matsim.utils.objectattributes.attributable.AttributesXmlReaderDelegate;
import org.xml.sax.Attributes;

import java.util.Map;
import java.util.Stack;

/**
 * A reader for facilities-files of MATSim according to <code>facilities_v1.dtd</code>.
 *
 * @author mrieser
 * @author balmermi
 */
public class FacilitiesReaderMatsimV1 extends MatsimXmlParser {

    private final static String FACILITIES = "facilities";
    private final static String FACILITY = "facility";
    private final static String ACTIVITY = "activity";
    private final static String CAPACITY = "capacity";
    private final static String OPENTIME = "opentime";
    private static final String ATTRIBUTES = "attributes";
    private static final String ATTRIBUTE = "attribute";

    private final ActivityFacilities facilities;
    private final ActivityFacilitiesFactory factory;
    private final AttributesXmlReaderDelegate attributesReader;
    private ActivityFacility currfacility = null;
    private ActivityOption curractivity = null;
    private org.matsim.utils.objectattributes.attributable.Attributes currAttributes;

    private final CoordinateTransformation coordinateTransformation;

    public FacilitiesReaderMatsimV1(final Scenario scenario) {
        this(new IdentityTransformation(), scenario);
    }

    public FacilitiesReaderMatsimV1(
            final CoordinateTransformation coordinateTransformation,
            final Scenario scenario) {
        this.coordinateTransformation = coordinateTransformation;
        this.facilities = scenario.getActivityFacilities();
        this.factory = this.facilities.getFactory();
        this.attributesReader = new AttributesXmlReaderDelegate();
        this.currAttributes = null;
    }

    public void putAttributeConverter(Class<?> clazz, AttributeConverter<?> converter) {
        this.attributesReader.putAttributeConverter(clazz, converter);
    }

    public void putAttributeConverters(Map<Class<?>, AttributeConverter<?>> converters) {
        this.attributesReader.putAttributeConverters(converters);
    }

    @Override
    public void startTag(final String name, final org.xml.sax.Attributes atts, final Stack<String> context) {
        if (FACILITIES.equals(name)) {
            startFacilities(atts);
        } else if (FACILITY.equals(name)) {
            startFacility(atts);
        } else if (ACTIVITY.equals(name)) {
            startActivity(atts);
        } else if (CAPACITY.equals(name)) {
            startCapacity(atts);
        } else if (OPENTIME.equals(name)) {
            startOpentime(atts);
        } else if (ATTRIBUTE.equals(name)) {
            this.attributesReader.startTag(name, atts, context, this.currAttributes);
        } else if (ATTRIBUTES.equals(name)) {
            currAttributes = this.currfacility.getAttributes();
            attributesReader.startTag(name, atts, context, currAttributes);
        }
    }

    @Override
    public void endTag(final String name, final String content, final Stack<String> context) {
        if (FACILITY.equals(name)) {
            this.currfacility = null;
        } else if (ACTIVITY.equals(name)) {
            this.curractivity = null;
        } else if (ATTRIBUTES.equalsIgnoreCase(name)) {
            this.currAttributes = null;
        } else if (ATTRIBUTE.equalsIgnoreCase(name)) {
            this.attributesReader.endTag(name, content, context);
        }
    }

    private void startFacilities(final Attributes atts) {
        this.facilities.setName(atts.getValue("name"));
        if (atts.getValue("aggregation_layer") != null) {
            Logger.getLogger(FacilitiesReaderMatsimV1.class).warn("aggregation_layer is deprecated.");
        }
    }

    private void startFacility(final Attributes atts) {
        this.currfacility =
                this.factory.createActivityFacility(
                        Id.create(atts.getValue("id"), ActivityFacility.class),
                        coordinateTransformation.transform(
                                new Coord(
                                        Double.parseDouble(atts.getValue("x")),
                                        Double.parseDouble(atts.getValue("y")))));
        this.facilities.addActivityFacility(this.currfacility);
        String value = atts.getValue("linkId");
        if (value != null) {
            ((ActivityFacilityImpl) this.currfacility).setLinkId(Id.create(value, Link.class));
        }
        ((ActivityFacilityImpl) this.currfacility).setDesc(atts.getValue("desc"));
    }

    private void startActivity(final Attributes atts) {
        this.curractivity = this.factory.createActivityOption(atts.getValue("type"));
        this.currfacility.addActivityOption(this.curractivity);
    }

    private void startCapacity(final Attributes atts) {
        double cap = Double.parseDouble(atts.getValue("value"));
        this.curractivity.setCapacity(cap);
    }

    private void startOpentime(final Attributes atts) {
        this.curractivity.addOpeningTime(new OpeningTimeImpl(Time.parseTime(atts.getValue("start_time")), Time.parseTime(atts.getValue("end_time"))));
    }


}
