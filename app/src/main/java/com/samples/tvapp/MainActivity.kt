package com.samples.tvapp

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.tv.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.tvprovider.media.tv.Channel
import com.google.gson.Gson


class MainActivity : Activity() {

    companion object {
        private val IS_TUNED: String = "IS_TUNED"
        val PROJECTION: Array<String> = getProjection()
        private val TAG: String = "CHANNELS_DATA"

        private fun getProjection(): Array<String> {
            val baseColumns = arrayOf(
                TvContract.Channels._ID,
                TvContract.Channels.COLUMN_DESCRIPTION,
                TvContract.Channels.COLUMN_DISPLAY_NAME,
                TvContract.Channels.COLUMN_DISPLAY_NUMBER,
                TvContract.Channels.COLUMN_INPUT_ID,
                TvContract.Channels.COLUMN_INTERNAL_PROVIDER_DATA,
                TvContract.Channels.COLUMN_NETWORK_AFFILIATION,
                TvContract.Channels.COLUMN_ORIGINAL_NETWORK_ID,
                TvContract.Channels.COLUMN_PACKAGE_NAME,
                TvContract.Channels.COLUMN_SEARCHABLE,
                TvContract.Channels.COLUMN_SERVICE_ID,
                TvContract.Channels.COLUMN_SERVICE_TYPE,
                TvContract.Channels.COLUMN_TRANSPORT_STREAM_ID,
                TvContract.Channels.COLUMN_TYPE,
                TvContract.Channels.COLUMN_VIDEO_FORMAT
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val marshmallowColumns = arrayOf(
                    TvContract.Channels.COLUMN_APP_LINK_COLOR,
                    TvContract.Channels.COLUMN_APP_LINK_ICON_URI,
                    TvContract.Channels.COLUMN_APP_LINK_INTENT_URI,
                    TvContract.Channels.COLUMN_APP_LINK_POSTER_ART_URI,
                    TvContract.Channels.COLUMN_APP_LINK_TEXT
                )
                return CollectionUtils.concatAll(baseColumns, marshmallowColumns)
            }
            return baseColumns
        }
    }

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var mTvInputManager: TvInputManager
    private lateinit var mTvView: TvView
    private lateinit var progressbar: ProgressBar
    private lateinit var time: TextView
    var channels = ArrayList<Channel>()
    private val handler = Handler()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mTvView = findViewById(R.id.tvview)
        progressbar = findViewById(R.id.progress)
        time = findViewById(R.id.time)
        sharedPreferences = getSharedPreferences("TVAPP_SHARED_PREFS", Context.MODE_PRIVATE)

        mTvInputManager = getSystemService(TvInputManager::class.java)
        mTvInputManager.registerCallback(object : TvInputManager.TvInputCallback() {
            fun TvInputCallback() {
                throw RuntimeException("Stub!")
            }

            override fun onInputStateChanged(inputId: String?, state: Int) {
                throw RuntimeException("Stub!")
            }

            override fun onInputAdded(inputId: String?) {
                throw RuntimeException("Stub!")
            }

            override fun onInputRemoved(inputId: String?) {
                throw RuntimeException("Stub!")
            }

            override fun onInputUpdated(inputId: String?) {
                throw RuntimeException("Stub!")
            }

            override fun onTvInputInfoUpdated(inputInfo: TvInputInfo?) {
                throw RuntimeException("Stub!")
            }
        }, handler)

