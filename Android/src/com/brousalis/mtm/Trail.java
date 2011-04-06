package com.brousalis.mtm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

/**
 * Represents a single Trail
 * 
 * @author ericstokes
 * 
 */
public class Trail extends ItemizedOverlay<OverlayItem> {
	
	private Paint _trailPaint;
	private String _name;
	private ArrayList<TrailPoint> _trailPoints;
	private ArrayList<TrailPoint> _trailHeads;
	
	public Trail(String name) {
		super(boundCenter(ShowMap.bubble));
		_trailPaint = getCoolPaint();
		this._name = name;
		this._trailPoints = new ArrayList<TrailPoint>();
		this._trailHeads = new ArrayList<TrailPoint>();
	}
	
	public void setName(String name) {
		this._name = name;
	}
	
	public String getName() {
		return this._name;
	}
	
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		
		Point screenPts;
		for (TrailPoint p : _trailHeads) {
			screenPts = new Point();
			mapView.getProjection().toPixels(p.getLocation(), screenPts);
			canvas.drawCircle(screenPts.x, screenPts.y, 15, p.getColor());
		}
		super.draw(canvas, mapView, false);
	}
	
	/**
	 * Returns what I think is, but what will have to be tuned with a better algorithm, a cool colored paint that is antialiased.
	 * 
	 * @return A cool Paint color
	 */
	private Paint getCoolPaint() {
		Paint p = new Paint();
		Random r = new Random(1);
		p.setARGB(255, 255 * r.nextInt(2), 255 * r.nextInt(2), 255 * r.nextInt(2));
		p.setAntiAlias(true);
		return p;
	}
	
	/**
	 * Creates a new activity and activates it, packing the activity with the point details
	 */
	@Override
	protected boolean onTap(int index) {
		Log.w(ShowMap.MTM, "You just touched item: " + index);
		Intent details = new Intent(ShowMap.thisActivity, ItemDetails.class);
		
		// Do we want to pull the name of the point, or of the trail?
		TrailPoint touched = this._trailHeads.get(index);
		details.putExtra("id", touched.getID());
		details.putExtra("category", touched.getCategory());
		details.putExtra("title", touched.getTitle());
		details.putExtra("summary", touched.getSummary());
		details.putExtra("condition", touched.getCondition());
		ShowMap.thisActivity.startActivity(details);
		return true;
	}
	
	/**
	 * This method is DANGEROUS, it assumes each point is connected to the next.
	 * 
	 * Adds a List of TrailPoints to this trail. It should be noted that the first point of this trail is NOT linked to any other point on this trail
	 * 
	 * @param trailPoints
	 */
	public void addLinkedPoints(LinkedList<TrailPoint> trailPoints) {
		while (!trailPoints.isEmpty()) {
			TrailPoint p = trailPoints.poll();
			this._trailPoints.add(p);
			p.addConnection(trailPoints.peek());
		}
	}
	
	/**
	 * Adds a List of TrailPoints to this trail. It should be noted that the first point of this trail is NOT linked to any other point on this trail
	 * 
	 * @param trailPoints
	 */
	public void addLinkedPoints(LinkedList<TrailPoint> trailPoints, TrailPoint pOld) {
		while (!trailPoints.isEmpty()) {
			TrailPoint p = trailPoints.poll();
			this._trailPoints.add(p);
			p.addConnection(trailPoints.peek());
		}
	}
	
	/**
	 * Returns the number of points in this trail
	 * 
	 * @return The number of points in this trail
	 */
	public int getNumberOfTrailPoints() {
		return this._trailPoints.size();
	}
	
	/**
	 * Adds a new TrailPoint to this Trail and Adds a new connection from pOld to pNew
	 * 
	 * @param pNew The new TrailPoint to add
	 * @param pOld The existing TrailPoint to form the connection from
	 */
	public boolean addPoint(TrailPoint pNew, TrailPoint pOld) {
		if (this.hasPoint(pNew))
			return false;
		this.addPoint(pNew);
		if (this.hasPoint(pOld))
			pOld.addConnection(pNew);
		return true;
	}
	
	/**
	 * Adds a new TrailPoint to this Trail
	 * 
	 * @param point The new TrailPoint to add
	 */
	public void addPoint(TrailPoint point) {
		point.setColor(this._trailPaint);
		this._trailPoints.add(point);
		
		// TODO: Remove this static reference
		// This is using a static category reference, when category parsing is implemented
		// thsi will go away
		if (point.getCategoryID() != 2) {
			this._trailHeads.add(point);
			
		}
		populate();
	}
	
	/**
	 * Removes a TrailPoint if it is in the current trail
	 * 
	 * @param point The TrailPoint to remove
	 */
	public void removePoint(TrailPoint point) {
		this._trailPoints.remove(point);
	}
	
	/**
	 * Gets a specific TrailPoint based on an ID. After Testing, this method will be made private
	 * 
	 * @param id The ID of the TrailPoint
	 * @return A TrailPoint if it exists, or null
	 */
	public TrailPoint getTrailPoint(int id) {
		Iterator<TrailPoint> iter = _trailPoints.iterator();
		while (iter.hasNext()) {
			TrailPoint current = iter.next();
			if (current.getID() == id) {
				return current;
			}
		}
		return null;
	}
	
	/**
	 * Returns the Set of all TrailPoints
	 * 
	 * @return The Set of all TrailPoints
	 */
	public Collection<TrailPoint> getTrailPoints() {
		return this._trailPoints;
	}
	
	/**
	 * Returns the Set of all Trail Heads
	 * 
	 * @return The Set of all Trail Heads
	 */
	public Collection<TrailPoint> getTrailHeads() {
		return this._trailHeads;
	}
	
	/**
	 * Determines if this Trail does or does not contain a specific point
	 * 
	 * @param point The TrailPoint to check the list against
	 * @return True if the Point is contained within the list
	 */
	public boolean hasPoint(TrailPoint point) {
		if (point == null)
			return false;
		return this._trailPoints.contains(point);
	}
	
	/**
	 * Corrects any unresolved Links. Unresolved links can occur when importing data from XML. This function takes the list of Integers and parses them properly. That list is then cleared out.
	 */
	public void resolveConnections() {
		Log.w("MTM", "MTM: Pre  " + this.toStringList());
		for (TrailPoint p : this._trailPoints) {
			if (p.hasUnresolvedLinks()) {
				for (Integer i : p.getUnresolvedLinks()) {
					p.addConnection(this.getTrailPoint(i));
				}
			}
		}
		Log.w("MTM", "MTM: Post " + this.toStringList());
	}
	
	@Override
	public String toString() {
		return "Trail: " + this._name + " (" + this._trailPoints.size() + " TrailPoints)";
	}
	
	/**
	 * Returns a list of Strings describing the current points.
	 * 
	 * @return A list of Strings describing the current points.
	 */
	public String toStringList() {
		String trailList = "Trail: " + this._name + " (";
		for (TrailPoint p : this._trailPoints) {
			trailList += p.getID() + ", ";
		}
		return trailList;
	}
	
	@Override
	protected OverlayItem createItem(int i) {
		return convertPointToOverlay(_trailHeads.get(i));
	}
	
	/**
	 * Converts a TrailPoint to an OverlayItem
	 * 
	 * @param trailPoint The TrailPoint to convert
	 * @return An OverlayItem
	 */
	private OverlayItem convertPointToOverlay(TrailPoint trailPoint) {
		return new OverlayItem(trailPoint.getLocation(), trailPoint.getTitle(), trailPoint.getSummary());
	}
	
	/**
	 * Implemented by ItemizedOverlay
	 */
	@Override
	public int size() {
		return _trailHeads.size();
	}
}
