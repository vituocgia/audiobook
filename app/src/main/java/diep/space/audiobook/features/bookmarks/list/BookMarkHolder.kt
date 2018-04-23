package diep.space.audiobook.features.bookmarks.list

import android.view.ViewGroup
import diep.space.audiobook.R
import diep.space.audiobook.data.Bookmark
import diep.space.audiobook.data.Chapter
import diep.space.audiobook.uitools.ExtensionsHolder
import kotlinx.android.synthetic.main.bookmark_row_layout.*
import java.util.concurrent.TimeUnit.MILLISECONDS

/**
 * ViewHolder for displaying a Bookmark
 */
class BookMarkHolder(
  parent: ViewGroup,
  private val listener: BookmarkClickListener
) : ExtensionsHolder(parent, R.layout.bookmark_row_layout) {

  var boundBookmark: Bookmark? = null
    private set

  init {
    edit.setOnClickListener { view ->
      boundBookmark?.let {
        listener.onOptionsMenuClicked(it, view)
      }
    }
    itemView.setOnClickListener {
      boundBookmark?.let {
        listener.onBookmarkClicked(it)
      }
    }
  }

  fun bind(bookmark: Bookmark, chapters: List<Chapter>) {
    boundBookmark = bookmark
    title.text = bookmark.title

    val size = chapters.size
    val currentChapter = chapters.single { it.file == bookmark.mediaFile }
    val index = chapters.indexOf(currentChapter)

    summary.text = itemView.context.getString(
      diep.space.audiobook.R.string.format_bookmarks_n_of,
      index + 1,
      size
    )
    time.text = itemView.context.getString(
      diep.space.audiobook.R.string.format_bookmarks_time, formatTime(bookmark.time),
      formatTime(currentChapter.duration)
    )
  }

  private fun formatTime(ms: Int): String {
    val h = MILLISECONDS.toHours(ms.toLong()).toString()
    val m = "%02d".format((MILLISECONDS.toMinutes(ms.toLong()) % 60))
    val s = "%02d".format((MILLISECONDS.toSeconds(ms.toLong()) % 60))
    var returnString = ""
    if (h != "0") {
      returnString += "$h:"
    }
    return "$returnString$m:$s"
  }
}
