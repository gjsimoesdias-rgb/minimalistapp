package com.minimal.launcher.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimal.launcher.AppInfo
import com.minimal.launcher.AppRepository
import com.minimal.launcher.LauncherState
import com.minimal.launcher.Screen

@Composable
fun AppDrawer(state: LauncherState) {
    val focus = remember { FocusRequester() }
    val results = state.searchResults

    LaunchedEffect(Unit) { focus.requestFocus() }
    LaunchedEffect(state.query, results.size) {
        if (state.autoLaunch && state.query.isNotBlank() && results.size == 1) state.launch(results.first())
    }

    Column(
        Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding()
            .padding(horizontal = 32.dp),
    ) {
        BasicTextField(
            value = state.query,
            onValueChange = { state.query = it },
            singleLine = true,
            textStyle = TextStyle(color = Color.White, fontSize = 22.sp),
            cursorBrush = SolidColor(Color.White),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                autoCorrectEnabled = false,
                imeAction = ImeAction.Go,
            ),
            keyboardActions = KeyboardActions(onGo = { results.firstOrNull()?.let(state::launch) }),
            decorationBox = { field ->
                Box {
                    if (state.query.isEmpty()) Text("search", color = Faint, fontSize = 22.sp)
                    field()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focus)
                .padding(top = 28.dp, bottom = 16.dp),
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            items(results, key = { it.key }) { app ->
                AppEntry(app = app, state = state, fontSize = 22.sp, verticalPadding = 11.dp)
            }
            if (state.query.isEmpty()) {
                item {
                    Text(
                        "settings",
                        color = Muted,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .plainClickable { state.screen = Screen.Settings }
                            .padding(vertical = 24.dp),
                    )
                }
            }
        }
    }
}

/** A single app name. Tap to open, long-press for options. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppEntry(app: AppInfo, state: LauncherState, fontSize: TextUnit, verticalPadding: Dp) {
    val context = LocalContext.current
    var menuOpen by remember { mutableStateOf(false) }

    Box {
        Text(
            app.label.lowercase(),
            fontSize = fontSize,
            fontWeight = FontWeight.Light,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    interactionSource = null,
                    indication = null,
                    onLongClick = { menuOpen = true },
                    onClick = { state.launch(app) },
                )
                .padding(vertical = verticalPadding),
        )
        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
            MenuItem(if (app.key in state.favorites) "remove from home" else "add to home") {
                menuOpen = false
                state.toggleFavorite(app)
            }
            MenuItem(if (app.key in state.mindful) "remove mindful delay" else "add mindful delay") {
                menuOpen = false
                state.toggleMindful(app)
            }
            MenuItem(if (app.key in state.hidden) "unhide" else "hide") {
                menuOpen = false
                state.toggleHidden(app)
            }
            MenuItem("app info") {
                menuOpen = false
                AppRepository.openAppInfo(context, app)
            }
            MenuItem("uninstall") {
                menuOpen = false
                AppRepository.uninstall(context, app)
            }
        }
    }
}

@Composable
private fun MenuItem(label: String, onClick: () -> Unit) {
    DropdownMenuItem(text = { Text(label, fontSize = 16.sp) }, onClick = onClick)
}
