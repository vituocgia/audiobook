package diep.space.audiobook.features.bookmarks

import diep.space.audiobook.data.Bookmark
import diep.space.audiobook.data.Chapter
import diep.space.audiobook.data.repo.BookRepository
import diep.space.audiobook.data.repo.BookmarkRepo
import diep.space.audiobook.injection.PrefKeys
import diep.space.audiobook.mvp.Presenter
import diep.space.audiobook.persistence.pref.Pref
import diep.space.audiobook.playback.PlayStateManager
import diep.space.audiobook.playback.PlayerController
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import javax.inject.Inject
import javax.inject.Named

/**
 * Presenter for the bookmark MVP
 */
class BookmarkPresenter @Inject constructor(
  @Named(PrefKeys.CURRENT_BOOK)
  private val currentBookIdPref: Pref<Long>,
  private val repo: BookRepository,
  private val bookmarkRepo: BookmarkRepo,
  private val playStateManager: PlayStateManager,
  private val playerController: PlayerController
) : Presenter<BookmarkView>() {

  var bookId = -1L
  private val bookmarks = ArrayList<Bookmark>()
  private val chapters = ArrayList<Chapter>()

  override fun onAttach(view: BookmarkView) {
    check(bookId != -1L) { "You must initialize the bookId" }

    val book = repo.bookById(bookId) ?: return

    launch(UI) {
      bookmarks.clear()
      bookmarks.addAll(bookmarkRepo.bookmarks(book))
      chapters.clear()
      chapters.addAll(book.content.chapters)

      if (attached) renderView()
    }
  }

  fun deleteBookmark(id: Long) {
    launch(UI) {
      bookmarkRepo.deleteBookmark(id)
      bookmarks.removeAll { it.id == id }

      renderView()
    }
  }

  fun selectBookmark(id: Long) {
    val bookmark = bookmarks.find { it.id == id }
        ?: return

    val wasPlaying = playStateManager.playState == PlayStateManager.PlayState.PLAYING

    currentBookIdPref.value = bookId
    playerController.changePosition(bookmark.time, bookmark.mediaFile)

    if (wasPlaying) {
      playerController.play()
    }

    view.finish()
  }

  fun editBookmark(id: Long, newTitle: String) {
    launch(UI) {
      bookmarks.find { it.id == id }?.let {
        val withNewTitle = it.copy(
          title = newTitle,
          id = Bookmark.ID_UNKNOWN
        )
        bookmarkRepo.deleteBookmark(it.id)
        val newBookmark = bookmarkRepo.addBookmark(withNewTitle)
        val index = bookmarks.indexOfFirst { it.id == id }
        bookmarks[index] = newBookmark
        if (attached) renderView()
      }
    }
  }

  fun addBookmark(name: String) {
    launch(UI) {
      val book = repo.bookById(bookId) ?: return@launch
      val title = if (name.isEmpty()) book.content.currentChapter.name else name
      val addedBookmark = bookmarkRepo.addBookmarkAtBookPosition(book, title)
      bookmarks.add(addedBookmark)
      if (attached) renderView()
    }
  }

  private fun renderView() {
    view.render(bookmarks, chapters)
  }
}
