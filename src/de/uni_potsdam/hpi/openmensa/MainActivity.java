package de.uni_potsdam.hpi.openmensa;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Set;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SpinnerAdapter;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

public class MainActivity extends FragmentActivity implements
		OnSharedPreferenceChangeListener, OnNavigationListener {

	public static final String TAG = "Canteendroid";
	public static final Boolean LOGV = true;
	public static final String PREFS_NAME = "CanteendroidPrefs";

	private int mYear;
	private int mMonth;
	private int mDay;

	private ArrayList<Canteen> activeCanteens = new ArrayList<Canteen>();
	private HashMap<String, Canteen> availableCanteens = new HashMap<String, Canteen>();
	private String activeCanteen = "1";
	private SpinnerAdapter mSpinnerAdapter;
	
	Gson gson = new Gson();

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// Create the adapter that will return a fragment for each of the three
		// primary sections
		// of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// set date to today
		// get the current date
		final Calendar c = Calendar.getInstance();
		mYear = c.get(Calendar.YEAR);
		mMonth = c.get(Calendar.MONTH);
		mDay = c.get(Calendar.DAY_OF_MONTH);

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);

		ActionBar actionBar = getActionBar();

		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

		try {
			refreshAvailableCanteens();
		} catch (Exception ex) {
			Log.w(TAG, "Exception: " + ex.getMessage());
			if (LOGV) {
				Log.d(TAG, Log.getStackTraceString(ex));
			}
		}

		refreshActiveCanteens();
		mSpinnerAdapter = new ArrayAdapter<Canteen>(this, android.R.layout.simple_spinner_dropdown_item, activeCanteens);

		actionBar.setListNavigationCallbacks(mSpinnerAdapter, this);

		reload();
	}

	/**
	 * Refreshes the canteens in the action bar
	 */
	private void refreshActiveCanteens() {

		Set<String> set = SettingsActivity.getActiveCanteens(this);

		if (set.size() > 0) {
			for (String key : set) {
				if (set.contains(key)) {
					activeCanteens.add(availableCanteens.get(key));
				} else {
					Log.w(TAG, String.format("Key not found: %s", key));
				}				
			}
		}
	}

	/**
	 * Refreshes the available canteens list
	 */
	private void refreshAvailableCanteens() {
		
		//RetrieveCanteenFeedTask task = new RetrieveCanteenFeedTask(this.getActivity());
		//task.execute(new String[] { url });
		
		Log.d(TAG, "Refresh available canteens");
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

		String jsonString = "[{'name':'Griebnitzsee', 'id':'1'}, " +
				"{'name':'Golm', 'id':'2'}, " +
				"{'name':'Neues Palais', 'id':'3'}]";

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(SettingsActivity.KEY_AVAILABLE_CANTEENS, jsonString);
        editor.commit();
        
        Log.d(TAG, String.format("Saved %s canteens", 3));

		
		String json = prefs.getString(SettingsActivity.KEY_AVAILABLE_CANTEENS, "[]");
		Canteen[] canteens = gson.fromJson(json, Canteen[].class);
		for (Canteen canteen : canteens) {
			availableCanteens.put(canteen.key, canteen);
		}

		Log.d(TAG, String.format("Loaded %s canteens", canteens.length));
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	/**
	 * Change the current canteen
	 */
	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		// TODO Auto-generated method stub
		Canteen c = activeCanteens.get(itemPosition);
		Log.d(TAG, String.format("Chose canteen %s", c));
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent settings = new Intent(this, SettingsActivity.class);

		// Handle item selection
		switch (item.getItemId()) {
			case R.id.menu_settings:
				startActivity(settings);
				return true;
			case R.id.reload:
				reload();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals(SettingsActivity.KEY_ACTIVE_CANTEENS)
				|| key.equals(SettingsActivity.KEY_SOURCE_URL)) {
			reload();
		}
	}

	/**
	 * Checks if we have a valid Internet Connection on the device.
	 * 
	 * @param context
	 * @return True if device has internet
	 * 
	 *         Code from: http://www.androidsnippets.org/snippets/131/
	 */
	public static boolean isOnline(Context context) {

		NetworkInfo info = (NetworkInfo) ((ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE))
				.getActiveNetworkInfo();

		if (info == null || !info.isConnected()) {
			return false;
		}
		if (info.isRoaming()) {
			// here is the roaming option you can change it if you want to
			// disable internet while roaming, just return false
			return false;
		}
		return true;
	}

	private void reload() {
		if (!isOnline(MainActivity.this)) {
			new AlertDialog.Builder(MainActivity.this)
					.setNegativeButton("Okay",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
							}).setTitle("Not Connected").setMessage(
							"You are not connected to the Internet.");
		}

		int index = mViewPager.getCurrentItem();
		mSectionsPagerAdapter.getItem(index);
		mSectionsPagerAdapter.notifyDataSetChanged();
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the primary sections of the app.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {
		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		/**
		 * Creates an Item
		 */
		@Override
		public Fragment getItem(int i) {
			Fragment fragment = new DaySectionFragment();
			Bundle args = new Bundle();

			String urlPattern = SettingsActivity.getSourceUrl(MainActivity.this);
			String url = String.format(urlPattern, activeCanteen);

			args.putString(DaySectionFragment.ARG_URL, url);
			args.putInt(DaySectionFragment.ARG_SECTION_NUMBER, i + 1);
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public int getCount() {
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
				case 0:
					return getString(R.string.title_section1).toUpperCase();
				case 1:
					return getString(R.string.title_section2).toUpperCase();
				case 2:
					return getString(R.string.title_section3).toUpperCase();
			}
			return null;
		}
	}

	/**
	 * A fragment representing a section of the app, that displays the Meals for
	 * one Day.
	 */
	public static class DaySectionFragment extends ListFragment {
		public DaySectionFragment() {
		}

		public static final String ARG_URL = "source_url";
		public static final String ARG_SECTION_NUMBER = "number";
		public static final String ARG_MENSA_NAME = "Griebnitzsee";
		ArrayList<Meal> listItems;
		MealAdapter adapter;

		String url, mensaName;
		int section_number;

		/**
		 * When creating, retrieve this instance's number from its arguments.
		 */
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			Bundle args = getArguments();

			url = args.getString(ARG_URL);
			section_number = args.getInt(ARG_SECTION_NUMBER);
			mensaName = args.getString(ARG_MENSA_NAME);
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

			fetchMealFeed();
		}

		@Override
		public void onListItemClick(ListView l, View v, int position, long id) {
			Log.i("FragmentList", "Item clicked: " + id);
		}

		public void fetchMealFeed()
		{
			RetrieveFeedTask task = new RetrieveMenuFeedTask(this.getActivity());
			task.execute(new String[] { url });
		}

		class RetrieveMenuFeedTask extends RetrieveFeedTask {

			public RetrieveMenuFeedTask(Context context) {
				super(context);
			}
			
			// wrap the meal because the api needs this
			private class WrappedMeal {
				@SerializedName("meal")
				public Meal meal;
			}

			protected void parseFromJSON(String string) {
				WrappedMeal[] meals = gson.fromJson(string, WrappedMeal[].class);
				for (WrappedMeal wrappedMeal : meals) {
					listItems.add(wrappedMeal.meal);
				}
			}

			protected void onPostExecuteFinished() {
				Log.d(TAG, String.format("%s Items", listItems.size()));
				adapter.notifyDataSetChanged();
			}
		}
	}
}
