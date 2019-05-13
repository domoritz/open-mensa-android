package de.uni_potsdam.hpi.openmensa.extension

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

fun EditText.addTextChangeListener(listener: (EditText) -> Unit) {
    val editText = this

    editText.addTextChangedListener(object: TextWatcher {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            listener(editText)
        }

        override fun afterTextChanged(s: Editable?) {
            // ignore
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            // ignore
        }
    })
}