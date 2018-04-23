package diep.space.audiobook.features.bookmarks.dialogs

import android.app.Dialog
import android.os.Bundle
import android.text.InputType
import android.view.inputmethod.EditorInfo
import com.afollestad.materialdialogs.MaterialDialog
import com.bluelinelabs.conductor.Controller
import diep.space.audiobook.R
import diep.space.audiobook.data.Bookmark
import diep.space.audiobook.misc.DialogController

/**
 * Dialog for changing the bookmark title.
 */
class EditBookmarkDialog : DialogController() {

  override fun onCreateDialog(savedViewState: Bundle?): Dialog {
    val bookmarkTitle = args.getString(NI_BOOKMARK_TITLE)
    val bookmarkId = args.getLong(NI_BOOK_ID)

    val dialog = MaterialDialog.Builder(activity!!)
      .title(diep.space.audiobook.R.string.bookmark_edit_title)
      .inputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or InputType.TYPE_TEXT_FLAG_AUTO_CORRECT)
      .input(
        activity!!.getString(R.string.bookmark_edit_hint),
        bookmarkTitle,
        false
      ) { _, charSequence ->
        val callback = targetController as EditBookmarkDialog.Callback
        val newTitle = charSequence.toString()
        callback.onEditBookmark(bookmarkId, newTitle)
      }
      .positiveText(diep.space.audiobook.R.string.dialog_confirm)
      .build()
    val editText = dialog.inputEditText!!
    editText.setOnEditorActionListener { _, actionId, _ ->
      if (actionId == EditorInfo.IME_ACTION_DONE) {
        val callback = targetController as EditBookmarkDialog.Callback
        val newTitle = editText.text.toString()
        callback.onEditBookmark(bookmarkId, newTitle)
        dismissDialog()
        true
      } else false
    }
    return dialog
  }

  interface Callback {
    fun onEditBookmark(id: Long, title: String)
  }

  companion object {

    private const val NI_BOOK_ID = "ni#bookId"
    private const val NI_BOOKMARK_TITLE = "ni#bookmarkTitle"

    operator fun <T> invoke(
      target: T,
      bookmark: Bookmark
    ) where T : Controller, T : EditBookmarkDialog.Callback = EditBookmarkDialog().apply {
      targetController = target
      args.putLong(NI_BOOK_ID, bookmark.id)
      args.putString(NI_BOOKMARK_TITLE, bookmark.title)
    }
  }
}
