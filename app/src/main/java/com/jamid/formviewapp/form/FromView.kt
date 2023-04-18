package com.jamid.formviewapp.form

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.jamid.formviewapp.R
import com.jamid.formviewapp.Size
import kotlinx.coroutines.runBlocking

class FormView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null): LinearLayout(context, attrs) {

    private var mFieldContainer: LinearLayout? = null
    private var mSingleErrorText: TextView? = null
    private var mProgressBar: ProgressBar? = null
    private var mSubmitBtn: Button? = null
    private var mCustomLayoutHolder: FrameLayout? = null

    private val labelToFieldMap = mutableMapOf<String, FieldView>()

    private var isSingleErrorForm = false
    private var isReactive = false

    private var error: String? = null

    private val _isValid = MutableLiveData<Boolean>().apply { value = false }
    val isValid: LiveData<Boolean> = _isValid

    private var mSubmitAction:  (suspend () -> Result<Any>)? = null
    private var hasAnimationEnded = true

    private val size = Size(context)

    interface FormListener {

        /**
         * Invoked upon pressing submit button
         * */
        fun onSubmit(result: Result<Any>)

        /**
         * Invoked upon any data change such as text content
         * TODO("Data is not yet formed nicely")
         * */
        fun onDataChange(data: Map<String, Any?>)
    }

    private var mFormListener: FormListener? = object: FormListener {
        override fun onSubmit(result: Result<Any>) {

        }

        override fun onDataChange(data: Map<String, Any?>) {

        }
    }

    init {
        val root = LayoutInflater.from(context).inflate(R.layout.form_layout, this, true)

        mFieldContainer = root.findViewById(R.id.fields_container)
        mSingleErrorText = root.findViewById(R.id.single_error_text)
        mProgressBar = root.findViewById(R.id.form_progress_bar)
        mSubmitBtn = root.findViewById(R.id.form_submit_btn)
        mCustomLayoutHolder = root.findViewById(R.id.custom_view_holder)

        var submitBtnLabel: String? = null

        attrs?.let {
            val styledAttributes =
                context.obtainStyledAttributes(it, R.styleable.FormView, 0, 0)

            submitBtnLabel = styledAttributes.getString(R.styleable.FormView_submitBtnLabel)
            isSingleErrorForm = styledAttributes.getBoolean(R.styleable.FormView_isSingleErrorForm, false)
            isReactive = styledAttributes.getBoolean(R.styleable.FormView_reactive, false)
            /* currency = styledAttributes.getString(R.styleable.CurrencyView_defaultCurrency) ?: "IN"
             amount = (styledAttributes.getString(R.styleable.CurrencyView_defaultAmount)?.toDouble()
                 ?.times(100))?.toLong() ?: 0
             currencyHint = styledAttributes.getString(R.styleable.CurrencyView_currencyHint)*/

            styledAttributes.recycle()
        }

        setSubmitButton(submitBtnLabel)

        setFieldsListeners()

    }


    data class FieldData(
        val label: String,
        val hint: String = "",
        val vm: List<ValidationMethod> = emptyList(),
        val startIcon: Drawable? = null
    )

    fun addField(vararg fieldData: FieldData, startIndex: Int = -1) {
        var currentPos = startIndex

        for (fd in fieldData) {
            if (startIndex != -1)
                addField(fd.label, fd.hint, fd.vm, fd.startIcon, currentPos++)
            else
                addField(fd.label, fd.hint, fd.vm, fd.startIcon)
        }
    }


    /* have a map that contains the id and field by some relation */
    private fun addField(label: String, hint: String = "", vm: List<ValidationMethod> = emptyList(), icon: Drawable? = null, at: Int = -1) {
        val fieldView = FieldView(label, context, validationMethods = vm)
        fieldView.setFieldHint(hint)
        fieldView.tag = label
        fieldView.setStartIcon(icon)
        labelToFieldMap[label] = fieldView

        if (at < 0) {
            mFieldContainer?.addView(fieldView)
        } else {
            mFieldContainer?.addView(fieldView, at)
        }

        setFieldsListeners()
    }

    fun removeField(label: String) {
        labelToFieldMap.remove(label)

        val fieldViewToRemove = mFieldContainer?.findViewWithTag<FieldView>(label)

        if (fieldViewToRemove != null) {
            mFieldContainer?.removeView(fieldViewToRemove)
        }
    }

