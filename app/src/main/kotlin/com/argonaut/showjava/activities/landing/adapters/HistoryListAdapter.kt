

package com.argonaut.showjava.activities.landing.adapters

import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.argonaut.showjava.R
import com.argonaut.showjava.data.SourceInfo
import kotlinx.android.synthetic.main.layout_app_list_item.view.*
import java.io.File

class HistoryListAdapter(
    private var historyItems: List<SourceInfo>,
    private val itemClick: (SourceInfo) -> Unit
) : androidx.recyclerview.widget.RecyclerView.Adapter<HistoryListAdapter.ViewHolder>() {

    class ViewHolder(view: View, private val itemClick: (SourceInfo) -> Unit) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {

        fun bindSourceInfo(sourceInfo: SourceInfo) {
            with(sourceInfo) {
                itemView.itemLabel.text = sourceInfo.packageLabel
                itemView.itemSecondaryLabel.text = sourceInfo.packageName
                val iconPath =
                    "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)}/ShowJavaPro/sources/${sourceInfo.packageName}/icon.png"
                if (File(iconPath).exists()) {
                    val iconBitmap = BitmapFactory.decodeFile(iconPath)
                    itemView.itemIcon.setImageDrawable(
                        BitmapDrawable(
                            itemView.context.resources,
                            iconBitmap
                        )
                    )
                } else {
                    itemView.itemIcon.setImageResource(R.drawable.ic_list_generic)
                }
                itemView.itemCard.cardElevation = 1F
                itemView.itemCard.setOnClickListener { itemClick(this) }
            }
        }
    }

    fun updateData(historyItems: List<SourceInfo>) {
        this.historyItems = historyItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_app_list_item, parent, false)
        return ViewHolder(view, itemClick)
    }

    override fun onBindViewHolder(holder: HistoryListAdapter.ViewHolder, position: Int) {
        holder.bindSourceInfo(historyItems[position])
    }

    override fun getItemCount(): Int {
        return historyItems.size
    }
}
