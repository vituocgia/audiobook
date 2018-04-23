package diep.space.audiobook.features.audio

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import com.afollestad.materialdialogs.MaterialDialog
import diep.space.audiobook.R
import diep.space.audiobook.data.repo.BookRepository
import diep.space.audiobook.injection.App
import diep.space.audiobook.misc.DialogController
import diep.space.audiobook.misc.DialogLayoutContainer
import diep.space.audiobook.misc.progressChangedStream
import diep.space.audiobook.playback.PlayerController
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.loudness.*
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Dialog for controlling the loudness.
 */
class LoudnessDialog(args: Bundle) : DialogController(args) {

  @Inject
  lateinit var repo: BookRepository
  @Inject
  lateinit var player: PlayerController

  private val dbFormat = DecimalFormat("0.0 dB")

  @SuppressLint("InflateParams")
  override fun onCreateDialog(savedViewState: Bundle?): Dialog {
    App.component.inject(this)

    val container = DialogLayoutContainer(
      activity!!.layoutInflater.inflate(R.layout.loudness, null, false)
    )

    val bookId = args.getLong(NI_BOOK_ID)
    val book = repo.bookById(bookId)
        ?: return MaterialDialog.Builder(activity!!).build()

    container.seekBar.max = LoudnessGain.MAX_MB
    container.seekBar.progress = book.content.loudnessGain
    container.seekBar.progressChangedStream()
      .throttleLast(200, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
      .subscribe {
        player.setLoudnessGain(it)
        container.currentValue.text = format(it)
      }

    container.currentValue.text = format(book.content.loudnessGain)
    container.maxValue.text = format(container.seekBar.max)

    return MaterialDialog.Builder(activity!!)
      .title(R.string.volume_boost)
      .customView(container.containerView, true)
      .build()
  }

  private fun format(milliDb: Int) = dbFormat.format(milliDb / 100.0)

  companion object {
    private const val NI_BOOK_ID = "ni#bookId"
    operator fun invoke(bookId: Long) = LoudnessDialog(
      Bundle().apply {
        putLong(NI_BOOK_ID, bookId)
      }
    )
  }
}
