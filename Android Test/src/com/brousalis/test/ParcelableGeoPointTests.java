package com.brousalis.test;

import junit.framework.TestCase;
import android.test.ActivityInstrumentationTestCase2;

import com.brousalis.ParcelableGeoPoint;
import com.brousalis.ShowMap;
import com.google.android.maps.GeoPoint;

public class ParcelableGeoPointTests extends TestCase {
	
	public ParcelableGeoPointTests() {
		//super("com.brousalis", ShowMap.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testPointCanBeCreated() {
		GeoPoint gp = new GeoPoint(5, 5);
		assertNotNull("GeoPoint was NULL", gp);
		
		ParcelableGeoPoint pgp = new ParcelableGeoPoint(5, 5);
		assertNotNull("ParcelableGeoPoint was NULL", pgp);
	}
	
	public void testPGPcanbecreatedfromGP() {
		
	}
	
	public void testParcelablePackagingWorks(){
		
	}
	
	
}
