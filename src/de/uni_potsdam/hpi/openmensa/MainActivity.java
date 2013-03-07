package de.uni_potsdam.hpi.openmensa;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.osmdroid.tileprovider.util.CloudmadeUtil;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import com.google.gson.Gson;

import de.uni_potsdam.hpi.openmensa.api.Canteen;
import de.uni_potsdam.hpi.openmensa.api.Day;
import de.uni_potsdam.hpi.openmensa.api.preferences.SettingsActivity;
import de.uni_potsdam.hpi.openmensa.api.preferences.SettingsProvider;
import de.uni_potsdam.hpi.openmensa.api.preferences.Storage;
import de.uni_potsdam.hpi.openmensa.helpers.CustomViewPager;
import de.uni_potsdam.hpi.openmensa.helpers.OnFinishedFetchingCanteensListener;
import de.uni_potsdam.hpi.openmensa.helpers.OnFinishedFetchingDaysListener;
import de.uni_potsdam.hpi.openmensa.helpers.RetrieveFeedTask;
import de.uni_potsdam.hpi.openmensa.helpers.SpinnerItem;

@SuppressLint("NewApi")
public class MainActivity extends FragmentActivity implements
		OnNavigationListener, OnFinishedFetchingCanteensListener, OnFinishedFetchingDaysListener {

	public static final String TAG = "Canteendroid";
	public static final Boolean LOGV = true;
	public static final String PREFS_NAME = "CanteendroidPrefs";

	static Storage storage;
	private SpinnerAdapter spinnerAdapter;
	private ArrayList<SpinnerItem> spinnerItems;
	
	private static LocationManager locationManager;
	
	private static Context context;
	
	Gson gson = new Gson();
	
	OnSharedPreferenceChangeListener listener;

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter sectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	static CustomViewPager viewPager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		context = this;
		
		locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		
		storage = SettingsProvider.getStorage(context);
		
		createSectionsPageAdapter();

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {	
				if (key.equals(SettingsProvider.KEY_FAVOURITES)) {
					refreshFavouriteCanteens();
				} else if (key.equals(SettingsProvider.KEY_SOURCE_URL)) {
					reload();
				}
			}
		};

		prefs.registerOnSharedPreferenceChangeListener(listener);

		spinnerItems = new ArrayList<SpinnerItem>();
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		try {
			actionBar.setHomeButtonEnabled(true);
		} catch (NoSuchMethodError e) {
			// ignore this error which happens on Android 3
		}
		
		CloudmadeUtil.retrieveCloudmadeKey(MainActivity.context);
		
		reload();
		refreshFavouriteCanteens();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		storage.saveToPreferences(context);
	}

	@Override
	public void onResume() {
		super.onResume();
		storage.loadFromPreferences(context);
		updateMealStorage();
	}

	public static Context getAppContext() {
        return MainActivity.context;
    }
	
	public static LocationManager getLocationManager() {
		return MainActivity.locationManager;
	}

	private void createSectionsPageAdapter() {
		// Create the adapter that will return a fragment for each day fragment views
		sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		viewPager = (CustomViewPager) findViewById(R.id.pager);
		viewPager.setAdapter(sectionsPagerAdapter);
		// 2 is today
		viewPager.setCurrentItem(2);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		Log.d(TAG, "Save state, flushed cache storage");
		outState.putInt("page",	viewPager.getCurrentItem());
		storage.saveToPreferences(this);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedState) {
		Log.d(TAG, "Restore state");
		viewPager.setCurrentItem(savedState.getInt("page"));
	}

	/**
	 * Change the current canteen
	 * 
	 * @param canteen
	 */
	public void changeCanteenTo(Canteen canteen) {
		if (storage.getCurrentCanteen().key.compareTo(canteen.key) == 0)
			return;
		storage.setCurrentCanteen(canteen);
		storage.saveToPreferences(this);
		
		updateMealStorage();
		sectionsPagerAdapter.notifyDataSetChanged();
	}

	public void updateMealStorage() {
		updateMealStorage(false);
	}

	/**
	 * Fetch meal data, if not already in storage. Also sets the date for fragments.
	 */
	public void updateMealStorage(Boolean force) {
		Canteen canteen = storage.getCurrentCanteen();

		if (canteen == null)
			return;
		
		Date now = new Date();
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		
		Boolean startedFetching = false;
		
		ArrayList<Integer> sections = sectionsPagerAdapter.getDaySections();
		int i = -1;
		for (Integer position : sections) {
			cal.setTime(now);
			cal.add(Calendar.DAY_OF_YEAR, i++);
			Date date = cal.getTime();
			
			String dateString = df.format(date);
			
			Day day = canteen.getDay(dateString);
			
			DaySectionFragment fragment = (DaySectionFragment) sectionsPagerAdapter.getItem(position);
			fragment.setDate(df.format(date));
			
			if (startedFetching) {
				fragment.setToFetching(true, !fragment.isListShown());
				canteen.justUpdated(dateString);
				continue;
			}

			if (day == null || canteen.isOutOfDate(dateString) || force) {
				if (day == null) {
					Log.d(MainActivity.TAG, "Meal cache miss");
				} else if (canteen.isOutOfDate(dateString)) {
					Log.d(MainActivity.TAG, "Out of date");
				} else if (force) {
					Log.d(MainActivity.TAG, "Forced update");
				}
				
				if (isOnline(this)) {
					fragment.setToFetching(true, !fragment.isListShown());
					String baseUrl = SettingsProvider.getSourceUrl(MainActivity.context);
					String url = baseUrl + "canteens/" + canteen.key + "/meals/?start=" + dateString;
					RetrieveFeedTask task = new RetrieveDaysFeedTask(MainActivity.context, this, canteen, dateString);
					task.execute(new String[] { url });
					startedFetching = true;
				}
			} else {
				Log.d(MainActivity.TAG, "Meal cache hit");
				fragment.setToFetching(false, !fragment.isListShown());
			}
		}
	}

	@Override
	public void onDaysFetchFinished(RetrieveDaysFeedTask task) {
		// the fragment might have been deleted while we were fetching something
		sectionsPagerAdapter.setToFetching(false, false);
		task.canteen.updateDays(task.getDays());
		task.canteen.justUpdated(task.dateString);
		sectionsPagerAdapter.notifyDataSetChanged();
	}

	/**
	 * Refreshes the canteens in the action bar
	 * 
	 * TODO: should wait for completion of refreshAvailableCanteens()
	 */
	private void refreshFavouriteCanteens() {
		Log.d(TAG, "Refreshing favourite canteen list");
		
		SettingsProvider.updateFavouriteCanteensFromPreferences(context);

		storage.loadFromPreferences(context);
		spinnerItems.clear();
		spinnerItems.addAll(storage.getFavouriteCanteens());
		
		if (spinnerItems.size() == 0 && !storage.getCanteens(this).isEmpty()) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.noactivecanteens)
				.setMessage(R.string.chooseone)
				.setCancelable(true)
				.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int which) {
						dialog.dismiss();
					}
				})
				.setNeutralButton(android.R.string.ok,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int which) {
							Intent settings = new Intent(
									MainActivity.context,
									SettingsActivity.class);
							startActivity(settings);
						}
					});
			AlertDialog alert = builder.create();
			alert.show();
		}
		
		Log.d(TAG, String.format("Spinner items: %s", spinnerItems));
		
		ActionBar actionBar = getActionBar();
		spinnerAdapter = new ArrayAdapter<SpinnerItem>(this, android.R.layout.simple_spinner_dropdown_item, spinnerItems);
		actionBar.setListNavigationCallbacks(spinnerAdapter, this);
		
		Canteen curr = storage.getCurrentCanteen();
		if(curr != null) {
			Log.d(TAG, curr.toString());
			int displayedCanteenPosition = spinnerItems.indexOf(curr);
			actionBar.setSelectedNavigationItem(displayedCanteenPosition);
		}
	}

	/**
	 * Fetches the available canteens list form the server
	 */
	private void refreshAvailableCanteens() {
		String baseUrl = SettingsProvider.getSourceUrl(this);
		String url = baseUrl + "canteens" + "?limit=50";

		RetrieveFeedTask task = new RetrieveCanteenFeedTask(this, this, url);
		task.execute(url);
	}

	@Override
	public void onCanteenFetchFinished(RetrieveCanteenFeedTask task) {
		storage.saveCanteens(this, task.getCanteens());
		
		refreshFavouriteCanteens();
	}

	/**
	 * Is called when an item from the spinner in the action bar is selected.
	 */
	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		SpinnerItem item = spinnerItems.get(itemPosition);
		return item.execute(this, itemPosition);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
			case android.R.id.home:
	        	viewPager.setCurrentItem(2);
	            return true;
			case R.id.menu_settings:
				Intent settings = new Intent(this, SettingsActivity.class);
				startActivity(settings);
				return true;
			case R.id.reload:
				reload(true);  // force update
				return true;
			case R.id.canteen_info:
				viewPager.setCurrentItem(0);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Checks if we have a valid Internet Connection on the device.
	 * 
	 * @param context
	 * @return True if device has Internet
	 * 
	 *  Code from: http://www.androidsnippets.org/snippets/131/
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
			// disable Internet while roaming, just return false
			return false;
		}
		return true;
	}

	private void reload() {
		reload(false);
	}

	/**
	 * Refreshes the meals hash by fetching the data from the API and then displays the latest data.
	 * 
	 */
	private void reload(boolean force) {
		storage.loadFromPreferences(this);
		
		if (isOnline(MainActivity.this)) {
			// fetch meal feed and maybe canteens
			if (storage.areCanteensOutOfDate() || storage.isEmpty() || force) {
				Log.d(TAG, "Fetch canteens because storage is out of date or empty");
				// async
				refreshAvailableCanteens();
			}
			updateMealStorage(force);
		} else {
			if (force) {
				new AlertDialog.Builder(MainActivity.this).setNegativeButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						}).setTitle(R.string.noconnection).setMessage(R.string.pleaseconnect).create().show();
			} else {
				Toast.makeText(getApplicationContext(), R.string.noconnection, Toast.LENGTH_LONG).show();
			}
		}
	}
}
