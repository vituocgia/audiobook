package diep.space.audiobook.features.bookOverview

import diep.space.audiobook.data.Book

sealed class BookShelfState {

  data class Content(
    val books: List<Book>,
    val currentBook: Book?,
    val playing: Boolean
  ) : BookShelfState()

  object Loading : BookShelfState()

  object NoFolderSet : BookShelfState()
}
