package de.uni_potsdam.hpi.openmensa;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.ExpandableListFragment;
import android.util.Log;
import de.uni_potsdam.hpi.openmensa.api.Canteen;
import de.uni_potsdam.hpi.openmensa.api.Day;
import de.uni_potsdam.hpi.openmensa.api.Meal;
import de.uni_potsdam.hpi.openmensa.helpers.RefreshableFragment;

/**
 * A fragment representing a section of the app, that displays the Meals for
 * one Day.
 */
public class DayFragment extends ExpandableListFragment implements RefreshableFragment {
	private ArrayList<Meal> listItems = new ArrayList<Meal>();
	private String date = null;
	private Boolean fetching = false;
	private boolean listVisible = false;

	MealAdapter adapter;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		adapter = new MealAdapter(getActivity(), R.layout.list_item, listItems);

		// Assign adapter to ListView
		setListAdapter(adapter);
	}

	@Override
	public void onResume() {
		refresh();
		super.onResume() ; 
	}
	
	public void refresh() {
		if (isDetached() || !isAdded() || date == null)
			return;

		Canteen canteen = MainActivity.storage.getCurrentCanteen();
		if (canteen == null)
			return;
		
		Day day = canteen.getDay(date);
		
		if (day == null) {
			if (fetching) {
				setToFetching(true, false);
			} else {
				if (MainActivity.isOnline(MainActivity.getAppContext())) {
					setToNoInformation();
				} else {
					setToNotOnline();
				}
				
			}
			return;
		}
		
		if (day.closed) {
			setToClosed();
			return;
		}

		setMealList(day);
	}
	
	public void setEmptyText(String text) {
		if (getView() == null) {
        	Log.w(MainActivity.TAG, "List not yet created.");
        	return;
        }
		Log.d(MainActivity.TAG, String.format("Set text %s day %s", text, date));
		super.setEmptyText(text);
	}

	/**
	 * tell the fragment that the canteen is closed today
	 */
	public void setToClosed() {
		setEmptyText(getResources().getString(R.string.canteenclosed));
		setToFetching(false, true);
		listVisible = false;
	}
	
	/**
	 * tell the fragment that there is no information available for today
	 */
	public void setToNoInformation() {
		setEmptyText(getResources().getString(R.string.noinfo));
		setToFetching(false, true);
		listVisible = false;
	}
	
	/**
	 * tell the fragment that there we are currently not online
	 */
	public void setToNotOnline() {
		setEmptyText(getResources().getString(R.string.noconnection));
		setToFetching(false, true);
		listVisible = false;
	}
	
	/**
	 * clear the list of items
	 */
	public void clear() {
		if (listItems == null || adapter == null)
			return;
		listItems.clear();
		adapter.notifyDataSetChanged();
	}
	
	public void setToFetching(boolean on, boolean animated) {
		fetching = on;
		if (isDetached() || !isAdded())
			return;
		clear();
		if (animated) {
			setListShown(!on);
		} else {
			setListShownNoAnimation(!on);
		}
	}
	
	protected void setMealList(Day day) {
		if (listItems == null || adapter == null)
			return;
		
		if (day.isNullObject()) {
			setToNoInformation();
			Log.d(MainActivity.TAG, String.format("Null object for day %s", day.date));
			return;
		}

		listVisible = true;
		date = day.date;
		setToFetching(false, false);
		listItems.addAll(day.getMeals());
		adapter.notifyDataSetChanged();
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getDate() {
		return date;
	}

	public boolean isListShown() {
		return listVisible;
	}
}