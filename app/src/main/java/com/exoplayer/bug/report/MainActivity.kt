package com.exoplayer.bug.report

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.extractor.ts.DefaultTsPayloadReaderFactory
import com.google.android.exoplayer2.source.hls.DefaultHlsExtractorFactory
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.EventLogger

class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "MainActivity"
        const val SHOW_UI_FOREVER = -1
        const val FIRST_MEDIA_ITEM_URL =
            "http://stand.netup.tv/downloads/T7080/hls/first/index.m3u8"
        const val SECOND_MEDIA_ITEM_URL =
            "http://stand.netup.tv/downloads/T7080/hls/second/index.m3u8"
    }

    private lateinit var player: SimpleExoPlayer
    private var isReadyFirstTime = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupPlayer()
        val playerView = findViewById<PlayerView>(R.id.player_view)
        playerView.controllerShowTimeoutMs = SHOW_UI_FOREVER
        playerView.player = player
        setMediaItems()
    }

    override fun onStart() {
        super.onStart()
        play()
    }

    override fun onStop() {
        super.onStop()
        player.release()
    }

    private fun setupPlayer() {
        val trackSelector = DefaultTrackSelector(this, AdaptiveTrackSelection.Factory())
        val dataSourceFactory = DefaultDataSourceFactory(this, "exo-bug-sandbox")
        val extractorFactory = DefaultHlsExtractorFactory(
            DefaultTsPayloadReaderFactory.FLAG_ALLOW_NON_IDR_KEYFRAMES,
            false
        )
        val hlsMediaSourceFactory = HlsMediaSource.Factory(dataSourceFactory)
            .setExtractorFactory(extractorFactory)
        player = SimpleExoPlayer.Builder(this, DefaultRenderersFactory(this))
            .setMediaSourceFactory(hlsMediaSourceFactory)
            .setTrackSelector(trackSelector)
            .build()
        player.addAnalyticsListener(EventLogger(trackSelector, TAG))
        player.addListener(EventListener())
    }

    private fun setMediaItems() = player.setMediaItems(
        listOf(MediaItem.fromUri(FIRST_MEDIA_ITEM_URL), MediaItem.fromUri(SECOND_MEDIA_ITEM_URL))
    )

    private fun play() {
        isReadyFirstTime = true
        player.playWhenReady = false
        player.prepare()
    }

    inner class EventListener : Player.Listener {
        override fun onPlaybackStateChanged(state: Int) {
            super.onPlaybackStateChanged(state)
            if (state != Player.STATE_READY) {
                return
            }
            if (isReadyFirstTime) {
                player.seekTo(110 * 1000)
                player.playWhenReady = true
                isReadyFirstTime = false
            }
        }
    }
}
