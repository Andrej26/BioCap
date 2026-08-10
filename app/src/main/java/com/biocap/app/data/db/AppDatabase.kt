package com.biocap.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        ParticipantEntity::class,
        SessionEntity::class,
        ScenarioEntity::class,
        SensorSampleEntity::class
    ],
    // v2: added SensorType.WATCH_IBI for Galaxy Watch store-and-forward.
    // v3: split per-device SensorTypes — HEART_RATE → ESENSE_HEART_RATE + WATCH_HR, EDA → WATCH_EDA —
    //     so HR from the eSense Pulse and the Galaxy Watch (recorded simultaneously) never merge.
    // v4: added sessions.watchHrSampleCount + watchIbiSampleCount so the session summary counters
    //     cover all six sample types (previously WATCH_HR/WATCH_IBI were uncounted).
    // v5: biofeedback-only pivot — dropped scenarios.scenarioCategory/eventTimestampMs/
    //     reactionTimestampMs and sessions.notes (reaction-time measurement + the VR link were
    //     removed; the app now records only sensor samples per scenario).
    // v6: renamed the nine industrial ScenarioCode values to the five biofeedback scenarios
    //     (REFERENCE_STATE/COGNITIVE_LOAD/DISTRACTING_ENVIRONMENT/LONG_TERM_FATIGUE/REACTION_TASKS).
    // v7: renamed those five to the study's final terminology (BASELINE_CALIBRATION/
    //     HIGH_COGNITIVE_DEMAND/ENVIRONMENTAL_DISTRACTION/SUSTAINED_WORKLOAD/SENSORIMOTOR_RESPONSE).
    //     Unlike the earlier bumps this one ships a real Migration (MIGRATION_6_7) that rewrites the
    //     stored strings in place, because the affected rows are recorded sessions that must survive.
    // The DB still keeps fallbackToDestructiveMigration (see AppModule) as the catch-all for older
    // versions with no hand-written path; enum values are stored as strings by Converters.
    version = 7,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun participantDao(): ParticipantDao
    abstract fun sessionDao(): SessionDao
    abstract fun scenarioDao(): ScenarioDao
    abstract fun sensorSampleDao(): SensorSampleDao
}
