# Sensor Sampling Rates

Sampling rates, data types, and synchronization approach for all sensors in BioCap.

## Overview

| Sensor | Vendor | Connection | Data Types | Rate |
|--------|--------|------------|------------|------|
| eSense Pulse | Mindfield | BLE | Heart Rate (BPM), R-R Intervals (ms) | ≈ 4.5 Hz (225 ms period; nominal 5 Hz) |
| eSense Respiration | Mindfield | Audio Jack | Respiration Amplitude (RA) | 5 Hz (configured) |
| Galaxy Watch 8 | Samsung | Wearable Data Layer (via `:wear`) | Heart Rate (BPM), IBI (ms), EDA (µS) | ≈ 1 Hz (HR, EDA); IBI variable |

## Per-Sensor Details

### eSense Pulse (Heart Rate + R-R)

**Output rate: ≈ 4.5 Hz** (225 ms period, clock-driven; observed 4.27–5.01 Hz across recordings; nominal 5 Hz)

- Internal PPG sensor: 500 Hz, processed on-device before BLE transmission
- BLE Heart Rate Service (`0x180D`), characteristic `0x2A37`
- Each notification contains: BPM value + optionally R-R intervals (beat-to-beat timing in ms)
- Measuring range: 30–240 BPM (±2 BPM accuracy)

See [sensor_esense_pulse.md](sensor_esense_pulse.md) for protocol details.

### eSense Respiration

**Output rate: 5 Hz** (configurable via SDK, currently set to 5 Hz)

```kotlin
private const val SAMPLE_FREQ = 5  // in MindfieldRespiration.kt
```

- Respiration Amplitude (RA) — raw chest expansion/contraction waveform
- Breathing rate derived via zero-crossing detection on a 30-second window

**Nyquist analysis:** Normal breathing rate is 0.2–0.33 Hz. At 5 Hz, oversampling is >15x — more than adequate.

See [sensor_esense_respiration.md](sensor_esense_respiration.md) for protocol details.

### Galaxy Watch 8 (HR + IBI + EDA)

**Output rate: ≈ 1 Hz** for HR and EDA. Both come from Samsung Health Sensor SDK *continuous*
trackers running **on the watch** (`HEART_RATE_CONTINUOUS`, `EDA_CONTINUOUS`).

- `HEART_RATE_CONTINUOUS` yields **both** HR and IBI in a single `HeartRateSet` — one tracker, two streams
- **IBI is variable-rate**, tied to heartbeat timing, and arrives as a **list** per data point (the parser iterates `IBI_LIST`; it is not one IBI per callback)
- EDA is the reason the watch is used at all — it is unreachable over BLE on this device

**Delivery rate ≠ sampling rate.** The watch samples at ~1 Hz continuously, but delivery to the phone
is bursty:

- **Screen on / AP awake:** a 1 Hz `HealthTracker.flush()` loop forces the SDK's buffered batch out, so readings arrive live at roughly their sampling rate.
- **Screen off / Doze:** the flush loop's `delay()` freezes, the SDK buffers, and one callback later carries several 1 Hz samples at once. A low-rate (~30 s) heartbeat keeps the phone showing "dozing — buffering" rather than "disconnected".
- **Session end:** the phone sends a remote `FLUSH`; the watch ships its entire durable store back as `DataClient` DataItems. This store-and-forward path is what guarantees no sample is lost regardless of Doze.

Each reading carries its **own** timestamp stamped when it was sampled, so late or batched delivery
never distorts the timeline.

See [sensor_galaxy_watch.md](sensor_galaxy_watch.md) for the transport, store-and-forward, and flush
handshake.

## Data Collection

`ScenarioRecordingRepositoryImpl` collects samples from all connected sensors concurrently, scoped to
the scenario being recorded.

```
eSense Pulse ──(~4.5 Hz)──► heartRateSampleFlow ────────► ESENSE_HEART_RATE
                          ► rrIntervalSampleFlow ───────► ESENSE_RR_INTERVAL
eSense Respiration ──(5 Hz)──► sampleFlow ─────────────► RESPIRATION
Galaxy Watch ──(~1 Hz, bursty)──► watchSampleFlow ─────► WATCH_HR / WATCH_IBI / WATCH_EDA
```

Each sample is stored with:
- `timestampMs` — absolute epoch-ms timestamp from the **NTP-corrected clock** (`TimeProvider.nowMs()`, Kronos), *not* the raw device clock
- `elapsedMs` — time since recording start (`now - startTimeMs`, same corrected clock)
- `sensorType` — one of the `SensorType` enum values
- `value` — the sensor reading (Float)

Samples are batched (50 samples or 1-second flush interval) before database write.

> The watch is the one exception to "stamped by the phone": its readings are stamped **on the watch**
> with the watch's own `System.currentTimeMillis()`, then lifted onto the same NTP timeline by
> `WatchSensorReceiver.correctedTimestamp()` (which adds `TimeProvider.ntpOffsetMs()`). This is why
> the watch is collected session-long and split into scenario windows by `WatchSessionDrainer`
> afterwards, rather than started/stopped per scenario.

## Synchronization Approach

Sensors run at their native rates. Alignment is handled via timestamp-based storage:

- **eSense Pulse (~4.5 Hz) + Respiration (5 Hz)** are close enough in cadence that samples remain co-located on the timeline at the typical sub-second analysis resolution; precise alignment is done by per-sample timestamp regardless
- **R-R intervals** (eSense) and **IBI** (watch) arrive at variable rates tied to heartbeat timing
- **Watch streams (~1 Hz)** are an order of magnitude sparser than the eSense streams and may arrive in late bursts, so they must be aligned by timestamp — never by arrival order or by index

**Why there is no clock-sync step:** every persisted timestamp — phone-side samples, session and
scenario boundaries, and NTP-lifted watch stamps — sits on one NTP-corrected UTC timeline
(`TimeProvider`). An Android app cannot set the OS clock, so the correction is applied as an offset
at stamp time instead. Cross-stream alignment is therefore a plain timestamp comparison, with no
device-to-device handshake.

All continuous sensor samples share the same `sensor_samples` table with per-sample timestamps, so downstream analysis can align across rates as needed.

## References

- [eSense Pulse Technical Documentation](https://mindfield-store.com/en/eSense-Pulse/)
- [Bluetooth Heart Rate Service Specification](https://www.bluetooth.com/specifications/specs/heart-rate-service-1-0/)
- [eSense SDK Documentation](https://mindfield-store.com/en/eSense-SDK/)
- [Samsung Health Sensor SDK](https://developer.samsung.com/health/sensor/overview.html)
