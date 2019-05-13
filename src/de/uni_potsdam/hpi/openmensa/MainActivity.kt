package de.uni_potsdam.hpi.openmensa

import android.content.Intent
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.preference.PreferenceManager

import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
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

// TODO: open tab for today after launch
class MainActivity : FragmentActivity() {
    // TODO: remove this
    internal lateinit var listener: OnSharedPreferenceChangeListener

    val model: MainModel by lazy {
        ViewModelProviders.of(this).get(MainModel::class.java)
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val initialTheme = SettingsUtils.getSelectedTheme(this)
        var lastSnackbar: Snackbar? = null

        setTheme(initialTheme)
        super.onCreate(savedInstanceState)

        SettingsUtils.getSelectedThemeLive(this).observe(this, Observer {
            if (it != initialTheme) recreate()
        })

        setContentView(R.layout.activity_main)

        // setup toolbar
        toolbar.title = title
        toolbar.inflateMenu(R.menu.menu_main)
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))

                    true
                }
                R.id.reload -> {
                    model.refresh(force = true)

                    true
                }
                R.id.canteen_info -> {
                    CanteenFragment().show(supportFragmentManager)

                    true
                }
                else -> false
            }
        }

        // prepare setup screens
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.no_canteen_container, NoCanteenFragment())
                    .commit()
        }

        PrivacyDialogFragment.showIfRequired(this)

        // TODO: remove these things
        listener = OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == SettingsUtils.KEY_SOURCE_URL) {
                // TODO: invalidiate database when changing the URL
            }
        }

        prefs.registerOnSharedPreferenceChangeListener(listener)

        // setup pager
        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)

        pager.adapter = sectionsPagerAdapter

        tabs.setupWithViewPager(pager)

        model.currentDate.observe(this, Observer { sectionsPagerAdapter.currentDate = it })
        model.datesToShow.observe(this, Observer { sectionsPagerAdapter.dates = it })

        // init canteen selection
        val spinnerItems = mutableListOf<Canteen>()
        val spinnerAdapter = object: ArrayAdapter<Canteen>(this, android.R.layout.simple_spinner_dropdown_item, spinnerItems) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View = (super.getView(position, convertView, parent) as TextView).let { view ->
                view.text = spinnerItems[position].name
                view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View = (super.getDropDownView(position, convertView, parent) as TextView).let { view ->
                view.text = spinnerItems[position].name
                view
            }
        }

        spinner.adapter = spinnerAdapter
        spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                model.currentlySelectedCanteenId.postValue(spinnerItems[position].id)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // ignore
            }
        }

        model.favoriteCanteens.observe(this, Observer { favoriteCanteens ->
            spinnerItems.clear()
            spinnerItems.addAll(favoriteCanteens)
            spinnerAdapter.notifyDataSetChanged()

            model.currentlySelectedCanteenId.value?.let { cur ->
                val displayedCanteenPosition = spinnerItems.indexOfFirst { it.id == cur }

                if (spinner.selectedItemPosition != displayedCanteenPosition) {
                    spinner.setSelection(displayedCanteenPosition)
                }
            }
        })

        // do background query of data after canteen selection
        model.currentlySelectedCanteenId.observe(this, Observer {
            if (it != null) {
                // TODO: do this from the model
                model.refresh(force = false)

                lastSnackbar?.dismiss()
            }
        })

        // show sync notifications
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

        // handle empty list of favorite canteens
        model.noFavoriteCanteens.observe(this, Observer {
            flipper.displayedChild = if (it) 1 else 0
            spinner.visibility = if (it) View.GONE else View.VISIBLE
            tabs.visibility = if (it) View.GONE else View.VISIBLE
            toolbar.menu.findItem(R.id.reload).isVisible = !it
            toolbar.menu.findItem(R.id.canteen_info).isVisible = !it
        })
    }

    override fun onResume() {
        super.onResume()

        CanteenSyncing.runBackgroundSync(context = applicationContext)
    }

    override fun onPause() {
        super.onPause()

        model.saveSelectedCanteenId()
    }
}
