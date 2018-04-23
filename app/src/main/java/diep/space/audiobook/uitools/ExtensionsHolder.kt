package diep.space.audiobook.uitools

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import diep.space.audiobook.misc.layoutInflater
import kotlinx.android.extensions.LayoutContainer

open class ExtensionsHolder(itemView: View) : RecyclerView.ViewHolder(itemView), LayoutContainer {

  constructor(parent: ViewGroup, layoutRes: Int) : this(
    parent.layoutInflater().inflate(
      layoutRes,
      parent,
      false
    )
  )

  final override val containerView: View? get() = itemView
}
