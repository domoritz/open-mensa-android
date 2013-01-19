package de.uni_potsdam.hpi.openmensa;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.ExpandableListFragment;
import de.uni_potsdam.hpi.openmensa.api.Canteen;
import de.uni_potsdam.hpi.openmensa.api.Day;
import de.uni_potsdam.hpi.openmensa.api.Meal;
import de.uni_potsdam.hpi.openmensa.helpers.RefreshableFragment;

/**
 * A fragment representing a section of the app, that displays the Meals for
 * one Day.
 */
public class DaySectionFragment extends ExpandableListFragment implements RefreshableFragment {
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
		listVisible = false;
		setEmptyText(getResources().getString(R.string.canteenclosed));
	}
	
	/**
	 * tell the fragment that there is no information available for today
	 */
	public void setToNoInformation() {
		setToFetching(false, true);
		listVisible = false;
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
	
	protected void setMealList(Day day) {
		if (listItems == null || adapter == null)
			return;

		listVisible = true;
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

	public boolean isListShown() {
		return listVisible;
	}
}