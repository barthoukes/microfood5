package com.hha.resources

import android.database.sqlite.SQLiteDatabase
import android.os.Handler
import java.io.File

import com.hha.framework.CTimeFrame
import com.hha.framework.CTransaction
import com.hha.grpc.GrpcServiceFactory
import com.hha.resources.Configuration
import com.hha.types.ETaal

class Global private constructor() {
    // Properties
    var firstTablet : Short = 15
    lateinit var myDir: File
    var currentMenuCardId = 1
    var currentPage = 1
    var currentItem = 5
    var currentKeyIndex = 2
    var cursor = 0
    var language: ETaal = ETaal.LANG_SIMPLIFIED
    var pageOffset = 0
    lateinit var transaction: CTransaction
    var transactionId: Int = 0
    lateinit var timeFrameDB: CTimeFrame
    lateinit var transactionDB: CTransaction
    //lateinit var transactionList: CTransactionList
    var selectedItem = 10
    var pageBackgroundColor = 0xFF400040.toInt()
    var pageSelectedBackgroundColor = 0xFFc000c0.toInt()
    var pageTextColor = 0xFFf0f080.toInt()
    var mainMenuHandler: Handler? = null
    var askTableHandler: Handler? = null
    public val CFG: Configuration = Configuration()
    val userCFG: Configuration = Configuration()
    val fontCFG: Configuration = Configuration()
    val colourCFG: Configuration = Configuration()
    lateinit var timeFrame: CTimeFrame

    companion object {
        const val MY_DATABASE_NAME = "ZhongCan"

        @Volatile
        private var instance: Global? = null

        fun getInstance(): Global {
            return instance ?: synchronized(this) {
                instance ?: Global().also { instance = it }
            }
        }
    }

    fun setPage(page: Int) {
        pageOffset = 0
        currentPage = page
    }

    fun getOptions() {
        val dbs = GrpcServiceFactory.createDatabaseService()
        val config = dbs.getConfigurationList()
        if (config != null) {
            CFG.setConfigurations(config)
        }
        val userConfig = dbs.getUserConfigurationList()
        if (userConfig != null) {
            userCFG.setConfigurations(userConfig)
        }
        val colourConfig = dbs.getColourConfigurationList()
        if (colourConfig != null) {
            colourCFG.setConfigurations(colourConfig)
        }
        val fontConfig = dbs.getFontConfigurationList()
        if (fontConfig != null) {
            fontCFG.setConfigurations(fontConfig)
        }
    }

}