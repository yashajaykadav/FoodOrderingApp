package com.example.foodorderingapp.user.fragment

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodorderingapp.R
import com.example.foodorderingapp.user.adapter.FoodAdapter
import com.example.foodorderingapp.user.manager.CartManager
import com.example.foodorderingapp.user.viewmodel.FoodViewModel

class MenuFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var foodAdapter: FoodAdapter
    private lateinit var categoryContainer: LinearLayout
    private val foodViewModel: FoodViewModel by viewModels()

    private var selectedCategory: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.user_fragment_menu, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        categoryContainer = view.findViewById(R.id.categoryContainer)

        foodAdapter = FoodAdapter(mutableListOf()) { foodItem ->
            CartManager.addToCart(foodItem)
            Toast.makeText(requireContext(), "${foodItem.name} added to cart!", Toast.LENGTH_SHORT).show()
        }

        // ✅ Use GridLayoutManager for 2-column layout
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.adapter = foodAdapter
        recyclerView.addItemDecoration(GridSpacingItemDecoration(2, 32, true))

        // Observe food items
        foodViewModel.foodItems.observe(viewLifecycleOwner) { foodList ->
            foodAdapter.updateList(foodList)
        }

        // Observe categories
        foodViewModel.categories.observe(viewLifecycleOwner) { categories ->
            categoryContainer.removeAllViews()
            selectedCategory = null // Clear selected on category reload

            val allButton = createCategoryButton("All")
            categoryContainer.addView(allButton)

            categories.forEach { category ->
                val button = createCategoryButton(category)
                categoryContainer.addView(button)
            }
        }

        foodViewModel.fetchFoodItems()
        foodViewModel.fetchCategories()

        return view
    }

    // ✅ Create styled category button
    private fun createCategoryButton(category: String): Button {
        val button = Button(requireContext()).apply {
            text = category
            textSize = 14f
            setPadding(32, 16, 32, 16)
            setBackgroundResource(
                if (category == selectedCategory) R.drawable.category_selected_bg
                else R.drawable.category_unselected_bg
            )
            setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    if (category == selectedCategory) R.color.white else R.color.colorPrimary
                )
            )

            setOnClickListener {
                selectedCategory = if (category == "All") null else category
                highlightSelectedCategory(category)
                filterFoodByCategory(selectedCategory)
            }
        }

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.marginEnd = 16
        button.layoutParams = layoutParams
        return button
    }

    // ✅ Filter logic
    private fun filterFoodByCategory(category: String?) {
        val allItems = foodViewModel.foodItems.value ?: return
        val filtered = if (category == null) allItems else allItems.filter { it.category == category }
        foodAdapter.updateList(filtered)
    }

    // ✅ Highlight active button
    private fun highlightSelectedCategory(category: String) {
        for (i in 0 until categoryContainer.childCount) {
            val button = categoryContainer.getChildAt(i) as Button
            val isSelected = button.text == category
            button.setBackgroundResource(
                if (isSelected) R.drawable.category_selected_bg
                else R.drawable.category_unselected_bg
            )
            button.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    if (isSelected) R.color.white else R.color.colorPrimary
                )
            )
        }
    }

    // ✅ Grid spacing decoration
    inner class GridSpacingItemDecoration(
        private val spanCount: Int,
        private val spacing: Int,
        private val includeEdge: Boolean
    ) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
        ) {
            val position = parent.getChildAdapterPosition(view)
            val column = position % spanCount

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount
                outRect.right = (column + 1) * spacing / spanCount
                if (position < spanCount) outRect.top = spacing
                outRect.bottom = spacing
            } else {
                outRect.left = column * spacing / spanCount
                outRect.right = spacing - (column + 1) * spacing / spanCount
                if (position >= spanCount) outRect.top = spacing
            }
        }
    }
}
