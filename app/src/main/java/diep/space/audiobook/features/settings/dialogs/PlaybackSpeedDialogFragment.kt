package diep.space.audiobook.features.settings.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import com.afollestad.materialdialogs.MaterialDialog
import dagger.android.support.AndroidSupportInjection
import diep.space.audiobook.R
import diep.space.audiobook.data.Book
import diep.space.audiobook.data.repo.BookRepository
import diep.space.audiobook.injection.PrefKeys
import diep.space.audiobook.misc.DialogLayoutContainer
import diep.space.audiobook.misc.inflate
import diep.space.audiobook.misc.progressChangedStream
import diep.space.audiobook.persistence.pref.Pref
import diep.space.audiobook.playback.PlayerController
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.dialog_amount_chooser.*
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named

/**
 * Dialog for setting the playback speed of the current book.
 */
class PlaybackSpeedDialogFragment : DialogFragment() {

  @Inject
  lateinit var repo: BookRepository
  @field:[Inject Named(PrefKeys.CURRENT_BOOK)]
  lateinit var currentBookIdPref: Pref<Long>
  @Inject
  lateinit var playerController: PlayerController

  @SuppressLint("InflateParams")
  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    AndroidSupportInjection.inject(this)

    // init views
    val container =
      DialogLayoutContainer(activity!!.layoutInflater.inflate(R.layout.dialog_amount_chooser))

    // setting current speed
    val book = repo.bookById(currentBookIdPref.value)
        ?: throw AssertionError("Cannot instantiate $TAG without a current book")
    val speed = book.content.playbackSpeed
    seekBar.max = ((MAX - MIN) * FACTOR).toInt()
    seekBar.progress = ((speed - MIN) * FACTOR).toInt()

    // observable of seek bar, mapped to speed
    seekBar.progressChangedStream(initialNotification = true)
      .map { Book.SPEED_MIN + it.toFloat() / FACTOR }
      .doOnNext {
        // update speed text
        val text = "${getString(R.string.playback_speed)}: ${speedFormatter.format(it)}"
        textView.text = text
      }
      .debounce(50, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
      .subscribe { playerController.setSpeed(it) } // update speed after debounce

    return MaterialDialog.Builder(activity!!)
      .title(R.string.playback_speed)
      .customView(container.containerView, true)
      .build()
  }

  companion object {
    val TAG: String = PlaybackSpeedDialogFragment::class.java.simpleName
    private const val MAX = Book.SPEED_MAX
    private const val MIN = Book.SPEED_MIN
    private const val FACTOR = 100F
    private val speedFormatter = DecimalFormat("0.0 x")
  }
}
