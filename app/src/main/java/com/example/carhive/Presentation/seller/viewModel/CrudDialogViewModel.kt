package com.example.carhive.Presentation.seller.viewModel

import androidx.lifecycle.ViewModel
import com.example.carhive.Domain.usecase.database.SaveCarToDatabaseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CrudDialogViewModel @Inject constructor(
    private val saveCarToDatabaseUseCase: SaveCarToDatabaseUseCase
) : ViewModel() {

    fun addCarToDatabase(){

    }

}