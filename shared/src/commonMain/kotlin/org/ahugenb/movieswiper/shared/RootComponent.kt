package org.ahugenb.movieswiper.shared

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.*
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable
import org.ahugenb.movieswiper.feature.matcher.MatcherComponent
import org.ahugenb.movieswiper.feature.matcher.DefaultMatcherComponent
import org.ahugenb.movieswiper.feature.questions.FinderComponent
import org.ahugenb.movieswiper.feature.questions.DefaultFinderComponent
import org.ahugenb.movieswiper.feature.browser.BrowserComponent
import org.ahugenb.movieswiper.feature.browser.DefaultBrowserComponent
import org.ahugenb.movieswiper.data.localdb.MovieRepository
import org.ahugenb.movieswiper.data.api.MovieBackendApi

interface RootComponent {
    val stack: Value<ChildStack<*, Child>>
    
    fun onMatcherTabClicked()
    fun onFinderTabClicked()
    fun onBrowserTabClicked()

    sealed class Child {
        data class Matcher(val component: MatcherComponent) : Child()
        data class Finder(val component: FinderComponent) : Child()
        data class Browser(val component: BrowserComponent) : Child()
    }
}

class DefaultRootComponent(
    componentContext: ComponentContext,
    private val repository: MovieRepository,
    private val api: MovieBackendApi
) : RootComponent, ComponentContext by componentContext {

    private val navigation = StackNavigation<Config>()

    override val stack: Value<ChildStack<*, RootComponent.Child>> =
        childStack(
            source = navigation,
            serializer = Config.serializer(),
            initialConfiguration = Config.Matcher,
            handleBackButton = true,
            childFactory = ::child
        )

    private fun child(config: Config, componentContext: ComponentContext): RootComponent.Child =
        when (config) {
            is Config.Matcher -> RootComponent.Child.Matcher(
                DefaultMatcherComponent(componentContext, repository)
            )
            is Config.Finder -> RootComponent.Child.Finder(
                DefaultFinderComponent(componentContext, api)
            )
            is Config.Browser -> RootComponent.Child.Browser(
                DefaultBrowserComponent(componentContext, repository)
            )
        }

    override fun onMatcherTabClicked() {
        navigation.bringToFront(Config.Matcher)
    }

    override fun onFinderTabClicked() {
        navigation.bringToFront(Config.Finder)
    }

    override fun onBrowserTabClicked() {
        navigation.bringToFront(Config.Browser)
    }

    @Serializable
    private sealed interface Config {
        @Serializable
        object Matcher : Config
        @Serializable
        object Finder : Config
        @Serializable
        object Browser : Config
    }
}