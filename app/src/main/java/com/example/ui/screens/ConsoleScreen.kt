package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.TerminalLine
import com.example.ui.TerminalLineType
import com.example.ui.theme.HydraAmber
import com.example.ui.theme.HydraAndroidGreen
import com.example.ui.theme.HydraBorder
import com.example.ui.theme.HydraCyan
import com.example.ui.theme.HydraPurple
import com.example.ui.theme.HydraRed
import com.example.ui.theme.HydraSurface
import com.example.ui.theme.HydraTerminalBg
import com.example.ui.theme.HydraTerminalGreen
import com.example.ui.theme.HydraTextMuted
import com.example.ui.theme.HydraTextPrimary

@Composable
fun ConsoleScreen(
    terminalLines: List<TerminalLine>,
    onExecuteCommand: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var commandInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Scroll to bottom when new line appears
    LaunchedEffect(terminalLines.size) {
        if (terminalLines.isNotEmpty()) {
            listState.animateScrollToItem(terminalLines.size - 1)
        }
    }

    val quickCommands = listOf(
        "help",
        "doctor",
        "scope",
        "carkali fingerprint",
        "carkali verify",
        "audit verify",
        "audit tail",
        "scan com.hydranet.wifi_audit",
        "scan com.hydranet.ble_scan",
        "clear"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HydraTerminalBg)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .imePadding()
    ) {
        // Console Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = null,
                    tint = HydraCyan,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "HYDRANETD SHELL [TTY /dev/pts/0]",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = HydraCyan
                )
            }
            Text(
                text = "UDS: OK",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = HydraAndroidGreen
            )
        }

        // Quick Command Pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            quickCommands.forEach { cmd ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(HydraSurface)
                        .border(1.dp, HydraBorder, RoundedCornerShape(6.dp))
                        .testTag("quick_cmd_${cmd.replace(" ", "_")}")
                        .clickable { onExecuteCommand(cmd) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = cmd,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = HydraCyan
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Terminal Log History
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(HydraTerminalBg)
                .border(1.dp, HydraBorder, RoundedCornerShape(8.dp))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(terminalLines) { line ->
                val textColor = when (line.type) {
                    TerminalLineType.INPUT -> HydraCyan
                    TerminalLineType.SUCCESS -> HydraTerminalGreen
                    TerminalLineType.ERROR -> HydraRed
                    TerminalLineType.WARN -> HydraAmber
                    TerminalLineType.OUTPUT -> HydraTextPrimary
                }

                Text(
                    text = line.text,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = textColor,
                    lineHeight = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Command Prompt Input
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(HydraSurface)
                .border(1.dp, HydraCyan, RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "hydranet# ",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = HydraCyan
            )

            BasicTextField(
                value = commandInput,
                onValueChange = { commandInput = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("terminal_input"),
                textStyle = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = HydraTextPrimary
                ),
                cursorBrush = SolidColor(HydraCyan),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (commandInput.isNotBlank()) {
                            onExecuteCommand(commandInput)
                            commandInput = ""
                        }
                    }
                )
            )

            IconButton(
                onClick = {
                    if (commandInput.isNotBlank()) {
                        onExecuteCommand(commandInput)
                        commandInput = ""
                    }
                },
                modifier = Modifier
                    .size(30.dp)
                    .testTag("btn_send_command")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send Command",
                    tint = HydraCyan,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
