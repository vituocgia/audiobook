package diep.space.audiobook.features.bookPlaying

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import com.afollestad.materialdialogs.MaterialDialog
import dagger.android.support.AndroidSupportInjection
import diep.space.audiobook.R
import diep.space.audiobook.injection.PrefKeys
import diep.space.audiobook.misc.DialogLayoutContainer
import diep.space.audiobook.misc.inflate
import diep.space.audiobook.misc.onProgressChanged
import diep.space.audiobook.persistence.pref.Pref
import kotlinx.android.synthetic.main.dialog_amount_chooser.*
import javax.inject.Inject
import javax.inject.Named

class SeekDialogFragment : DialogFragment() {

  @field:[Inject Named(PrefKeys.SEEK_TIME)]
  lateinit var seekTimePref: Pref<Int>

  @SuppressLint("InflateParams")
  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    AndroidSupportInjection.inject(this)

    val container =
      DialogLayoutContainer(activity!!.layoutInflater.inflate(R.layout.dialog_amount_chooser))

    // init
    val oldSeekTime = seekTimePref.value
    container.seekBar.max = (MAX - MIN) * FACTOR
    container.seekBar.onProgressChanged(initialNotification = true) {
      val value = it / FACTOR + MIN
      container.textView.text =
          context!!.resources.getQuantityString(R.plurals.seconds, value, value)
    }
    container.seekBar.progress = (oldSeekTime - MIN) * FACTOR

    return MaterialDialog.Builder(context!!)
      .title(R.string.pref_seek_time)
      .customView(container.containerView, true)
      .positiveText(R.string.dialog_confirm)
      .negativeText(R.string.dialog_cancel)
      .onPositive { _, _ ->
        val newSeekTime = container.seekBar.progress / FACTOR + MIN
        seekTimePref.value = newSeekTime
      }.build()
  }

  companion object {
    val TAG: String = SeekDialogFragment::class.java.simpleName

    private const val FACTOR = 10
    private const val MIN = 3
    private const val MAX = 60
  }
}
