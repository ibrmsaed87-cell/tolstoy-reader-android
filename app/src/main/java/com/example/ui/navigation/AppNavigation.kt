package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.ui.screens.BookDetailsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ReaderScreen
import com.example.ui.viewmodel.BookViewModel

sealed class Screen(val route: String) {
    object Home : Screen("home")
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
    modifier: Modifier = Modifier
) {
    val bookViewModel: BookViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = bookViewModel,
                onBookClick = { bookId ->
                    navController.navigate(Screen.BookDetails.createRoute(bookId))
                }
            )
        }
        
        composable(Screen.BookDetails.route) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId")
            if (bookId != null) {
                BookDetailsScreen(
                    bookId = bookId,
                    viewModel = bookViewModel,
                    onBackClick = { navController.popBackStack() },
                    onReadClick = { navController.navigate(Screen.Reader.createRoute(bookId)) }
                )
            }
        }
        
        composable(Screen.Reader.route) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId")
            if (bookId != null) {
                ReaderScreen(
                    bookId = bookId,
                    viewModel = bookViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
