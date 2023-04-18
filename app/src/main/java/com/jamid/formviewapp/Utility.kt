package com.jamid.formviewapp

import android.content.Context
import android.text.method.PasswordTransformationMethod
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.core.view.isVisible

fun CharSequence?.isValidEmail() =
    !isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun hide(vararg v: View) {
    for (i in v)
        i.hide()
}

fun show(vararg v: View) {
    for (i in v)
        i.show()
}

fun View.disable() {
    isEnabled = false
}

fun View.enable() {
    isEnabled = true
}

fun View.show() {
    isVisible = true
}

fun View.hide() {
    isVisible = false
}

class Size(private val context: Context) {

    val _56dp: Float
        get() {
            return context.resources.getDimension(R.dimen.action_bar)
        }

}

fun Button.setPasswordVisibility(parent: EditText) {
    parent.transformationMethod = PasswordTransformationMethod()

    this.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.round_visibility_24, 0, 0, 0)

    this.setOnClickListener {
        val currentCursorPosition = parent.selectionEnd
        isSelected = !isSelected

        if (parent.transformationMethod == null) {
            parent.transformationMethod = PasswordTransformationMethod()
        } else {
            parent.transformationMethod = null
        }
        parent.setSelection(currentCursorPosition)

        if (isSelected) {
            this.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.round_visibility_off_24, 0, 0, 0)
        } else {
            this.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.round_visibility_24, 0, 0, 0)
        }
    }

}