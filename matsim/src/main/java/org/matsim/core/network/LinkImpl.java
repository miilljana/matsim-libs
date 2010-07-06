/* *********************************************************************** *
 * project: org.matsim.*
 * AbstractLink.java
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

package org.matsim.core.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.utils.geometry.CoordImpl;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.core.utils.misc.Time;
import org.matsim.world.AbstractLocation;

public class LinkImpl extends AbstractLocation implements Link {

	private final static Logger log = Logger.getLogger(LinkImpl.class);

	//////////////////////////////////////////////////////////////////////
	// member variables
	//////////////////////////////////////////////////////////////////////

	protected Node from = null;
	protected Node to = null;

	protected double length = Double.NaN;
	protected double freespeed = Double.NaN;
	protected double capacity = Double.NaN;
	protected double nofLanes = Double.NaN;

	protected HashSet<String> allowedModes = new HashSet<String>();

	private double flowCapacity;

	protected String type = null;

	protected String origid = null;

	protected double euklideanDist;

	private static int fsWarnCnt = 0 ;
	private static int cpWarnCnt = 0 ;
	private static int plWarnCnt = 0 ;
	private static int lengthWarnCnt = 0;
	private static int loopWarnCnt = 0 ;
	private static int maxFsWarnCnt = 1;
  private static int maxCpWarnCnt = 1;
  private static int maxPlWarnCnt = 1;
  private static int maxLengthWarnCnt = 1;
  private static int maxLoopWarnCnt = 1;



	protected LinkImpl(final Id id, final Node from, final Node to,
			final NetworkLayer network, final double length, final double freespeed, final double capacity, final double lanes) {
		super(network, id,
				new CoordImpl(0.5*(from.getCoord().getX() + to.getCoord().getX()), 0.5*(from.getCoord().getY() + to.getCoord().getY()))
		);
		this.from = from;
		this.to = to;

		// set attributes and do semantic checks
		this.allowedModes.add(TransportMode.car);
		this.setLength(length);
		this.setFreespeed(freespeed);
		this.setCapacity(capacity);
		this.setNumberOfLanes(lanes);

		this.euklideanDist = CoordUtils.calcDistance(this.from.getCoord(), this.to.getCoord());

		if (this.from.equals(this.to) && (loopWarnCnt < maxLoopWarnCnt)) {
			loopWarnCnt++ ;
			log.warn("[from=to=" + this.to + " link is a loop]");
			log.warn(Gbl.ONLYONCE);
		}
	}

	private void calculateFlowCapacity() {
		this.flowCapacity = this.capacity / ((NetworkLayer)this.getLayer()).getCapacityPeriod();
		this.checkCapacitiySemantics();
	}

	private void checkCapacitiySemantics() {
		/*
		 * I see no reason why a freespeed and a capacity of zero should not be
		 * allowed! joh 9may2008
		 */
		if ((this.capacity <= 0.0) && (cpWarnCnt < maxCpWarnCnt) ) {
			cpWarnCnt++ ;
			log.warn("[capacity=" + this.capacity + " of link id " + this.getId() + " may cause problems. Future occurences of this warning are suppressed.]");
		}
	}

	private void checkFreespeedSemantics() {
		if ((this.freespeed <= 0.0) && (fsWarnCnt < maxFsWarnCnt) ) {
			fsWarnCnt++ ;
			log.warn("[freespeed=" + this.freespeed + " of link id " + this.getId() + " may cause problems. Future occurences of this warning are suppressed.]");
		}
	}

	private void checkNumberOfLanesSemantics(){
		if ((this.nofLanes < 1) && (plWarnCnt < maxPlWarnCnt) ) {
			plWarnCnt++ ;
			log.warn("[permlanes=" + this.nofLanes + " of link id " + this.getId() +" may cause problems. Future occurences of this warning are suppressed.]");
		}
	}

	private void checkLengthSemantics(){
		if ((this.getLength() <= 0.0) && (lengthWarnCnt < maxLengthWarnCnt)) {
			lengthWarnCnt++;
			log.warn("[length=" + this.length + " of link id " + this.getId() + " may cause problems. Future occurences of this warning are suprressed.]");
		}
	}

	//////////////////////////////////////////////////////////////////////
	// calc methods
	//////////////////////////////////////////////////////////////////////

	@Override
	public final double calcDistance(final Coord coord) {
		// yyyy should, in my view, call the generalized utils method. kai, jul09
		Coord fc = this.from.getCoord();
		Coord tc =  this.to.getCoord();
		double tx = tc.getX();    double ty = tc.getY();
		double fx = fc.getX();    double fy = fc.getY();
		double zx = coord.getX(); double zy = coord.getY();
		double ax = tx-fx;        double ay = ty-fy;
		double bx = zx-fx;        double by = zy-fy;
		double la2 = ax*ax + ay*ay;
		double lb2 = bx*bx + by*by;
		if (la2 == 0.0) {  // from == to
			return Math.sqrt(lb2);
		}
		double xla = ax*bx+ay*by; // scalar product
		if (xla <= 0.0) {
			return Math.sqrt(lb2);
		}
		if (xla >= la2) {
			double cx = zx-tx;
			double cy = zy-ty;
			return Math.sqrt(cx*cx+cy*cy);
		}
		// lb2-xla*xla/la2 = lb*lb-x*x
		double tmp = xla*xla;
		tmp = tmp/la2;
		tmp = lb2 - tmp;
		// tmp can be slightly negativ, likely due to rounding errors (coord lies on the link!). Therefore, use at least 0.0
		tmp = Math.max(0.0, tmp);
		return Math.sqrt(tmp);
	}

	//////////////////////////////////////////////////////////////////////
	// get methods
	//////////////////////////////////////////////////////////////////////

	@Override
	public Node getFromNode() {
		return this.from;
	}

	@Override
	public final boolean setFromNode(final Node node) {
		this.from = node;
		return true;
	}

	@Override
	public Node getToNode() {
		return this.to;
	}

	@Override
	public final boolean setToNode(final Node node) {
		this.to = node;
		return true;
	}

	public double getFreespeedTravelTime() {
		return getFreespeedTravelTime(Time.UNDEFINED_TIME);
	}

	public double getFreespeedTravelTime(final double time) {
		return this.length / this.freespeed;
	}

	public double getFlowCapacity() {
		return getFlowCapacity(Time.UNDEFINED_TIME);
	}

	public double getFlowCapacity(final double time) {
		return this.flowCapacity;
	}

	public final String getOrigId() {
		return this.origid;
	}

	public final String getType() {
		return this.type;
	}

	public final double getEuklideanDistance() {
		return this.euklideanDist;
	}

	@Override
	public double getCapacity() {
		return getCapacity(Time.UNDEFINED_TIME);
	}

	@Override
	public double getCapacity(final double time) { // not final since needed in TimeVariantLinkImpl
		return this.capacity;
	}

	@Override
	public final void setCapacity(double capacityPerNetworkCapcityPeriod){
		this.capacity = capacityPerNetworkCapcityPeriod;
		this.calculateFlowCapacity();
	}

	@Override
	public double getFreespeed() {
		return getFreespeed(Time.UNDEFINED_TIME);
	}

	/**
	 * This method returns the freespeed velocity in meter per seconds.
	 *
	 * @param time - the current time
	 * @return freespeed
	 */
	@Override
	public double getFreespeed(final double time) { // not final since needed in TimeVariantLinkImpl
		return this.freespeed;
	}

	@Override
	public final void setFreespeed(double freespeed) {
		this.freespeed = freespeed;
		this.checkFreespeedSemantics();
	}

	@Override
	public double getLength() {
		return this.length;
	}

	@Override
	public final void setLength(double length) {
		this.length = length;
		this.checkLengthSemantics();
	}

	@Override
	public double getNumberOfLanes() {
		return getNumberOfLanes(Time.UNDEFINED_TIME);
	}

	@Override
	public double getNumberOfLanes(final double time) { // not final since needed in TimeVariantLinkImpl
		return this.nofLanes;
	}

	@Override
	public final void setNumberOfLanes(double lanes) {
		this.nofLanes = lanes;
		this.checkNumberOfLanesSemantics();
	}

	@Override
	public final Set<String> getAllowedModes() {
		return new HashSet<String>(this.allowedModes);
	}

	@Override
	public final void setAllowedModes(final Set<String> modes) {
		this.allowedModes.clear();
		this.allowedModes.addAll(modes);
	}

	public final void setOrigId(final String id) {
		this.origid = id;
	}

	public void setType(final String type) {
		this.type = type;
	}

	private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
		ois.defaultReadObject();
		this.from.addOutLink(this);
		this.to.addInLink(this);
	}

	@Override
	public String toString() {
		return super.toString() +
		"[from_id=" + this.from.getId() + "]" +
		"[to_id=" + this.to.getId() + "]" +
		"[length=" + this.length + "]" +
		"[freespeed=" + this.freespeed + "]" +
		"[capacity=" + this.capacity + "]" +
		"[permlanes=" + this.nofLanes + "]" +
		"[origid=" + this.origid + "]" +
		"[type=" + this.type + "]";
	}

}
