package de.uni_potsdam.hpi.openmensa

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.preference.PreferenceManager

import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import android.util.Log
import android.view.*
import android.widget.ArrayAdapter
import android.widget.SpinnerAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders

import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.Date

import de.uni_potsdam.hpi.openmensa.api.preferences.SettingsActivity
import de.uni_potsdam.hpi.openmensa.api.preferences.SettingsUtils
import de.uni_potsdam.hpi.openmensa.data.model.Canteen
import de.uni_potsdam.hpi.openmensa.helpers.CustomViewPager
import de.uni_potsdam.hpi.openmensa.helpers.OnFinishedFetchingCanteensListener
import de.uni_potsdam.hpi.openmensa.helpers.OnFinishedFetchingDaysListener
import de.uni_potsdam.hpi.openmensa.ui.day.DayFragment

// TODO: update canteens if not yet done
// TODO: update canteens if not done during the last 14 days
// TODO: add refresh indicator at meal list
// TODO: refresh meals if older than 1 hour
// TODO: fix wrong headers for days (data always starting at today -> remove yesterday)
// TODO: first time setup
// TODO: open tab for today after launch
@SuppressLint("NewApi")
class MainActivity : AppCompatActivity(), ActionBar.OnNavigationListener, OnFinishedFetchingCanteensListener, OnFinishedFetchingDaysListener {
    private var spinnerAdapter: SpinnerAdapter? = null
    private var spinnerItems: ArrayList<Canteen>? = null

    internal lateinit var listener: OnSharedPreferenceChangeListener

    /**
     * The [PagerAdapter] that will provide
     * fragments for each of the sections. We use a
     * [FragmentPagerAdapter] derivative, which
     * will keep every loaded fragment in memory. If this becomes too memory
     * intensive, it may be best to switch to a
     * [FragmentStatePagerAdapter].
     */
    internal lateinit var sectionsPagerAdapter: SectionsPagerAdapter

    val model: MainModel by lazy {
        ViewModelProviders.of(this).get(MainModel::class.java)
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)

