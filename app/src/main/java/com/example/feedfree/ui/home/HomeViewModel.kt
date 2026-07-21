import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import java.util.Calendar

class HomeViewModel : ViewModel() {
    private val initialTime = Calendar.getInstance()

    var hour by mutableIntStateOf(initialTime.get(Calendar.HOUR_OF_DAY))
    var minute by mutableIntStateOf(initialTime.get(Calendar.MINUTE))
    var isTimerActive by mutableStateOf(false)
}