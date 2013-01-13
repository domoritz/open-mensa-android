package de.uni_potsdam.hpi.openmensa;

import java.util.ArrayList;

import de.uni_potsdam.hpi.openmensa.api.Canteen;
import de.uni_potsdam.hpi.openmensa.helpers.RefreshableFragment;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.OverlayItem.HotspotPlace;

public class CanteenFragment extends Fragment implements RefreshableFragment {

	private MapView mapView;
	private MapController mapController;
	private OverlayItem canteenLocation;
	private ItemizedIconOverlay overlay;
	private DefaultResourceProxyImpl resourceProxy;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.canteen_fragment, container, false);
		
		mapView = (MapView) view.findViewById(R.id.mapview);
		mapView.setTileSource(TileSourceFactory.CLOUDMADESTANDARDTILES);
		mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        mapView.setClickable(true);
		mapController = mapView.getController();
		
		return view;
	}
	
	@Override
	public void onResume() {
		refresh();
		super.onResume() ; 
	}

	@Override
	public void refresh() {
		if (isDetached() || !isAdded())
			return;

		Canteen canteen = MainActivity.storage.getCurrentCanteen();
		if (canteen == null)
			return;
		
		TextView address = (TextView) getView().findViewById(R.id.txtAddress);
		address.setText(canteen.address);
		
		TextView name = (TextView) getView().findViewById(R.id.txtName);
		name.setText(canteen.name);
		
		mapController.setZoom(18);
		int lat = (int) (canteen.coordinates[0]*1E6);
		int lon = (int) (canteen.coordinates[1]*1E6);
		
		GeoPoint center = new GeoPoint(lat, lon);
		mapController.setCenter(center);

		
		canteenLocation = new OverlayItem(canteen.name,canteen.address, center);
        Drawable canteenMarker = this.getResources().getDrawable(R.drawable.marker_blue);
        canteenLocation.setMarker(canteenMarker);

        final ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
        items.add(canteenLocation);
        
        resourceProxy = new DefaultResourceProxyImpl(MainActivity.context);

        overlay = new ItemizedIconOverlay<OverlayItem>(items,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                        return true;
                    }
                    public boolean onItemLongPress(final int index, final OverlayItem item) {
                        return true;
                    }
                }, resourceProxy);
        this.mapView.getOverlays().clear();
        this.mapView.getOverlays().add(this.overlay);
	}

}
