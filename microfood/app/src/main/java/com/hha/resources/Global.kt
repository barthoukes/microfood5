package com.hha.resources

import android.os.Handler
import com.hha.framework.CCursor
import java.io.File

import com.hha.framework.CTimeFrame
import com.hha.framework.CTransaction
import com.hha.grpc.GrpcServiceFactory
import com.hha.types.EAccess
import com.hha.types.ETaal
import com.hha.types.ETimeFrameIndex

class Global private constructor() {
    // Properties
    var firstTablet: Short = 15
    lateinit var myDir: File
    var menuCardId = 1
    var menuPageId = 1
    var currentKeyIndex = 2
    var cursor = CCursor(0)
    var language: ETaal = ETaal.LANG_SIMPLIFIED
    var pageOffset = 0
    var transactionId = 0
    var transaction: CTransaction? = null
    var selectedItem = 10
    var pageBackgroundColor = 0xFF400040.toInt()
    var pageSelectedBackgroundColor = 0xFFc000c0.toInt()
    var pageTextColor = 0xFFf0f080.toInt()
    var mainMenuHandler: Handler? = null
    var askTableHandler: Handler? = null
    var clusterNoItems = false
    var showAllprices = false
    var showAllTimes = false
    var access = EAccess.ACCESS_EMPLOYEE_KEY
    public val CFG: Configuration = Configuration()
    val userCFG: Configuration = Configuration()
    val fontCFG: Configuration = Configuration()
    val colourCFG: Configuration = Configuration()
    var timeFrame = CTimeFrame(-1)
    val pcNumber: Short = 33

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

    fun isChinese(): Boolean {
        return language == ETaal.LANG_TRADITIONAL
                || language == ETaal.LANG_SIMPLIFIED
    }

    fun setPage(page: Int) {
        pageOffset = 0
        menuPageId = page
    }

    fun getOptions() {
        while (true) {
            if (tryLoadAllConfigs()) {
                break  // Success - exit loop
            }
            GrpcServiceFactory.reconnect()
            Thread.sleep(1000)  // Wait before retry
        }
    }

    private fun tryLoadAllConfigs(): Boolean {
        val dbs = GrpcServiceFactory.createDatabaseService()

        val config = dbs.getConfigurationList() ?: return false
        CFG.setConfigurations(config)

        val userConfig = dbs.getUserConfigurationList() ?: return false
        userCFG.setConfigurations(userConfig)

        val colourConfig = dbs.getColourConfigurationList() ?: return false
        colourCFG.setConfigurations(colourConfig)

        val fontConfig = dbs.getFontConfigurationList() ?: return false
        fontCFG.setConfigurations(fontConfig)

        clusterNoItems = CFG.getOption("cluster_no_items")
        return true  // All configs loaded successfully
    }
}
