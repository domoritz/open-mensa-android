package de.uni_potsdam.hpi.openmensa;

import android.content.Context;
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
 * @author dominik
 *
 * TODO: give this a better name or better make it abstract and build a 
 * 		separate preference where entries and entyValues has a concrete implementation
 */
public class DynamicPreference extends MultiSelectListPreference {


    public DynamicPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DynamicPreference(Context context) {
        super(context);
    }

    @Override
    protected View onCreateDialogView() {
        ListView view = new ListView(getContext());
        view.setAdapter(adapter());
        setEntries(entries());
        setEntryValues(entryValues());
        //setValueIndex(initializeIndex());
        return view;
    }

    private ListAdapter adapter() {
        return new ArrayAdapter<String>(getContext(), android.R.layout.select_dialog_multichoice);
    }

    // TODO: adjust available entries by SettingsProvider.getAvailableCanteens()!
    private CharSequence[] entries() {
    	CharSequence[] entries = { "Griebnitzsee", "Golm", "Neues Palais" };
        return entries;
    }

    private CharSequence[] entryValues() {
    	CharSequence[] entryValues = { "1", "2", "3" };
    	return entryValues;
    }
}
