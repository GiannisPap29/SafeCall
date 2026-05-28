package com.pavloskerasidis.mobileapp_safecall.domain.model

sealed interface CallEvent {
    data class Incoming(val number: String?) : CallEvent
    data object Connected : CallEvent
    data object Ended : CallEvent
}