        setTheme(SettingsUtils.getSelectedTheme(this))
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS)

        setContentView(R.layout.activity_main)

        appContext = this

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        createSectionsPageAdapter()


        listener = OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == SettingsUtils.KEY_SOURCE_URL) {
                // TODO: solve this differently
                reload()
            } else if (key == SettingsUtils.KEY_STYLE) {
                recreate()
            }
        }

        prefs.registerOnSharedPreferenceChangeListener(listener)

        spinnerItems = ArrayList()
        val actionBar = supportActionBar
        actionBar!!.navigationMode = ActionBar.NAVIGATION_MODE_LIST
        try {
            actionBar.setHomeButtonEnabled(true)
        } catch (e: NoSuchMethodError) {
            // ignore this error which happens on Android 3
        }

        val spinner = layoutInflater.inflate(R.layout.spinner_layout, null)
        actionBar.customView = spinner
        actionBar.setDisplayShowCustomEnabled(true)

        //we must do this after setting the style
        reload()

        // init canteen selection
        model.favoriteCanteens.observe(this, Observer { favoriteCanteens ->
            spinnerItems!!.clear()
            spinnerItems!!.addAll(favoriteCanteens)

            // TODO: handling if nothing selected -> e.g. dialog

            spinnerAdapter = object: ArrayAdapter<Canteen>(this, android.R.layout.simple_spinner_dropdown_item, spinnerItems!!) {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View = (super.getView(position, convertView, parent) as TextView).let { view ->
                    view.text = spinnerItems!![position].name
                    view
                }

                override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View = (super.getDropDownView(position, convertView, parent) as TextView).let { view ->
                    view.text = spinnerItems!![position].name
                    view
                }
            }
            actionBar.setListNavigationCallbacks(spinnerAdapter, this)

            val curr = model.currentlySelectedCanteenId.value
            if (curr != null) {
                Log.d(TAG, curr.toString())
                val displayedCanteenPosition = spinnerItems!!.indexOfFirst { it.id == curr }
                actionBar.setSelectedNavigationItem(displayedCanteenPosition)
            }
        })

        model.currentlySelectedCanteenId.observe(this, Observer {
            // FIXME: use a better way to load content
            if (it != null) {
                Toast.makeText(appContext, "SYNC $it", Toast.LENGTH_SHORT).show()

                RetrieveDaysFeedTask(MainActivity.appContext!!, this, this, it)
                        .execute()
            }
        })

        model.currentlySelectedCanteen.observe(this, Observer { canteen ->
            val days = canteen?.days ?: emptyList()

            // FIXME: the section labels are horrible wrong with that
            sectionsPagerAdapter.dates = days.map { it.date }
        })
    }

    private fun createSectionsPageAdapter() {
        // Create the adapterOld that will return a fragment for each day fragment views
        sectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

        // Set up the ViewPager with the sections adapterOld.
        viewPager = findViewById<View>(R.id.pager) as CustomViewPager
        viewPager.adapter = sectionsPagerAdapter
        // 2 is today
        viewPager.currentItem = 2
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("page", viewPager.currentItem)
    }

    override fun onRestoreInstanceState(savedState: Bundle) {
        Log.d(TAG, "Restore state")
        viewPager.currentItem = savedState.getInt("page")
    }

    override fun onDaysFetchFinished(task: RetrieveDaysFeedTask) {
        // the fragment might have been deleted while we were fetching something
        sectionsPagerAdapter.setToFetching(false, false)
        sectionsPagerAdapter.notifyDataSetChanged()

        //if (task.noPending()) {
        setProgressBarIndeterminateVisibility(java.lang.Boolean.FALSE)
        //}
    }

    /**
     * Fetches the available canteens list form the server
     */
    private fun refreshAvailableCanteens() {
        val baseUrl = SettingsUtils.getSourceUrl(this)
        val url = baseUrl + "canteens" + "?limit=50"

        val task = RetrieveCanteenFeedTask(this, this, this)
        task.execute(url)
    }

    // TODO: remove this
    override fun onCanteenFetchFinished(task: RetrieveCanteenFeedTask) {
    }

    /**
     * Is called when an item from the spinner in the action bar is selected.
     */
    override fun onNavigationItemSelected(itemPosition: Int, itemId: Long): Boolean {
        val item = spinnerItems!![itemPosition]

        model.currentlySelectedCanteenId.postValue(item.id)

        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        when (item.itemId) {
            android.R.id.home -> {
                viewPager.currentItem = 2
                return true
            }
            R.id.menu_settings -> {
                val settings = Intent(this, SettingsActivity::class.java)
                startActivity(settings)
                return true
            }
            R.id.reload -> {
                reload(true)  // force update
                return true
            }
            R.id.canteen_info -> {
                viewPager.currentItem = 0
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    /**
     * Refreshes the meals hash by fetching the data from the API and then displays the latest data.
     *
     */
    // TODO: remove this
    private fun reload(force: Boolean = false) {
        if (isOnline(this@MainActivity)) {
            // fetch meal feed and maybe canteens
            //if (storage.areCanteensOutOfDate()!! || storage.isEmpty || force) {
                //Log.d(TAG, "Fetch canteens because storage is out of date or empty")
                refreshAvailableCanteens()
            //}
            //updateMealStorage(force)
        }/* else {
            if (force) {
                AlertDialog.Builder(this@MainActivity).setNegativeButton(android.R.string.ok
                ) { dialog, id -> dialog.cancel() }.setTitle(R.string.noconnection).setMessage(R.string.pleaseconnect).create().show()
            } else {
                Toast.makeText(applicationContext, R.string.noconnection, Toast.LENGTH_LONG).show()
            }
        }*/
    }

    companion object {

        val TAG = "Canteendroid"
        val LOGV = true

        var locationManager: LocationManager? = null
            private set

        var appContext: Context? = null
            private set

        /**
         * The [ViewPager] that will host the section contents.
         */
        internal lateinit var viewPager: CustomViewPager

        /**
         * Checks if we have a valid Internet Connection on the device.
         *
         * @param context app context
         * @return True if device has Internet
         *
         * Code from: http://www.androidsnippets.org/snippets/131/
         */
        fun isOnline(context: Context): Boolean {

            val info = (context
                    .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)
                    .activeNetworkInfo as NetworkInfo

            if (info == null || !info.isConnected) {
                return false
            }
            return if (info.isRoaming) {
                // here is the roaming option you can change it if you want to
                // disable Internet while roaming, just return false
                false
            } else true
        }
    }
}
