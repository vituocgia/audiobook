package diep.space.audiobook.data.repo.internals

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.support.annotation.VisibleForTesting
import diep.space.audiobook.data.repo.internals.tables.BookTable
import diep.space.audiobook.data.repo.internals.tables.ChapterTable
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper class that manages the underlying the database.
 */
@Singleton
class InternalDb
@Inject constructor(
  private val context: Context
) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

  override fun onCreate(db: SQLiteDatabase) {
    BookTable.onCreate(db)
    ChapterTable.onCreate(db)
  }

  override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    val migrator = DataBaseMigrator(db, context)
    migrator.upgrade(oldVersion, newVersion)
  }

  companion object {

    private const val DATABASE_VERSION = 44
    @VisibleForTesting
    const val DATABASE_NAME = "autoBookDB"
  }
}
