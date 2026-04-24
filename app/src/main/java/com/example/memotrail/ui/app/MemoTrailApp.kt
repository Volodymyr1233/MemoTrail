package com.example.memotrail.ui.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.memotrail.di.AppContainer
import com.example.memotrail.ui.navigation.MemoTrailDestination
import com.example.memotrail.ui.navigation.MemoTrailNavHost
import com.example.memotrail.ui.navigation.bottomNavDestinations

@Composable
fun MemoTrailApp(appContainer: AppContainer) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = bottomNavDestinations.any {
        currentDestination?.hierarchy?.any { destination -> destination.route == it.route } == true
    }

    MemoTrailScaffold(
        showBottomBar = showBottomBar,
        bottomBar = {
            MemoTrailBottomNav(
                currentRoute = currentDestination?.route,
                onDestinationClick = { destination ->
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { modifier ->
        MemoTrailNavHost(
            modifier = modifier.fillMaxSize(),
            navController = navController,
            appContainer = appContainer
        )
    }
}

@Composable
fun MemoTrailScaffold(
    showBottomBar: Boolean,
    bottomBar: @Composable () -> Unit,
    content: @Composable (Modifier) -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                bottomBar()
            }
        }
    ) { innerPadding ->
        content(Modifier.padding(innerPadding))
    }
}

@Composable
fun MemoTrailBottomNav(
    currentRoute: String?,
    onDestinationClick: (MemoTrailDestination) -> Unit
) {
    NavigationBar {
        bottomNavDestinations.forEach { destination ->
            val isSelected = currentRoute == destination.route
            val label = stringResource(destination.labelRes)
            NavigationBarItem(
                selected = isSelected,
                onClick = { onDestinationClick(destination) },
                icon = {
                    destination.icon?.let { icon ->
                        Icon(imageVector = icon, contentDescription = label)
                    }
                },
                label = { Text(text = label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

