package com.udacity.project4.locationreminders.reminderslist

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
import org.junit.runner.RunWith
import org.hamcrest.core.IsNot
import org.junit.*
import org.koin.core.context.stopKoin
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert
import org.junit.After
import org.junit.Rule
import org.junit.Test

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutine()


    private lateinit var remindersRepository: FakeDataSource

    //Subject under test
    private lateinit var viewModel: RemindersListViewModel

    private val reminder1 =  ReminderDTO("abc", "abc_desc", "location", 0.0, 0.0)
    private val reminder2 =  ReminderDTO("xyz", "xyz_desc", "location",  (-360..360).random().toDouble(),(-360..360).random().toDouble())
    private val reminder3 =  ReminderDTO("eee", "eee_desc", "location",  (-360..360).random().toDouble(),(-360..360).random().toDouble())


    @Before
    fun setupViewModel() {
        remindersRepository = FakeDataSource()
        viewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), remindersRepository)

    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun loadReminders_showLoading() {



        mainCoroutineRule.pauseDispatcher()

        val remindersList = mutableListOf(reminder1, reminder2, reminder3)
        remindersRepository = FakeDataSource(remindersList)
        viewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), remindersRepository)

        viewModel.loadReminders()

        assertThat(viewModel.showLoading.getOrAwaitValue()).isTrue()

        mainCoroutineRule.resumeDispatcher()

        assertThat(viewModel.showLoading.getOrAwaitValue()).isFalse()

    }

    /*
  1-- LiveData value set by create reminder object and add it into reminder list
  then load all reminders
  2-- control threading through stop and resume dispatcher before and after return error

  3-- The assertThat method is a stylized sentence for making a test assertion


   */
    @Test
    fun testShouldReturnError () = runBlockingTest  {

        remindersRepository = FakeDataSource(null)
        viewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), remindersRepository)
        viewModel.loadReminders()
        MatcherAssert.assertThat(
            viewModel.showSnackBar.value, CoreMatchers.`is`("Reminders not exist")
        )
    }




    /*1- add check loading method

    1- use run block test : Executes a testBody inside an immediate execution dispatcher.
This method is deprecated in favor of runTest

    2- create reminder object and save data reminder method
    3- load reminders after saving
    4- use matcher assert of Hamcrest frame work
Hamcrest comes with a library of useful matchers. Here are some of the most important ones.
Core
anything - always matches, useful if you don’t care what the object under test
     */

    @Test
    fun check_loading() = runBlockingTest {

        remindersRepository = FakeDataSource(mutableListOf())
        viewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), remindersRepository)
        mainCoroutineRule.pauseDispatcher()
        viewModel.loadReminders()
        MatcherAssert.assertThat(viewModel.showLoading.value, CoreMatchers.`is`(true))


    }
/*
    @Test
    fun loadReminders_remainderListNotEmpty() = mainCoroutineRule.runBlockingTest  {
        val reminder = ReminderDTO("My Store", "Pick Stuff", "Abuja", 6.454202, 7.599545)

        remindersRepository.saveReminder(reminder)
        viewModel.loadReminders()

        assertThat(viewModel.remindersList.getOrAwaitValue()).isNotEmpty()
    }
*/

    /*@Test
    fun loadReminders_updateSnackBarValue() {
        mainCoroutineRule.pauseDispatcher()

        remindersRepository.setReturnError(true)

        viewModel.loadReminders()

       mainCoroutineRule.resumeDispatcher()

        MatcherAssert.assertThat(viewModel.showLoading.value, CoreMatchers.`is`(false))

       //assertThat(viewModel.showSnackBar.getOrAwaitValue()).isEqualTo("Error is happening during get the reminders")
    }

     */
}