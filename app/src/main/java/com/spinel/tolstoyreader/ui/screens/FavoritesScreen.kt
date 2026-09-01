package com.spinel.tolstoyreader.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spinel.tolstoyreader.R
import com.spinel.tolstoyreader.ui.viewmodel.BookViewModel

@Composable
fun FavoritesScreen(
    viewModel: BookViewModel,
    onBookClick: (String) -> Unit
) {
    val allBooks by viewModel.books.collectAsState()
    val favoriteBooks = allBooks.filter { it.isFavorite }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = stringResource(id = R.string.nav_favorites),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(16.dp)
        )
        
        if (favoriteBooks.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(id = R.string.no_favorite_books_yet),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(favoriteBooks, key = { it.id }) { book ->
                    BookGridItem(
                        book = book,
                        onClick = { onBookClick(book.id) },
                        onFavoriteClick = { viewModel.toggleFavorite(book.id, !book.isFavorite) }
                    )
                }
            }
        }
    }
}
