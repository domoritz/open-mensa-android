package de.uni_potsdam.hpi.openmensa

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
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
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.SpinnerAdapter
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.ViewModelStore

import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.Date

import de.uni_potsdam.hpi.openmensa.api.Canteen
import de.uni_potsdam.hpi.openmensa.api.Day
import de.uni_potsdam.hpi.openmensa.api.Days
import de.uni_potsdam.hpi.openmensa.api.preferences.SettingsActivity
import de.uni_potsdam.hpi.openmensa.api.preferences.SettingsUtils
import de.uni_potsdam.hpi.openmensa.api.preferences.Storage
import de.uni_potsdam.hpi.openmensa.helpers.CustomViewPager
import de.uni_potsdam.hpi.openmensa.helpers.OnFinishedFetchingCanteensListener
import de.uni_potsdam.hpi.openmensa.helpers.OnFinishedFetchingDaysListener
import de.uni_potsdam.hpi.openmensa.helpers.RetrieveAsyncTask
import de.uni_potsdam.hpi.openmensa.helpers.RetrieveFeedTask
import de.uni_potsdam.hpi.openmensa.helpers.SpinnerItem

@SuppressLint("NewApi")
class MainActivity : AppCompatActivity(), ActionBar.OnNavigationListener, OnFinishedFetchingCanteensListener, OnFinishedFetchingDaysListener {
    private var spinnerAdapter: SpinnerAdapter? = null
    private var spinnerItems: ArrayList<de.uni_potsdam.hpi.openmensa.data.model.Canteen>? = null

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

    private val model: MainModel by lazy {
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

        storage = SettingsUtils.getStorage(appContext!!)

        createSectionsPageAdapter()


        listener = OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == SettingsUtils.KEY_FAVOURITES) {
                refreshFavouriteCanteens()
            } else if (key == SettingsUtils.KEY_SOURCE_URL) {
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
        refreshFavouriteCanteens()

        // init canteen selection
        model.favoriteCanteens.observe(this, Observer { favoriteCanteens ->
            spinnerItems!!.clear()
            spinnerItems!!.addAll(favoriteCanteens)

            // TODO: handling if nothing selected -> e.g. dialog

            spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, spinnerItems!!)
            actionBar.setListNavigationCallbacks(spinnerAdapter, this)

            val curr = model.currentlySelectedCanteenId.value
            if (curr != null) {
                Log.d(TAG, curr.toString())
                val displayedCanteenPosition = spinnerItems!!.indexOfFirst { it.id == curr }
                actionBar.setSelectedNavigationItem(displayedCanteenPosition)
            }
        })
    }

    public override fun onPause() {
        super.onPause()
        storage.saveToPreferences(appContext)
    }

    public override fun onResume() {
        super.onResume()
        storage.loadFromPreferences(appContext)
        updateMealStorage()
    }

    private fun createSectionsPageAdapter() {
        // Create the adapter that will return a fragment for each day fragment views
        sectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

        // Set up the ViewPager with the sections adapter.
        viewPager = findViewById<View>(R.id.pager) as CustomViewPager
        viewPager.adapter = sectionsPagerAdapter
        // 2 is today
        viewPager.currentItem = 2
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        Log.d(TAG, "Save state, flushed cache storage")
        outState.putInt("page", viewPager.currentItem)
        storage.saveToPreferences(this)
    }

    override fun onRestoreInstanceState(savedState: Bundle) {
        Log.d(TAG, "Restore state")
        viewPager.currentItem = savedState.getInt("page")
    }

    /**
     * Change the current canteen
     *
     * @param canteen which canteen to switch to
     */
    fun changeCanteenTo(canteen: Canteen) {
        if (storage.getCurrentCanteen()!!.key.compareTo(canteen.key) == 0)
            return
        storage.setCurrentCanteen(canteen)
        storage.saveToPreferences(this)

        updateMealStorage()
        sectionsPagerAdapter.notifyDataSetChanged()
    }

    /**
     * Fetch meal data, if not already in storage. Also sets the date for fragments.
     */
    @JvmOverloads
    fun updateMealStorage(force: Boolean? = false) {
        val canteen = storage.getCurrentCanteen() ?: return

        val now = Date()
        val cal = Calendar.getInstance()
        val df = SimpleDateFormat("yyyy-MM-dd")

        var startedFetching: Boolean? = false

        val sections = sectionsPagerAdapter.daySections
        var i = -1
        for (position in sections) {
            cal.time = now
            cal.add(Calendar.DAY_OF_YEAR, i++)
            val date = cal.time

            val dateString = df.format(date)

            val day = canteen.getDay(dateString)

            val fragment = sectionsPagerAdapter.getItem(position!!) as DayFragment
            fragment.date = df.format(date)

            if (startedFetching!!) {
                fragment.setToFetching(true, !fragment.isListShown)
                canteen.justUpdated(dateString)
                continue
            }

            if (day == null || canteen.isOutOfDate(dateString) || force!!) {
                if (day == null) {
                    Log.d(MainActivity.TAG, "Meal cache miss")
                } else if (canteen.isOutOfDate(dateString)) {
                    Log.d(MainActivity.TAG, "Out of date")
                } else if (force!!) {
                    Log.d(MainActivity.TAG, "Forced update")
                }

                if (isOnline(this)) {
                    if (day == null) {
                        val newDay = Days()
                        val nullDay = Day(dateString)
                        newDay.add(nullDay)
                        canteen.updateDays(newDay)
                    }
                    fragment.setToFetching(true, !fragment.isListShown)
                    val baseUrl = SettingsUtils.getSourceUrl(MainActivity.appContext!!)
                    val url = baseUrl + "canteens/" + canteen.key + "/meals/?start=" + dateString
                    RetrieveDaysFeedTask(MainActivity.appContext!!, this, this, Integer.parseInt(canteen.key))
                            .execute()
                    startedFetching = true
                    setProgressBarIndeterminateVisibility(java.lang.Boolean.TRUE)
                } else {
                    Toast.makeText(applicationContext, R.string.noconnection, Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.d(MainActivity.TAG, "Meal cache hit")
                fragment.setToFetching(false, !fragment.isListShown)
            }
        }
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
     * Refreshes the canteens in the action bar
     *
     * TODO: should wait for completion of refreshAvailableCanteens()
     */
    private fun refreshFavouriteCanteens() {
        // TODO: remove this
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

    override fun onCanteenFetchFinished(task: RetrieveCanteenFeedTask) {
        // storage.saveCanteens(this, task.getCanteens());

        refreshFavouriteCanteens()
        updateMealStorage()
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
    private fun reload(force: Boolean = false) {
        storage.loadFromPreferences(this)

        if (isOnline(this@MainActivity)) {
            // fetch meal feed and maybe canteens
            if (storage.areCanteensOutOfDate()!! || storage.isEmpty || force) {
                Log.d(TAG, "Fetch canteens because storage is out of date or empty")
                refreshAvailableCanteens()
            }
            updateMealStorage(force)
        } else {
            if (force) {
                AlertDialog.Builder(this@MainActivity).setNegativeButton(android.R.string.ok
                ) { dialog, id -> dialog.cancel() }.setTitle(R.string.noconnection).setMessage(R.string.pleaseconnect).create().show()
            } else {
                Toast.makeText(applicationContext, R.string.noconnection, Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {

        val TAG = "Canteendroid"
        val LOGV = true

        internal lateinit var storage: Storage

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
