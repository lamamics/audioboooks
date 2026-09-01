package com.lamamics.audioboooks

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.lamamics.audioboooks.ui.AudioboooksTheme
import com.lamamics.audioboooks.ui.LegalScreen
import com.lamamics.audioboooks.ui.LibraryScreen
import com.lamamics.audioboooks.ui.PlayerScreen
import com.lamamics.audioboooks.ui.SettingsScreen
import com.lamamics.audioboooks.ui.SourcesScreen

class MainActivity : ComponentActivity() {

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private val controllerState = mutableStateOf<MediaController?>(null)

    private val notifPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 33) {
            notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        setContent {
            AudioboooksTheme {
                AppRoot(controllerState = controllerState)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val token = SessionToken(this, ComponentName(this, PlaybackService::class.java))
        val future = MediaController.Builder(this, token).buildAsync()
        controllerFuture = future
        future.addListener({
            try {
                if (!future.isCancelled) controllerState.value = future.get()
            } catch (e: Exception) {
                // service indisponible : l'UI reste utilisable sans lecture
            }
        }, ContextCompat.getMainExecutor(this))
    }

    override fun onStop() {
        controllerState.value = null
        controllerFuture?.let { MediaController.releaseFuture(it) }
        controllerFuture = null
        super.onStop()
    }
}

sealed interface Screen {
    data object Library : Screen
    data class Player(val book: Book) : Screen
    data object Settings : Screen
    data object Sources : Screen
    data object Legal : Screen
}

@Composable
fun AppRoot(controllerState: MutableState<MediaController?>) {
    var screen by remember { mutableStateOf<Screen>(Screen.Library) }
    val controller = controllerState.value

    when (val s = screen) {
        is Screen.Library -> LibraryScreen(
            onOpenBook = { book -> screen = Screen.Player(book) },
            onOpenSettings = { screen = Screen.Settings },
            onOpenSources = { screen = Screen.Sources },
        )

        is Screen.Player -> PlayerScreen(
            controller = controller,
            book = s.book,
            onBack = { screen = Screen.Library },
        )

        is Screen.Settings -> SettingsScreen(
            onBack = { screen = Screen.Library },
            onOpenLegal = { screen = Screen.Legal },
        )

        is Screen.Sources -> SourcesScreen(onBack = { screen = Screen.Library })

        is Screen.Legal -> LegalScreen(onBack = { screen = Screen.Settings })
    }
}

/**
 * Charge le livre dans le lecteur en reprenant à la position sauvegardée.
 * Ne recharge pas si ce livre est déjà chargé (pour ne pas couper la lecture).
 */
fun openBook(context: Context, controller: MediaController, book: Book) {
    if (book.chapters.isEmpty()) return
    val previousId = Store.currentBookId(context)
    if (previousId == book.id && controller.mediaItemCount > 0) {
        return
    }

    // Sauvegarde la position du livre précédent avant de changer.
    if (previousId != null && previousId != book.id && controller.mediaItemCount > 0) {
        val prev = Store.getBookState(context, previousId)
        Store.saveBookState(
            context,
            previousId,
            prev.copy(
                chapterIndex = controller.currentMediaItemIndex,
                positionMs = controller.currentPosition.coerceAtLeast(0L),
            )
        )
        controller.pause()
    }

    Store.setCurrentBookId(context, book.id)
    val state = Store.getBookState(context, book.id)

    val items = book.chapters.map { chapter ->
        MediaItem.Builder()
            .setUri(chapter.uri)
            .setMediaId(chapter.uri.toString())
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(chapter.name)
                    .setArtist(book.name)
                    .build()
            )
            .build()
    }

    val startIndex = state.chapterIndex.coerceIn(0, items.size - 1)
    controller.setMediaItems(items, startIndex, state.positionMs.coerceAtLeast(0L))
    controller.setPlaybackSpeed(Store.speed(context))
    controller.prepare()
}