        mTvView.setCallback(object : TvView.TvInputCallback() {
            override fun onConnectionFailed(inputId: String?) {
                Log.d(TAG, "onConnectionFailed $inputId")
            }

            override fun onDisconnected(inputId: String?) {
                Log.d(TAG, "onDisconnected $inputId")
                progressbar.visibility = View.GONE
            }

            override fun onChannelRetuned(inputId: String?, channelUri: Uri?) {
                Log.d(TAG, "onChannelRetuned $inputId for Channel URI = $channelUri")
            }

            override fun onTracksChanged(inputId: String?, tracks: List<TvTrackInfo?>?) {
                Log.d(TAG, "onTracksChanged $inputId")
            }

            override fun onTrackSelected(inputId: String?, type: Int, trackId: String?) {
                Log.d(TAG, "onTrackSelected $inputId for Type = $type & Track Id = $trackId")
            }

            override fun onVideoSizeChanged(inputId: String?, width: Int, height: Int) {
                Log.d(TAG, "onVideoSizeChanged $inputId to Width = $width & Height = $height")
            }

            override fun onVideoAvailable(inputId: String?) {
                Log.d(TAG, "onVideoAvailable $inputId")
                progressbar.visibility = View.GONE
            }

            override fun onVideoUnavailable(inputId: String?, reason: Int) {
                Log.d(TAG, "onVideoUnavailable $inputId for $reason")
                progressbar.visibility = View.VISIBLE
            }

            override fun onContentBlocked(inputId: String?, rating: TvContentRating?) {
                Log.d(TAG, "onContentBlocked $inputId with rating = $rating")
            }

            override fun onTimeShiftStatusChanged(inputId: String?, status: Int) {
                Log.d(TAG, "onTimeShiftStatusChanged $inputId with time status = $status")
                time.text = status.toString()
            }
        })

        mTvView.setTimeShiftPositionCallback(object : TvView.TimeShiftPositionCallback() {

            override fun onTimeShiftStartPositionChanged(inputId: String?, timeMs: Long) {
                Log.d(TAG, "onTimeShiftStartPositionChanged $inputId with time status = ${timeMs}")
                time.text = timeMs.toString()
            }

            override fun onTimeShiftCurrentPositionChanged(inputId: String?, timeMs: Long) {
                Log.d(
                    TAG,
                    "onTimeShiftCurrentPositionChanged $inputId with time status = ${(timeMs / 1000) / 60}"
                )
                time.text = ((timeMs / 1000)).toString()
            }
        })

        if (!sharedPreferences.getBoolean(IS_TUNED, false)) {
//            insertDummyChannel()
            startActivityForResult(mTvInputManager.tvInputList[0].createSetupIntent(), 100)
        } else {
            channels.clear()
            val channel =
                Gson().fromJson(sharedPreferences.getString("CHANNELS", null), Channel::class.java)
            findViewById<TextView>(R.id.channel_number).text = channel.id.toString()
            findViewById<TextView>(R.id.channel_name).text = channel.displayName
            mTvView.tune(
                channel.inputId,
                Uri.parse("content://android.media.tv/channel/${channel.id}")
            )

            mTvView.setCaptionEnabled(true)
            mTvView.setStreamVolume(0.2f)
//            mTvView.time
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        sharedPreferences.edit().putBoolean(IS_TUNED, true).apply()

        if (resultCode == RESULT_OK && requestCode == 100) {
            if (data != null) {
                Log.d("CHANNELS_DATA", "DATA FROM INTENT")
                getChannels()
            }
        }
        getChannels()
        super.onActivityResult(requestCode, resultCode, data)
    }

    @SuppressLint("RestrictedApi")
    private fun insertDummyChannel() {
        contentResolver.insert(
            TvContract.Channels.CONTENT_URI, Channel.Builder()
                .setDisplayName("Dummy Channel")
                .setDisplayNumber("414")
                .setInputId(mTvInputManager.tvInputList[0].id)
                .build().toContentValues()
        )
    }

