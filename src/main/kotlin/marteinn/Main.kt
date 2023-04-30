package marteinn

import com.github.kinquirer.KInquirer
import com.github.kinquirer.components.*
import com.github.kinquirer.core.Choice
import java.util.Date
import kotlin.system.exitProcess


private val database = Database()
private val debug = false

fun main() {
    if (debug) {
        // Output: Connected to MySQL version 8.0.32
        database.test()

        // Output: Jóhanna Andradóttir
        database.read(2).let { student ->
            println("${student?.firstName} ${student?.lastName}")
        }


        database.readToDict(10).forEach { student ->
            println()
            student.forEach { t, u ->
                println("$t: $u")
            }
        }
    }

    while (true) {
        renderMenu()
        println()
    }
}

fun renderMenu() {
    val choice: String = KInquirer.promptList(
        message = "Select operation:", choices = listOf(
            "1 Create student",
            "2 Read student",
            "3 Update student",
            "4 Delete student",
            "5 Student list",
            "0 Quit"
        ), viewOptions = ListViewOptions(
            questionMarkPrefix = "E",
            cursor = " >  ",
            nonCursor = "    ",
        )
    )

    when (choice[0].digitToInt()) {
        1 -> renderCreateStudent()
        2 -> renderReadStudent()
        3 -> renderUpdateStudent()
        4 -> renderDeleteStudent()
        5 -> renderStudentList()
        0 -> {
            println("Goodbye!")
            exitProcess(0)
        }

        else -> {}
    }
}

fun renderStudentList() {
    val limit = KInquirer.promptInputNumber("Limit: ")
    val students = database.students(limit.toInt())

    val choice = KInquirer.promptListObject(
        message = "Select student:", choices = students.map(::studentToChoice).toList(), viewOptions = ListViewOptions(
            questionMarkPrefix = "E",
            cursor = " >  ",
            nonCursor = "    ",
        ), pageSize = 8
    )

    renderStudentMenu(choice)
}

fun renderStudentMenu(student: Student) {
    val operation = KInquirer.promptList(
        message = "${student.firstName} ${student.lastName}", choices = listOf(
            "Read", "Update", "Delete"
        ), viewOptions = ListViewOptions(
            questionMarkPrefix = "E",
            cursor = " >  ",
            nonCursor = "    ",
        )
    )

    when (operation) {
        "Read" -> {
            println("Name: ${student.firstName} ${student.lastName}")
            println("Date of birth: ${student.dob}")
            println("Track: ${student.trackId}")
        }

        "Update" -> {
            val tracks = database.tracks()

            val existingStudentTrack = tracks.first { it.id == student.trackId }

            val firstName = KInquirer.promptInput("First Name: ", hint = student.firstName)
            val lastName = KInquirer.promptInput("Last Name: ", hint = student.lastName)
            val dob = KInquirer.promptInput("Date of Birth (DD/MM/YYYY): ")
            val trackId = KInquirer.promptListObject(
                message = "Track:", hint = existingStudentTrack.name, choices = tracks.map(::trackToChoice).toList(), viewOptions = ListViewOptions(
                    questionMarkPrefix = "E",
                    cursor = " >  ",
                    nonCursor = "    ",
                )
            )

            val changes = database.update(student.id, firstName, lastName, Date(dob), trackId)
            println("Changed $changes rows")
        }

        "Delete" -> {
            val deleted = database.delete(student.id)
            println("Deleted $deleted rows")
        }

        else -> {}
    }
}

fun renderDeleteStudent() {
    val range = database.idRange()
    val id = KInquirer.promptInputNumber("Student ID: ", hint = "${range.first} - ${range.second}")

    val deleted = database.delete(id.toInt())

    println("Deleted $deleted rows")
}

fun renderUpdateStudent() {
    val tracks = database.tracks()
    val range = database.idRange()
    val id = KInquirer.promptInputNumber("Student ID: ", hint = "${range.first} - ${range.second}")
    val existingStudent = database.read(id.toInt())

    if (existingStudent == null) {
        println("No such student exists")
        return;
    }

    val existingStudentTrack = tracks.first { it.id == existingStudent.trackId }

    val firstName = KInquirer.promptInput("First Name: ", hint = existingStudent.firstName)
    val lastName = KInquirer.promptInput("Last Name: ", hint = existingStudent.lastName)
    val dob = KInquirer.promptInput("Date of Birth (DD/MM/YYYY): ")
    val trackId = KInquirer.promptListObject(
        message = "Track:", hint = existingStudentTrack.name, choices = tracks.map(::trackToChoice).toList(), viewOptions = ListViewOptions(
            questionMarkPrefix = "E",
            cursor = " >  ",
            nonCursor = "    ",
        )
    )

    val changes = database.update(id.toInt(), firstName, lastName, Date(dob), trackId)
    println("Changed $changes rows")
}

fun renderReadStudent() {
    val range = database.idRange()

    val id = KInquirer.promptInputNumber("Student ID: ", hint = "${range.first} - ${range.second}")
    val student = database.read(id.toInt())

    println("Name: ${student?.firstName} ${student?.lastName}")
    println("Date of birth: ${student?.dob}")
    println("Track: ${student?.trackId}")
}

fun renderCreateStudent() {
    val tracks = database.tracks()
    val firstName = KInquirer.promptInput("First Name: ")
    val lastName = KInquirer.promptInput("Last Name: ")
    val dob = KInquirer.promptInput("Date of Birth (DD/MM/YYYY): ")
    val trackId = KInquirer.promptListObject(
        message = "Track:", choices = tracks.map(::trackToChoice).toList(), viewOptions = ListViewOptions(
            questionMarkPrefix = "E",
            cursor = " >  ",
            nonCursor = "    ",
        )
    )

    val id = database.create(firstName, lastName, Date(dob), trackId)

    println("Created student #$id")
}

fun studentToChoice(student: Student): Choice<Student> {
    return Choice("${student.firstName} ${student.lastName}", student)
}


fun trackToChoice(track: Track): Choice<Int> {
    return Choice(Regex("[^A-Za-z0-9 ]").replace(track.name, ""), track.id)
}

