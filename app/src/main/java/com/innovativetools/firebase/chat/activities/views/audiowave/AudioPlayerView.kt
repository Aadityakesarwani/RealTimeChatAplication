package com.innovativetools.firebase.chat.activities.views.audiowave

import com.innovativetools.firebase.chat.activities.managers.Utils.isEmpty
import com.innovativetools.firebase.chat.activities.managers.Utils.convertSecondsToHMmSs
import com.innovativetools.firebase.chat.activities.managers.Utils.getErrors
import com.innovativetools.firebase.chat.activities.managers.Utils.sout
import android.graphics.drawable.GradientDrawable
import com.innovativetools.firebase.chat.activities.views.audiowave.AudioWave
import android.media.audiofx.Visualizer
import android.media.MediaPlayer
import com.innovativetools.firebase.chat.activities.views.voiceplayer.PlayerVisualizerSeekbar
import android.content.res.TypedArray
import com.innovativetools.firebase.chat.activities.R
import com.innovativetools.firebase.chat.activities.constants.IConstants
import android.view.LayoutInflater
import android.graphics.PorterDuff
import com.innovativetools.firebase.chat.activities.views.audiowave.MyDrawableCompat
import android.media.AudioManager
import android.media.MediaPlayer.OnPreparedListener
import android.media.MediaPlayer.OnCompletionListener
import com.innovativetools.firebase.chat.activities.managers.Screens
import android.media.audiofx.Visualizer.OnDataCaptureListener
import android.widget.SeekBar.OnSeekBarChangeListener
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.StrictMode.VmPolicy
import android.os.StrictMode
import android.os.Looper
import android.util.AttributeSet
import android.widget.*
import com.innovativetools.firebase.chat.activities.views.voiceplayer.FileUtils
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.net.URLConnection

class AudioPlayerView : LinearLayout {
    private var headsetBackgroundColor = 0
    var playPauseBackgroundColor = 0
    var playPauseIconColor = 0
    private var pauseBackgroundColor = 0
    private var pauseIconColor = 0
    private var downloadBackgroundColor = 0
    private var downloadIconColor = 0
    var shareBackgroundColor = 0
    var viewBackgroundColor = 0
    var seekBarProgressColor = 0
    var seekBarThumbColor = 0
    var progressTimeColor = 0
    var timingBackgroundColor = 0
    var visualizationPlayedColor = 0
    var visualizationNotPlayedColor = 0
    var playProgressbarColor = 0
    private var headsetDirection = 0
    var viewCornerRadius = 0f
    private var headsetCornerRadius = 0f
    var playCornerRadius = 0f
    private var pauseCornerRadius = 0f
    private var downloadCornerRadius = 0f
    var shareCornerRadius = 0f
    var isShowShareButton = false
    var isShowTiming = false
    var isEnableVirtualizer = false
    var playShape: GradientDrawable? = null
    private var pauseShape: GradientDrawable? = null
    private var downloadShape: GradientDrawable? = null
    var shareShape: GradientDrawable? = null
    var viewShape: GradientDrawable? = null
    private var headsetShape: GradientDrawable? = null
//    private lateinit var context: Context? = null
    var path: String? = null
    var shareTitle: String? = "Share Voice"
    var main_layout: LinearLayout? = null
    var padded_layout: LinearLayout? = null
    var imgPlay: ImageView? = null
    var imgPause: ImageView? = null
    var imgShare: ImageView? = null
    var imgDownload: ImageView? = null
    private var imgHeadset: ImageView? = null
    private var audioHeadsetLayout: RelativeLayout? = null
    var container_layout: RelativeLayout? = null
    private var audioWave: AudioWave? = null
    private var mVisualizer: Visualizer? = null
    var seekBar: SeekBar? = null
    var progressBar: ProgressBar? = null
    var txtProcess: TextView? = null
    private var txtAudioFileName: TextView? = null
    var mediaPlayer: MediaPlayer? = null
    var playProgressbar: ProgressBar? = null
        get() = field
        set
    var seekbarV: PlayerVisualizerSeekbar? = null


    constructor(context: Context) : super(context) {
        this.context = context
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initViews(context, attrs)
        this.context = context
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initViews(context, attrs)
        this.context = context
    }

