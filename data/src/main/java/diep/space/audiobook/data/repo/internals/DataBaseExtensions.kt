package diep.space.audiobook.data.repo.internals

import android.annotation.SuppressLint
import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import java.util.ArrayList


inline fun Cursor.moveToNextLoop(func: Cursor.() -> Unit) = use {
  while (moveToNext()) {
    func()
  }
}

/** a function that iterates of the rows of a cursor and maps all using a supplied mapper function */
inline fun <T> Cursor.mapRows(mapper: Cursor.() -> T): List<T> = use {
  val list = ArrayList<T>(count)
  while (moveToNext()) {
    list.add(mapper())
  }
  list
}

@SuppressLint("Recycle")
fun SQLiteDatabase.query(
  table: String,
  columns: List<String>? = null,
  selection: String? = null,
  selectionArgs: List<Any>? = null,
  groupBy: String? = null,
  having: String? = null,
  orderBy: String? = null,
  limit: String? = null,
  distinct: Boolean = false
): Cursor {
  val argsAsString = selectionArgs?.map(Any::toString)?.toTypedArray()
  return query(
    distinct,
    table,
    columns?.toTypedArray(),
    selection,
    argsAsString,
    groupBy,
    having,
    orderBy,
    limit
  )
}

fun SQLiteDatabase.update(
  table: String,
  values: ContentValues,
  whereClause: String,
  vararg whereArgs: Any
): Int {
  val whereArgsMapped = whereArgs.map(Any::toString).toTypedArray()
  return update(table, values, whereClause, whereArgsMapped)
}

fun SQLiteDatabase.delete(table: String, whereClause: String, vararg whereArgs: Any): Int {
  val whereArgsMapped = whereArgs.map(Any::toString).toTypedArray()
  return delete(table, whereClause, whereArgsMapped)
}
