package fr.sneakernet.clusteringmapview;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;

public class ClusteringMapOverlay extends ItemizedOverlay<OverlayItem> implements OnClusterMapZoomListener
{
	private static final String TAG = "ClusteringMapOverlay";

	private Context mContext;
	private ClusteringMapView mMap;
	private Projection mProjection;
	private Drawable mMarker;
	
	// Original list of objects
	private List<MyObject> mList;
	
	// Copy of list of objects
	private List<MyObject> mListCopy = new ArrayList<MyObject> ();

	// Distance tolerated between two markers
	private int mDistance;
	
	// List containing clustered items
	private List<OverlayItem> mClusteredItems = new ArrayList<OverlayItem>();
	
	// List containing alone items
	private List<OverlayItem> mItems = new ArrayList<OverlayItem> ();

	// List containing temporary set of points overlapping another points
	private List<Point> mSetOfPoints = new ArrayList<Point>();

	public ClusteringMapOverlay(Drawable defaultMarker, ClusteringMapView mapView, List<MyObject> list) {
		super(boundCenter(defaultMarker));
		
		this.mContext = mapView.getContext();
		this.mList = list;
		this.mMap = mapView;
		this.mMap.getController().setZoom(5);

		// Register as zoom listener
		this.mMap.setOnClusterMapZoomListener(this);

		this.mMarker = defaultMarker;
		this.mDistance = this.mMarker.getIntrinsicWidth();
	}

	@Override
	protected OverlayItem createItem(int i)
	{
		Log.d(TAG,  "Create item "+i);
		
		// Return clustered overlay or item depending on index
		return i < mClusteredItems.size() ? mClusteredItems.get(i) : mItems.get(i-mClusteredItems.size());
	}

	@Override
	public int size()
	{
		// Return clustered items list size plus alone items list size
		return mClusteredItems.size()+mItems.size();
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow)
	{
		// Do not draw the shadow, nothing to do with clustering thought
		super.draw(canvas, mapView, false);
	}
	
	@Override
	protected boolean onTap(int index)
	{
		Toast.makeText(this.mContext, index < mClusteredItems.size() ? "Clustered item: "+mClusteredItems.get(index).getTitle() : "Item "+mItems.get(index-mClusteredItems.size()).getTitle(), Toast.LENGTH_SHORT).show();

		return true;
	}

	@Override
	public void onZoomLevelChanged(int zoomLevel)
	{
		// Redraw all overlays
		if (zoomLevel == 21)
		{
			// All the points if zoom level is max (21)
			addClusteredOverlayItems(true);
		}
		else
		{
			// Cluster points if needed when zoom level is below
			addClusteredOverlayItems(false);
		}
	}

	private void addClusteredOverlayItems(boolean displayAll)
	{
		// Clear clustered and single overlays list
		this.mClusteredItems.clear();
		this.mItems.clear();
		
		// Re-set map projection (hey, zoom level has changed!)
		this.mProjection = this.mMap.getProjection();

		// Copy the original list of objects into the temporary list which is used only in this method
		for (MyObject object : this.mList)
		{
			mListCopy.add(object);
		}

		// If zoom level is max, display all of the items as single overlays
		if (displayAll)
		{
			int count =  this.mList.size();
			for (int index=0 ; index<count ; index++)
			{
				MyObject object = (MyObject) this.mList.get(index);
				OverlayItem locationItem = new OverlayItem(object.getGeoPoint(), object.getName(), "");
				locationItem.setMarker(this.mMarker);
				this.mItems.add(locationItem);
			}
		}
		// Else, try to make cluster of items near each others
		else
		{
			// For each item in the list of items
			while (!this.mListCopy.isEmpty())
			{
				// Clear the list of nearby items
				this.mSetOfPoints.clear();

				// Get the point of the item
				MyObject object = this.mListCopy.remove(this.mListCopy.size()-1);
				GeoPoint siteGeoPoint = object.getGeoPoint();
				Point sitePxCoordinates = this.mProjection.toPixels(siteGeoPoint, null);
				
				// Add it to list of nearby points
				this.mSetOfPoints.add(sitePxCoordinates);

				// Run through all other items in the list
				for (int position=0 ; position<this.mListCopy.size() ; position++)
				{
					// Get point position
					MyObject myobject = this.mListCopy.get(position);
					GeoPoint otherSiteGeoPoint = myobject.getGeoPoint();
					Point otherSitePxCoordinates = this.mProjection.toPixels(otherSiteGeoPoint, null);

					int x = 0;
					int y = 0;
					double distance;
					
					// Check if the current point is near one of the nearby points
					for (Point p : this.mSetOfPoints)
					{
						x += p.x;
						y += p.y;
						
						// Get distance between point and current nearby point to check
						distance = Math.sqrt(Math.pow(p.x - otherSitePxCoordinates.x, 2) + Math.pow(p.y - otherSitePxCoordinates.y, 2));

						// If distance between the two points is lesser than size of the marker
						if (distance <= this.mDistance)
						{
							// Add this point to lit of nearby points
							this.mSetOfPoints.add(otherSitePxCoordinates);
							// Remove if from list of items
							this.mListCopy.remove(position);
							// Don't go further into the list of items cause we just removed the current
							position--;
							break;
						}
					}
					
					// If this point isn't already in the list of nearby points
					if (!this.mSetOfPoints.contains(otherSitePxCoordinates))
					{
						// Get centroid of the nearby points
						Point clusterCenter = new Point(x/this.mSetOfPoints.size(), y/this.mSetOfPoints.size());
						
						// Calculate distance between centroid point and current item
						distance = Math.sqrt(Math.pow(clusterCenter.x - otherSitePxCoordinates.x, 2) + Math.pow(clusterCenter.y - otherSitePxCoordinates.y, 2));

						// If distance between the two points is lesser than size of the marker
						if (distance <= this.mDistance)
						{
							// Add this point to lit of nearby points
							this.mSetOfPoints.add(otherSitePxCoordinates);
							// Remove if from list of items
							this.mListCopy.remove(position);
							// Don't go further into the list of items cause we just removed the current
							position--;
						}
					}
				}

				// If the current item is not alone in the nearby points list
				if (this.mSetOfPoints.size()>1)
				{
					// Add a clustered overlay item
					GeoPoint centroid = getGeoPointFromSetOfPoints();
					OverlayItem locationItem = new OverlayItem(centroid, this.mSetOfPoints.size()+" items", "");
					locationItem.setMarker(this.mMarker);
					this.mClusteredItems.add(locationItem);
				}
				else
				{
					// Add a single overlay item
					OverlayItem locationItem = new OverlayItem(siteGeoPoint, object.getName(), "");
					locationItem.setMarker(this.mMarker);
					this.mItems.add(locationItem);
				}
			}
		}

		this.setLastFocusedIndex(-1);
		this.populate();
	}
	
	public GeoPoint getGeoPointFromSetOfPoints()
	{
		int x = 0;
		int y = 0;
		for (Point p : this.mSetOfPoints)
		{
			x += p.x;
			y += p.y;
		}
		
		int size = this.mSetOfPoints.size();
		
		return this.mProjection.fromPixels(x/size, y/size);
	}
}
