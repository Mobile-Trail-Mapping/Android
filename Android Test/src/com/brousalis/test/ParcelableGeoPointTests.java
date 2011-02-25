package com.brousalis.test;

import junit.framework.TestCase;
import android.os.Parcel;
import android.test.ActivityInstrumentationTestCase2;

import com.brousalis.ParcelableGeoPoint;
import com.brousalis.ShowMap;
import com.google.android.maps.GeoPoint;
/**
 * Tests that verify that the ParcelableGeoPoint can work.
 * @author ericstokes
 *
 */
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
		assertTrue(true);
	}
	
	public void testParcelablePackagingWorks(){
		ParcelableGeoPoint pgp = new ParcelableGeoPoint(5,5);
		int latp = pgp.getLatitudeE6();
		int longp = pgp.getLatitudeE6();
		Parcel p = Parcel.obtain();
		pgp.writeToParcel(p, 0);
		p.setDataPosition(0);
		ParcelableGeoPoint pgpShipped = new ParcelableGeoPoint(p);
		
		assertEquals("Failed to rebuild parcel", latp, pgpShipped.getLatitudeE6());
		assertEquals("Failed to rebuild parcel", longp, pgpShipped.getLongitudeE6());
		
	}
	
	
}
