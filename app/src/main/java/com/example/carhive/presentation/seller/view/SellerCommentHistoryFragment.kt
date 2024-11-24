package com.example.carhive.presentation.seller.view

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.carhive.R
import com.example.carhive.data.model.RatingSellerEntity
import com.example.carhive.databinding.FragmentUserCommentHistoryBinding
import com.example.carhive.presentation.seller.adapter.CommentHistoryAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch

class SellerCommentHistoryFragment : Fragment() {
    private var _binding: FragmentUserCommentHistoryBinding? = null
    private val binding get() = _binding!!

    private val commentsList = mutableListOf<RatingSellerEntity>()
    private var filteredComments = mutableListOf<RatingSellerEntity>()
    private lateinit var commentsAdapter: CommentHistoryAdapter

    private val sellerId = FirebaseAuth.getInstance().currentUser?.uid // Suponemos que el vendedor es el usuario actual

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserCommentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearch()
        loadComments()

        binding.ibtnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        commentsAdapter = CommentHistoryAdapter(sellerId ?: "", filteredComments)
        binding.recyclerViewHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = commentsAdapter
        }
    }

    private fun setupSearch() {
        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                filterComments(query)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadComments() {
        sellerId?.let { sid ->
            val databaseReference = FirebaseDatabase.getInstance().getReference("RatingSeller")
            lifecycleScope.launch {
                databaseReference.child(sid).get().addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        commentsList.clear()
                        for (commentSnapshot in snapshot.children) {
                            val comment = commentSnapshot.getValue(RatingSellerEntity::class.java)
                            if (comment != null) {
                                commentsList.add(comment)
                            } else {
                                println("Comentario inválido en Firebase: ${commentSnapshot.key}")
                            }
                        }
                        if (commentsList.isEmpty()) {
                            println("No hay comentarios para este vendedor.")
                        } else {
                            println("Comentarios cargados: $commentsList")
                        }
                        // Ordenar y actualizar datos
                        commentsList.sortByDescending { it.date }
                        filteredComments.clear()
                        filteredComments.addAll(commentsList)
                        commentsAdapter.notifyDataSetChanged()
                    } else {
                        println("No se encontraron datos en RatingSeller/$sid")
                    }
                }.addOnFailureListener {
                    println("Error al obtener comentarios: ${it.message}")
                }
            }
        } ?: println("No se encontró el ID del vendedor.")
    }

    private fun filterComments(query: String) {
        filteredComments.clear()
        if (query.isEmpty()) {
            filteredComments.addAll(commentsList)
        } else {
            filteredComments.addAll(commentsList.filter { it.comment.contains(query, ignoreCase = true) })
        }
        commentsAdapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

