package com.behnamuix.antisleep.Utils

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.behnamuix.antisleep.R

object MySoundPlayer {

    private var soundPool: SoundPool? = null
    private var soundId: Int = 0
    private var streamId: Int = 0   // ⭐ خیلی مهم
    private var loaded = false

    fun init(context: Context) {
        if (soundPool != null) return

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(audioAttributes)
            .build()

        soundId = soundPool!!.load(context, R.raw.alarm, 1)

        soundPool!!.setOnLoadCompleteListener { _, _, status ->
            loaded = status == 0
        }
    }

    fun play() {
        if (loaded && streamId == 0) {
            streamId = soundPool!!.play(
                soundId,
                1f,
                1f,
                1,
                -1, // loop
                1f
            )
        }
    }

    fun stop() {
        if (streamId != 0) {
            soundPool?.stop(streamId)
            streamId = 0
        }
    }

    fun release() {
        soundPool?.release()
        soundPool = null
        streamId = 0
        loaded = false
    }
}