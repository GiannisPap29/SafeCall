package com.pavloskerasidis.mobileapp_safecall.core.logging

import timber.log.Timber

interface Logger {
    fun d(tag: String, message: String)
    fun i(tag: String, message: String)
    fun w(tag: String, message: String, throwable: Throwable? = null)
    fun e(tag: String, message: String, throwable: Throwable? = null)
}

class TimberLogger : Logger {
    override fun d(tag: String, message: String) { Timber.tag(tag).d(message) }
    override fun i(tag: String, message: String) { Timber.tag(tag).i(message) }
    override fun w(tag: String, message: String, throwable: Throwable?) { Timber.tag(tag).w(throwable, message) }
    override fun e(tag: String, message: String, throwable: Throwable?) { Timber.tag(tag).e(throwable, message) }
}
