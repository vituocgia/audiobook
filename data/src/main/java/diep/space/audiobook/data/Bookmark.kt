package diep.space.audiobook.data

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import diep.space.audiobook.common.comparator.NaturalOrderComparator
import java.io.File

/**
 * Represents a bookmark in the book.
 */
@Entity(tableName = "bookmark")
data class Bookmark(
  @ColumnInfo(name = "file")
  val mediaFile: File,
  @ColumnInfo(name = "title")
  val title: String,
  @ColumnInfo(name = "time")
  val time: Int,
  @ColumnInfo(name = "id")
  @PrimaryKey(autoGenerate = true)
  val id: Long = ID_UNKNOWN
) : Comparable<Bookmark> {

  init {
    require(title.isNotEmpty())
  }

  override fun compareTo(other: Bookmark): Int {
    // compare files
    val fileCompare = NaturalOrderComparator.fileComparator.compare(mediaFile, other.mediaFile)
    if (fileCompare != 0) {
      return fileCompare
    }

    // if files are the same compare time
    val timeCompare = time.compareTo(other.time)
    if (timeCompare != 0) return timeCompare

    // if time is the same compare the titles
    val titleCompare = NaturalOrderComparator.stringComparator.compare(title, other.title)
    if (titleCompare != 0) return titleCompare

    return id.compareTo(other.id)
  }

  companion object {
    const val ID_UNKNOWN = 0L
  }
}
