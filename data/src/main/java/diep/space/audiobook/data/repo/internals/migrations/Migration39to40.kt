package diep.space.audiobook.data.repo.internals.migrations

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import diep.space.audiobook.data.repo.internals.update

/**
 * From DB version 39, the position of a book must no longer be negative. So all negative positions
 * get set to 0.
 */
class Migration39to40 : Migration {

  private val BOOK_TABLE_NAME = "tableBooks"
  private val BOOK_TIME = "bookTime"

  override fun migrate(db: SQLiteDatabase) {
    val positionZeroContentValues = ContentValues().apply {
      put(BOOK_TIME, 0)
    }
    db.update(BOOK_TABLE_NAME, positionZeroContentValues, "$BOOK_TIME < ?", 0)
  }
}
