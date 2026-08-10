package com.biocap.app.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Hand-written Room migrations.
 *
 * The database otherwise relies on `fallbackToDestructiveMigration` (see `AppModule`), which is
 * acceptable for structural churn on a device whose sessions were already exported/uploaded. A
 * *rename* of stored [ScenarioCode] values is different: the rows are still perfectly good data and
 * only their label changed, so wiping them would throw away recorded sessions for no reason. Hence
 * this migration rewrites the strings in place.
 */

/**
 * v6 → v7: renamed the five biofeedback [ScenarioCode] values to the study's final terminology.
 * Enum constants are persisted as strings (see [Converters]), so the migration is a plain
 * `UPDATE` per value — no schema change, no data loss.
 *
 * | old value                 | new value                  |
 * |---------------------------|----------------------------|
 * | `REFERENCE_STATE`         | `BASELINE_CALIBRATION`     |
 * | `COGNITIVE_LOAD`          | `HIGH_COGNITIVE_DEMAND`    |
 * | `DISTRACTING_ENVIRONMENT` | `ENVIRONMENTAL_DISTRACTION`|
 * | `LONG_TERM_FATIGUE`       | `SUSTAINED_WORKLOAD`       |
 * | `REACTION_TASKS`          | `SENSORIMOTOR_RESPONSE`    |
 */
val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        val renames = listOf(
            "REFERENCE_STATE" to "BASELINE_CALIBRATION",
            "COGNITIVE_LOAD" to "HIGH_COGNITIVE_DEMAND",
            "DISTRACTING_ENVIRONMENT" to "ENVIRONMENTAL_DISTRACTION",
            "LONG_TERM_FATIGUE" to "SUSTAINED_WORKLOAD",
            "REACTION_TASKS" to "SENSORIMOTOR_RESPONSE"
        )
        for ((old, new) in renames) {
            db.execSQL(
                "UPDATE scenarios SET scenarioCode = ? WHERE scenarioCode = ?",
                arrayOf(new, old)
            )
        }
    }
}
