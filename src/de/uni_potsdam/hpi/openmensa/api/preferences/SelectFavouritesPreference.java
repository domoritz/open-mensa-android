package de.uni_potsdam.hpi.openmensa.api.preferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.MultiSelectListPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import de.uni_potsdam.hpi.openmensa.MainActivity;
import de.uni_potsdam.hpi.openmensa.api.Canteen;

/**
 * A multi select list preference for the preferences file (res->xml->preferences). 
 * This enables the preference item to get the list from another place than the resources xml.
 * 
 * Read more: http://stackoverflow.com/a/10121132/214950
 * 
 * @author dominik
 *
 * TODO: give this a better name or better make it abstract and build a 
 * 		separate preference where entries and entyValues has a concrete implementation
 * TODO: use ListPreferenceMultiSelect as it is compatible with older versions
 */
public class SelectFavouritesPreference extends MultiSelectListPreference {

	protected Context context;
	protected CharSequence[] entries = {};
	protected CharSequence[] entryValues = {};
	
	private Location location = null;

    public SelectFavouritesPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SelectFavouritesPreference(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
    }

    @Override
    protected View onCreateDialogView() {
        ListView view = new ListView(getContext());
        view.setAdapter(adapter());
        
        return view;
    }
    
    @Override
    protected void showDialog(Bundle state) {
    	initializeValues();
        
        setEntries(entries());
        setEntryValues(entryValues());
        //setValueIndex(initializeIndex());
        
    	super.showDialog(state);
    }
    
	private void initializeValues() {
		HashMap<String, Canteen> canteens = SettingsUtils.getStorage(context).getCanteens();
    	ArrayList<CharSequence> entriesList = new ArrayList<CharSequence>();
    	ArrayList<CharSequence> entryValuesList = new ArrayList<CharSequence>();
    	
    	//entriesList.add("Select all");
    	//entryValuesList.add("#ALL#");
    	
    	List<Canteen> orderedCanteens = new ArrayList<Canteen>(canteens.values());
    	
    	location = getLastBestLocation();
    	
    	if (location != null) {
    		Collections.sort(orderedCanteens, new Comparator<Canteen>() {
            	private Float distanceToCurrentLocation(Canteen canteen) {
            		Location point = new Location("");
            		point.setLatitude(canteen.coordinates[0]);
            		point.setLongitude(canteen.coordinates[1]);
            		return location.distanceTo(point);
            	}
            	
                public int compare(Canteen o1, Canteen o2) {
                    return distanceToCurrentLocation(o1).compareTo(distanceToCurrentLocation(o2));
                }
            });
		} else {
			Collections.sort(orderedCanteens, new Comparator<Canteen>() {
	            public int compare(Canteen o1, Canteen o2) {
	                return o1.name.compareTo(o2.name);
	            }
	        });
		}
        
    	
        for (Canteen canteen : orderedCanteens) {
        	entriesList.add(canteen.name);
        	entryValuesList.add(canteen.key);
		}
        
        entries = (CharSequence[]) entriesList.toArray(new CharSequence[entriesList.size()]);
        entryValues = (CharSequence[]) entryValuesList.toArray(new CharSequence[entryValuesList.size()]);
	}

    private ListAdapter adapter() {
        return new ArrayAdapter<String>(getContext(), android.R.layout.select_dialog_multichoice);
    }

    // TODO: adjust available entries with SettingsUtils.getAvailableCanteens()!
    private CharSequence[] entries() {
        return entries;
    }

    private CharSequence[] entryValues() {
    	return entryValues;
    }
    
    /**
     * @return the last know best location
     */
    private Location getLastBestLocation() {
		Location locationGPS = MainActivity.getLocationManager().getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location locationNet = MainActivity.getLocationManager().getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        long GPSLocationTime = 0;
        if (null != locationGPS) { GPSLocationTime = locationGPS.getTime(); }

        long NetLocationTime = 0;

        if (null != locationNet) {
            NetLocationTime = locationNet.getTime();
        }

        if ( 0 < GPSLocationTime - NetLocationTime ) {
            return locationGPS;
        }
        else{
            return locationNet;
        }

    }
}
