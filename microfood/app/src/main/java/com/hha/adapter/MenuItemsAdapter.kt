import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hha.adapter.MenuItemViewHolder
import com.hha.framework.CMenuItem
import com.hha.framework.CMenuItems
import com.hha.resources.Global

import tech.hha.microfood.databinding.AdapterMenuItemBinding

class MenuItemsAdapter(
   private var menuItems: CMenuItems,
   private var itemWidth: Int,
   private val onItemSelected: (CMenuItem) -> Unit
) : RecyclerView.Adapter<MenuItemViewHolder>() {

   private val global = Global.getInstance()

   override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuItemViewHolder {
      val binding = AdapterMenuItemBinding.inflate(
         LayoutInflater.from(parent.context),
         parent,
         false
      )
      return MenuItemViewHolder(binding, onItemSelected, global.isChinese())
   }

   override fun onBindViewHolder(holder: MenuItemViewHolder, position: Int) {
      setupItemWidth(holder.itemView)
      val item = menuItems.findItemByPosition(position)
      holder.bind(item)
   }

   private fun setupItemWidth(itemView: View) {
      val displayWidth = itemView.context.resources.displayMetrics.widthPixels
      val calculatedWidth = displayWidth * itemWidth / 120
      itemView.layoutParams.width = calculatedWidth
   }

   fun setNewItems(newItems: CMenuItems) {
      menuItems = newItems
      notifyDataSetChanged()
   }

   override fun getItemCount() = menuItems.getItemCount()
}