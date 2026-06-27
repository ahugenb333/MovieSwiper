package org.ahugenb.movieswiper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.arkivanov.decompose.defaultComponentContext
import org.ahugenb.movieswiper.data.localdb.MovieRepository
import org.ahugenb.movieswiper.data.api.MovieBackendApi
import org.ahugenb.movieswiper.data.localdb.DriverFactory
import org.ahugenb.movieswiper.data.localdb.AndroidDriverFactory
import org.ahugenb.movieswiper.shared.DefaultRootComponent
import org.ahugenb.movieswiper.shared.initKoin
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
class MainActivity : ComponentActivity() {
    
    private val repository: MovieRepository by inject()
    private val api: MovieBackendApi by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        try {
            initKoin {
                androidContext(this@MainActivity)
                modules(module {
                    single<DriverFactory> { AndroidDriverFactory(get()) }
                })
            }
        } catch (_: Exception) {
            // Koin may already be started on activity recreation.
        }

        val root = DefaultRootComponent(
            componentContext = defaultComponentContext(),
            repository = repository,
            api = api
        )

        setContent {
            App(root)
        }
    }
}