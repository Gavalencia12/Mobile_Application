package com.example.carhive.presentation.seller.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.carhive.data.model.UserEntity
import com.example.carhive.data.model.RatingSellerEntity
import com.example.carhive.presentation.seller.adapter.CommentsAdapter
import com.example.carhive.presentation.seller.viewModel.SellerHomeViewModel
import com.example.carhive.R
import com.example.carhive.databinding.FragmentSellerHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SellerHomeFragment : Fragment() {

    // Binding for the fragment's layout (auto-generated class from the layout file)
    private var _binding: FragmentSellerHomeBinding? = null
    private val binding get() = _binding!!

    // ViewModel to manage seller-related data using Hilt for dependency injection
    private val viewModel: SellerHomeViewModel by viewModels()

    private val sellerId: String? by lazy { FirebaseAuth.getInstance().currentUser?.uid }
    private val commentsList = mutableListOf<RatingSellerEntity>()

    // Inflate the fragment's view and bind it to the FragmentSellerHomeBinding object
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSellerHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    // This method is called after the view has been created. It handles UI setup and observers.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView() // Configura el RecyclerView

        // Set up the BottomNavigationView with the NavController for navigation
        val navController = findNavController()
        NavigationUI.setupWithNavController(binding.bottomNavigationSeller, navController)

        // Observe user data and update UI when the data is available
        viewModel.userData.observe(viewLifecycleOwner) { result ->
            result.onSuccess { user ->
                if (user != null) {
                    // Set the user's full name in the TextView
                    GetUsername(user)
                }
            }.onFailure {
                // Show a toast message if there is an error retrieving user data
                Toast.makeText(requireContext(), "Error fetching user data", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.unapprovedCarsCount.observe(viewLifecycleOwner) { count ->
            binding.tvApprovedCarsCount.text = count.toString()
        }


        // Observe the total number of cars and update the TextView accordingly
        viewModel.totalCarsCount.observe(viewLifecycleOwner) { count ->
            binding.tvAllCarsCount.text = count.toString()
        }

        // Observe the number of sold cars and update the TextView accordingly
        viewModel.soldCarsCount.observe(viewLifecycleOwner) { count ->
            binding.tvSoldCarsCount.text = count.toString()
        }

        // Observe the number of unsold cars and update the TextView accordingly
        viewModel.unsoldCarsCount.observe(viewLifecycleOwner) { count ->
            binding.tvUnsoldCarsCount.text = count.toString()
        }
        // Observe the number of favorite cars and update the TextView accordingly
        viewModel.favoriteReactionsCount.observe(viewLifecycleOwner) { count ->
            binding.tvFavoriteCarsCount.text = count.toString() // Actualiza el TextView con el contador
        }

        // Call the function to fetch favorite reactions for user cars
        viewModel.fetchFavoriteReactionsForUserCars()


        // Fetch user data and car data when the view is created
        viewModel.fetchUserData()
        viewModel.fetchCarsForUser()

        // Set up click listeners for the car count cards
        // When "All Cars" card is clicked, navigate to the cars list showing all cars
        binding.cardAllCars.setOnClickListener {
            findNavController().navigate(
                R.id.carsListFragment,
                CarsListFragment.newInstance(-1, "All Cars").arguments // Show all cars
            )
        }

        // When "Sold Cars" card is clicked, navigate to the cars list showing only sold cars
        binding.cardSoldCars.setOnClickListener {
            findNavController().navigate(
                R.id.carsListFragment,
                CarsListFragment.newInstance(1, "Sold Cars").arguments // Show sold cars
            )
        }

        // When "Unsold Cars" card is clicked, navigate to the cars list showing only unsold cars
        binding.cardUnsoldCars.setOnClickListener {
            findNavController().navigate(
                R.id.carsListFragment,
                CarsListFragment.newInstance(0, "Unsold Cars").arguments // Show unsold cars
            )
        }

        binding.cardFavoriteCars.setOnClickListener{
            findNavController().navigate(
                R.id.sellerFavoriteFragment,
                CarsListFragment.newInstance(0, "Favorites").arguments
            )

        }
        binding.cardApprovedCars.setOnClickListener {
            findNavController().navigate(R.id.approvedCarsFragment)

        }

    }
    private fun setupRecyclerView() {
        val adapter = CommentsAdapter(
            sellerId ?: "",
            commentsList
        ) { averageRatingPercentage ->
            binding.score.text = "$averageRatingPercentage%"
        }

        binding.recyclerViewComments.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            this.adapter = adapter
        }

        loadComments(adapter)
    }


    private fun loadComments(adapter: CommentsAdapter) {
        val sellerId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val databaseReference = FirebaseDatabase.getInstance().getReference("RatingSeller")

        databaseReference.child(sellerId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val comments = mutableListOf<RatingSellerEntity>()
                for (commentSnapshot in snapshot.children) {
                    val comment = commentSnapshot.getValue(RatingSellerEntity::class.java)
                    comment?.let { comments.add(it) }
                }
                comments.sortByDescending { it.date } // Ordenar por fecha
                adapter.updateComments(comments) // Actualizar el adaptador
            }
        }
    }



    // Function to set the user's full name in the username TextView in the UI
    private fun GetUsername(user: UserEntity) {
        binding.tvUsername.text = "${user.firstName} ${user.lastName}"
    }

    // Clean up the binding when the view is destroyed to avoid memory leaks
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
