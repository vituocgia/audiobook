package diep.space.audiobook.features.bookmarks

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.View
import android.widget.PopupMenu
import diep.space.audiobook.R
import diep.space.audiobook.data.Bookmark
import diep.space.audiobook.data.Chapter
import diep.space.audiobook.features.bookmarks.dialogs.AddBookmarkDialog
import diep.space.audiobook.features.bookmarks.dialogs.DeleteBookmarkDialog
import diep.space.audiobook.features.bookmarks.dialogs.EditBookmarkDialog
import diep.space.audiobook.features.bookmarks.list.BookMarkHolder
import diep.space.audiobook.features.bookmarks.list.BookmarkAdapter
import diep.space.audiobook.features.bookmarks.list.BookmarkClickListener
import diep.space.audiobook.injection.App
import diep.space.audiobook.mvp.MvpController
import diep.space.audiobook.uitools.VerticalDividerItemDecoration
import kotlinx.android.synthetic.main.bookmark.*

/**
 * Dialog for creating a bookmark
 */
class BookmarkController(args: Bundle) :
  MvpController<BookmarkView, BookmarkPresenter>(args), BookmarkView,
  BookmarkClickListener, AddBookmarkDialog.Callback, DeleteBookmarkDialog.Callback,
  EditBookmarkDialog.Callback {

  private val bookId = args.getLong(NI_BOOK_ID)
  private val adapter = BookmarkAdapter(this)

  override val layoutRes = R.layout.bookmark
  override fun createPresenter() = App.component.bookmarkPresenter.apply {
    bookId = this@BookmarkController.bookId
  }

  override fun render(bookmarks: List<Bookmark>, chapters: List<Chapter>) {
    adapter.newData(bookmarks, chapters)
  }

  override fun showBookmarkAdded(bookmark: Bookmark) {
    val index = adapter.indexOf(bookmark)
    recycler.smoothScrollToPosition(index)
    Snackbar.make(view!!, R.string.bookmark_added, Snackbar.LENGTH_SHORT)
      .show()
  }

  override fun onDeleteBookmarkConfirmed(id: Long) {
    presenter.deleteBookmark(id)
  }

  override fun onBookmarkClicked(bookmark: Bookmark) {
    presenter.selectBookmark(bookmark.id)
    router.popController(this)
  }

  override fun onEditBookmark(id: Long, title: String) {
    presenter.editBookmark(id, title)
  }

  override fun onBookmarkNameChosen(name: String) {
    presenter.addBookmark(name)
  }

  override fun finish() {
    router.popController(this)
  }

  override fun onViewCreated() {
    setupToolbar()
    setupList()

    addBookmarkFab.setOnClickListener {
      showAddBookmarkDialog()
    }
  }

  override fun onDestroyView() {
    recycler.adapter = null
  }

  private fun setupToolbar() {
    toolbar.setTitle(R.string.bookmark)
    toolbar.setNavigationIcon(R.drawable.close)
    toolbar.setNavigationOnClickListener {
      router.popController(this)
    }
  }

  private fun setupList() {
    val layoutManager = LinearLayoutManager(activity)
    recycler.addItemDecoration(VerticalDividerItemDecoration(activity))
    recycler.layoutManager = layoutManager
    recycler.adapter = adapter
    val itemAnimator = recycler.itemAnimator as DefaultItemAnimator
    itemAnimator.supportsChangeAnimations = false

    val swipeCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
      override fun onMove(
        recyclerView: RecyclerView?,
        viewHolder: RecyclerView.ViewHolder?,
        target: RecyclerView.ViewHolder?
      ): Boolean {
        return false
      }

      override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val boundBookmark = (viewHolder as BookMarkHolder).boundBookmark
        boundBookmark?.let { presenter.deleteBookmark(it.id) }
      }
    }
    ItemTouchHelper(swipeCallback).attachToRecyclerView(recycler)
  }

  override fun onOptionsMenuClicked(bookmark: Bookmark, v: View) {
    val popup = PopupMenu(activity, v)
    popup.menuInflater.inflate(R.menu.bookmark_popup, popup.menu)
    popup.setOnMenuItemClickListener {
      when (it.itemId) {
        R.id.edit -> {
          showEditBookmarkDialog(bookmark)
          true
        }
        R.id.delete -> {
          showDeleteBookmarkDialog(bookmark)
          true
        }
        else -> false
      }
    }
    popup.show()
  }

  private fun showEditBookmarkDialog(bookmark: Bookmark) {
    EditBookmarkDialog(this, bookmark).showDialog(router)
  }

  private fun showAddBookmarkDialog() {
    AddBookmarkDialog(this).showDialog(router)
  }

  private fun showDeleteBookmarkDialog(bookmark: Bookmark) {
    DeleteBookmarkDialog(this, bookmark).showDialog(router)
  }

  companion object {

    private const val NI_BOOK_ID = "ni#bookId"

    fun newInstance(bookId: Long) = BookmarkController(
      Bundle().apply {
        putLong(NI_BOOK_ID, bookId)
      }
    )
  }
}
