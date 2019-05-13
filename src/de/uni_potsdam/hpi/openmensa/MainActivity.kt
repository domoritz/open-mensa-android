package de.uni_potsdam.hpi.openmensa

import android.annotation.SuppressLint
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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar

import de.uni_potsdam.hpi.openmensa.api.preferences.SettingsActivity
import de.uni_potsdam.hpi.openmensa.api.preferences.SettingsUtils
import de.uni_potsdam.hpi.openmensa.data.model.Canteen
import de.uni_potsdam.hpi.openmensa.helpers.MapViewPager
import de.uni_potsdam.hpi.openmensa.sync.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

// TODO: fix wrong headers for days (data always starting at today -> remove yesterday)
// TODO: first time setup
// TODO: open tab for today after launch
@SuppressLint("NewApi")
class MainActivity : AppCompatActivity(), ActionBar.OnNavigationListener {
    private var spinnerAdapter: SpinnerAdapter? = null
    private var spinnerItems: ArrayList<Canteen>? = null
    private var lastSnackbar: Snackbar? = null

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
                // TODO: invalidiate database when changing the URL
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
            if (it != null) {
                // TODO: do this from the model
                model.refresh(force = false)

                lastSnackbar?.dismiss()
            }
        })

        model.currentDate.observe(this, Observer { sectionsPagerAdapter.currentDate = it })
        model.datesToShow.observe(this, Observer { sectionsPagerAdapter.dates = it })

        model.syncStatus.observe(this, Observer {
            if (it == MealSyncingDone) {
                lastSnackbar = Snackbar.make(pager, R.string.sync_snackbar_done, Snackbar.LENGTH_SHORT).apply { show() }

                model.confirmSyncStatus()
            } else if (it == MealSyncingFailed) {
                lastSnackbar = Snackbar.make(pager, R.string.sync_snackbar_failed, Snackbar.LENGTH_LONG)
                        .setAction(R.string.sync_snackbar_retry) { model.refresh(true) }
                        .apply { show() }

                model.confirmSyncStatus()
            } else if (it == MealSyncingRunning) {
                lastSnackbar = Snackbar.make(pager, R.string.sync_snackbar_running, Snackbar.LENGTH_SHORT).apply { show() }
            }
        })
    }

    override fun onResume() {
        super.onResume()

        CanteenSyncing.runBackgroundSync(context = applicationContext)
    }

    private fun createSectionsPageAdapter() {
        // Create the adapterOld that will return a fragment for each day fragment views
        sectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

        // Set up the ViewPager with the sections adapterOld.
        viewPager = findViewById<View>(R.id.pager) as MapViewPager
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
                model.refresh(force = true)
                return true
            }
            R.id.canteen_info -> {
                viewPager.currentItem = 0
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
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
        internal lateinit var viewPager: MapViewPager

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
