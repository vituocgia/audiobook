package diep.space.audiobook.features.bookmarks.list

import diep.space.audiobook.data.Bookmark

interface BookmarkClickListener {

  fun onOptionsMenuClicked(bookmark: Bookmark, v: android.view.View)
  fun onBookmarkClicked(bookmark: Bookmark)
}
