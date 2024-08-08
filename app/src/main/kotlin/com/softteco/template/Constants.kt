package com.softteco.template

object Constants {
    const val EMAIL_PATTERN = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$"
    const val USERNAME_PATTERN = "^[a-zA-Z0-9]{3,}\$"
    const val PASSWORD_PATTERN_CAPITALIZED_LETTER = ".*[A-Z].*"
    const val PASSWORD_PATTERN_MIN = ".{6,}"
    const val CONTACT_EMAIL = "softteco.os.dev@gmail.com"
    const val CONTACT_SUBJECT = "User Inquiry or Feedback"
    const val TERMS_OF_SERVICES_URL = "https://softteco.com/terms-of-services"
    const val REQUEST_RETRY_DELAY = 5000L
    const val READ_BLUETOOTH_CHARACTERISTIC_DELAY = 60000L
    const val START_INDEX_OF_TEMPERATURE = 0
    const val END_INDEX_OF_TEMPERATURE = 2
    const val INDEX_OF_HUMIDITY = 2
    const val START_INDEX_OF_BATTERY = 3
    const val END_INDEX_OF_BATTERY = 5
    const val DIVISION_VALUE_OF_VALUES = 100
    const val BIT_SHIFT_VALUE = 8
}
