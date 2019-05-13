package de.uni_potsdam.hpi.openmensa;

import java.util.ArrayList;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import de.uni_potsdam.hpi.openmensa.api.Canteen;
import de.uni_potsdam.hpi.openmensa.helpers.RefreshableFragment;

public class CanteenFragment extends Fragment implements RefreshableFragment, OnClickListener {

	private MapView mapView;
	private OverlayItem canteenLocation;
	private ItemizedIconOverlay<OverlayItem> overlay;
	private DefaultResourceProxyImpl resourceProxy;
	
	private int zoom = 18;
	private GeoPoint center;
	private Canteen canteen;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.canteen_fragment, container, false);
		
		mapView = (MapView) view.findViewById(R.id.mapview);
		mapView.setTileSource(TileSourceFactory.MAPNIK);
		mapView.setBuiltInZoomControls(false);
        mapView.setMultiTouchControls(true);
        
        mapView.setVisibility(MapView.INVISIBLE);
		
		TextView address = (TextView) view.findViewById(R.id.txtAddress);
		address.setOnClickListener(this);
		
		TextView title = (TextView) view.findViewById(R.id.txtName);
		title.setOnClickListener(this);

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

		canteen = MainActivity.storage.getCurrentCanteen();
		if (canteen == null)
			return;
		
		mapView.setVisibility(MapView.VISIBLE);

		TextView address = (TextView) getView().findViewById(R.id.txtAddress);
		address.setText(canteen.address);
		
		TextView name = (TextView) getView().findViewById(R.id.txtName);
		name.setText(canteen.name);

        mapView.getController().setZoom(zoom);
		int lat = (int) (canteen.coordinates[0]*1E6);
		int lon = (int) (canteen.coordinates[1]*1E6);
		
		center = new GeoPoint(lat, lon);
        mapView.getController().setCenter(center);
		
		canteenLocation = new OverlayItem(canteen.name,canteen.address, center);
        Drawable canteenMarker = this.getResources().getDrawable(R.drawable.marker_blue);
        canteenLocation.setMarker(canteenMarker);

        final ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
        items.add(canteenLocation);
        
        resourceProxy = new DefaultResourceProxyImpl(MainActivity.getAppContext());

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

	@Override
	public void onClick(View v) {
		openMapIntent();
	}
	
	private void openMapIntent() {
		if (center == null || canteen == null)
			return;
		
		double lat = center.getLatitudeE6()/1E6;
		double lon = center.getLongitudeE6()/1E6;
		String latlon = lat + "," + lon;
		String uri = "geo:"+ latlon +
				"?z=" + zoom +
				"&q=" + latlon + "(" + canteen.name + ")";
		try {
			startActivity(new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri)));
		} catch (ActivityNotFoundException e) {
			Toast.makeText(MainActivity.getAppContext(), getResources().getString(R.string.nomapapp), Toast.LENGTH_LONG).show();
		}
		
	}
}
