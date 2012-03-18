package fr.sneakernet.clusteringmapview;

import com.google.android.maps.GeoPoint;

public class GeoUtils
{
	public static GeoPoint getPoint(double lat, double lon)
	{
		return(new GeoPoint((int)(lat*1000000.0),
				(int)(lon*1000000.0)));
	}
}
