package com.jamid.formviewapp

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.jamid.formviewapp.form.EmailValidation
import com.jamid.formviewapp.form.FormView
import com.jamid.formviewapp.form.PasswordValidation
import com.jamid.formviewapp.form.RequiredValidation

class MainActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d(TAG, "onCreate: Activity started")

        findViewById<FormView>(R.id.formView)?.apply {
            addField(
                FormView.FieldData(
                "Email", "Email", listOf(RequiredValidation(), EmailValidation())
                ),
                FormView.FieldData(
                    "Password", "Password", listOf(RequiredValidation(), PasswordValidation())
                )
            )
        }

    }

    companion object {
        private const val TAG = "MainActivity"
    }

}