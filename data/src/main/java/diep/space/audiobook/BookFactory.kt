package diep.space.audiobook

import diep.space.audiobook.data.Book
import diep.space.audiobook.data.BookContent
import diep.space.audiobook.data.Chapter

object BookFactory {

  fun create(
    id: Long = -1,
    type: Book.Type = Book.Type.SINGLE_FOLDER,
    author: String = "TestAuthor",
    currentFileIndex: Int = 0,
    time: Int = 0,
    name: String = "TestBook",
    playbackSpeed: Float = 1F,
    loudnessGain: Int = 500,
    chapters: List<Chapter> = listOf(ChapterFactory.create())
  ): Book {

    val currentFile = chapters[currentFileIndex].file
    val root = if (currentFile.parent != null) currentFile.parent else "fakeRoot"

    return Book(
      id = id,
      type = type,
      author = author,
      content = BookContent(
        id = id,
        currentFile = currentFile,
        positionInChapter = time,
        chapters = chapters,
        playbackSpeed = playbackSpeed,
        loudnessGain = loudnessGain
      ),
      name = name,
      root = root
    )
  }
}
