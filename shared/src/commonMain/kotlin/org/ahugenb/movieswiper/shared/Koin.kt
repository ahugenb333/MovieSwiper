package org.ahugenb.movieswiper.shared

import org.ahugenb.movieswiper.data.api.BackendEndpoints
import org.ahugenb.movieswiper.data.api.MovieBackendApi
import org.ahugenb.movieswiper.data.api.createHttpClient
import org.ahugenb.movieswiper.data.localdb.MovieRepository
import org.ahugenb.movieswiper.data.localdb.MovieDatabase
import org.ahugenb.movieswiper.data.localdb.DriverFactory
import org.ahugenb.movieswiper.data.localdb.createDatabase
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
    startKoin {
        appDeclaration()
        modules(coreModule, dataModule, featureModule)
    }
}

val coreModule = module {
    // Core dependencies
}

val dataModule = module {
    // This client is for the Mobile App talking to the MovieSwiper Backend
    single { createHttpClient(withTmdbAuth = false) }

    single { MovieBackendApi(get(), BackendEndpoints.baseUrl) }
    single { createDatabase(get()) }
    single { MovieRepository(get(), get()) }
}

val featureModule = module {
    // Feature specific dependencies
}