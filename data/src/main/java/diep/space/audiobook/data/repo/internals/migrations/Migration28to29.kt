package diep.space.audiobook.data.repo.internals.migrations

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import org.json.JSONObject
import timber.log.Timber
import java.io.File

class Migration28to29 : Migration {

  override fun migrate(db: SQLiteDatabase) {
    db.query("TABLE_BOOK", arrayOf("BOOK_JSON", "BOOK_ID"), null, null, null, null, null)
      .use { cursor ->
        while (cursor.moveToNext()) {
          val book = JSONObject(cursor.getString(0))
          val chapters = book.getJSONArray("chapters")
          for (i in 0 until chapters.length()) {
            val chapter = chapters.getJSONObject(i)
            val fileName = File(chapter.getString("path")).name
            val dotIndex = fileName.lastIndexOf(".")
            val chapterName = if (dotIndex > 0) {
              fileName.substring(0, dotIndex)
            } else {
              fileName
            }
            chapter.put("name", chapterName)
          }
          val cv = ContentValues()
          Timber.d("so saving book=$book")
          cv.put("BOOK_JSON", book.toString())
          db.update("TABLE_BOOK", cv, "BOOK_ID" + "=?", arrayOf(cursor.getLong(1).toString()))
        }
      }
  }
}
