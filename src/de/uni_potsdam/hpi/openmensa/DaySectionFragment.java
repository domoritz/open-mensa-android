package de.uni_potsdam.hpi.openmensa;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.ExpandableListFragment;
import de.uni_potsdam.hpi.openmensa.api.Day;
import de.uni_potsdam.hpi.openmensa.api.Meal;

/**
 * A fragment representing a section of the app, that displays the Meals for
 * one Day.
 */
public class DaySectionFragment extends ExpandableListFragment {
	private ArrayList<Meal> listItems = new ArrayList<Meal>();
	private String date = null;
	private Boolean fetching = false;

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
		update();
		super.onResume() ; 
	}
	
	public void update() {
		if (isDetached() || !isAdded() || date == null)
			return;

		Day day = MainActivity.storage.getCurrentCanteen().getDay(date);
		
		if (day == null) {
			if (fetching) {
				setToFetching(true, false);
			} else {
				setToNoInformation();
			}
			return;
		}
		
		if (day.closed) {
			setToClosed();
			return;
		}

		setMealList(day);
	}
	
	/**
	 * tell the fragment that the canteen is closed today
	 */
	public void setToClosed() {
		setToFetching(false, true);
		setEmptyText(getResources().getString(R.string.canteenclosed));
	}
	
	/**
	 * tell the fragment that there is no information available for today
	 */
	public void setToNoInformation() {
		setToFetching(false, true);
		setEmptyText(getResources().getString(R.string.noinfo));
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
	
	public void setMealList(Day day) {
		if (listItems == null || adapter == null)
			return;

		setToFetching(false, true);
		listItems.addAll(day.getMeals());
		adapter.notifyDataSetChanged();
	}
	
	public void setDate(String date) {
		this.date = date;
	}
	
	public String getDate() {
		return date;
	}
}