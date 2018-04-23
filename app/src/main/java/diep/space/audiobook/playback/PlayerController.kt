package diep.space.audiobook.playback

import android.content.Context
import android.content.Intent
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Class for controlling the player through the service
 */
@Singleton
class PlayerController
@Inject constructor(val context: Context) {

  val playPauseIntent = intent(ACTION_PLAY_PAUSE)
  val rewindAutoPlayerIntent = intent(ACTION_REWIND_AUTO_PLAY)
  val stopIntent = intent(ACTION_STOP)
  val fastForwardAutoPlayIntent = intent(ACTION_FAST_FORWARD_AUTO_PLAY)

  private val fastForwardIntent = intent(ACTION_FAST_FORWARD)
  private val rewindIntent = intent(ACTION_REWIND)
  private val playIntent = intent(ACTION_PLAY)
  private val nextIntent = intent(ACTION_FORCE_NEXT)
  private val previousIntent = intent(ACTION_FORCE_PREVIOUS)

  private fun intent(action: String) = Intent(context, PlaybackService::class.java).apply {
    setAction(action)
  }

  fun stop() = fire(stopIntent)

  fun rewind() = fire(rewindIntent)

  fun play() = fire(playIntent)

  fun fastForward() = fire(fastForwardIntent)

  private fun fire(intent: Intent) {
    context.startService(intent)
  }

  fun previous() = fire(previousIntent)

  fun playPause() = fire(playPauseIntent)

  fun next() = fire(nextIntent)

  fun setSpeed(speed: Float) {
    fire(
      intent(ACTION_SPEED).apply {
        putExtra(EXTRA_SPEED, speed)
      }
    )
  }

  fun changePosition(time: Int, file: File) {
    fire(
      intent(ACTION_CHANGE).apply {
        putExtra(CHANGE_TIME, time)
        putExtra(CHANGE_FILE, file.absolutePath)
      }
    )
  }

  fun setLoudnessGain(mB: Int) {
    fire(
      intent(ACTION_LOUDNESS).apply {
        putExtra(CHANGE_LOUDNESS, mB)
      }
    )
  }

  companion object {

    const val ACTION_SPEED = "diep.space.audiobook.ACTION_SPEED"
    const val ACTION_STOP = "diep.space.audiobook.ACTION_STOP"
    const val ACTION_PLAY = "diep.space.audiobook.ACTION_PLAY"
    const val ACTION_REWIND = "diep.space.audiobook.ACTION_REWIND"
    const val ACTION_REWIND_AUTO_PLAY = "diep.space.audiobook.ACTION_REWIND_AUTO_PLAY"
    const val ACTION_FAST_FORWARD = "diep.space.audiobook.ACTION_FAST_FORWARD"
    const val ACTION_FAST_FORWARD_AUTO_PLAY = "diep.space.audiobook.ACTION_FAST_FORWARD_AUTO_PLAY"
    const val ACTION_FORCE_NEXT = "diep.space.audiobook.ACTION_FORCE_NEXT"
    const val ACTION_FORCE_PREVIOUS = "diep.space.audiobook.ACTION_FORCE_PREVIOUS"
    const val ACTION_PLAY_PAUSE = "diep.space.audiobook.ACTION_PLAY_PAUSE"
    const val ACTION_LOUDNESS = "diep.space.audiobook.ACTION_LOUDNESS"
    const val EXTRA_SPEED = "diep.space.audiobook.EXTRA_SPEED"
    const val ACTION_CHANGE = "diep.space.audiobook.ACTION_CHANGE"
    const val CHANGE_TIME = "diep.space.audiobook.CHANGE_TIME"
    const val CHANGE_LOUDNESS = "diep.space.audiobook.CHANGE_LOUDNESS"
    const val CHANGE_FILE = "diep.space.audiobook.CHANGE_FILE"
  }
}
