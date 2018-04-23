package diep.space.audiobook.features.bookOverview.list

import android.support.v7.util.DiffUtil
import diep.space.audiobook.data.Book

class BookShelfDiffCallback : DiffUtil.ItemCallback<Book>() {

  override fun areContentsTheSame(oldItem: Book, newItem: Book): Boolean {
    return oldItem.id == newItem.id
        && oldItem.content.position == newItem.content.position
        && oldItem.name == newItem.name
  }

  override fun areItemsTheSame(oldItem: Book, newItem: Book): Boolean {
    return oldItem.id == newItem.id
  }
}
