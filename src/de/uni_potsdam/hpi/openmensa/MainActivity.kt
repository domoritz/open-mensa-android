package de.uni_potsdam.hpi.openmensa

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import de.uni_potsdam.hpi.openmensa.ui.viewer.ViewerActivity

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startActivity(
            Intent(this, ViewerActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        )

        finish()
    }
}
