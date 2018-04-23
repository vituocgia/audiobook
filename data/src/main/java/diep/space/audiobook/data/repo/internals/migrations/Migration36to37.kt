package diep.space.audiobook.data.repo.internals.migrations

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.database.getLong
import androidx.database.getString
import androidx.database.sqlite.transaction
import diep.space.audiobook.data.repo.internals.mapRows
import diep.space.audiobook.data.repo.internals.query

/**
 * The field LAST_MODIFIED was added to the chapters
 */
class Migration36to37 : Migration {

  private val TABLE_NAME = "tableChapters"
  private val DURATION = "chapterDuration"
  private val NAME = "chapterName"
  private val PATH = "chapterPath"
  private val BOOK_ID = "bookId"
  private val LAST_MODIFIED = "lastModified"
  private val CREATE_TABLE = """
      CREATE TABLE $TABLE_NAME (
        $DURATION INTEGER NOT NULL,
        $NAME TEXT NOT NULL,
        $PATH TEXT NOT NULL,
        $BOOK_ID INTEGER NOT NULL,
        $LAST_MODIFIED INTEGER NOT NULL,
        FOREIGN KEY ($BOOK_ID) REFERENCES tableBooks (bookId)
      )
  """

  override fun migrate(db: SQLiteDatabase) {
    val data = db.query(TABLE_NAME).mapRows {
      Holder(getLong(DURATION), getString(NAME), getString(PATH), getLong(BOOK_ID))
    }

    db.transaction {
      db.execSQL("DROP TABLE $TABLE_NAME")
      db.execSQL(CREATE_TABLE)
      data.forEach {
        val cv = ContentValues().apply {
          put(DURATION, it.duration)
          put(NAME, it.name)
          put(BOOK_ID, it.bookId)
          put(PATH, it.path)
          put(LAST_MODIFIED, 0L)
        }
        db.insert(TABLE_NAME, null, cv)
      }
    }
  }

  data class Holder(val duration: Long, val name: String, val path: String, val bookId: Long)
}
