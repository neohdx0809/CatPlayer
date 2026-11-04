package dev.anilbeesetti.nextplayer.feature.player.audio

import android.content.Context
import android.media.AudioManager
import dev.anilbeesetti.nextplayer.core.common.Logger

/**
 * Routes media playback through the built-in earpiece (听筒).
 *
 * The audio mode is switched to [AudioManager.MODE_IN_CALL], which makes the system
 * route all audio, including media, to the earpiece and keeps the speaker disabled.
 * Works without root, Shizuku or any special permissions.
 */
object EarpieceRouter {

    private const val TAG = "EarpieceRouter"

    private var originalSpeakerphoneState: Boolean? = null

    /** Whether audio is currently routed to the earpiece. */
    val isActive: Boolean
        get() = originalSpeakerphoneState != null

    /**
     * Starts routing audio to the earpiece and disables the speaker.
     * Does nothing when already active or when no AudioManager is available.
     */
    fun enable(context: Context) {
        if (originalSpeakerphoneState != null) return
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        if (audioManager == null) return
        originalSpeakerphoneState = audioManager.isSpeakerphoneOn
        runCatching {
            audioManager.setMode(AudioManager.MODE_IN_CALL)
            audioManager.setSpeakerphoneOn(false)
        }.onFailure {
            Logger.logError(
                tag = TAG,
                message = "Failed to route audio to earpiece: $it",
            )
        }
    }

    /**
     * Restores normal speaker routing using the speakerphone state saved by [enable].
     * Does nothing when the earpiece routing is not active.
     */
    fun disable(context: Context) {
        val originalState = originalSpeakerphoneState ?: return
        originalSpeakerphoneState = null
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        if (audioManager == null) return
        runCatching {
            audioManager.setMode(AudioManager.MODE_NORMAL)
            audioManager.setSpeakerphoneOn(originalState)
        }.onFailure {
            Logger.logError(
                tag = TAG,
                message = "Failed to restore speaker routing: $it",
            )
        }
    }
}
