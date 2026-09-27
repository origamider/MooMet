package com.example.moomet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.moomet.ui.theme.MooMetTheme

class PermissionsRationaleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MooMetTheme {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp)
                ) {
                    Text(
                        text = "健康データの利用について",
                        style = MaterialTheme.typography.headlineSmall
                    )

                    Text(
                        text = "MooMetは、メンタルスコアの算出と表示のために、" +
                                "Health Connectから睡眠時間と運動時間を読み取ります。"
                    )
                }
            }
        }
    }
}