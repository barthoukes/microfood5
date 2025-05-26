package com.hha.resources

import java.util.*

class Timeout {
    companion object {
        private val lock = Any()
    }

    private var startTime: Long = System.currentTimeMillis()
    private var timeout: Long = 1_000_000
    private var detected: Boolean = false
    private var margin: Long = 0
    private var tries: Int = 100_000
    private var minRetries: Int = 10_000

    fun setTimer(timeout: Int, margin: Int, minTries: Int) {
        synchronized(lock) {
            startTime = System.currentTimeMillis()
            this.timeout = timeout.toLong()
            this.margin = margin.toLong()
            this.minRetries = minTries
            detected = false
            tries = 0
        }
    }

    fun running(): Long {
        return System.currentTimeMillis() - startTime
    }

    fun expired(): Boolean {
        return synchronized(lock) {
            when {
                detected -> true
                timeout < 0 -> true
                else -> {
                    val timeNow = System.currentTimeMillis() - startTime
                    if (timeNow >= timeout) {
                        if (++tries > minRetries) {
                            if (timeNow >= timeout + margin && margin != 0L) {
                                startTime = System.currentTimeMillis()
                                tries = 0
                                false
                            } else {
                                detected = true
                                true
                            }
                        } else {
                            false
                        }
                    } else {
                        false
                    }
                }
            }
        }
    }

    fun stop() {
        synchronized(lock) {
            startTime = 0
            timeout = 10_000
            margin = 10
            minRetries = 10_000
            detected = false
            tries = 0
        }
    }

    fun elapsed(): Long {
        return synchronized(lock) {
            System.currentTimeMillis() - startTime
        }
    }
}