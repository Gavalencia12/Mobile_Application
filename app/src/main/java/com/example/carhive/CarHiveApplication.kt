package com.example.carhive

import android.app.Application
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.carhive.worker.UnreadMessagesWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit

@HiltAndroidApp
class CarHiveApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        scheduleUnreadMessagesCheck()
    }

    /**
     * Schedules a periodic work to check for unread messages every 10 hours.
     * This work will persist even after the app is closed.
     */
    private fun scheduleUnreadMessagesCheck() {
        // Define constraints for the worker
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // Create a periodic work request
        val workRequest = PeriodicWorkRequestBuilder<UnreadMessagesWorker>(
            10, TimeUnit.HOURS // Set the periodic interval
        )
            .setConstraints(constraints) // Add the network constraints
            .build()

        // Enqueue the work, ensuring it only runs once per unique name
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "UnreadMessagesCheck", // Unique name for the work
            androidx.work.ExistingPeriodicWorkPolicy.KEEP, // Keep existing work to prevent duplication
            workRequest
        )
    }
//    private fun executeUnreadMessagesCheckImmediately() {
//        val workRequest = OneTimeWorkRequestBuilder<UnreadMessagesWorker>().build()
//        WorkManager.getInstance(this).enqueue(workRequest)
//    }

}
