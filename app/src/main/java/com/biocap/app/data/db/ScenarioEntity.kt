package com.biocap.app.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class ScenarioCode(
    val officialCode: String,
    val displayName: String,
    val countdownMinutes: Int
) {
    BASELINE_CALIBRATION("A", "Scenario A – Baseline Calibration", 10),
    HIGH_COGNITIVE_DEMAND("B", "Scenario B – High Cognitive Demand", 20),
    ENVIRONMENTAL_DISTRACTION("C", "Scenario C – Environmental Distraction", 20),
    SUSTAINED_WORKLOAD("D", "Scenario D – Sustained Workload", 30),
    SENSORIMOTOR_RESPONSE("E", "Scenario E – Sensorimotor Response", 10);

    companion object {
        fun fromOfficialCode(code: String): ScenarioCode? =
            entries.firstOrNull { it.officialCode.equals(code, ignoreCase = true) }
    }
}

@Entity(
    tableName = "scenarios",
    foreignKeys = [
        ForeignKey(
            entity = SessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["sessionId", "scenarioCode"])
    ]
)
data class ScenarioEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val sessionId: Long,

    val scenarioCode: ScenarioCode,

    val startedAt: Long,

    val endedAt: Long? = null
)
