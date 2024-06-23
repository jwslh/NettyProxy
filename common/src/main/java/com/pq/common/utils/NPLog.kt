package com.pq.common.utils

import java.lang.Exception
import java.util.logging.Level
import java.util.logging.Logger

/**
 * @author  calesq
 * @date    2024/6/23
 **/
object NPLog {

    private val PREFIX_TAG = "NP_"

    var currentLevel: Level = Level.INFO

    private fun logger(tag: String): Logger {
        return Logger.getLogger(tag).apply {
            level = when (currentLevel) {
                Level.INFO -> java.util.logging.Level.INFO
                Level.WARN -> java.util.logging.Level.WARNING
                Level.ERROR -> java.util.logging.Level.SEVERE
                else -> java.util.logging.Level.FINER
            }
        }
    }

    fun d(subTag: String, msg: String?) {
        printLog(Level.DEBUG) {
            logger("$PREFIX_TAG$subTag").log(java.util.logging.Level.FINE, msg)
        }
    }

    fun i(subTag: String, msg: String?) {
        printLog(Level.INFO) {
            logger("$PREFIX_TAG$subTag").log(java.util.logging.Level.INFO, msg)
        }
    }

    fun w(subTag: String, msg: String?) {
        printLog(Level.WARN) {
            logger("$PREFIX_TAG$subTag").log(java.util.logging.Level.WARNING, msg)
        }
    }

    fun e(subTag: String, msg: String?, exception: Exception? = null) {
        printLog(Level.ERROR) {
            logger("$PREFIX_TAG$subTag").log(java.util.logging.Level.SEVERE, msg, exception)
        }
    }

    private fun printLog(level: Level, action: () -> Unit) {
        if (level.ordinal >= currentLevel.ordinal) {
            action.invoke()
        }
    }

    enum class Level {
        DEBUG,
        INFO,
        WARN,
        ERROR;
    }
}