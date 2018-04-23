package diep.space.audiobook.playback.utils

import android.graphics.Bitmap
import diep.space.audiobook.data.Book

/**
 * A cache entry for a bitmap
 */
data class CachedImage(val bookId: Long, val cover: Bitmap) {

  fun matches(book: Book) = book.id == bookId
}
