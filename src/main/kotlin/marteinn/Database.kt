package marteinn

import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.util.Date
import java.util.Dictionary
import java.util.LinkedList

class Database {
    private val connection: Connection

    init {
        connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/progresstracker", "root", "qwerty")
    }

    fun create(firstName: String, lastName: String, dob: Date, trackId: Int): Int {
        connection.prepareCall("CALL CreateStudent(?, ?, ?, ?)").use { statement ->
            statement.setString(1, firstName)
            statement.setString(2, lastName)
            statement.setDate(3, java.sql.Date(dob.time))
            statement.setInt(4, trackId)
            statement.executeQuery()
            statement.resultSet.next()

            return statement.resultSet.getInt("studentID")
        }
    }

    fun read(id: Int): Student? {
        connection.prepareCall("CALL ReadStudent(?)").use { statement ->
            statement.setInt(1, id)
            statement.executeQuery()

            if (statement.resultSet.next()) {
                val id: Int = statement.resultSet.getInt("studentID")
                val firstName: String = statement.resultSet.getString("firstName")
                val lastName: String = statement.resultSet.getString("lastName")
                val dob: Date = statement.resultSet.getDate("dob")
                val trackId: Int = statement.resultSet.getInt("trackId")
                return Student(id, firstName, lastName, dob, trackId)
            } else {
                return null
            }
        }
    }

    fun update(id: Int, firstName: String, lastName: String, dob: Date, trackId: Int): Int {
        connection.prepareCall("CALL UpdateStudent(?,?,?,?,?)").use { statement ->
            statement.setInt(1, id)
            statement.setString(2, firstName)
            statement.setString(3, lastName)
            statement.setDate(4, java.sql.Date(dob.time))
            statement.setInt(5, trackId)
            statement.execute()
            return statement.updateCount
        }
    }

    fun delete(id: Int): Int {
        connection.prepareCall("CALL DeleteStudents(?)").use { statement ->
            statement.setInt(1, id)
            statement.execute()
            return statement.updateCount
        }
    }

    fun tracks(): List<Track> {
        val list = mutableListOf<Track>()

        connection.prepareStatement("SELECT trackID, trackName FROM tracks").use { statement ->
            statement.execute()

            while (statement.resultSet.next()) {
                val trackId = statement.resultSet.getInt(1)
                val trackName = statement.resultSet.getString(2)
                list.add(Track(trackId, trackName))
            }
        }

        return list
    }

    fun idRange(): Pair<Int, Int> {
        connection.prepareStatement("SELECT MIN(studentID),MAX(studentID) FROM students").use { statement ->
            statement.executeQuery()
            statement.resultSet.next()
            return statement.resultSet.getInt(1) to statement.resultSet.getInt(2)
        }
    }

    fun students(limit: Int): List<Student> {
        val list = LinkedList<Student>()
        connection.prepareCall("CALL ListStudents($limit)").use { statement ->
            statement.execute()
            while (statement.resultSet.next()) {
                val id: Int = statement.resultSet.getInt("studentID")
                val firstName: String = statement.resultSet.getString("firstName")
                val lastName: String = statement.resultSet.getString("lastName")
                val dob: Date = statement.resultSet.getDate("dob")
                val trackId: Int = statement.resultSet.getInt("trackId")
                list.add(Student(id, firstName, lastName, dob, trackId))
            }
        }
        return list
    }

    fun readToDict(limit: Int): List<Map<String, String>> {
        val list = ArrayList<Map<String, String>>()
        connection.prepareCall("CALL ListStudents($limit)").use { statement ->
            statement.execute()
            while (statement.resultSet.next()) {
                val id: Int = statement.resultSet.getInt("studentID")
                val firstName: String = statement.resultSet.getString("firstName")
                val lastName: String = statement.resultSet.getString("lastName")
                val dob: Date = statement.resultSet.getDate("dob")
                val trackId: Int = statement.resultSet.getInt("trackId")

                val student = buildMap {
                    put("id", "$id")
                    put("firstName", firstName)
                    put("lastName", lastName)
                    put("dob", "$dob")
                    put("trackId", "$trackId")
                }

                list.add(student)
            }
        }
        return list
    }

    fun test() {
        println("Connected to ${connection.metaData.databaseProductName} version ${connection.metaData.databaseProductVersion}")
    }
}