package de.uni_potsdam.hpi.openmensa

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.location.LocationManager
import android.os.Bundle
import android.preference.PreferenceManager

import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SpinnerAdapter
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar

import de.uni_potsdam.hpi.openmensa.api.preferences.SettingsActivity
import de.uni_potsdam.hpi.openmensa.api.preferences.SettingsUtils
import de.uni_potsdam.hpi.openmensa.data.model.Canteen
import de.uni_potsdam.hpi.openmensa.sync.*
import de.uni_potsdam.hpi.openmensa.ui.privacy.PrivacyDialogFragment
import de.uni_potsdam.hpi.openmensa.ui.nocanteen.NoCanteenFragment
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

// TODO: open tab for today after launch
@SuppressLint("NewApi")
class MainActivity : FragmentActivity() {
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

        setContentView(R.layout.activity_main)
        toolbar.title = title
        toolbar.inflateMenu(R.menu.menu_main)
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                android.R.id.home -> {
                    // TODO: should this be page 1? or dynamic?
                    pager.currentItem = 2

                    true
                }
                R.id.menu_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))

                    true
                }
                R.id.reload -> {
                    model.refresh(force = true)

                    true
                }
                R.id.canteen_info -> {
                    pager.currentItem = 0

                    true
                }
                else -> false
            }
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.no_canteen_container, NoCanteenFragment())
                    .commit()
        }

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

        // init canteen selection
        model.favoriteCanteens.observe(this, Observer { favoriteCanteens ->
            spinnerItems!!.clear()
            spinnerItems!!.addAll(favoriteCanteens)

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
            spinner.adapter = spinnerAdapter
            spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    model.currentlySelectedCanteenId.postValue(spinnerItems!![position].id)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // ignore
                }
            }

            val curr = model.currentlySelectedCanteenId.value

            if (curr != null) {
                val displayedCanteenPosition = spinnerItems!!.indexOfFirst { it.id == curr }

                if (spinner.selectedItemPosition != displayedCanteenPosition) {
                    spinner.setSelection(displayedCanteenPosition)
                }
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

        model.noFavoriteCanteens.observe(this, Observer {
            flipper.displayedChild = if (it) 1 else 0
            spinner.visibility = if (it) View.GONE else View.VISIBLE
            tabs.visibility = if (it) View.GONE else View.VISIBLE
            toolbar.menu.findItem(R.id.reload).isVisible = !it
            toolbar.menu.findItem(R.id.canteen_info).isVisible = !it
        })

        PrivacyDialogFragment.showIfRequired(this)
    }

    override fun onResume() {
        super.onResume()

        CanteenSyncing.runBackgroundSync(context = applicationContext)
    }

    private fun createSectionsPageAdapter() {
        // Create the adapterOld that will return a fragment for each day fragment views
        sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)

        pager.adapter = sectionsPagerAdapter
        // 2 is today
        pager.currentItem = 2

        tabs.setupWithViewPager(pager)
    }

    companion object {
        var locationManager: LocationManager? = null
            private set
    }
}
