package dev.anilbeesetti.nextplayer.feature.player.audio

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import dev.anilbeesetti.nextplayer.core.common.Logger

/**
 * Routes media playback through the built-in earpiece (听筒).
 *
 * The audio mode is switched to [AudioManager.MODE_IN_CALL], which makes the system
 * route all audio, including media, to the earpiece and keeps the speaker disabled.
 * The communication device is also pointed at the earpiece where supported.
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
     * Returns whether the routing was applied successfully.
     */
    fun enable(context: Context): Boolean {
        if (originalSpeakerphoneState != null) return true
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        if (audioManager == null) return false
        originalSpeakerphoneState = audioManager.isSpeakerphoneOn
        val applied = try {
            audioManager.setMode(AudioManager.MODE_IN_CALL)
            audioManager.setSpeakerphoneOn(false)
            setCommunicationDeviceToEarpiece(audioManager)
            audioManager.mode == AudioManager.MODE_IN_CALL
        } catch (exception: Exception) {
            Logger.logError(
                tag = TAG,
                message = "Failed to apply earpiece routing: $exception",
            )
            false
        }
        if (applied) {
            Logger.logInfo(
                tag = TAG,
                message = "Audio routed to earpiece",
            )
        } else {
            Logger.logError(
                tag = TAG,
                message = "Earpiece routing not applied, audio mode is ${audioManager.mode}",
            )
        }
        return applied
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
        try {
            audioManager.setMode(AudioManager.MODE_NORMAL)
            audioManager.setSpeakerphoneOn(originalState)
        } catch (exception: Exception) {
            Logger.logError(
                tag = TAG,
                message = "Failed to restore speaker routing: $exception",
            )
        }
    }

    private fun setCommunicationDeviceToEarpiece(audioManager: AudioManager) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
        val earpiece = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            .firstOrNull { device -> device.type == AudioDeviceInfo.TYPE_BUILTIN_EARPIECE }
        if (earpiece != null) {
            audioManager.setCommunicationDevice(earpiece)
        }
    }
}
