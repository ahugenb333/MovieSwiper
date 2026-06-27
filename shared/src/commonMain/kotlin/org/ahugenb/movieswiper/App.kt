package org.ahugenb.movieswiper

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import org.ahugenb.movieswiper.feature.browser.BrowserContent
import org.ahugenb.movieswiper.feature.matcher.MatcherContent
import org.ahugenb.movieswiper.feature.questions.FinderContent
import org.ahugenb.movieswiper.shared.RootComponent
import org.ahugenb.movieswiper.ui.MovieSwiperTheme

@Composable
fun App(root: RootComponent) {
    MovieSwiperTheme {
        Scaffold(
            bottomBar = {
                val stack by root.stack.subscribeAsState()
                val activeChild = stack.active.instance
                
                NavigationBar {
                    NavigationBarItem(
                        selected = activeChild is RootComponent.Child.Matcher,
                        onClick = root::onMatcherTabClicked,
                        icon = { Text("M") },
                        label = { Text("Matcher") }
                    )
                    NavigationBarItem(
                        selected = activeChild is RootComponent.Child.Finder,
                        onClick = root::onFinderTabClicked,
                        icon = { Text("F") },
                        label = { Text("Finder") }
                    )
                    NavigationBarItem(
                        selected = activeChild is RootComponent.Child.Browser,
                        onClick = root::onBrowserTabClicked,
                        icon = { Text("W") },
                        label = { Text("Watchlist") }
                    )
                }
            }
        ) { padding ->
            // Use Box without the Scaffold padding for Matcher to achieve true full-screen
            val stack by root.stack.subscribeAsState()
            val isMatcher = stack.active.instance is RootComponent.Child.Matcher

            Box(modifier = Modifier.fillMaxSize().then(
                if (isMatcher) Modifier else Modifier.padding(padding)
            )) {
                Children(
                    stack = root.stack,
                    animation = stackAnimation()
                ) {
                    when (val child = it.instance) {
                        is RootComponent.Child.Matcher -> {
                            // Matcher handles its own internal bottom padding for the nav bar
                            Box(modifier = Modifier.padding(bottom = padding.calculateBottomPadding())) {
                                MatcherContent(child.component)
                            }
                        }
                        is RootComponent.Child.Finder -> FinderContent(child.component)
                        is RootComponent.Child.Browser -> BrowserContent(child.component)
                    }
                }
            }
        }
    }
}