package diep.space.audiobook.features.crashlytics

import android.app.Application
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import diep.space.audiobook.BuildConfig
import diep.space.audiobook.misc.ErrorReporter
import io.fabric.sdk.android.Fabric

/**
 * Proxy-class that forwards to crashlytics
 */
object CrashlyticsProxy : ErrorReporter {

  override fun log(message: String) {
    Crashlytics.log(message)
  }

  override fun logException(throwable: Throwable) {
    Crashlytics.logException(throwable)
  }

  fun init(app: Application) {
    val crashlytics = Crashlytics.Builder()
      .core(
        CrashlyticsCore.Builder()
          .disabled(BuildConfig.DEBUG)
          .build()
      )
      .build()
    Fabric.with(app, crashlytics)
  }
}
