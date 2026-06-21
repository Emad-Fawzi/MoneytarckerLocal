package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppDatabase
import com.example.data.TransactionRepository
import com.example.ui.screens.ExpenseTrackerScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.TransactionViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val database = AppDatabase.getDatabase(applicationContext)
    val repository = TransactionRepository(database.transactionDao)

    setContent {
      MyApplicationTheme(dynamicColor = false) { // Don't use system accent to keep deep premium style
        val mainViewModel: TransactionViewModel = viewModel(
          factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
              return TransactionViewModel(repository) as T
            }
          }
        )

        Surface(modifier = Modifier.fillMaxSize()) {
          ExpenseTrackerScreen(viewModel = mainViewModel)
        }
      }
    }
  }
}
