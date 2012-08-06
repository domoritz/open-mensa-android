package de.uni_potsdam.hpi.openmensa;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
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
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends FragmentActivity implements OnSharedPreferenceChangeListener {

    public static final String TAG = "Canteendroid";
    public static final Boolean LOGV = true;
    
    private int mYear;
	private int mMonth;
	private int mDay;

	/**
     * The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the
     * sections. We use a {@link android.support.v4.app.FragmentPagerAdapter} derivative, which will
     * keep every loaded fragment in memory. If this becomes too memory intensive, it may be best
     * to switch to a {@link android.support.v4.app.FragmentStatePagerAdapter}.
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
        // Create the adapter that will return a fragment for each of the three primary sections
        // of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        
        // set date to today
     	// get the current date
     	final Calendar c = Calendar.getInstance();
     	mYear = c.get(Calendar.YEAR);
     	mMonth = c.get(Calendar.MONTH);
     	mDay = c.get(Calendar.DAY_OF_MONTH);
     	
     	
     	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
     	prefs.registerOnSharedPreferenceChangeListener(this);
     	
     	reload();
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
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    	if (key.equals(SettingsActivity.KEY_CANTEEN) || key.equals(SettingsActivity.KEY_SOURCE_URL)) {
            reload();
        }
	}
    
    /**
     * Checks if we have a valid Internet Connection on the device.
     * @param context
     * @return True if device has internet
     *
     * Code from: http://www.androidsnippets.org/snippets/131/
     */
    public static boolean isOnline(Context context) {

        NetworkInfo info = (NetworkInfo) ((ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();

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
    	if ( !isOnline(MainActivity.this)) {
    		new AlertDialog.Builder(MainActivity.this)
         	.setNegativeButton("Okay", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
               }}).setTitle("Not Connected").setMessage("You are not connected to the Internet.");
    	}

    	int index = mViewPager.getCurrentItem();
    	mSectionsPagerAdapter.getItem(index);
    	mSectionsPagerAdapter.notifyDataSetChanged();
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
     * sections of the app.
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
            String activeMensa = SettingsActivity.getActiveCanteen(MainActivity.this);
            String url = String.format(urlPattern, activeMensa);
            
            args.putString(DaySectionFragment.ARG_URL, url);
            args.putString(DaySectionFragment.ARG_MENSA_NAME, activeMensa);
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
                case 0: return getString(R.string.title_section1).toUpperCase();
                case 1: return getString(R.string.title_section2).toUpperCase();
                case 2: return getString(R.string.title_section3).toUpperCase();
            }
            return null;
        }
    }

    /**
     * A fragment representing a section of the app, that displays the Meals for one Day.
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
         * The Fragment's UI is just a simple text view showing its
         * instance number.
         */
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
        	
        	View listView = inflater.inflate(R.layout.fragment_pager_list, container, false);
        	//TextView titleView = (TextView) listView.findViewById(R.id.title);
        	//titleView.setText(mensaName);
            return listView;
        }
        
        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            
            listItems = new ArrayList<Meal>();
            
            adapter = new MealAdapter(getActivity(), 
        			R.layout.list_item, listItems);
            
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
        	RetreiveFeedTask task = new RetreiveFeedTask(this.getActivity());
        	task.execute(new String[] { url }); 		
        }
        
        
        class RetreiveFeedTask extends AsyncTask<String, Integer, Integer> {
            private Exception exception;
            ProgressDialog dialog;
            Builder builder;
            Context context;
            
            private final int DEFAULT_BUFFER_SIZE = 1024;
            
            public RetreiveFeedTask(Context context) {
            	// progress dialog
             	dialog = new ProgressDialog(context);
             	dialog.setMessage("Fetching");
             	dialog.setIndeterminate(false);
             	dialog.setMax(100);
             	dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
             	
             	builder = new AlertDialog.Builder(context)
             	.setNegativeButton("Okay", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                   }
               });;
			}
            
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                dialog.show();
            }
            
            private void parseFromJSON(String string) throws JSONException {
            	JSONArray jsonArray = new JSONArray(string);
            	
            	for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject root = jsonArray.getJSONObject(i);
                    JSONObject meal = root.getJSONObject("meal");
                    listItems.add(new Meal(meal.getString("name"), meal.getString("description")));
                }
            }

            protected Integer doInBackground(String... urls) {    
            	for (String url : urls) {
            		try {
                        URL feed = new URL(url);
                        URLConnection urlConnection = feed.openConnection();
                        BufferedReader in = new BufferedReader(new InputStreamReader(
                        		urlConnection.getInputStream()));
                        
                        //urlConnection.connect();
                        
                        StringBuilder builder = new StringBuilder();
                        long fileLength = urlConnection.getContentLength();
                        long total = 0;
                        int count;
                        
                        // content length is sometimes not sent
                        if (fileLength < 0) {
                        	dialog.setIndeterminate(true);
						}
                        
                        char buf[] = new char[DEFAULT_BUFFER_SIZE];
                        while ((count = in.read(buf, 0, DEFAULT_BUFFER_SIZE)) > 0) {
                            total += count;
                            // publishing the progress....
                            publishProgress((int) (total * 100 / fileLength));
                            builder.append(buf, 0, count);
                        }

                        parseFromJSON(builder.toString());
                    } catch (Exception ex){                	
                    	this.exception = ex;
                        return null;
                    }
				}
    			return listItems.size();
            }
            
            protected void onProgressUpdate(Integer... progress) {
                super.onProgressUpdate(progress);
                //Log.d(TAG, ""+progress[0]);
                dialog.setProgress(progress[0]);
            }

            protected void onPostExecute(Integer count) {
            	if (this.exception != null) {
            		Log.w(TAG, "Exception: "+ exception.getMessage());
                    if (LOGV) {
                        Log.d(TAG, Log.getStackTraceString(exception));
                    }
                    
                    showErrorMessage(this.exception);
    			} else {   	
    				Log.d(TAG, String.format("%s Items",count));
    				Log.d(TAG, "Update View");
    	            adapter.notifyDataSetChanged();
    			}
            	
            	if (dialog.isShowing()) {
                    dialog.dismiss();
                }

            }
            
            public void showErrorMessage(Exception ex) {
            	builder.setTitle(ex.getClass().getName());
            	builder.setMessage(ex.toString());
            	builder.show();
            }
         }
    }
}
