package diep.space.audiobook.features.bookOverview

interface BookShelfView {
  fun bookCoverChanged(bookId: Long)
  fun render(state: BookShelfState)
}
