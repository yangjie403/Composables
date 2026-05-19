package com.mjieg.composables.screen

import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.mjieg.composables.R
import com.mjieg.composables.ui.PreviewLayout
import java.util.Locale

@Composable
fun LanguageScreen() {
    var currentLanguage by remember {
        mutableStateOf("zh")
    }
    val context = LocalContext.current
    val localizedContext = remember(currentLanguage) {
        getLocalizedContext(context, currentLanguage)
    }

    CompositionLocalProvider(LocalContext provides localizedContext) {
        PreviewLayout {
            MainApp(currentLanguage = currentLanguage) { newLang ->
                currentLanguage = newLang
            }
        }
    }
}

@Composable
private fun MainApp(currentLanguage: String, onLanguageChange: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = stringResource(id = R.string.home))

        Button(onClick = {
            onLanguageChange("en")
        }) {
            Text("Switch to English")
        }

        Button(onClick = {
            onLanguageChange("zh")
        }) {
            Text("切换至中文")
        }
    }
}

fun getLocalizedContext(baseContext: Context, languageCode: String): Context {
    val locale = if (languageCode.contains("-")) {
        val parts = languageCode.split("-")
        Locale(parts[0], parts[1])
    } else {
        Locale(languageCode)
    }
    Locale.setDefault(locale)
    val config = Configuration(baseContext.resources.configuration)
    config.setLocale(locale)
    return baseContext.createConfigurationContext(config)
}