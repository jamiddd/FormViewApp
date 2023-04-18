package com.jamid.formviewapp.form

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.addTextChangedListener
import com.jamid.formviewapp.R
import com.jamid.formviewapp.hide
import com.jamid.formviewapp.setPasswordVisibility
import com.jamid.formviewapp.show

@SuppressLint("ViewConstructor")
class FieldView @JvmOverloads constructor(
    val label: String,
    context: Context,
    attrs: AttributeSet? = null,
    val validationMethods: List<ValidationMethod>? = null
) : ConstraintLayout(context, attrs) {

    private var mText: EditText? = null
    private var mHelperText: TextView? = null
    private var mBtn: Button? = null
    var error: String? = null

    val value: String
        get() {
            return mText?.text?.toString() ?: ""
        }

    init {
        val root = LayoutInflater.from(context).inflate(R.layout.field_edit_text_layout, this, true)

        mText = root.findViewById(R.id.field_edit_text)
        mHelperText = root.findViewById(R.id.field_helper_text)
        mBtn = root.findViewById(R.id.end_action)

        attrs?.let {
            val styledAttributes =
                context.obtainStyledAttributes(it, R.styleable.FieldView, 0, 0)

            /* currency = styledAttributes.getString(R.styleable.CurrencyView_defaultCurrency) ?: "IN"
             amount = (styledAttributes.getString(R.styleable.CurrencyView_defaultAmount)?.toDouble()
                 ?.times(100))?.toLong() ?: 0
             currencyHint = styledAttributes.getString(R.styleable.CurrencyView_currencyHint)*/

            styledAttributes.recycle()
        }

        if (label.contains("Password") || label.contains("password")) {
            // show end icon as password visibility icon
            mBtn?.show()
            mText?.let { mBtn?.setPasswordVisibility(it) }
        }

        mText?.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                // upon losing focus, trim the end only if it's not password
                if (label != "Password") {
                    val s = mText?.text?.trimEnd()
                    mText?.setText(s)
                }
            }
        }

        validate(false)

    }

    fun setStartIcon(icon: Drawable?) {
        mText?.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null)
    }

    fun setFieldHint(hint: String) {
        mText?.hint = hint
    }

    fun showError() {
        if (error != null) {
            mHelperText?.text = error
            mHelperText?.show()
        }
    }

    fun hideError() {
        mHelperText?.hide()
    }

    fun clear() {
        mText?.text?.clear()
        hideError()
    }

    fun validate(show: Boolean = true, errors: Array<String>? = null): Boolean {
        if (validationMethods != null) {
            for (i in validationMethods.indices) {
                when (val res = validationMethods[i].validate(this)) {
                    is ValidationResult.Error -> {
                        error = res.error
                        errors?.set(i, res.error)

                        if (show)
                            showError()
                        else
                            hideError()

                        break
                    }
                    is ValidationResult.Valid -> {
                        error = null
                        continue
                    }
                }
            }
        }

        return error == null
    }

    fun trim() {
        val p = listOf("Password", "password")
        if (!p.contains(label)) {
            val s = value.trimEnd()
            mText?.setText(s)
            mText?.setSelection(s.length)
        }
    }

    fun setFieldTextListener(function: () -> Unit) {
        mText?.addTextChangedListener {
            function()
        }
    }

}