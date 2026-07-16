package com.biocap.app.data.export

/**
 * Uploads a completed session to the VitalWork server. Implemented by
 * [com.biocap.app.data.export.upload.SessionHttpUploader]; the local Documents export is the
 * separate [SessionExporter] (see [SessionExportService]).
 */
interface SessionUploader {
    suspend fun upload(sessionId: Long): Result<String>
}
