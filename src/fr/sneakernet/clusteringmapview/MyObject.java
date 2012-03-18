package fr.sneakernet.clusteringmapview;

import com.google.android.maps.GeoPoint;

public class MyObject
{
	private String mName;
	private GeoPoint mGeoPoint;
	
	public MyObject (String name, GeoPoint geoPoint) {
		this.mName = name;
		this.mGeoPoint = geoPoint;
	}

	public String getName() {
		return mName;
	}

	public void setName(String mName) {
		this.mName = mName;
	}

	public GeoPoint getGeoPoint() {
		return mGeoPoint;
	}

	public void setGeoPoint(GeoPoint mGeoPoint) {
		this.mGeoPoint = mGeoPoint;
	}
}
