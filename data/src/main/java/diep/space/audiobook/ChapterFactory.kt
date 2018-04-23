package diep.space.audiobook

import android.support.v4.util.SparseArrayCompat
import diep.space.audiobook.common.sparseArray.emptySparseArray
import diep.space.audiobook.data.Chapter
import java.io.File

object ChapterFactory {

  fun create(
    file: String = "First.mp3",
    parent: String = "/root/",
    duration: Int = 100,
    lastModified: Long = 12345L,
    marks: SparseArrayCompat<String> = emptySparseArray()
  ) = Chapter(
    file = File(parent, file),
    name = file,
    duration = duration,
    fileLastModified = lastModified,
    marks = marks
  )
}
