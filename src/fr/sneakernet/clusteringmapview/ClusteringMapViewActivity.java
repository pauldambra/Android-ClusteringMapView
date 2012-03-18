package fr.sneakernet.clusteringmapview;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;

public class ClusteringMapViewActivity extends MapActivity
{
	private ClusteringMapView mMap;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.main );

        this.mMap = (ClusteringMapView) findViewById( R.id.mapview );
		this.mMap.setBuiltInZoomControls(true);
		this.mMap.setSatellite(false);
		
		// Populate list of objects containing a geoPoint field
		List<MyObject> objectList = populateList();

		ClusteringMapOverlay clusteringOverlay = 
				new ClusteringMapOverlay(
						getResources().getDrawable( android.R.drawable.ic_menu_myplaces ),
						this.mMap,
						objectList);
		this.mMap.getOverlays().add(clusteringOverlay);
    }
    
    private List<MyObject> populateList()
    {
    	List<MyObject> list = new ArrayList<MyObject> ();
    	for (int i=0 ; i<50 ; i++)
    	{
    		int line = i/10;
    		int column = i%10;

    		GeoPoint geoPoint = GeoUtils.getPoint(43.604385+(column*0.003), 1.44336+(line*0.003));
    		list.add(new MyObject("Object "+i, geoPoint));
    	}
    	return list;
    }

	@Override
	protected boolean isRouteDisplayed()
	{
		return false;
	}
}