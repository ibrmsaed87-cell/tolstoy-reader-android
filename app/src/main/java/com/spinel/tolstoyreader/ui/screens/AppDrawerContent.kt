package com.spinel.tolstoyreader.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spinel.tolstoyreader.R

@Composable
fun AppDrawerContent(
    currentLang: String,
    currentTheme: String,
    onLangChange: (String) -> Unit,
    onThemeChange: (String) -> Unit,
    onCloseDrawer: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(300.dp)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = stringResource(id = R.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(id = R.string.explore_works),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        Divider()
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(id = R.string.appearance),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        ThemeOption(
            title = stringResource(id = R.string.theme_system),
            selected = currentTheme == "system",
            onClick = { onThemeChange("system") }
        )
        ThemeOption(
            title = stringResource(id = R.string.theme_light),
            selected = currentTheme == "light",
            onClick = { onThemeChange("light") }
        )
        ThemeOption(
            title = stringResource(id = R.string.theme_dark),
            selected = currentTheme == "dark",
            onClick = { onThemeChange("dark") }
        )

        Spacer(modifier = Modifier.height(24.dp))
        Divider()
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(id = R.string.language),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        LanguageOption(
            title = stringResource(id = R.string.lang_ar),
            selected = currentLang == "ar",
            onClick = { onLangChange("ar") }
        )
        LanguageOption(
            title = stringResource(id = R.string.lang_en),
            selected = currentLang == "en",
            onClick = { onLangChange("en") }
        )
        LanguageOption(
            title = stringResource(id = R.string.lang_ru),
            selected = currentLang == "ru",
            onClick = { onLangChange("ru") }
        )

        Spacer(modifier = Modifier.height(24.dp))
        Divider()
        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = {
                com.spinel.tolstoyreader.ui.utils.IntentHelper.openUrlSafely(
                    context,
                    "https://play.google.com/store/apps/dev?id=7189513262046406321",
                    context.getString(R.string.intent_failed)
                )
                onCloseDrawer()
            },
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 0.dp)
        ) {
            Text(
                text = stringResource(id = R.string.more_books),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyLarge
            )
        }

        TextButton(
            onClick = {
                val textToShare = context.getString(R.string.share_text) + "\nhttps://play.google.com/store/apps/details?id=com.spinel.tolstoyreader"
                com.spinel.tolstoyreader.ui.utils.IntentHelper.shareTextSafely(
                    context,
                    textToShare,
                    context.getString(R.string.intent_failed)
                )
                onCloseDrawer()
            },
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 0.dp)
        ) {
            Text(
                text = stringResource(id = R.string.share_app),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun ThemeOption(title: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        TextButton(onClick = onClick, contentPadding = PaddingValues(0.dp)) {
            Text(text = title, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun LanguageOption(title: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        TextButton(onClick = onClick, contentPadding = PaddingValues(0.dp)) {
            Text(text = title, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
