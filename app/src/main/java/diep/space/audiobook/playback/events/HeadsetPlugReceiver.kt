package diep.space.audiobook.playback.events

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import diep.space.audiobook.misc.RxBroadcast
import io.reactivex.Observable
import timber.log.Timber

/**
 * Simple receiver wrapper which holds a [android.content.BroadcastReceiver] that notifies on headset changes.
 */
object HeadsetPlugReceiver {

  private val filter = IntentFilter(Intent.ACTION_HEADSET_PLUG)
  private val PLUGGED = 1
  private val UNPLUGGED = 0

  fun events(c: Context): Observable<HeadsetState> = RxBroadcast.register(c, filter)
    .map {
      Timber.i("onReceive with intent=$it")
      val intState = it.getIntExtra("state", UNPLUGGED)
      when (it.getIntExtra("state", UNPLUGGED)) {
        UNPLUGGED -> HeadsetState.UNPLUGGED
        PLUGGED -> HeadsetState.PLUGGED
        else -> {
          Timber.i("Unknown headsetState $intState")
          HeadsetState.UNKNOWN
        }
      }
    }

  enum class HeadsetState {
    PLUGGED,
    UNPLUGGED,
    UNKNOWN
  }
}
