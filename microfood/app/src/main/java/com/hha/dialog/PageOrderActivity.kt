package com.hha.dialog

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.hha.frame.MenuItemViewHolder
import com.hha.frame.MenuPageViewHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tech.hha.microfood.R

class PageOrderActivity(private val activity: AppCompatActivity) {
    private lateinit var dialog: BottomSheetDialog
    private lateinit var orderList: RecyclerView
    private lateinit var menuItemsRecycler: RecyclerView
    private lateinit var menuPagesGrid: RecyclerView
    // Declare other views you need to access
    private lateinit var okButton: Button
    private lateinit var plusOne: Button
    private lateinit var minusOne: Button
    private lateinit var portionButton: Button
    private lateinit var removeItem: Button
    private lateinit var languageButton: Button
    private lateinit var totalPrice: TextView

    @SuppressLint("MissingInflatedId")
    fun show() {
        val view = LayoutInflater.from(activity).inflate(R.layout.page_order_layout, null)
        dialog = BottomSheetDialog(activity).apply {
            setContentView(view)
            window?.setDimAmount(0.5f)
        }

        // Initialize views
        orderList = view.findViewById(R.id.orderList)
        menuItemsRecycler = view.findViewById(R.id.menuItemsRecycler)
        menuPagesGrid = view.findViewById(R.id.menuPagesGrid)
        okButton = view.findViewById(R.id.okButton)
        plusOne = view.findViewById(R.id.plusOne)
        minusOne = view.findViewById(R.id.minusOne)
        portionButton = view.findViewById(R.id.portionButton)
        removeItem = view.findViewById(R.id.removeItem)
        languageButton = view.findViewById(R.id.languageButton)
        totalPrice = view.findViewById(R.id.totalPrice)

        setupUI()
        loadData()
        dialog.show()
    }

    private fun setupUI() {
        // 1. Order List Setup (Left Panel)
        orderList.apply {
            //adapter = OrderAdapter().apply {
                //submitList(getDummyOrders())
            //}
            addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL))
        }

        // 2. Menu Items Grid (Top Right - Horizontally Scrollable)
        menuItemsRecycler.apply {
            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
            adapter = MenuItemAdapter(4, 12) // 4 rows x 12 columns
        }

        // 3. Menu Pages Grid (Bottom Right - Fixed)
        menuPagesGrid.apply {
            layoutManager = GridLayoutManager(activity, 6) // 6 columns
            adapter = MenuPageAdapter(3, 6) // 3 rows x 6 columns
        }

        // 4. Action Buttons
        plusOne.setOnClickListener {

        }
        minusOne.setOnClickListener {

        }
        portionButton.setOnClickListener {

        }
        removeItem.setOnClickListener {

        }
        languageButton.setOnClickListener {

        }
        okButton.setOnClickListener {

        }
    }

    private fun loadData() {
        //viewModelScope.launch {
            try {
                //val orders = repository.getOrders()
                //val menuItems = repository.getMenuItems()
                //val menuPages = repository.getMenuPages()

                //withContext(Dispatchers.Main) {
//                    orderList.adapter?.submitList(orders)
//                    (menuItemsRecycler.adapter as MenuItemAdapter).updateData(menuItems)
//                    (menuPagesGrid.adapter as MenuPageAdapter).updateData(menuPages)
//                    totalPrice.text = calculateTotal(orders)
                //}
            } catch (e: Exception) {
                Toast.makeText(activity, "Error loading data", Toast.LENGTH_SHORT).show()
            }
    }

   private inner class MenuItemAdapter(
       rows: Int, cols: Int) : RecyclerView.Adapter<MenuItemViewHolder>() {
        // Implementation...
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int,
        ): MenuItemViewHolder {
            TODO("Not yet implemented")
        }

        override fun onBindViewHolder(holder: MenuItemViewHolder, position: Int) {
            TODO("Not yet implemented")
        }

        override fun getItemCount(): Int {
            TODO("Not yet implemented")
        }
    }

    private inner class MenuPageAdapter(rows: Int, cols: Int) : RecyclerView.Adapter<MenuPageViewHolder>() {
        // Implementation...
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int,
        ): MenuPageViewHolder {
            TODO("Not yet implemented")
        }

        override fun onBindViewHolder(
            holder: MenuPageViewHolder,
            position: Int,
        ) {
            TODO("Not yet implemented")
        }

        override fun getItemCount(): Int {
            TODO("Not yet implemented")
        }
    }
}
