package io.github.ayandaw.enmelo

import android.os.Handler
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.ForwardingPlayer
import androidx.media3.exoplayer.ExoPlayer

@OptIn(UnstableApi::class)
class FadePausePlayer(private val exo: ExoPlayer) : ForwardingPlayer(exo) {
    private val handler = Handler(exo.applicationLooper)
    private var step = 0
    private var fading = false

    private val tick = object : Runnable {
        override fun run() {
            step++
            if (step >= STEPS) {
                fading = false
                exo.pause()
                exo.volume = 1f
            } else {
                exo.volume = 1f - step / STEPS.toFloat()
                handler.postDelayed(this, FADE_MS / STEPS)
            }
        }
    }

    override fun pause() {
        if (!exo.isPlaying) { exo.pause(); return }
        if (fading) return
        fading = true
        step = 0
        handler.postDelayed(tick, FADE_MS / STEPS)
    }

    override fun play() { cancelFade(); exo.play() }

    override fun setPlayWhenReady(playWhenReady: Boolean) {
        if (playWhenReady) cancelFade()
        super.setPlayWhenReady(playWhenReady)
    }

    private fun cancelFade() {
        handler.removeCallbacks(tick)
        fading = false
        exo.volume = 1f
    }

    private companion object {
        const val FADE_MS = 500L
        const val STEPS = 20
    }
}