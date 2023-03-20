package de.uni_potsdam.hpi.openmensa

import android.content.Intent
import android.os.Bundle

import android.view.*
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import de.uni_potsdam.hpi.openmensa.databinding.ActivityMainBinding

import de.uni_potsdam.hpi.openmensa.ui.settings.SettingsActivity
import de.uni_potsdam.hpi.openmensa.helpers.SettingsUtils
import de.uni_potsdam.hpi.openmensa.sync.*
import de.uni_potsdam.hpi.openmensa.ui.canteenlist.small.SmallCanteenListDialogFragment
import de.uni_potsdam.hpi.openmensa.ui.privacy.PrivacyDialogFragment
import de.uni_potsdam.hpi.openmensa.ui.nocanteen.NoCanteenFragment

class MainActivity : FragmentActivity() {
    private var isFirstResume = true

    val model: MainModel by lazy {
        ViewModelProviders.of(this).get(MainModel::class.java)
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        val settings = SettingsUtils.with(this)
        val initialTheme = settings.selectedTheme
        var lastSnackbar: Snackbar? = null

        setTheme(initialTheme)
        super.onCreate(savedInstanceState)

        settings.selectedThemeLive.observe(this, Observer {
            if (it != initialTheme) recreate()
        })

        val binding = ActivityMainBinding.inflate(layoutInflater).also {
            setContentView(it.root)
        }

        // setup toolbar
        binding.toolbar.title = title
        binding.toolbar.inflateMenu(R.menu.menu_main)
        binding.toolbar.setOnMenuItemClickListener { item ->
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
                R.id.menu_star -> {
                    model.toggleFavorite()

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

        // setup pager
        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)

        binding.pager.adapter = sectionsPagerAdapter

        binding.tabs.setupWithViewPager(binding.pager)

        model.currentDate.observe(this, Observer { sectionsPagerAdapter.currentDate = it })
        model.datesToShow.observe(this, Observer { sectionsPagerAdapter.dates = it })

        // init canteen selection
        model.currentlySelectedCanteen.observe(this, Observer { currentCanteen ->
            binding.spinnerText.text = currentCanteen?.canteen?.name ?: ""

            val noCanteen = currentCanteen == null
            val hasCanteen = currentCanteen != null

            binding.flipper.displayedChild = if (noCanteen) 1 else 0
            binding.spinner.visibility = if (noCanteen) View.GONE else View.VISIBLE
            binding.tabs.visibility = if (noCanteen) View.GONE else View.VISIBLE
            binding.toolbar.menu.findItem(R.id.reload).isVisible = hasCanteen
            binding.toolbar.menu.findItem(R.id.canteen_info).isVisible = hasCanteen
            binding.toolbar.menu.findItem(R.id.menu_star).isVisible = hasCanteen
        })
        binding.spinner.setOnClickListener { SmallCanteenListDialogFragment().show(supportFragmentManager) }
        binding.spinnerImage.setImageDrawable(DrawableCompat.wrap(ContextCompat.getDrawable(this, R.drawable.ic_arrow_drop_down_black_24dp)!!).apply {
            DrawableCompat.setTint(this, settings.selectedThemeIconColor)
        })

        // do background query of data after canteen selection
        model.currentlySelectedCanteenId.observe(this, Observer {
            lastSnackbar?.dismiss()
        })

        // show sync notifications
        model.syncStatus.observe(this, Observer {
            if (it == MealSyncingDone) {
                lastSnackbar = Snackbar.make(binding.pager, R.string.sync_snackbar_done, Snackbar.LENGTH_SHORT).apply { show() }

                model.confirmSyncStatus()
            } else if (it == MealSyncingFailed) {
                lastSnackbar = Snackbar.make(binding.pager, R.string.sync_snackbar_failed, Snackbar.LENGTH_LONG)
                        .setAction(R.string.sync_snackbar_retry) { model.refresh(true) }
                        .apply { show() }

                model.confirmSyncStatus()
            } else if (it == MealSyncingRunning) {
                lastSnackbar = Snackbar.make(binding.pager, R.string.sync_snackbar_running, Snackbar.LENGTH_SHORT).apply { show() }
            }
        })

        model.isSelectedCanteenFavorite.observe(this, Observer { isFavorite ->
            val star = binding.toolbar.menu.findItem(R.id.menu_star)

            star.icon = DrawableCompat.wrap(ContextCompat.getDrawable(
                    this,
                    if (isFavorite) R.drawable.ic_star_black_24dp else R.drawable.ic_star_border_black_24dp
            )!!).apply {
                DrawableCompat.setTint(this, settings.selectedThemeIconColor)
            }
            star.setTitle(if (isFavorite) R.string.menu_favorite_rm else R.string.menu_favorite_add)
        })
    }

    override fun onResume() {
        super.onResume()

        CanteenSyncing.runBackgroundSync(context = applicationContext)

        if (isFirstResume) {
            isFirstResume = false
        } else {
            model.refresh(force = false)
        }
    }

    override fun onPause() {
        super.onPause()

        model.saveSelectedCanteenId()
    }
}
