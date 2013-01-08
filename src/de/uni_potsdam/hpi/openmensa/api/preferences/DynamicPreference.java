package de.uni_potsdam.hpi.openmensa.api.preferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import de.uni_potsdam.hpi.openmensa.api.Canteen;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.MultiSelectListPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

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
public class DynamicPreference extends MultiSelectListPreference {

	protected Context context;
	protected CharSequence[] entries = {};
	protected CharSequence[] entryValues = {};

    public DynamicPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public DynamicPreference(Context context) {
        super(context);
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
		HashMap<String, Canteen> canteens = SettingsProvider.getStorage(context).getCanteens();
    	ArrayList<CharSequence> entriesList = new ArrayList<CharSequence>();
    	ArrayList<CharSequence> entryValuesList = new ArrayList<CharSequence>();
    	
    	//entriesList.add("Select all");
    	//entryValuesList.add("#ALL#");
    	
    	List<Canteen> orderedCanteens = new ArrayList<Canteen>(canteens.values());
    	
        Collections.sort(orderedCanteens, new Comparator<Canteen>() {
            public int compare(Canteen o1, Canteen o2) {
                return o1.name.compareTo(o2.name);
            }
        });
    	
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

    // TODO: adjust available entries by SettingsProvider.getAvailableCanteens()!
    private CharSequence[] entries() {
        return entries;
    }

    private CharSequence[] entryValues() {
    	return entryValues;
    }
}
