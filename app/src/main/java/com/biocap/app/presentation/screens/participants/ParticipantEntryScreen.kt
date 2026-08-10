package com.biocap.app.presentation.screens.participants

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

private const val MIN_AGE = 18
private const val MAX_AGE = 80

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParticipantEntryScreen(
    onNavigateBack: () -> Unit,
    onSessionStarted: (Long) -> Unit,
    viewModel: ParticipantEntryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ParticipantEntryEvent.SessionStarted -> onSessionStarted(event.sessionId)
                is ParticipantEntryEvent.ActiveSessionDetected -> onSessionStarted(event.sessionId)
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 560.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                com.biocap.app.presentation.components.BioCapTopBar(
                    title = "New Participant",
                    subtitle = "Anonymized code + basic demographics",
                    onNavigateBack = onNavigateBack
                )

                com.biocap.app.presentation.components.AppCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Participant code — auto-generated from the device prefix (Settings),
                        // read-only so it can't be edited into a colliding code.
                        OutlinedTextField(
                            value = uiState.participantCode,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Participant code") },
                            singleLine = true,
                            isError = uiState.codeError != null,
                            supportingText = uiState.codeError?.let { { Text(it) } },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Age stepper
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Age (18 to 80 years old)",
                                style = MaterialTheme.typography.labelLarge
                            )
                            AgeStepper(
                                value = uiState.ageInput,
                                enabled = !uiState.isSubmitting,
                                isError = uiState.ageError != null,
                                onValueChange = viewModel::onAgeChange
                            )
                            uiState.ageError?.let {
                                Text(
                                    text = it,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        // Gender segmented buttons
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Gender",
                                style = MaterialTheme.typography.labelLarge
                            )
                            GenderSegmentedRow(
                                selected = uiState.gender,
                                enabled = !uiState.isSubmitting,
                                onSelected = viewModel::onGenderChange
                            )
                        }

                        uiState.submitError?.let {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Button(
                            onClick = viewModel::submit,
                            enabled = uiState.isInitialized && !uiState.isSubmitting,
                            modifier = Modifier.fillMaxWidth(),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = com.biocap.app.ui.theme.Navy,
                                contentColor = androidx.compose.ui.graphics.Color.White
                            )
                        ) {
                            if (uiState.isSubmitting) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .size(20.dp),
                                    color = androidx.compose.ui.graphics.Color.White
                                )
                            }
                            Text(text = "Start session", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AgeStepper(
    value: String,
    enabled: Boolean,
    isError: Boolean,
    onValueChange: (String) -> Unit
) {
    val current = value.toIntOrNull()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedIconButton(
            onClick = {
                current?.let { onValueChange((it - 1).coerceIn(MIN_AGE, MAX_AGE).toString()) }
            },
            enabled = enabled && current != null
        ) {
            Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = "Decrease age"
            )
        }

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = "–",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            singleLine = true,
            isError = isError,
            enabled = enabled,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle = MaterialTheme.typography.titleLarge.copy(textAlign = TextAlign.Center),
            modifier = Modifier.width(96.dp)
        )

        OutlinedIconButton(
            onClick = {
                current?.let { onValueChange((it + 1).coerceIn(MIN_AGE, MAX_AGE).toString()) }
            },
            enabled = enabled && current != null
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Increase age"
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GenderSegmentedRow(
    selected: GenderOption,
    enabled: Boolean,
    onSelected: (GenderOption) -> Unit
) {
    val options = GenderOption.entries
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, option ->
            SegmentedButton(
                selected = option == selected,
                onClick = { onSelected(option) },
                enabled = enabled,
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size)
            ) {
                Text(text = option.shortLabel())
            }
        }
    }
}

private fun GenderOption.shortLabel(): String =
    if (this == GenderOption.NOT_SPECIFIED) "N/A" else label
