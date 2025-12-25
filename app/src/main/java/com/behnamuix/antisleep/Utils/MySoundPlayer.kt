package com.behnamuix.antisleep.Utils

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.behnamuix.antisleep.R

object MySoundPlayer {

    private var soundPool: SoundPool? = null
    private var soundId: Int = 0
    private var loaded = false

    fun init(context: Context) {
        if (soundPool != null) return

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(2)
            .setAudioAttributes(audioAttributes)
            .build()

        soundId = soundPool!!.load(context, R.raw.alarm, 1)

        soundPool!!.setOnLoadCompleteListener { _, _, status ->
            loaded = status == 0
        }
    }

    fun play() {
        if (loaded) {
            soundPool?.play(
                soundId,
                1f, 1f, // left / right volume
                1,
                -1,
                1f
            )
        }
    }

    fun stop() {
        if (loaded) {
            soundPool?.stop(soundId)
        }
    }

    fun release() {
        soundPool?.release()
        soundPool = null
        loaded = false
    }
}