    @SuppressLint("Range")
    private fun getChannels() {
        if (mTvInputManager.tvInputList != null && mTvInputManager.tvInputList.size > 0) {
            for (input in mTvInputManager.tvInputList) {
                if (input != null && !input.isPassthroughInput) {
                    val projection = arrayOf(
                        TvContract.Channels.COLUMN_DISPLAY_NAME,
                        TvContract.Channels.COLUMN_DISPLAY_NUMBER
                    )

                    var tvInputInfo = mTvInputManager.getTvInputInfo(input.id)

//                    mTvView.tune(mTvInputManager.tvInputList[0].id, null)

                    /*tvInputInfo?.let {
                        mTvView.tune(it.toString(), null)
                    }*/
//                    var uri = TvContractCompat.buildChannelsUriForInput(tvInputInfo?.id)


                    val cursor = contentResolver.query(
                        TvContract.Channels.CONTENT_URI,
                        PROJECTION,
                        null,
                        null,
                        null
                    )
                    Log.d("CHANNELS_DATA", "${TvContract.Channels.CONTENT_URI}")

                    Log.d("CHANNELS_DATA", "${cursor?.count} CHANNELS FOUND")

                    val channels = ArrayList<Channel>()
                    cursor?.let { cursorData ->
                        if (cursorData.moveToFirst()) {
                            do {
                                channels.add(Channel.fromCursor(cursorData))
                                Log.d(
                                    TAG,
                                    "TEST :: ${
                                        cursorData.getColumnName(
                                            cursorData.getColumnIndex(TvContract.Channels.COLUMN_DISPLAY_NAME)
                                        )
                                    } " +
                                            "=> ${
                                                cursorData.getString(
                                                    cursorData.getColumnIndex(
                                                        TvContract.Channels.COLUMN_DISPLAY_NAME
                                                    )
                                                )
                                            }"
                                )
                                Log.d(
                                    TAG,
                                    "TEST :: ${
                                        cursorData.getColumnName(
                                            cursorData.getColumnIndex(TvContract.Channels.COLUMN_DISPLAY_NUMBER)
                                        )
                                    } " +
                                            "=> ${
                                                cursorData.getString(
                                                    cursorData.getColumnIndex(
                                                        TvContract.Channels.COLUMN_DISPLAY_NUMBER
                                                    )
                                                )
                                            }"
                                )
                                Log.d(
                                    TAG,
                                    "TEST :: ${
                                        cursorData.getColumnName(
                                            cursorData.getColumnIndex(TvContract.Channels.COLUMN_DESCRIPTION)
                                        )
                                    } " +
                                            "=> ${
                                                cursorData.getString(
                                                    cursorData.getColumnIndex(
                                                        TvContract.Channels.COLUMN_DESCRIPTION
                                                    )
                                                )
                                            }"
                                )
//                                Log.d(TAG, "TEST :: ${cursorData.getColumnName(cursorData.getColumnIndex(TvContract.Channels.COLUMN_CHANNEL_LIST_ID))} " +
//                                        "=> ${cursorData.getString(cursorData.getColumnIndex(TvContract.Channels.COLUMN_CHANNEL_LIST_ID))}")
                            } while (cursorData.moveToNext())


                            Log.d(TAG, "Parsed channels = ${channels.size}")
                            if (channels.size > 0) {
                                val gson = Gson().toJson(channels[0])
                                sharedPreferences.edit().putString("CHANNELS", gson).apply()
                            }


                            Log.d(TAG, "Tuning to Channel ${channels[0].displayName}")
                            findViewById<TextView>(R.id.channel_name).text = channels[1].displayName
                            findViewById<TextView>(R.id.channel_number).text =
                                channels[1].id.toString()
                            mTvView.tune(
                                channels[1].inputId,
                                Uri.parse("content://android.media.tv/channel/${channels[1].id}")
                            )
                            /*var count = 0
                            while (cursorData.count != count) {
                                count++
                                val channel = cursorData.moveToNext()
                                Log.d("CHANNELS_DATA", "CHANNEL NAME : ${channel.displayName}")
                                Log.d("CHANNELS_DATA", "CHANNEL NAME : ${channel.displayNumber}")
                            }*/
                        }
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        mTvView.reset()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        event?.let { keyEvent ->
            if (keyEvent.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                mTvView.dispatchKeyEvent(
                    KeyEvent(
                        KeyEvent(
                            KeyEvent.ACTION_DOWN,
                            KeyEvent.KEYCODE_DPAD_CENTER
                        )
                    )
                )
            }
        }
        return super.onKeyDown(keyCode, event)
    }

}