package com.example.foodorderingapp.user.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
    private val foodViewModel: FoodViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.user_fragment_menu, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        foodAdapter = FoodAdapter(mutableListOf()) { foodItem ->
            CartManager.addToCart(foodItem)
            Toast.makeText(requireContext(), "${foodItem.name} added to cart!", Toast.LENGTH_SHORT).show()
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = foodAdapter

        // ✅ Observe LiveData from ViewModel
        foodViewModel.foodItems.observe(viewLifecycleOwner) { foodList ->
            foodAdapter.updateList(foodList)
        }

        foodViewModel.fetchFoodItems() // ✅ Fetch data from Firebase

        return view
    }
}
