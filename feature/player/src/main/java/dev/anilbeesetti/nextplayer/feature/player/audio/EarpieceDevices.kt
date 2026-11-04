package dev.anilbeesetti.nextplayer.feature.player.audio

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager

/**
 * Provides the built-in earpiece (听筒) output device.
 *
 * Media3's [androidx.media3.exoplayer.ExoPlayer.setPreferredAudioDevice] routes the
 * audio output to this device, keeping the speaker silent. Works without root,
 * Shizuku or any special permissions.
 */
object EarpieceDevices {

    /**
     * The built-in earpiece output device, or null when the device has no earpiece.
     */
    fun earpieceDevice(context: Context): AudioDeviceInfo? {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        if (audioManager == null) return null
        return audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            .firstOrNull { device -> device.type == AudioDeviceInfo.TYPE_BUILTIN_EARPIECE }
    }
}
