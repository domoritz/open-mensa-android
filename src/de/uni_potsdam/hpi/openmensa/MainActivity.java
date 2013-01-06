package de.uni_potsdam.hpi.openmensa;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
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

import com.google.gson.Gson;

import de.uni_potsdam.hpi.openmensa.api.Canteen;
import de.uni_potsdam.hpi.openmensa.api.preferences.SettingsActivity;
import de.uni_potsdam.hpi.openmensa.api.preferences.SettingsProvider;
import de.uni_potsdam.hpi.openmensa.api.preferences.Storage;
import de.uni_potsdam.hpi.openmensa.helpers.OnFinishedFetchingCanteensListener;
import de.uni_potsdam.hpi.openmensa.helpers.RetrieveFeedTask;
import de.uni_potsdam.hpi.openmensa.RetrieveCanteenFeedTask;

public class MainActivity extends FragmentActivity implements
		OnSharedPreferenceChangeListener, OnNavigationListener,
		OnFinishedFetchingCanteensListener {

	public static final String TAG = "Canteendroid";
	public static final Boolean LOGV = true;
	public static final String PREFS_NAME = "CanteendroidPrefs";

	static Storage storage = new Storage();
	private SpinnerAdapter spinnerAdapter;
	
	static Context context;
	
	Gson gson = new Gson();

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
	ViewPager viewPager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		context = this;
		
		createSectionsPageAdapter();

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);

		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		
		reload();
		
		refreshActiveCanteens();
		setDatesForFragments();
	}

	private void createSectionsPageAdapter() {
		// Create the adapter that will return a fragment for each day fragment views
		sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		viewPager = (ViewPager) findViewById(R.id.pager);
		viewPager.setAdapter(sectionsPagerAdapter);
	}
	
	public void onSaveInstanceState(Bundle outState) {
		Log.d(TAG, "Flushed cache storage");
		storage.flush(this);
	}
	
	/**
	 * Change the current canteen
	 * 
	 * @param canteen
	 */
	public void changeCanteenTo(Canteen canteen) {
		storage.setCurrentCanteen(canteen);
		storage.flush(this);
		
		setDatesForFragments();
		sectionsPagerAdapter.notifyDataSetChanged();
	}

	/**
	 * Refreshes the canteens in the action bar
	 * 
	 * TODO: should wait for completion of refreshAvailableCanteens()
	 */
	private void refreshActiveCanteens() {
		Log.d(TAG, "Refreshing active canteen list");
		
		ArrayList<Canteen> activeCanteens = storage.getActiveCanteens();
		
		if (activeCanteens.size() == 0 && !storage.getCanteens(this).isEmpty()) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.noactivecanteens)
				.setMessage(R.string.chooseone)
				.setCancelable(false)
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
		
		Log.d(TAG, String.format("%s active canteens", activeCanteens.size()));
		
		ActionBar actionBar = getActionBar();
		spinnerAdapter = new ArrayAdapter<Canteen>(this, android.R.layout.simple_spinner_dropdown_item, activeCanteens);
		actionBar.setListNavigationCallbacks(spinnerAdapter, this);
		
		Canteen curr = storage.getCurrentCanteen();
		if(curr != null) {
			Log.d(TAG, curr.toString());
			int displayedCanteenPosition = activeCanteens.indexOf(curr);
			actionBar.setSelectedNavigationItem(displayedCanteenPosition);
		}
	}

	/**
	 * Refreshes the available canteens list
	 */
	private void refreshAvailableCanteens() {
		// load available canteens from settings and afterwards refetch the list from server
		// TODO: should be avoided by caching..., 
		// TODO: but still needs to refresh the view and set the available canteens
		
		String baseUrl = SettingsProvider.getSourceUrl(this);
		String url = baseUrl + "canteens" + "?limit=50";

		RetrieveFeedTask task = new RetrieveCanteenFeedTask(this, this, url);
		task.execute(url);
	}
	
	@Override
	public void onCanteenFetchFinished(RetrieveCanteenFeedTask task) {
		storage.saveCanteens(this, task.getCanteens());
		
		refreshActiveCanteens();
	}
	
	/**
	 * Set dates for fragments
	 */
	public void setDatesForFragments() {
		Canteen canteen = storage.getCurrentCanteen();

		if (canteen == null)
			return;
		
		Date now = new Date();
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		
		int numberSections = sectionsPagerAdapter.getCount();
		for (int position = 0; position < numberSections; position++) {
			cal.setTime(now);
			cal.add(Calendar.DAY_OF_YEAR, position-1);
			Date date = cal.getTime();
			
			DaySectionFragment fragment = sectionsPagerAdapter.getItem(position);
			fragment.setDate(df.format(date));
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	/**
	 * Is called when another canteen is selected
	 */
	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		Canteen c = storage.getActiveCanteens().get(itemPosition);
		Log.d(TAG, String.format("Chose canteen %s", c.key));
		this.changeCanteenTo(c);
		// TODO: need to refresh the view
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
		if (key.equals(SettingsProvider.KEY_ACTIVE_CANTEENS)) {
			// when changed in settings -> also change in canteens object
			SettingsProvider.refreshActiveCanteens(this);
			refreshActiveCanteens();
		}
		if (key.equals(SettingsProvider.KEY_SOURCE_URL)) {
			reload();
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

	/**
	 * Refreshes the meals hash by fetching the data from the API and then displays the latest data.
	 * 
	 */
	private void reload() {
		storage.refreshStorage(this);
		
		if (isOnline(MainActivity.this)) {
			// fetch meal feed and maybe canteens
			if (storage.isOutOfDate() || storage.isEmpty()) {
				Log.d(TAG, "Fetch canteens because storage is out of date or empty");
				// async
				refreshAvailableCanteens();
			}
			setDatesForFragments();
		} else {
			new AlertDialog.Builder(MainActivity.this).setNegativeButton("Okay",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				}).setTitle("Not Connected").setMessage("You are not connected to the Internet.");
		}
	}
}
