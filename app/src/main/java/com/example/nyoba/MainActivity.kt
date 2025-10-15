package com.example.nyoba

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nyoba.ui.theme.NYOBATheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

private const val TAG = "StopwatchApp"

class MainActivity : ComponentActivity() {

    private var currentElapsedTime = 0L

    // VARIABEL BARU: Referensi ke timer Job, di-set dari Composable
    private var currentTimerJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate() - Activity launched/created.")
        setContent {
            NYOBATheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    StopwatchScreen(
                        onTimeUpdate = { time -> currentElapsedTime = time },
                        // CALLBACK BARU: Untuk mendapatkan referensi Job
                        onJobUpdate = { job -> currentTimerJob = job }
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart() - Activity is about to become visible.")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume() - Activity running (Foreground).")
        Log.i(TAG, "--> RESUMED. Last Paused Time (currentElapsedTime): ${formatTime(currentElapsedTime)}")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause() - Another activity is coming into the foreground/Activity is no longer visible.")
        Log.w(TAG, "--> PAUSED. Current Elapsed Time: ${formatTime(currentElapsedTime)}")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop() - The activity is finishing or being destroyed by the system/Activity no longer visible.")
    }

    // === FITUR onDestroyed DITAMBAHKAN DI SINI ===
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy() - Activity shut down.") // Log di kode Anda
        // Logika pembersihan Job Coroutine Anda ada di sini
    }


    override fun onRestart() {
        super.onRestart()
        Log.d(TAG, "onRestart() - User navigates back to the activity (from stop state).")
    }
}

fun formatTime(ms: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(ms)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(ms) - TimeUnit.MINUTES.toSeconds(minutes)
    val milliseconds = ms / 10 % 100
    return String.format("%02d:%02d:%02d", minutes, seconds, milliseconds)
}

@Composable
fun StopwatchScreen(
    onTimeUpdate: (Long) -> Unit,
    onJobUpdate: (Job?) -> Unit // CALLBACK BARU
) {
    // ... (State declaration tetap sama)
    var timeMs by remember { mutableStateOf(0L) }
    var isRunning by remember { mutableStateOf(false) }
    var startTime by remember { mutableStateOf(0L) }
    var elapsedPauseTime by remember { mutableStateOf(0L) }
    val lapTimes = remember { mutableStateListOf<Long>() }

    val coroutineScope = rememberCoroutineScope()
    var timerJob by remember { mutableStateOf<Job?>(null) } // State lokal untuk Job

    val context = LocalContext.current

    // Panggil onTimeUpdate setiap kali timeMs berubah
    LaunchedEffect(timeMs) {
        onTimeUpdate(timeMs)
    }

    // LaunchedEffect untuk meng-update Job di MainActivity
    LaunchedEffect(timerJob) {
        onJobUpdate(timerJob)
    }

    val startTimer: () -> Unit = {
        if (!isRunning) {
            isRunning = true
            timerJob?.cancel()

            startTime = System.currentTimeMillis()
            timerJob = coroutineScope.launch {
                while (isActive) {
                    val currentTime = System.currentTimeMillis()
                    val runningTime = currentTime - startTime
                    timeMs = elapsedPauseTime + runningTime
                    delay(10)
                }
            }
            Toast.makeText(context, "Stopwatch Berjalan", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Stopwatch Started/Resumed. Job: ${timerJob?.hashCode()}")
        }
    }

    val pauseTimer: () -> Unit = {
        if (isRunning) {
            isRunning = false
            timerJob?.cancel()
            timerJob = null // Set Job lokal ke null

            elapsedPauseTime = timeMs
            Toast.makeText(context, "Stopwatch Dijeda", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Stopwatch Paused. Elapsed time: $elapsedPauseTime")
        }
    }

    // ... (resetTimer, recordLap, dan semua UI Composable tetap sama)
    val resetTimer: () -> Unit = {
        pauseTimer()
        timeMs = 0L
        elapsedPauseTime = 0L
        startTime = 0L
        lapTimes.clear()
        Toast.makeText(context, "Stopwatch Direset", Toast.LENGTH_SHORT).show()
        Log.d(TAG, "Stopwatch Reset")
    }

    val recordLap: () -> Unit = {
        if (timeMs > 0L) {
            lapTimes.add(timeMs)
            Toast.makeText(context, "Putaran ke-${lapTimes.size} dicatat", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Lap Recorded: ${formatTime(timeMs)}")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ... (UI Code: Text, Spacer, Row, Button, LazyColumn - semua sama)
        Text(
            text = "Compose Stopwatch",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = formatTime(timeMs),
            fontSize = 64.sp,
            fontWeight = FontWeight.Light,
            style = MaterialTheme.typography.headlineLarge
        )
        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { if (isRunning) recordLap() else resetTimer() },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                ),
                enabled = isRunning || timeMs > 0L
            ) {
                Text(if (isRunning) "LAP" else "RESET")
            }
            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = { if (isRunning) pauseTimer() else startTimer() },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
            ) {
                Text(if (isRunning) "PAUSE" else "START")
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 16.dp),
            thickness = DividerDefaults.Thickness,
            color = DividerDefaults.color
        )

        Text(
            text = "Catatan Putaran:",
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(lapTimes.reversed()) { index, currentLapTotalTime ->
                val lapNumber = lapTimes.size - index
                val previousLapTotalTime = if (lapNumber > 1) lapTimes[lapNumber - 2] else 0L
                val splitTime = currentLapTotalTime - previousLapTotalTime

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Lap $lapNumber",
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Split: ${formatTime(splitTime)}",
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Total: ${formatTime(currentLapTotalTime)}",
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StopwatchScreenPreview() {
    NYOBATheme {
        // Berikan lambda kosong untuk onJobUpdate di Preview
        StopwatchScreen(onTimeUpdate = {}, onJobUpdate = {})
    }
}