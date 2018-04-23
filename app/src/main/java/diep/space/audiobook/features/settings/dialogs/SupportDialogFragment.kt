package diep.space.audiobook.features.settings.dialogs

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.DialogFragment

import com.afollestad.materialdialogs.MaterialDialog

import diep.space.audiobook.R
import timber.log.Timber

class SupportDialogFragment : DialogFragment() {

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val onSupportListItemClicked = MaterialDialog.ListCallback { _, _, i, _ ->
      when (i) {
        0 -> visitUri(GITHUB_URL)
        1 -> visitUri(TRANSLATION_URL)
        else -> throw AssertionError("There are just 3 items")
      }
    }

    return MaterialDialog.Builder(activity!!)
      .title(R.string.pref_support_title)
      .items(R.array.pref_support_values)
      .itemsCallback(onSupportListItemClicked)
      .build()
  }

  private fun visitUri(uri: Uri) {
    try {
      startActivity(Intent(Intent.ACTION_VIEW, uri))
    } catch (exception: ActivityNotFoundException) {
      Timber.e(exception)
    }
  }

  companion object {
    val TAG: String = SupportDialogFragment::class.java.simpleName
    private val GITHUB_URL = Uri.parse("https://github.com/Ph1b/MaterialAudiobookPlayer")!!
    private val TRANSLATION_URL =
      Uri.parse("https://www.transifex.com/projects/p/material-audiobook-player")!!
  }
}
