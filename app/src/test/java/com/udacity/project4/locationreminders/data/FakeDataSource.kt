package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(var remindersList: MutableList<ReminderDTO>? = mutableListOf()) : ReminderDataSource {

    //    Done: Create a fake data source to act as a double to the real data source
    var error_flag = false


    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        //Done("Return the reminders")
        if (error_flag) {
            return Result.Error(
                "Error is happening during get the reminders"
            )
        }



        remindersList?.let { return Result.Success(it) }
        return Result.Error("Reminders not exist")
    }





    override suspend fun saveReminder(reminder: ReminderDTO) {
        // Done("save the reminder")
        remindersList?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        // Done("return the reminder with the id")
        val reminder = remindersList?.find { reminderDTO ->
            reminderDTO.id == id
        }
        return when {
            error_flag -> {
                Result.Error("Reminder not found!")
            }

            reminder != null -> {
                Result.Success(reminder)
            }
            else -> {
                Result.Error("Reminder not found!")
            }
        }
    }

    override suspend fun deleteAllReminders() {
        //Done("delete all the reminders")
        remindersList?.clear()
    }
/*
add the below method to allow test model class to use it in the method
testShouldReturnError  and load reminders

 */
    fun setReturnError(value: Boolean) {
        error_flag = value
    }

}