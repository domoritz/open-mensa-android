package de.uni_potsdam.hpi.openmensa.api.preferences

import android.os.Bundle

import android.view.MenuItem

import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import androidx.lifecycle.Observer

import de.uni_potsdam.hpi.openmensa.R

/**
 *
 * @author dominik
 */
class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val initialTheme = SettingsUtils.getSelectedTheme(this)
        setTheme(initialTheme)

        super.onCreate(savedInstanceState)

        SettingsUtils.getSelectedThemeLive(this).observe(this, Observer {
            if (it != initialTheme) recreate()
        })

        setContentView(R.layout.settings_activity)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        supportFragmentManager.beginTransaction()
                .replace(R.id.container, SettingsFragment())
                .commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                NavUtils.navigateUpFromSameTask(this)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}