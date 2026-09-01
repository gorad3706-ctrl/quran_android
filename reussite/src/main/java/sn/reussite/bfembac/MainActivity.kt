package sn.reussite.bfembac

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class MainActivity : AppCompatActivity() {
  private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/uuuu")

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    findViewById<Button>(R.id.continue_button).setOnClickListener {
      val name = findViewById<EditText>(R.id.first_name).text.toString().trim()
      val date = findViewById<EditText>(R.id.exam_date).text.toString().trim()
      when {
        name.isBlank() -> showError(R.string.validation_name)
        !isValidDate(date) -> showError(R.string.validation_date)
        else -> Toast.makeText(this, R.string.profile_created, Toast.LENGTH_LONG).show()
      }
    }
  }

  private fun isValidDate(value: String): Boolean = try {
    LocalDate.parse(value, dateFormatter)
    true
  } catch (_: DateTimeParseException) {
    false
  }

  private fun showError(messageId: Int) = Toast.makeText(this, messageId, Toast.LENGTH_SHORT).show()
}