    private fun initViews(context: Context, attrs: AttributeSet?) {
        val typedArray =
            context.theme.obtainStyledAttributes(attrs, R.styleable.VoicePlayerView, 0, 0)
        viewShape = GradientDrawable()
        playShape = GradientDrawable()
        pauseShape = GradientDrawable()
        downloadShape = GradientDrawable()
        shareShape = GradientDrawable()
        headsetShape = GradientDrawable()
        try {
            isShowShareButton =
                typedArray.getBoolean(R.styleable.VoicePlayerView_showShareButton, false)
            isShowTiming = typedArray.getBoolean(R.styleable.VoicePlayerView_showTiming, true)
            viewCornerRadius = typedArray.getFloat(R.styleable.VoicePlayerView_viewCornerRadius, 0f)
            playCornerRadius =
                typedArray.getFloat(R.styleable.VoicePlayerView_playPauseCornerRadius, 0f)
            pauseCornerRadius =
                typedArray.getFloat(R.styleable.VoicePlayerView_playPauseCornerRadius, 0f)
            downloadCornerRadius =
                typedArray.getFloat(R.styleable.VoicePlayerView_playPauseCornerRadius, 0f)
            shareCornerRadius =
                typedArray.getFloat(R.styleable.VoicePlayerView_shareCornerRadius, 0f)
            playPauseBackgroundColor = typedArray.getColor(
                R.styleable.VoicePlayerView_playPauseBackgroundColor,
                resources.getColor(R.color.colorAccent)
            )
            playPauseIconColor = typedArray.getColor(
                R.styleable.VoicePlayerView_playPauseIconColor,
                resources.getColor(R.color.colorAccent)
            )
            pauseBackgroundColor = typedArray.getColor(
                R.styleable.VoicePlayerView_playPauseBackgroundColor,
                resources.getColor(R.color.colorAccent)
            )
            pauseIconColor = typedArray.getColor(
                R.styleable.VoicePlayerView_playPauseIconColor,
                resources.getColor(R.color.colorAccent)
            )
            downloadBackgroundColor = typedArray.getColor(
                R.styleable.VoicePlayerView_playPauseBackgroundColor,
                resources.getColor(R.color.colorAccent)
            )
            downloadIconColor = typedArray.getColor(
                R.styleable.VoicePlayerView_playPauseIconColor,
                resources.getColor(R.color.colorAccent)
            )
            shareBackgroundColor = typedArray.getColor(
                R.styleable.VoicePlayerView_shareBackgroundColor,
                resources.getColor(R.color.colorAccent)
            )
            viewBackgroundColor = typedArray.getColor(
                R.styleable.VoicePlayerView_viewBackground,
                resources.getColor(R.color.white)
            )
            seekBarProgressColor = typedArray.getColor(
                R.styleable.VoicePlayerView_seekBarProgressColor,
                resources.getColor(R.color.colorAccent)
            )
            seekBarThumbColor = typedArray.getColor(
                R.styleable.VoicePlayerView_seekBarThumbColor,
                resources.getColor(R.color.colorAccent)
            )
            progressTimeColor =
                typedArray.getColor(R.styleable.VoicePlayerView_progressTimeColor, Color.GRAY)
            shareTitle = typedArray.getString(R.styleable.VoicePlayerView_shareText)
            isEnableVirtualizer =
                typedArray.getBoolean(R.styleable.VoicePlayerView_enableVisualizer, false)
            timingBackgroundColor = typedArray.getColor(
                R.styleable.VoicePlayerView_timingBackgroundColor,
                resources.getColor(android.R.color.transparent)
            )
            visualizationNotPlayedColor = typedArray.getColor(
                R.styleable.VoicePlayerView_visualizationNotPlayedColor,
                resources.getColor(R.color.gray)
            )
            visualizationPlayedColor = typedArray.getColor(
                R.styleable.VoicePlayerView_visualizationPlayedColor,
                resources.getColor(R.color.colorAccent)
            )
            playProgressbarColor = typedArray.getColor(
                R.styleable.VoicePlayerView_playProgressbarColor,
                resources.getColor(R.color.colorAccent)
            )
            headsetBackgroundColor = typedArray.getColor(
                R.styleable.VoicePlayerView_headsetBackgroundColor,
                resources.getColor(R.color.colorAccent)
            )
            headsetCornerRadius =
                typedArray.getFloat(R.styleable.VoicePlayerView_headsetCornerRadius, 0f)
            headsetDirection =
                typedArray.getInt(R.styleable.VoicePlayerView_headsetDirection, IConstants.ZERO)
        } finally {
            typedArray.recycle()
        }

        //0 = Right, 1 = Left
        if (headsetDirection == IConstants.ZERO) { //RIGHT
            LayoutInflater.from(context).inflate(R.layout.vp_audio_view_right, this)
        } else {
            LayoutInflater.from(context).inflate(R.layout.vp_audio_view_left, this)
        }
        main_layout = findViewById(R.id.collectorLinearLayout)
        padded_layout = findViewById(R.id.paddedLinearLayout)
        container_layout = findViewById(R.id.containerLinearLayout)
        imgPlay = findViewById(R.id.imgPlay)
        imgPause = findViewById(R.id.imgPause)
        imgShare = findViewById(R.id.imgShare)
        seekBar = findViewById(R.id.seekBar)
        txtAudioFileName = findViewById(R.id.txtAudioFileName)
        progressBar = findViewById(R.id.progressBar)
        txtProcess = findViewById(R.id.txtTime)
        seekbarV = findViewById(R.id.seekBarV)
        playProgressbar = findViewById(R.id.pb_play)
        imgDownload = findViewById(R.id.imgDownload)
        audioHeadsetLayout = findViewById(R.id.audioHeadsetLayout)
        audioWave = findViewById(R.id.audioWave)
        imgHeadset = findViewById(R.id.imgHeadset)
        playProgressbar?.setVisibility(GONE)
        imgDownload?.setVisibility(GONE)
        audioWave?.setVisibility(GONE)
        txtAudioFileName?.setVisibility(GONE)
        imgHeadset?.setVisibility(VISIBLE)
        viewShape!!.setColor(viewBackgroundColor)
        viewShape!!.cornerRadius = viewCornerRadius
        playShape!!.setColor(playPauseBackgroundColor)
        playShape!!.cornerRadius = playCornerRadius
        pauseShape!!.setColor(pauseBackgroundColor)
        pauseShape!!.cornerRadius = pauseCornerRadius
        downloadShape!!.setColor(downloadBackgroundColor)
        downloadShape!!.cornerRadius = downloadCornerRadius
        shareShape!!.setColor(shareBackgroundColor)
        shareShape!!.cornerRadius = shareCornerRadius
        headsetShape!!.setColor(headsetBackgroundColor)
        headsetShape!!.cornerRadius = headsetCornerRadius
        imgPause?.setBackground(playShape)
        imgPause?.setColorFilter(playPauseIconColor, PorterDuff.Mode.SRC_IN)
        imgPlay?.setBackground(pauseShape)
        imgPlay?.setColorFilter(pauseIconColor, PorterDuff.Mode.SRC_IN)
        imgDownload?.setBackground(downloadShape)
        imgDownload?.setColorFilter(downloadIconColor, PorterDuff.Mode.SRC_IN)
        audioHeadsetLayout?.setBackground(headsetShape)
        imgHeadset?.setBackground(playShape)
        imgHeadset?.setColorFilter(playPauseIconColor, PorterDuff.Mode.SRC_IN)
        //        imgHeadset.setColorFilter(ContextCompat.getColor(context, playPauseIconColor), PorterDuff.Mode.SRC_IN);
        audioWave?.config?.color = playPauseIconColor
        imgShare?.setBackground(shareShape)
        main_layout?.setBackground(viewShape)
        seekBar?.getProgressDrawable()
            ?.let { MyDrawableCompat.setColorFilter(it, seekBarProgressColor) }
        seekBar?.getThumb()?.let { MyDrawableCompat.setColorFilter(it, seekBarThumbColor) }
        //        seekBar.getProgressDrawable().setColorFilter(seekBarProgressColor, PorterDuff.Mode.SRC_IN);
//        seekBar.getThumb().setColorFilter(seekBarThumbColor, PorterDuff.Mode.SRC_IN);
        val timingBackground = GradientDrawable()
        timingBackground.setColor(timingBackgroundColor)
        timingBackground.cornerRadius = 25f
        txtProcess?.setBackground(timingBackground)
        txtProcess?.setPadding(6, 0, 6, 0)
        txtProcess?.setTextColor(progressTimeColor)
        playProgressbar?.getIndeterminateDrawable()?.let {
            MyDrawableCompat.setColorFilter(
                it,
                playProgressbarColor
            )
        }
        //        pb_play.getIndeterminateDrawable().setColorFilter(playProgressbarColor, PorterDuff.Mode.SRC_IN);
        if (!isShowShareButton) imgShare?.setVisibility(GONE)
        if (!isShowTiming) txtProcess?.setVisibility(INVISIBLE)
        if (isEnableVirtualizer) {
            seekbarV?.setVisibility(VISIBLE)
            seekBar?.setVisibility(GONE)
            seekbarV?.getProgressDrawable()?.let {
                MyDrawableCompat.setColorFilter(
                    it,
                    resources.getColor(android.R.color.transparent)
                )
            }
            seekbarV?.getThumb()?.let {
                MyDrawableCompat.setColorFilter(
                    it,
                    resources.getColor(android.R.color.transparent)
                )
            }
            //            seekbarV.getProgressDrawable().setColorFilter(getResources().getColor(android.R.color.transparent), PorterDuff.Mode.SRC_IN);
//            seekbarV.getThumb().setColorFilter(getResources().getColor(android.R.color.transparent), PorterDuff.Mode.SRC_IN);
            seekbarV?.setColors(visualizationPlayedColor, visualizationNotPlayedColor)
        }
    }

