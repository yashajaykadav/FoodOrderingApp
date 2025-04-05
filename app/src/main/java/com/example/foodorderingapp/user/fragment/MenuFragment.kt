package com.example.foodorderingapp.user.fragment

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
import androidx.recyclerview.widget.LinearLayoutManager
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

    private var selectedCategory: String? = null  // ✅ Store selected category

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.user_fragment_menu, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        categoryContainer = view.findViewById(R.id.categoryContainer)  // ✅ Get category container

        foodAdapter = FoodAdapter(mutableListOf()) { foodItem ->
            CartManager.addToCart(foodItem)
            Toast.makeText(requireContext(), "${foodItem.name} added to cart!", Toast.LENGTH_SHORT).show()
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = foodAdapter

        // ✅ Observe food items
        foodViewModel.foodItems.observe(viewLifecycleOwner) { foodList ->
            foodAdapter.updateList(foodList)
        }

        // ✅ Observe categories and create buttons dynamically
        foodViewModel.categories.observe(viewLifecycleOwner) { categories ->
            categoryContainer.removeAllViews()
            categories.forEach { category ->
                val categoryButton = createCategoryButton(category)
                categoryContainer.addView(categoryButton)
            }
        }

        foodViewModel.fetchFoodItems() // ✅ Fetch food data
        foodViewModel.fetchCategories() // ✅ Fetch categories

        return view
    }

    // ✅ Function to create category buttons dynamically
    private fun createCategoryButton(category: String): Button {
        val button = Button(requireContext()).apply {
            text = category
            textSize = 14f
            setPadding(20, 10, 20, 10)

            // ✅ Set background dynamically based on selection
            background = ContextCompat.getDrawable(
                requireContext(),
                if (category == selectedCategory) R.drawable.category_selected_bg else R.drawable.category_unselected_bg
            )

            setOnClickListener {
                selectedCategory = category
                filterFoodByCategory(category)
            }
        }
        return button
    }

    // ✅ Function to filter food items based on category
    private fun filterFoodByCategory(category: String) {
        foodViewModel.foodItems.value?.let { allFoodItems ->
            val filteredList = allFoodItems.filter { it.category == category }
            foodAdapter.updateList(filteredList)
        }
    }
}
