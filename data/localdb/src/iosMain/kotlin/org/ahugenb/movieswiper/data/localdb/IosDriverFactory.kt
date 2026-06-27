package org.ahugenb.movieswiper.data.localdb

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

class IosDriverFactory : DriverFactory {
    override fun createDriver(): SqlDriver {
        return NativeSqliteDriver(MovieDatabase.Schema, "movies.db")
    }
}