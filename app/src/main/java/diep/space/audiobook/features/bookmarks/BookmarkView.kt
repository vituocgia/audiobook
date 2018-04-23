package diep.space.audiobook.features.bookmarks

import diep.space.audiobook.data.Bookmark
import diep.space.audiobook.data.Chapter

/**
 * View of the bookmarks
 */
interface BookmarkView {

  fun render(bookmarks: List<Bookmark>, chapters: List<Chapter>)
  fun showBookmarkAdded(bookmark: Bookmark)
  fun finish()
}