    fun setFileName(str: String?) {
        if (!isEmpty(str)) {
            txtAudioFileName!!.visibility = VISIBLE
            txtAudioFileName!!.text = str
        } else {
            txtAudioFileName!!.visibility = GONE
        }
    }

    //Set the audio source and prepare mediaPlayer
    fun setAudio(audioPath: String?) {
        path = audioPath
        mediaPlayer = MediaPlayer()
        if (path != null) {
            try {
                mediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
                //                mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
//                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
//                        .build());
                mediaPlayer!!.setDataSource(path)
                mediaPlayer!!.prepare()
                prepareVisualizer()
                mediaPlayer!!.setVolume(10f, 10f)
                //START and PAUSE are in other listeners
                mediaPlayer!!.setOnPreparedListener { mp ->
                    seekBar!!.max = mp.duration
                    if (seekbarV!!.visibility == VISIBLE) {
                        seekbarV!!.max = mp.duration
                    }
                    //                        txtProcess.setText(DEFAULT_ZERO + " / " + convertSecondsToHMmSs(mp.getDuration()));
                    txtProcess!!.text = convertSecondsToHMmSs(mp.duration.toLong())
                }
                mediaPlayer!!.setOnCompletionListener {
                    try {
                        mVisualizer!!.enabled = false
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    imgPause!!.visibility = GONE
                    audioWave!!.visibility = GONE
                    imgPlay!!.visibility = VISIBLE
                    imgHeadset!!.visibility = VISIBLE
                    imgDownload!!.visibility = GONE
                    playProgressbar!!.visibility = GONE
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

//            seekBar.setOnSeekBarChangeListener(seekBarListener);
//            imgPlay.setOnClickListener(imgPlayClickListener);
//            imgPause.setOnClickListener(imgPauseClickListener);
            imgShare!!.setOnClickListener(imgShareClickListener)
            if (seekbarV!!.visibility == VISIBLE) {
                seekbarV!!.updateVisualizer(FileUtils.fileToBytes(File(path)))
            }
            seekbarV!!.setOnSeekBarChangeListener(seekBarListener)
            seekbarV!!.updateVisualizer(FileUtils.fileToBytes(File(path)))
        } else {
            imgPlay!!.setOnClickListener(imgPlayNoFileClickListener)
        }
    }

    //Components' listeners
    var imgPlayClickListener = OnClickListener {
        imgPause!!.visibility = VISIBLE
        audioWave!!.visibility = VISIBLE
        imgPlay!!.visibility = GONE
        imgHeadset!!.visibility = INVISIBLE
        imgDownload!!.visibility = GONE
        playProgressbar!!.visibility = GONE
        try {
            mVisualizer!!.enabled = true
        } catch (e: Exception) {
//                    Utils.getErrors(e);
            prepareVisualizer()
        }
        mediaPlayer!!.start()
        try {
            context?.let { it1 -> update(mediaPlayer, txtProcess, seekBar, it1) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    var imgPauseClickListener = OnClickListener {
        imgPlay!!.visibility = VISIBLE
        imgPause!!.visibility = GONE
        audioWave!!.visibility = GONE
        imgHeadset!!.visibility = VISIBLE
        imgDownload!!.visibility = GONE
        playProgressbar!!.visibility = GONE
        mediaPlayer!!.pause()
    }
    val imgPlayNoFileClickListener = OnClickListener {
        val screens = context?.let { it1 -> Screens(it1) }
        screens?.showToast(R.string.msgFileNotFound)
    }

    private fun prepareVisualizer() {
        try {
            mVisualizer = Visualizer(mediaPlayer!!.audioSessionId)
            mVisualizer!!.captureSize = Visualizer.getCaptureSizeRange()[1]
            mVisualizer!!.setDataCaptureListener(
                object : OnDataCaptureListener {
                    override fun onWaveFormDataCapture(
                        visualizer: Visualizer,
                        bytes: ByteArray,
                        samplingRate: Int
                    ) {
                        audioWave!!.updateVisualizer(bytes)
                    }

                    override fun onFftDataCapture(
                        visualizer: Visualizer,
                        bytes: ByteArray,
                        samplingRate: Int
                    ) {
                    }
                }, Visualizer.getMaxCaptureRate() / 2, true, false
            )
            mVisualizer!!.enabled = true
        } catch (e: Exception) {
            getErrors(e)
        }
    }

    var seekBarListener: OnSeekBarChangeListener = object : OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            if (fromUser) {
                mediaPlayer!!.seekTo(progress)
                context?.let { update(mediaPlayer, txtProcess, seekBar, it) }
                if (seekbarV!!.visibility == VISIBLE) {
                    seekbarV!!.updatePlayerPercent(mediaPlayer!!.currentPosition.toFloat() / mediaPlayer!!.duration)
                }
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {
            sout("onSeek touch")
            imgPause!!.visibility = GONE
            audioWave!!.visibility = GONE
            imgPlay!!.visibility = VISIBLE
            imgHeadset!!.visibility = VISIBLE
        }

        override fun onStopTrackingTouch(seekBar: SeekBar) {
            imgPlay!!.visibility = GONE
            imgHeadset!!.visibility = INVISIBLE
            imgPause!!.visibility = VISIBLE
            audioWave!!.visibility = VISIBLE
            mediaPlayer!!.start()
        }
    }

    fun stopPlayer() {
        try {
            sout("Stop Player from Audio Player")
            imgPause!!.callOnClick()
        } catch (e: Exception) {
            getErrors(e)
        }
    }

    var imgShareClickListener = OnClickListener {
        (context as Activity).runOnUiThread {
            imgShare!!.visibility = GONE
            progressBar!!.visibility = VISIBLE
        }
        val file = File(path)
        if (file.exists()) {
            val intentShareFile = Intent(Intent.ACTION_SEND)
            intentShareFile.type = URLConnection.guessContentTypeFromName(file.name)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val builder = VmPolicy.Builder()
                StrictMode.setVmPolicy(builder.build())
            }
            intentShareFile.putExtra(
                Intent.EXTRA_STREAM,
                Uri.parse("file://" + file.absolutePath)
            )
            (context as Activity).startActivity(Intent.createChooser(intentShareFile, shareTitle))
        }
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            (context as Activity).runOnUiThread {
                progressBar!!.visibility = GONE
                imgShare!!.visibility = VISIBLE
            }
        }, 500)
    }

    //Updating seekBar in realtime
    private fun update(
        mediaPlayer: MediaPlayer?,
        time: TextView?,
        seekBar: SeekBar?,
        context: Context
    ) {
        (context as Activity).runOnUiThread {
            seekBar!!.progress = mediaPlayer!!.currentPosition
            if (seekbarV!!.visibility == VISIBLE) {
                seekbarV!!.progress = mediaPlayer.currentPosition
                seekbarV!!.updatePlayerPercent(mediaPlayer.currentPosition.toFloat() / mediaPlayer.duration)
            }
            if (mediaPlayer.duration - mediaPlayer.currentPosition > 100) {
//                    time.setText(convertSecondsToHMmSs(mediaPlayer.getCurrentPosition()) + " / " + convertSecondsToHMmSs(mediaPlayer.getDuration() ));
                time!!.text = convertSecondsToHMmSs(mediaPlayer.currentPosition.toLong())
            } else {
//                    time.setText(DEFAULT_ZERO + " / " + convertSecondsToHMmSs(mediaPlayer.getDuration() / 1000));
                time!!.text = convertSecondsToHMmSs(mediaPlayer.duration.toLong())
                seekBar.progress = 0
                if (seekbarV!!.visibility == VISIBLE) {
                    seekbarV!!.updatePlayerPercent(0f)
                    seekbarV!!.progress = 0
                }
            }
            val handler = Handler(Looper.getMainLooper())
            try {
                val runnable = Runnable {
                    try {
                        if (mediaPlayer.currentPosition > -1) {
                            try {
                                update(mediaPlayer, time, seekBar, context)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                handler.postDelayed(runnable, 2)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    //Convert long milli seconds to a formatted String to display it
    //    private static String convertSecondsToHMmSs(long seconds) {
    //        long s = seconds % 60;
    //        long m = (seconds / 60) % 60;
    //        long h = (seconds / (60 * 60)) % 24;
    //        return String.format("%02d:%02d", m, s);
    //    }
    //These both functions to avoid mediaplayer errors
    fun onStop() {
        try {
            try {
                mVisualizer!!.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            mediaPlayer!!.stop()
            mediaPlayer!!.reset()
            mediaPlayer!!.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onPause() {
        try {
            if (mediaPlayer != null) {
                if (mediaPlayer!!.isPlaying) mediaPlayer!!.pause()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            mVisualizer!!.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        //        imgPause.setVisibility(View.GONE);
//        audioWave.setVisibility(GONE);
//        imgPlay.setVisibility(View.VISIBLE);
//        imgHeadset.setVisibility(VISIBLE);
//        imgDownload.setVisibility(GONE);
//        pb_play.setVisibility(GONE);
    }

    // Programmatically functions
    fun setViewBackgroundShape(color: Int, radius: Float) {
        val shape = GradientDrawable()
        shape.setColor(resources.getColor(color))
        shape.cornerRadius = radius
        main_layout!!.background = shape
    }

    fun setShareBackgroundShape(color: Int, radius: Float) {
        val shape = GradientDrawable()
        shape.setColor(resources.getColor(color))
        shape.cornerRadius = radius
        imgShare!!.background = shape
    }

    fun setPlayPauseBackgroundShape(color: Int, radius: Float) {
        val shape = GradientDrawable()
        shape.setColor(resources.getColor(color))
        shape.cornerRadius = radius
        imgPause!!.background = shape
        imgPlay!!.background = shape
    }

    fun setSeekBarStyle(progressColor: Int, thumbColor: Int) {
        MyDrawableCompat.setColorFilter(
            seekBar!!.progressDrawable,
            resources.getColor(progressColor)
        )
        MyDrawableCompat.setColorFilter(seekBar!!.thumb, resources.getColor(thumbColor))
        //        seekBar.getProgressDrawable().setColorFilter(getResources().getColor(progressColor), PorterDuff.Mode.SRC_IN);
//        seekBar.getThumb().setColorFilter(getResources().getColor(thumbColor), PorterDuff.Mode.SRC_IN);
    }

    fun setTimingVisibility(visibility: Boolean) {
        if (!visibility) txtProcess!!.visibility = INVISIBLE else txtProcess!!.visibility = VISIBLE
    }

    fun setShareButtonVisibility(visibility: Boolean) {
        if (!visibility) imgShare!!.visibility = GONE else imgShare!!.visibility = VISIBLE
    }

    fun setShareText(shareText: String?) {
        shareTitle = shareText
    }

    fun showDownloadButton() {
        imgPlay!!.visibility = GONE
        imgHeadset!!.visibility = VISIBLE
        imgPause!!.visibility = GONE
        audioWave!!.visibility = GONE
        imgDownload!!.visibility = VISIBLE
        playProgressbar!!.visibility = GONE
    }

    fun showPlayProgressbar() {
        imgPlay!!.visibility = GONE
        imgHeadset!!.visibility = VISIBLE
        imgPause!!.visibility = GONE
        audioWave!!.visibility = GONE
        imgDownload!!.visibility = GONE
        playProgressbar!!.visibility = VISIBLE
    }

    fun hidePlayProgressbar() {
        imgDownload!!.visibility = GONE
        playProgressbar!!.visibility = GONE
        imgPlay!!.visibility = VISIBLE
        imgHeadset!!.visibility = VISIBLE
    }

    fun hidePlayProgressAndPlay() {
        imgDownload!!.visibility = GONE
        playProgressbar!!.visibility = GONE
        imgPlay!!.visibility = VISIBLE
        imgHeadset!!.visibility = VISIBLE
        imgPlay!!.callOnClick()
    }

    fun refreshVisualizer() {
        if (seekbarV!!.visibility == VISIBLE) {
            seekbarV!!.updateVisualizer(path?.let { File(it) }?.let { FileUtils.fileToBytes(it) })
        }
    }

    fun setContext(context: Context) {
        if (this.context == null) { // or any other appropriate condition
            this.context = context
        }
    }





}