    private fun setFormSubmitActionLabel(label: String) {
        mSubmitBtn?.text = label
    }

    private fun setSubmitButton(label: String? = null) {

        if (label != null) {
            setFormSubmitActionLabel(label)
        }

        mSubmitBtn?.setOnClickListener {
            runBlocking {

                // always trim everything before checking
                labelToFieldMap.forEach { (_, u) ->
                    u.trim()
                }

                // checking one last time, might be redundant, only use case is when a field is added, the error of that field
                // is not added automatically and waits for user input on textWatcher
                check()

                if (!isReactive) {
                    showErrors()
                }

                if (isValid.value == true) {
                    val res = mSubmitAction?.invoke()
                    if (res != null) {
                        mFormListener?.onSubmit(res)
                    }
                }
            }
        }
    }

    fun setFormListener(listener: FormListener?) {
        mFormListener = listener
    }

    private fun check() {
        val errors = mutableListOf<Array<String>>()

        var flag = true

        labelToFieldMap.forEach { (l, u) ->
            val errorContainer = Array(u.validationMethods?.size ?: 0) { "" }
            flag = flag and u.validate(isReactive, errorContainer)
            errors.add(errorContainer)
        }

        if (isSingleErrorForm) {
            error = formatError(errors)

            if (isReactive) {
                mSingleErrorText?.text = error
            }
        }

        if (isReactive)
            mSubmitBtn?.isEnabled = flag

        _isValid.postValue(flag)
    }

    private fun showErrors() {
        if (!isSingleErrorForm) {
            labelToFieldMap.forEach { (t, u) ->
                u.showError()
            }
        } else {
            showSingleError()
        }
    }


    private fun showSingleError(err: Throwable? = null) {

        if (!isSingleErrorForm)
            return

        if (error == null)
            return

        val e = error

        mSingleErrorText?.apply {

            text = if (err != null) {
                error = err.localizedMessage
                err.localizedMessage
            } else {
                e
            }

            val anim = ValueAnimator.ofInt(0, size._56dp.toInt())
            anim.apply {
                addUpdateListener { valueAnimator ->
                    val h = valueAnimator.animatedValue as Int
                    updateLayoutParams<ViewGroup.LayoutParams> {
                        height = h
                    }
                }
                duration = 250
            }

            anim.start()

        }
    }

    private fun formatError(errors: List<Array<String>>): String? {
        var f = ""
        for (er in errors) {
            for (e in er) {
                if (e.isNotBlank()) {
                    f += "• $e\n"
                }
            }
        }

        val x = f.trimEnd()
        return x.ifBlank {
            null
        }
    }

    private fun removeError() {
        if (!isSingleErrorForm) {
            labelToFieldMap.forEach { (_, u) ->
                u.error = null
                u.hideError()
            }
        } else {
            hideSingleError()
        }
    }

    private fun setFieldsListeners() {
        labelToFieldMap.forEach { (_, u) ->
            u.setFieldTextListener {
                removeError()

                /* Add delay, if the form is reactive */
                check()

                mFormListener?.onDataChange(labelToFieldMap.mapValues {
                    it.value.value
                })
            }
        }
    }

    private fun hideSingleError() {
        if (mSingleErrorText?.height != 0 && hasAnimationEnded) {

            // only run animation if there is no animation running already,
            // and the error view is visible, (in our case when the height is not 0)

            mSingleErrorText?.apply {

                val anim = ValueAnimator.ofInt(measuredHeight, 0)
                anim.apply {
                    addUpdateListener { valueAnimator ->
                        val h = valueAnimator.animatedValue as Int
                        updateLayoutParams<ViewGroup.LayoutParams> {
                            height = h
                        }
                    }
                    duration = 250

                    addListener(object: Animator.AnimatorListener {
                        override fun onAnimationStart(animation: Animator) {
                            hasAnimationEnded = false
                        }

                        override fun onAnimationEnd(animation: Animator) {
                            hasAnimationEnded = true
                        }

                        override fun onAnimationCancel(animation: Animator) {

                        }

                        override fun onAnimationRepeat(animation: Animator) {
                            hasAnimationEnded = false
                        }
                    })
                }

                anim.start()
            }
        }
    }

    fun addCustomLayout(@LayoutRes layout: Int): View? {
        val holder = mCustomLayoutHolder
        return if (holder != null) {
            View.inflate(context, layout, holder)
        } else {
            null
        }
    }

}