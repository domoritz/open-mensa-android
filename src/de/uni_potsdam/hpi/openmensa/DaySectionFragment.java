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
import de.uni_potsdam.hpi.openmensa.api.preferences.SettingsProvider;
import de.uni_potsdam.hpi.openmensa.helpers.OnFinishedFetchingMealsListener;
import de.uni_potsdam.hpi.openmensa.helpers.RetrieveFeedTask;

/**
 * A fragment representing a section of the app, that displays the Meals for
 * one Day.
 */
public class DaySectionFragment extends ListFragment implements OnFinishedFetchingMealsListener{
	private ArrayList<Meal> listItems = new ArrayList<Meal>();
	private String date = null;
	MealAdapter adapter;


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

		adapter = new MealAdapter(getActivity(), R.layout.list_item, listItems);

		// Assign adapter to ListView
		setListAdapter(adapter);

		refresh();
	}
	
	public void refresh() {
		if (this.date == null)
			return;

		Canteen canteen = MainActivity.storage.getCurrentCanteen();
		ArrayList<Meal> meals = canteen.getMealList(date);
		if (meals != null) {
			setMealList(meals);
			Log.d(MainActivity.TAG, "Meal cache hit");
			return;
		}
		Log.d(MainActivity.TAG, "Meal cache miss");
		String baseUrl = SettingsProvider.getSourceUrl(MainActivity.context);
		String url = baseUrl + "canteens/" + canteen.key + "/days/" + date + "/meals";
		RetrieveFeedTask task = new RetrieveMealFeedTask(MainActivity.context, this, canteen, date);
		task.execute(new String[] { url });
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Log.i("FragmentList", "Item clicked: " + id);
	}
	
	public void setDate(String date) {
		this.date = date;
	}
	
	/**
	 * tell the fragment that the canteen is closed today
	 */
	public void setToClosed() {
		// TODO: implement
	}
	
	public void setMealList(ArrayList<Meal> meals) {
		if (listItems == null || adapter == null)
			return;

		listItems.clear();
		listItems.addAll(meals);
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onMealFetchFinished(RetrieveMealFeedTask task) {
		// the fragment might have been deleted while we were fetching something
		task.canteen.setMealList(task.getDate(), task.getMealList());
		setMealList(task.getMealList());
		
	}
}