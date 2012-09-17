package de.uni_potsdam.hpi.openmensa;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import de.uni_potsdam.hpi.openmensa.api.Canteen;
import de.uni_potsdam.hpi.openmensa.api.Meal;

/**
 * A fragment representing a section of the app, that displays the Meals for
 * one Day.
 */
public class DaySectionFragment extends ListFragment{
	public DaySectionFragment(String date) {
		setDate(date);
	}

	private ArrayList<Meal> listItems;
	private String date;
	MealAdapter adapter;
	private Canteen canteen;

	/**
	 * When creating, retrieve this instance's number from its arguments.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	/**
	 * The Fragment's UI is just a simple text view showing its instance
	 * number.
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View listView = inflater.inflate(R.layout.fragment_pager_list,
				container, false);
		// TextView titleView = (TextView)
		// listView.findViewById(R.id.title);
		// titleView.setText(mensaName);
		return listView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		listItems = new ArrayList<Meal>();

		adapter = new MealAdapter(getActivity(), R.layout.list_item, listItems);

		// Assign adapter to ListView
		setListAdapter(adapter);
		
		refresh(date);
	}
	
	public void refresh(String date) {
		canteen = MainActivity.storage.getCurrentCanteen();
		ArrayList<Meal> meals = canteen.getMeals(date);
		listItems.clear();
		listItems.addAll(meals);
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Log.i("FragmentList", "Item clicked: " + id);
	}
	
	public void setDate(String date) {
		this.date = date;
	}
}