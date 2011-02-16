package com.brousalis;

import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.util.Log;

import com.google.android.maps.GeoPoint;

/**
 * DataHandler allows parsing of trails XML from an online source.
 * @author ericstokes
 *
 */
public class DataHandler {
	
	private Trail _trail;
	private HashSet<Trail> _trails;
	private TrailPoint _trailPoint;
	
	private DocumentBuilderFactory _factory;
	private DocumentBuilder _builder;
	private URL _xmlFile;
	private Document _doc;
	
	/**
	 * Initializes the DataHandler values using a custom XML File
	 * 
	 * @param XMLFILE Location of the xml to parse.
	 */
	public DataHandler(String XMLFILE) {
		//Log.w("MTM", "Handler initialized");
		//Log.w("MTM", "Document Location:" + XMLFILE);
		_trails = new HashSet<Trail>();
		_factory = DocumentBuilderFactory.newInstance();
		try {
			_xmlFile = new URL(XMLFILE);
			_builder = _factory.newDocumentBuilder();
		} catch (Exception e) {
			//Log.w("MTM", "Caught Exception!");
			e.printStackTrace();
		}
	}
	
	/**
	 * Parses the document into trails and points.
	 */
	public void parseDocument() {
		try {
			_doc = _builder.parse(new InputSource(_xmlFile.openStream()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		//Log.w("MTM", "Parsing Document...");
		
		if (_doc != null) {
			extractTrails(_doc);
		} else {
			// Failure to parse XML
			Log.w("MTM", "Failure to parse XML...");
		}
		
	}
	
	/**
	 * Extracts trail information from the DOM. This will retrieve all trails currently
	 * 
	 * @param doc The XML Document to pull trails from
	 */
	private void extractTrails(Document doc) {
		//Log.w("MTM", "Extracting Trails...");
		NodeList itemList = doc.getElementsByTagName("trail");
		Node currentNode = itemList.item(0);
		while (currentNode != null) {
			if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
				String trailName = currentNode.getAttributes().getNamedItem("name").getNodeValue();
				//Log.w("MTM", "MTM: Trail ID   : " + currentNode.getAttributes().getNamedItem("id").getNodeValue());
				//Log.w("MTM", "MTM: Trail Name : " + trailName);
				createTrail(trailName);
				extractTrail(currentNode);
				saveTrail();
			}
			try {
				currentNode = currentNode.getNextSibling();
			} catch (Exception e) {
				currentNode = null;
			}
		}
	}
	
	/**
	 * Recursive function that iterates through all nodes in a trail
	 * 
	 * @param currentNode The Node (TrailPoint of some type) that is currently being analyzed.
	 */
	private void extractTrail(Node currentNode) {
		Node point = null;
		
		point = getPointNode(currentNode);
		
		while (point != null) {
			if (point.getNodeType() == Node.ELEMENT_NODE && point.getNodeName().equals("point")) {
				getPointInfo(point);
			}
			try {
				point = point.getNextSibling();
			} catch (Exception e) {
				//Log.w("MTM", "MTM Fixed another part of the 2.1 xml parser");
				point = null;
			}
		}
	}
	
	/**
	 * Helps extractTrail by returning a node on the current level, or traversing down the tree based on element type.
	 * 
	 * @param currentNode The node to look at
	 * @return A Node that is either on the current level or down one.
	 */
	private Node getPointNode(Node currentNode) {
		Node myNode = currentNode.getFirstChild();
		while (myNode.getNodeType() != Node.ELEMENT_NODE && !myNode.getNodeName().equals("points")) {
			myNode = myNode.getNextSibling();
		}
		myNode = myNode.getFirstChild();
		while (myNode.getNodeType() != Node.ELEMENT_NODE && !myNode.getNodeName().equals("point")) {
			myNode = myNode.getNextSibling();
		}
		return myNode;
	}
	
	/**
	 * Extracts and saves information to the temporary trail array.
	 * 
	 * @param point The Point to save
	 */
	private void getPointInfo(Node point) {
		Node localPoint = null;
		//Log.w("MTM", "MTM: Point         : " + point.getNodeName());
		//Log.w("MTM", "MTM: Point ID      : " + point.getAttributes().getNamedItem("id").getNodeValue());
		//Log.w("MTM", "MTM: Point Content : " + point.getNodeValue());
		HashMap<String, Object> trailPointInfo = new HashMap<String, Object>();
		trailPointInfo.put("id", point.getAttributes().getNamedItem("id").getNodeValue());
		
		localPoint = point.getFirstChild().getNextSibling();
		localPoint = getPointAttrs(localPoint, trailPointInfo);
		saveTrailPoint();
	}
	
	/**
	 * Second step recursive call to pull all connections out of trail points.
	 * 
	 * @param localPoint The new Root level point we're looking at.
	 * @param trailPointInfo A HashMap that contains basic TrailPoint info.
	 * @return The next point or null if there are no more.
	 */
	private Node getPointAttrs(Node localPoint, HashMap<String, Object> trailPointInfo) {
		while (localPoint != null) {
			if (localPoint.getNodeType() == Node.ELEMENT_NODE && !localPoint.getNodeName().equals("connection") && !localPoint.getNodeName().equals("connections")) {
				String name = localPoint.getNodeName();
				String value = localPoint.getFirstChild().getNodeValue();
				//Log.w("MTM", "MTM Value: " + name + " : " + value);
				
				trailPointInfo.put(name, value);
			}
			
			if (localPoint.getNodeName().equals("category")) {
				trailPointInfo.put("categoryID", localPoint.getAttributes().getNamedItem("id").getNodeValue());
			}
			
			if (localPoint.getNodeName().equals("connection")) {
				int connID = Integer.parseInt(localPoint.getFirstChild().getNodeValue());
				_trailPoint.addConnectionByID(connID);
				
				//Log.w("MTM", "Found the connections node " + localPoint.getFirstChild().getNodeValue());
			}
			if (localPoint.getNodeName().equals("longitude")) {
				createNewTrailPoint(Integer.parseInt((String) trailPointInfo.get("id")), Double.parseDouble((String) trailPointInfo.get("latitude")), Double.parseDouble((String) trailPointInfo.get("longitude")));
				_trailPoint.setCategoryID(Integer.parseInt((String) trailPointInfo.get("categoryID")));
				_trailPoint.setTitle((String) trailPointInfo.get("title"));
				_trailPoint.setSummary((String) trailPointInfo.get("description"));
			}
			if (localPoint.getNodeName().equals("connections")) {
				localPoint = localPoint.getFirstChild();
				
				//Log.w("MTM", "Found the connections node");
			} else {
				
				try {
					localPoint = localPoint.getNextSibling();
				} catch (Exception e) {
					// This is here to fix the IndexOutOfBoundsException that is present in Android 2.1
					//Log.w("MTM", "MTM I hate android 2.1-update1's parser...");
					localPoint = null;
				}
			}
		}
		return localPoint;
	}
	
	/**
	 * Performs validation on the document This is not yet implemented UPDATE: 2/2/2011 This will be so long as we support Android 2.1
	 */
	public boolean validateDocument() {
		return false;
	}
	
	/**
	 * Returns a single Trail
	 * 
	 * @param trail The name of the trail to get.
	 * @return A Trail who's name was given by trail
	 */
	public Trail getParsedTrail(String trail) {
		Iterator<Trail> iter = this._trails.iterator();
		while (iter.hasNext()) {
			Trail current = iter.next();
			if (current.getName().equals(trail)) {
				return current;
			}
		}
		return null;
	}
	
	/**
	 * Returns all the parsed trails
	 * 
	 * @return A Dictionary<String, Trail> of all the parsed trails.
	 */
	public HashSet<Trail> getParsedTrails() {
		return this._trails;
	}
	
	/**
	 * Sets the current working TrailPoint. This point will be added to the current trail once all of it's information has been read.
	 * 
	 * @param id ID of the TrailPoint
	 * @param lat Latitude in double format (ex. 14.1328)
	 * @param lon Longitude in double format (ex. 12.2334)
	 */
	private void createNewTrailPoint(int id, double lat, double lon) {
		this._trailPoint = new TrailPoint(id, new GeoPoint((int) (lat * 1E6), (int) (lon * 1E6)), new HashSet<TrailPoint>());
	}
	
	/**
	 * Creates a new trail
	 * 
	 * @param trailName The name of the Trail
	 */
	private void createTrail(String trailName) {
		this._trail = new Trail(trailName);
	}
	
	/**
	 * Saves a TrailPoint to the current trail.
	 */
	private void saveTrailPoint() {
		this._trail.addPoint(this._trailPoint);
		// this._trailPoint = new TrailPoint(-1, new GeoPoint(0,0), null);
	}
	
	/**
	 * Saves the current Trail to the Trail Dictionary
	 */
	private void saveTrail() {
		this._trails.add(this._trail);
	}
}
