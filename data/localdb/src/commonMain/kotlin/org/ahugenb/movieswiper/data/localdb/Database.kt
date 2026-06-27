package org.ahugenb.movieswiper.data.localdb

import app.cash.sqldelight.db.SqlDriver

interface DriverFactory {
    fun createDriver(): SqlDriver
}

fun createDatabase(driverFactory: DriverFactory): MovieDatabase {
    val driver = driverFactory.createDriver()
    return MovieDatabase(driver)
}