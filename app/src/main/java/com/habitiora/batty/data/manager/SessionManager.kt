package com.habitiora.batty.data.manager

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor() {
    private val _sessionId = MutableStateFlow("")
    val sessionId: StateFlow<String> = _sessionId

    fun startNewSession(isCharging: Boolean) {
        _sessionId.value = generateSessionId(isCharging)
    }

    private fun generateSessionId(isCharging: Boolean): String {
        val prefix = if (isCharging) "charge" else "discharge"
        val now = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            .format(Date())
        return "${prefix}_$now"
    }
}
