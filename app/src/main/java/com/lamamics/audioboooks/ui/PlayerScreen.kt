package com.lamamics.audioboooks.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Forward30
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Replay30
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import com.lamamics.audioboooks.Book
import com.lamamics.audioboooks.Store
import com.lamamics.audioboooks.openBook
import kotlinx.coroutines.delay
import java.util.Locale

private val SPEEDS = listOf(0.8f, 0.9f, 1.0f, 1.2f, 1.4f, 1.6f, 1.8f, 2.0f)

private fun formatTime(ms: Long): String {
    val totalSec = (ms / 1000).coerceAtLeast(0)
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    return if (h > 0) {
        String.format(Locale.FRANCE, "%d:%02d:%02d", h, m, s)
    } else {
        String.format(Locale.FRANCE, "%d:%02d", m, s)
    }
}

private fun formatSpeed(speed: Float): String =
    if (speed == speed.toInt().toFloat()) "${speed.toInt()}x"
    else "${String.format(Locale.US, "%.1f", speed)}x"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    controller: MediaController?,
    book: Book,
    onBack: () -> Unit,
) {
    val context = LocalContext.current

    var bookState by remember(book.id) { mutableStateOf(Store.getBookState(context, book.id)) }
    var isPlaying by remember { mutableStateOf(false) }
    var chapterIndex by remember(book.id) { mutableStateOf(bookState.chapterIndex) }
    var positionMs by remember(book.id) { mutableStateOf(bookState.positionMs) }
    var durationMs by remember { mutableStateOf(0L) }
    var speed by remember { mutableStateOf(Store.speed(context)) }
    var sliderDrag by remember { mutableStateOf<Float?>(null) }

    // Charge le livre dès que le contrôleur est prêt.
    LaunchedEffect(controller, book.id) {
        if (controller != null) {
            openBook(context, controller, book)
            isPlaying = controller.isPlaying
            chapterIndex = controller.currentMediaItemIndex
        }
    }

    DisposableEffect(controller) {
        if (controller == null) {
            onDispose { }
        } else {
            val listener = object : Player.Listener {
                override fun onIsPlayingChanged(playing: Boolean) {
                    isPlaying = playing
                }

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    chapterIndex = controller.currentMediaItemIndex
                }

                override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
                    speed = playbackParameters.speed
                }
            }
            controller.addListener(listener)
            onDispose { controller.removeListener(listener) }
        }
    }

    // Rafraîchit position/durée deux fois par seconde.
    LaunchedEffect(controller) {
        while (true) {
            if (controller != null && controller.mediaItemCount > 0) {
                chapterIndex = controller.currentMediaItemIndex
                positionMs = controller.currentPosition.coerceAtLeast(0L)
                val d = controller.duration
                durationMs = if (d > 0) d else 0L
                isPlaying = controller.isPlaying
            }
            delay(500)
        }
    }

    fun updateBookState(newState: com.lamamics.audioboooks.BookState) {
        bookState = newState
        Store.saveBookState(context, book.id, newState)
    }

    fun seekBy(deltaMs: Long) {
        val c = controller ?: return
        val target = (c.currentPosition + deltaMs).coerceAtLeast(0L)
        c.seekTo(c.currentMediaItemIndex, target)
    }

    val currentChapterName = book.chapters.getOrNull(chapterIndex)?.name ?: ""

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        book.name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        updateBookState(bookState.copy(favorite = !bookState.favorite))
                    }) {
                        Icon(
                            if (bookState.favorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = "Favori",
                            tint = if (bookState.favorite) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
        ) {
            // Note (étoiles) + terminé
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row {
                    for (i in 1..5) {
                        Icon(
                            if (i <= bookState.rating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = "Note $i",
                            modifier = Modifier
                                .size(28.dp)
                                .clickable {
                                    val newRating = if (bookState.rating == i) 0 else i
                                    updateBookState(bookState.copy(rating = newRating))
                                },
                            tint = if (i <= bookState.rating) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                Chip(
                    selected = bookState.finished,
                    label = if (bookState.finished) "Terminé ✓" else "Marquer lu",
                    onClick = {
                        updateBookState(bookState.copy(finished = !bookState.finished))
                    },
                )
            }

            Spacer(Modifier.height(18.dp))

            // Chapitre en cours
            Text(
                currentChapterName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                "Chapitre ${chapterIndex + 1} / ${book.chapters.size}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(8.dp))

            // Barre de progression
            Slider(
                value = sliderDrag ?: if (durationMs > 0) {
                    (positionMs.toFloat() / durationMs).coerceIn(0f, 1f)
                } else 0f,
                onValueChange = { sliderDrag = it },
                onValueChangeFinished = {
                    val c = controller
                    val drag = sliderDrag
                    if (c != null && drag != null && durationMs > 0) {
                        c.seekTo(c.currentMediaItemIndex, (drag * durationMs).toLong())
                    }
                    sliderDrag = null
                },
                modifier = Modifier.fillMaxWidth(),
            )
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    formatTime(sliderDrag?.let { (it * durationMs).toLong() } ?: positionMs),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    formatTime(durationMs),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(8.dp))

            // Transport : -30, -10, play/pause, +10, +30
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { seekBy(-30_000L) }, modifier = Modifier.size(52.dp)) {
                    Icon(
                        Icons.Filled.Replay30,
                        contentDescription = "Reculer de 30 secondes",
                        modifier = Modifier.size(34.dp),
                    )
                }
                IconButton(onClick = { seekBy(-10_000L) }, modifier = Modifier.size(52.dp)) {
                    Icon(
                        Icons.Filled.Replay10,
                        contentDescription = "Reculer de 10 secondes",
                        modifier = Modifier.size(34.dp),
                    )
                }
                FilledIconButton(
                    onClick = {
                        val c = controller
                        if (c != null) {
                            if (c.isPlaying) c.pause() else c.play()
                        }
                    },
                    modifier = Modifier.size(72.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Icon(
                        if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Lecture",
                        modifier = Modifier.size(40.dp),
                    )
                }
                IconButton(onClick = { seekBy(10_000L) }, modifier = Modifier.size(52.dp)) {
                    Icon(
                        Icons.Filled.Forward10,
                        contentDescription = "Avancer de 10 secondes",
                        modifier = Modifier.size(34.dp),
                    )
                }
                IconButton(onClick = { seekBy(30_000L) }, modifier = Modifier.size(52.dp)) {
                    Icon(
                        Icons.Filled.Forward30,
                        contentDescription = "Avancer de 30 secondes",
                        modifier = Modifier.size(34.dp),
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Vitesses de lecture
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SPEEDS.forEach { s ->
                    Chip(
                        selected = speed == s,
                        label = formatSpeed(s),
                        onClick = {
                            speed = s
                            Store.setSpeed(context, s)
                            controller?.setPlaybackSpeed(s)
                        },
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(Modifier.height(8.dp))

            Text(
                "Chapitres",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(4.dp))

            val listState = rememberLazyListState()
            LaunchedEffect(book.id) {
                if (chapterIndex > 2) listState.scrollToItem(chapterIndex - 1)
            }
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 16.dp),
            ) {
                itemsIndexed(book.chapters) { index, chapter ->
                    val isCurrent = index == chapterIndex
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val c = controller
                                if (c != null && c.mediaItemCount > index) {
                                    c.seekTo(index, 0L)
                                    chapterIndex = index
                                }
                            }
                            .background(
                                if (isCurrent) MaterialTheme.colorScheme.surfaceVariant
                                else MaterialTheme.colorScheme.background
                            )
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "${index + 1}.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isCurrent) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(32.dp),
                        )
                        Text(
                            chapter.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            color = if (isCurrent) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        if (isCurrent) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Chip(selected: Boolean, label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary
        else MaterialTheme.colorScheme.onSurfaceVariant,
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelLarge,
        )
    }
}
