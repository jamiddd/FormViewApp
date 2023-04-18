package com.jamid.formviewapp.form

import com.jamid.formviewapp.isValidEmail

sealed class ValidationResult {
    data class Valid(val data: String): ValidationResult()
    data class Error(val error: String): ValidationResult()
}

class MinLengthValidation(private val length: Int): ValidationMethod {
    override fun validate(fieldView: FieldView): ValidationResult {
        return if (fieldView.value.length < length) {
            ValidationResult.Error("${fieldView.label} is too short")
        } else {
            ValidationResult.Valid(fieldView.value)
        }
    }
}

class MaxLengthValidation(private val length: Int): ValidationMethod {
    override fun validate(fieldView: FieldView): ValidationResult {
        return if (fieldView.value.length > length) {
            ValidationResult.Error("${fieldView.label} is too long")
        } else {
            ValidationResult.Valid(fieldView.value)
        }
    }
}

class RequiredValidation: ValidationMethod {
    override fun validate(fieldView: FieldView): ValidationResult {
        return if (fieldView.value.isBlank()) {
            ValidationResult.Error("${fieldView.label} cannot be empty")
        } else {
            ValidationResult.Valid(fieldView.value)
        }
    }
}


class EmailValidation: ValidationMethod {
    override fun validate(fieldView: FieldView): ValidationResult {
        return if (!fieldView.value.isValidEmail()) {
            ValidationResult.Error("${fieldView.label} is not a valid email")
        } else {
            ValidationResult.Valid(fieldView.value)
        }
    }
}


class PhoneNumberValidation: ValidationMethod {
    override fun validate(fieldView: FieldView): ValidationResult {
        return if (fieldView.value.length != 10) {
            ValidationResult.Error("${fieldView.label} is not a valid phone number")
        } else {
            ValidationResult.Valid(fieldView.value)
        }
    }
}

class PasswordValidation: ValidationMethod {
    override fun validate(fieldView: FieldView): ValidationResult {
        return if (fieldView.value.isBlank()) {
            ValidationResult.Error("Password cannot be empty")
        } else {
            val password = fieldView.value

            var hasCapital = false
            var hasSmall = false
            var hasDigit = false
            var hasSymbol = false

            val symbols = "~`!@#\$%^&*()_-+={[}]|\\:;\"'<,>.?/"

            for (ch in password) {
                if (ch.code in 65..90) {
                    hasCapital = true
                }

                if (ch.code in 97..122) {
                    hasSmall = true
                }

                if (ch.code in 48..57) {
                    hasDigit = true
                }

                if (ch in symbols){
                    hasSymbol = true
                }
            }

            if (!hasCapital) {
                ValidationResult.Error("Password must contain a capital letter, A-Z")
            } else if (!hasSmall) {
                ValidationResult.Error("Password must contain a small letter, a-z")
            } else if (!hasDigit) {
                ValidationResult.Error("Password must contain a number, 0-9")
            } else if (!hasSymbol) {
                ValidationResult.Error("Password must contain a special character, $symbols")
            } else if (password.length < 8) {
                ValidationResult.Error("Password must be longer than 8 characters")
            } else {
                ValidationResult.Valid(fieldView.value)
            }
        }
    }
}

fun interface ValidationMethod {
    fun validate(fieldView: FieldView): ValidationResult
}