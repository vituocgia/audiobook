package diep.space.audiobook.data.repo

import diep.space.audiobook.data.Book
import diep.space.audiobook.data.Bookmark
import diep.space.audiobook.data.repo.internals.BookmarkDao
import diep.space.audiobook.data.repo.internals.IO
import kotlinx.coroutines.experimental.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Provides access to bookmarks.
 */
@Singleton
class BookmarkRepo
@Inject constructor(
  private val dao: BookmarkDao
) {

  suspend fun deleteBookmark(id: Long) {
    withContext(IO) {
      dao.deleteBookmark(id)
    }
  }

  suspend fun addBookmark(bookmark: Bookmark): Bookmark {
    return withContext(IO) {
      val insertedId = dao.addBookmark(bookmark)
      bookmark.copy(id = insertedId)
    }
  }

  suspend fun addBookmarkAtBookPosition(book: Book, title: String): Bookmark {
    return withContext(IO) {
      val addedBookmark =
        Bookmark(book.content.currentChapter.file, title, book.content.positionInChapter)
      Timber.v("Added bookmark=$addedBookmark")
      addBookmark(addedBookmark)
    }
  }

  suspend fun bookmarks(book: Book): List<Bookmark> {
    return withContext(IO) {
      val files = book.content.chapters.map {
        it.file
      }
      dao.allForFiles(files)
    }
  }
}
