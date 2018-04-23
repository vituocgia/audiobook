package diep.space.audiobook.playback.utils

import android.annotation.SuppressLint
import android.os.PowerManager
import diep.space.audiobook.BuildConfig
import javax.inject.Inject

/**
 * Simple wrapper for the wakelock
 */
class WakeLockManager @Inject constructor(powerManager: PowerManager) {

  private val lock = powerManager.newWakeLock(
    PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ON_AFTER_RELEASE,
    BuildConfig.APPLICATION_ID
  )
    .apply { setReferenceCounted(false) }

  @SuppressLint("WakelockTimeout") // audiobooks are potentially very long
  fun stayAwake(stayAwake: Boolean) {
    if (stayAwake && !lock.isHeld) {
      lock.acquire()
    } else if (!stayAwake && lock.isHeld) {
      lock.release()
    }
  }
}
