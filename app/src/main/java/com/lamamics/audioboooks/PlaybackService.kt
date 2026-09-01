package com.lamamics.audioboooks

import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

/**
 * Service de lecture en arrière-plan (notification, écran éteint).
 * Sauvegarde la position en continu et applique le retour en arrière
 * paramétrable lors d'une reprise après pause.
 */
class PlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private val handler = Handler(Looper.getMainLooper())

    private val saveRunnable = object : Runnable {
        override fun run() {
            savePosition()
            handler.postDelayed(this, 3000L)
        }
    }

    override fun onCreate() {
        super.onCreate()

        val exoPlayer = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                    .build(),
                true
            )
            .setHandleAudioBecomingNoisy(true)
            .build()

        // Reprise après pause : on recule de N secondes avant de relancer,
        // que le play vienne de l'app ou de la notification.
        val player = object : ForwardingPlayer(exoPlayer) {
            override fun play() {
                maybeRewindAfterPause()
                super.play()
            }

            override fun setPlayWhenReady(playWhenReady: Boolean) {
                if (playWhenReady && !getPlayWhenReady()) maybeRewindAfterPause()
                super.setPlayWhenReady(playWhenReady)
            }

            private fun maybeRewindAfterPause() {
                if (getPlayWhenReady()) return
                if (playbackState == Player.STATE_IDLE) return
                val pos = currentPosition
                if (pos <= 0L) return
                val rewindMs = Store.rewindSeconds(this@PlaybackService) * 1000L
                if (rewindMs <= 0L) return
                seekTo(currentMediaItemIndex, (pos - rewindMs).coerceAtLeast(0L))
            }
        }

        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                savePosition()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    val id = Store.currentBookId(this@PlaybackService) ?: return
                    val state = Store.getBookState(this@PlaybackService, id)
                    Store.saveBookState(this@PlaybackService, id, state.copy(finished = true))
                }
            }
        })

        mediaSession = MediaSession.Builder(this, player).build()
        handler.post(saveRunnable)
    }

    private fun savePosition() {
        val session = mediaSession ?: return
        val p = session.player
        if (p.currentMediaItem == null) return
        val id = Store.currentBookId(this) ?: return
        val state = Store.getBookState(this, id)
        Store.saveBookState(
            this,
            id,
            state.copy(
                chapterIndex = p.currentMediaItemIndex,
                positionMs = p.currentPosition.coerceAtLeast(0L),
            )
        )
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        val p = mediaSession?.player
        if (p == null || !p.playWhenReady) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        savePosition()
        handler.removeCallbacks(saveRunnable)
        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
        super.onDestroy()
    }
}
