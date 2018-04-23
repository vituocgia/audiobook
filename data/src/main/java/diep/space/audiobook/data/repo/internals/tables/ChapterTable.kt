package diep.space.audiobook.data.repo.internals.tables

import android.database.sqlite.SQLiteDatabase

/**
 * Collection of strings representing the chapters table
 */
object ChapterTable {

  const val DURATION = "chapterDuration"
  const val NAME = "chapterName"
  const val PATH = "chapterPath"
  const val TABLE_NAME = "tableChapters"
  const val BOOK_ID = "bookId"
  const val LAST_MODIFIED = "lastModified"
  const val MARKS = "marks"
  private const val CREATE_TABLE = """
    CREATE TABLE ${TABLE_NAME} (
      ${DURATION} INTEGER NOT NULL,
      ${NAME} TEXT NOT NULL,
      ${PATH} TEXT NOT NULL,
      ${BOOK_ID} INTEGER NOT NULL,
      ${LAST_MODIFIED} INTEGER NOT NULL,
      ${MARKS} TEXT,
      FOREIGN KEY (${BOOK_ID}) REFERENCES ${BookTable.TABLE_NAME} (${BookTable.ID})
    )
  """

  fun onCreate(db: SQLiteDatabase) {
    db.execSQL(CREATE_TABLE)
  }
}
