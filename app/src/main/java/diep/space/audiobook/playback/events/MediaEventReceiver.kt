package diep.space.audiobook.playback.events

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.support.v4.content.ContextCompat.startForegroundService
import diep.space.audiobook.playback.PlaybackService

/**
 * Forwards intents to [PlaybackService]
 */
class MediaEventReceiver : BroadcastReceiver() {

  override fun onReceive(context: Context?, intent: Intent?) {
    if (context != null && intent != null) {
      val playerIntent = Intent(intent).apply {
        component = ComponentName(context, PlaybackService::class.java)
      }
      startForegroundService(context, playerIntent)
    }
  }
}
