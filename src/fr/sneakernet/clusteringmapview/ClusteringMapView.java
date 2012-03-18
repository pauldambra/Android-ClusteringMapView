package fr.sneakernet.clusteringmapview;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

import com.google.android.maps.MapView;

public class ClusteringMapView extends MapView {

	private int oldZoomLevel = -1;
	private OnClusterMapZoomListener onClusterMapZoomListener;

	public ClusteringMapView(Context context, String apiKey) {
		super(context, apiKey);
	}

	public ClusteringMapView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ClusteringMapView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);

		// Notify listener, if not null, that the zoom level changed
		if (getZoomLevel() != oldZoomLevel) {
			oldZoomLevel = getZoomLevel();
			if (onClusterMapZoomListener != null) {
				onClusterMapZoomListener.onZoomLevelChanged(oldZoomLevel);
			}
		}
	}

	public OnClusterMapZoomListener getOnClusterMapZoomListener() {
		return onClusterMapZoomListener;
	}

	public void setOnClusterMapZoomListener(
			OnClusterMapZoomListener onClusterMapZoomListener) {
		this.onClusterMapZoomListener = onClusterMapZoomListener;
	}
}
