package com.spinel.tolstoyreader.ui.navigation
import androidx.compose.ui.unit.dp

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.launch
import com.spinel.tolstoyreader.ui.screens.AppDrawerContent
import androidx.navigation.compose.currentBackStackEntryAsState
import com.spinel.tolstoyreader.R
import com.spinel.tolstoyreader.ui.screens.BookDetailsScreen
import com.spinel.tolstoyreader.ui.screens.FavoritesScreen
import com.spinel.tolstoyreader.ui.screens.HomeScreen
import com.spinel.tolstoyreader.ui.screens.ReaderScreen
import com.spinel.tolstoyreader.ui.screens.SearchScreen
import com.spinel.tolstoyreader.ui.viewmodel.BookViewModel

import com.spinel.tolstoyreader.ads.AdManager
import android.app.Activity
import androidx.compose.ui.platform.LocalContext

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Search : Screen("search")
    object Favorites : Screen("favorites")
    object BookDetails : Screen("book_details/{bookId}") {
        fun createRoute(bookId: String) = "book_details/$bookId"
    }
    object Reader : Screen("reader/{bookId}") {
        fun createRoute(bookId: String) = "reader/$bookId"
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    bookViewModel: BookViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route
    
    val context = LocalContext.current
    val activity = context as? Activity
    
    // Update reader mode state to prevent ads during reading
    AdManager.isReaderModeActive = currentRoute?.startsWith("reader/") == true

    val showBottomNav = currentRoute in listOf(
        Screen.Home.route,
        Screen.Search.route,
        Screen.Favorites.route
    )
    
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    val currentLang by bookViewModel.appLanguage.collectAsState()
    val currentTheme by bookViewModel.appTheme.collectAsState()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                AppDrawerContent(
                    currentLang = currentLang,
                    currentTheme = currentTheme,
                    onLangChange = { bookViewModel.setAppLanguage(it) },
                    onThemeChange = { bookViewModel.setAppTheme(it) },
                    onCloseDrawer = { scope.launch { drawerState.close() } }
                )
            }
        },
        gesturesEnabled = showBottomNav
    ) {
        Scaffold(
            modifier = modifier,
            bottomBar = {
                if (showBottomNav) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 1.dp
                    ) {
                        NavigationBarItem(
                            icon = { Icon(Icons.Filled.Home, contentDescription = stringResource(id = R.string.nav_home)) },
                            label = { Text(stringResource(id = R.string.nav_home)) },
                            selected = currentDestination?.hierarchy?.any { it.route == Screen.Home.route } == true,
                            onClick = {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Filled.Search, contentDescription = stringResource(id = R.string.nav_search)) },
                            label = { Text(stringResource(id = R.string.nav_search)) },
                            selected = currentDestination?.hierarchy?.any { it.route == Screen.Search.route } == true,
                            onClick = {
                                navController.navigate(Screen.Search.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Filled.Favorite, contentDescription = stringResource(id = R.string.nav_favorites)) },
                            label = { Text(stringResource(id = R.string.nav_favorites)) },
                            selected = currentDestination?.hierarchy?.any { it.route == Screen.Favorites.route } == true,
                            onClick = {
                                navController.navigate(Screen.Favorites.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(
                        viewModel = bookViewModel,
                        onOpenDrawer = { scope.launch { drawerState.open() } },
                        onBookClick = { bookId ->
                            activity?.let {
                                AdManager.showInterstitialOnTransition(it) {
                                    navController.navigate(Screen.BookDetails.createRoute(bookId))
                                }
                            } ?: run {
                                navController.navigate(Screen.BookDetails.createRoute(bookId))
                            }
                        },
                        onReadClick = { bookId ->
                            activity?.let {
                                AdManager.showInterstitialOnTransition(it) {
                                    navController.navigate(Screen.Reader.createRoute(bookId))
                                }
                            } ?: run {
                                navController.navigate(Screen.Reader.createRoute(bookId))
                            }
                        }
                    )
                }
                composable(Screen.Search.route) {
                    SearchScreen(
                        viewModel = bookViewModel,
                        onBookClick = { bookId ->
                            activity?.let {
                                AdManager.showInterstitialOnTransition(it) {
                                    navController.navigate(Screen.BookDetails.createRoute(bookId))
                                }
                            } ?: run {
                                navController.navigate(Screen.BookDetails.createRoute(bookId))
                            }
                        }
                    )
                }
                composable(Screen.Favorites.route) {
                    FavoritesScreen(
                        viewModel = bookViewModel,
                        onBookClick = { bookId ->
                            activity?.let {
                                AdManager.showInterstitialOnTransition(it) {
                                    navController.navigate(Screen.BookDetails.createRoute(bookId))
                                }
                            } ?: run {
                                navController.navigate(Screen.BookDetails.createRoute(bookId))
                            }
                        }
                    )
                }
                
                composable(Screen.BookDetails.route) { backStackEntry ->
                    val bookId = backStackEntry.arguments?.getString("bookId")
                    if (bookId != null) {
                        BookDetailsScreen(
                            bookId = bookId,
                            viewModel = bookViewModel,
                            onBackClick = {
                                navController.popBackStack()
                            },
                            onReadClick = { id -> 
                                activity?.let {
                                    AdManager.showInterstitialOnTransition(it) {
                                        navController.navigate(Screen.Reader.createRoute(id))
                                    }
                                } ?: run {
                                    navController.navigate(Screen.Reader.createRoute(id))
                                }
                            }
                        )
                    }
                }
                
                composable(Screen.Reader.route) { backStackEntry ->
                    val bookId = backStackEntry.arguments?.getString("bookId")
                    if (bookId != null) {
                        ReaderScreen(
                            bookId = bookId,
                            viewModel = bookViewModel,
                            onBackClick = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
}
