package com.hha.dialog

import android.graphics.Rect
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tech.hha.microfood.databinding.PageOrderActivityBinding
import androidx.recyclerview.widget.RecyclerView

import com.hha.adapter.MenuPagesAdapter
import com.hha.framework.CMenuCards
import com.hha.framework.CMenuPage
import com.hha.resources.Global


class PageOrderActivity : AppCompatActivity() {
    private lateinit var binding: PageOrderActivityBinding
    val global = Global.getInstance()
    val menuCardId = global.menuCardId
    val menuCard = CMenuCards.getInstance().getMenuCard(menuCardId)
    val menuPages = menuCard.getOrderedPages()
    val totalColumns = (menuPages.size / 3) // 24 items / 3 rows = 8 columns
    private lateinit var pagesAdapter: MenuPagesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PageOrderActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        // GridLayoutManager with 3 rows (vertical span) and horizontal scrolling
        val gridLayoutManager = GridLayoutManager(
            this@PageOrderActivity,
            3, // Span count = number of rows (vertical)
            GridLayoutManager.HORIZONTAL, // Horizontal scrolling
            false
        )
        binding.layoutPages.layoutManager = gridLayoutManager

        // 2. Initialize adapter
        pagesAdapter = MenuPagesAdapter(menuPages) { selectedPage ->
            handlePageSelection(selectedPage)
        }.apply {
            // Set dynamic height based on screen size
            binding.layoutPages.setItemViewCacheSize(18)
            //itemHeight = (resources.displayMetrics.heightPixels * 0.15).toInt()
        }
        binding.layoutPages.adapter = pagesAdapter

        // 3. Add spacing between items
        binding.layoutPages.addItemDecoration(
            object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(outRect: Rect, view: View,
                                            parent: RecyclerView, state: RecyclerView.State) {
                    outRect.set(8.dpToPx(), 8.dpToPx(), 8.dpToPx(), 8.dpToPx()) // 8dp spacing
                }
                }
        )
    }

    // DP-to-pixel conversion extension
    fun Int.dpToPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

    private fun handlePageSelection(selectedPage: CMenuPage) {
        // Update selection state
        menuPages.forEach { page ->
            page.isSelected = (page.menuPageId == selectedPage.menuPageId)
        }
        pagesAdapter.notifyDataSetChanged()

        // Load items for the selected page
        loadPageItems(selectedPage.menuPageId)
    }

    private fun loadPageItems(pageId: Int) {
        // Implement your logic to load items for the selected page
        // This might involve another RecyclerView for the items in layout_items
    }

    private fun navigateBack() {
        // Intent to start MainActivity which will host your MainMenuDialog
        //val mainIntent = Intent(this@AboutActivity, MainMenuActivity::class.java)
        // Optional: Pass any fetched data to MainActivity
        // mainIntent.putExtra("MENU_DATA", "your_fetched_menu_data_string_or_parcelable")
        //startActivity(mainIntent)
        finish() // Close the SplashActivity so it's not in the back stack
    }
}
