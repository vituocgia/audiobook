package diep.space.audiobook.features.bookOverview

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.support.design.widget.BottomSheetDialog
import android.support.design.widget.BottomSheetDialogFragment
import android.widget.TextView
import com.bluelinelabs.conductor.Controller
import dagger.android.support.AndroidSupportInjection
import diep.space.audiobook.R
import diep.space.audiobook.data.Book
import diep.space.audiobook.data.repo.BookRepository
import diep.space.audiobook.features.bookmarks.BookmarkController
import diep.space.audiobook.misc.DialogLayoutContainer
import diep.space.audiobook.misc.RouterProvider
import diep.space.audiobook.misc.bottomCompoundDrawable
import diep.space.audiobook.misc.color
import diep.space.audiobook.misc.conductor.asTransaction
import diep.space.audiobook.misc.endCompoundDrawable
import diep.space.audiobook.misc.findCallback
import diep.space.audiobook.misc.inflate
import diep.space.audiobook.misc.startCompoundDrawable
import diep.space.audiobook.misc.tinted
import diep.space.audiobook.misc.topCompoundDrawable
import kotlinx.android.synthetic.main.book_more_bottom_sheet.*
import timber.log.Timber
import javax.inject.Inject

/**
 * Bottom sheet dialog fragment that will be displayed when a book edit was requested
 */
class EditBookBottomSheet : BottomSheetDialogFragment() {

  @Inject
  lateinit var repo: BookRepository

  private fun callback() = findCallback<Callback>(NI_TARGET)

  @SuppressLint("InflateParams")
  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    AndroidSupportInjection.inject(this)

    val dialog = BottomSheetDialog(context!!, R.style.BottomSheetStyle)

    // if there is no book, skip here
    val book = repo.bookById(bookId())
    if (book == null) {
      Timber.e("book is null. Return early")
      return dialog
    }

    val container =
      DialogLayoutContainer(activity!!.layoutInflater.inflate(R.layout.book_more_bottom_sheet))
    dialog.setContentView(container.containerView)

    container.title.setOnClickListener {
      EditBookTitleDialogFragment.newInstance(book)
        .show(fragmentManager, EditBookTitleDialogFragment.TAG)
      dismiss()
    }
    container.internetCover.setOnClickListener {
      callback().onInternetCoverRequested(book)
      dismiss()
    }
    container.fileCover.setOnClickListener {
      callback().onFileCoverRequested(book)
      dismiss()
    }
    container.bookmark.setOnClickListener {
      val router = (activity as RouterProvider).provideRouter()
      val controller = BookmarkController.newInstance(book.id)
      router.pushController(controller.asTransaction())

      dismiss()
    }

    tintLeftDrawable(container.title)
    tintLeftDrawable(container.internetCover)
    tintLeftDrawable(container.fileCover)
    tintLeftDrawable(container.bookmark)

    return dialog
  }

  private fun tintLeftDrawable(textView: TextView) {
    val left = textView.startCompoundDrawable()!!
    val tinted = left.tinted(context!!.color(R.color.icon_color))
    textView.setCompoundDrawablesRelative(
      tinted,
      textView.topCompoundDrawable(),
      textView.endCompoundDrawable(),
      textView.bottomCompoundDrawable()
    )
  }

  private fun bookId() = arguments!!.getLong(NI_BOOK)

  companion object {
    private const val NI_BOOK = "ni#book"
    private const val NI_TARGET = "ni#target"
    fun <T> newInstance(target: T, book: Book) where T : Controller, T : Callback =
      EditBookBottomSheet().apply {
        arguments = Bundle().apply {
          putLong(NI_BOOK, book.id)
          putString(NI_TARGET, target.instanceId)
        }
      }
  }

  interface Callback {
    fun onInternetCoverRequested(book: Book)
    fun onFileCoverRequested(book: Book)
  }
}
