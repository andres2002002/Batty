package com.habitiora.batty.domain.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Stable
sealed interface ServiceState {
    /** Estado inicial — aún no se sabe si el servicio está corriendo */
    data object Loading : ServiceState

    /** ServiceConnection conectada y el servicio responde */
    data object Active : ServiceState

    /**
     * No está corriendo — monitorBattery=false o el usuario lo detuvo.
     * Estado esperado, no es un error.
     */
    data object Inactive : ServiceState

    /**
     * monitorBattery=true pero el servicio no está corriendo o se desconectó
     * inesperadamente. Puede ser un crash del proceso o fallo al iniciar.
     */
    @Immutable
    data class Error(val cause: ServiceErrorCause) : ServiceState
}

enum class ServiceErrorCause {
    /** onServiceDisconnected — el proceso del servicio murió inesperadamente */
    UNEXPECTED_DISCONNECT,
    /** bindService devolvió false — el servicio no existe o no pudo iniciarse */
    BIND_FAILED,
}