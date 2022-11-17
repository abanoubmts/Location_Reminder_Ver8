package com.udacity.project4.locationreminders.reminderslist

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.udacity.project4.locationreminders.MainCoroutine
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.runner.RunWith
import org.junit.*
import org.koin.core.context.stopKoin
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.robolectric.annotation.Config


@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@Config(sdk = [Build.VERSION_CODES.P])
class RemindersListViewModelTest {

    private lateinit var fakeReminderDataSource: FakeDataSource
    private lateinit var remindersViewModel: RemindersListViewModel

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutine()


    @Before
    fun setupViewModel() {
        fakeReminderDataSource = FakeDataSource()
        remindersViewModel = RemindersListViewModel(
            ApplicationProvider.getApplicationContext(),
            fakeReminderDataSource)
    }

    @Test
    fun testShouldReturnError () = runBlockingTest  {
        fakeReminderDataSource.setShouldReturnError(true)
        saveReminderFakeData()
        remindersViewModel.loadReminders()

        MatcherAssert.assertThat(
            remindersViewModel.showSnackBar.value, CoreMatchers.`is`("Reminders not found")
        )
    }

    @Test
    fun check_loading() = runBlockingTest {

        mainCoroutineRule.pauseDispatcher()
        saveReminderFakeData()
        remindersViewModel.loadReminders()

        MatcherAssert.assertThat(remindersViewModel.showLoading.value, CoreMatchers.`is`(true))

        mainCoroutineRule.resumeDispatcher()
        MatcherAssert.assertThat(remindersViewModel.showLoading.value, CoreMatchers.`is`(false))
    }

    private suspend fun saveReminderFakeData() {
        fakeReminderDataSource.saveReminder(
            ReminderDTO(
                "title test",
                "description test",
                "location test",
                30.00,
                31.00)
        )
    }

    @After
    fun tearDown() {
        stopKoin()
    }
}