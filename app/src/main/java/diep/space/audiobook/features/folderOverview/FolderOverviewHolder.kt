package diep.space.audiobook.features.folderOverview

import android.view.ViewGroup
import diep.space.audiobook.R
import diep.space.audiobook.misc.drawable
import diep.space.audiobook.uitools.ExtensionsHolder
import kotlinx.android.synthetic.main.activity_folder_overview_row_layout.*

class FolderOverviewHolder(
  parent: ViewGroup,
  itemClicked: (position: Int) -> Unit
) : ExtensionsHolder(parent, R.layout.activity_folder_overview_row_layout) {

  init {
    remove.setOnClickListener {
      if (adapterPosition != -1) {
        itemClicked(adapterPosition)
      }
    }
  }

  fun bind(model: FolderModel) {
    // set text
    textView.text = model.folder

    // set correct image
    val drawableId = if (model.isCollection) R.drawable.folder_multiple else R.drawable.ic_folder
    val drawable = itemView.context.drawable(drawableId)
    icon.setImageDrawable(drawable)

    // set content description
    val contentDescriptionId =
      if (model.isCollection) R.string.folder_add_collection else R.string.folder_add_single_book
    val contentDescription = itemView.context.getString(contentDescriptionId)
    icon.contentDescription = contentDescription
  }
}
