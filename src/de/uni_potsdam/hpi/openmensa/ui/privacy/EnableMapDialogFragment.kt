package de.uni_potsdam.hpi.openmensa.ui.privacy

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import de.uni_potsdam.hpi.openmensa.R
import de.uni_potsdam.hpi.openmensa.api.preferences.SettingsUtils

class EnableMapDialogFragment: DialogFragment() {
    companion object {
        private const val DIALOG_TAG = "EnableMapDialogFragment"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = AlertDialog.Builder(context!!, theme)
            .setTitle(R.string.map_privacy_title)
            .setMessage(R.string.map_privacy_text)
            .setPositiveButton(R.string.map_privacy_accept) { _, _ ->
                SettingsUtils.setEnableMap(context!!, true)
            }
            .setNegativeButton(R.string.map_privacy_reject, null)
            .create()

    fun show(fragmentManager: FragmentManager) = show(fragmentManager, DIALOG_TAG)
}