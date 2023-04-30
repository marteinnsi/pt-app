package marteinn

import java.util.Date

data class Student(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val dob: Date,
    val trackId: Int
)