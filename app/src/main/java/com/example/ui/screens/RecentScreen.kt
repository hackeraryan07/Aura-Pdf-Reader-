package com.example.ui.screens
import androidx.compose.material.pullrefresh.pullRefresh

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.History
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ui.theme.Typography

import com.example.model.PdfFile
import com.example.model.AppDatabase
import com.example.model.PdfHistoryEntity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

import com.example.ui.components.PdfThumbnail

data class RecentSection(val title: String, val files: List<PdfHistoryEntity>, val formattedTime: List<String>)

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.material.ExperimentalMaterialApi::class)
@Composable
fun RecentScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val recentSections = remember { mutableStateListOf<RecentSection>() }

    var isRefreshing by remember { mutableStateOf(false) }

    val pullRefreshState = androidx.compose.material.pullrefresh.rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            coroutineScope.launch {
                kotlinx.coroutines.delay(1000)
                isRefreshing = false
            }
        }
    )

    LaunchedEffect(Unit) {
        val dao = AppDatabase.getDatabase(context).pdfHistoryDao()
        dao.getAllHistory().collectLatest { history ->
            recentSections.clear()
            if (history.isEmpty()) return@collectLatest

            val todayFiles = mutableListOf<PdfHistoryEntity>()
            val todayTime = mutableListOf<String>()
            val yesterdayFiles = mutableListOf<PdfHistoryEntity>()
            val yesterdayTime = mutableListOf<String>()
            val earlierFiles = mutableListOf<PdfHistoryEntity>()
            val earlierTime = mutableListOf<String>()
            val olderFiles = mutableListOf<PdfHistoryEntity>()
            val olderTime = mutableListOf<String>()

            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfToday = calendar.timeInMillis
            
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            val startOfYesterday = calendar.timeInMillis
            
            calendar.add(Calendar.DAY_OF_YEAR, -6)
            val startOfEarlierThisWeek = calendar.timeInMillis

            val sdfTime = SimpleDateFormat("HH:mm", Locale.getDefault())
            val sdfDate = SimpleDateFormat("MMM dd", Locale.getDefault())

            history.forEach { item ->
                val accessTime = item.lastAccessed
                val diffHours = (System.currentTimeMillis() - accessTime) / (1000 * 60 * 60)
                
                val timeStr = if (accessTime >= startOfToday) {
                    if (diffHours == 0L) {
                        val diffMins = (System.currentTimeMillis() - accessTime) / (1000 * 60)
                        if (diffMins <= 0) "Opened just now" else "Opened $diffMins mins ago"
                    } else {
                        "Opened $diffHours hours ago"
                    }
                } else if (accessTime >= startOfYesterday) {
                    "Opened Yesterday, ${sdfTime.format(Date(accessTime))}"
                } else {
                    "Opened ${sdfDate.format(Date(accessTime))}"
                }

                if (accessTime >= startOfToday) {
                    todayFiles.add(item)
                    todayTime.add(timeStr)
                } else if (accessTime >= startOfYesterday) {
                    yesterdayFiles.add(item)
                    yesterdayTime.add(timeStr)
                } else if (accessTime >= startOfEarlierThisWeek) {
                    earlierFiles.add(item)
                    earlierTime.add(timeStr)
                } else {
                    olderFiles.add(item)
                    olderTime.add(timeStr)
                }
            }

            if (todayFiles.isNotEmpty()) recentSections.add(RecentSection("TODAY", todayFiles, todayTime))
            if (yesterdayFiles.isNotEmpty()) recentSections.add(RecentSection("YESTERDAY", yesterdayFiles, yesterdayTime))
            if (earlierFiles.isNotEmpty()) recentSections.add(RecentSection("EARLIER THIS WEEK", earlierFiles, earlierTime))
            if (olderFiles.isNotEmpty()) recentSections.add(RecentSection("OLDER", olderFiles, olderTime))
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.PictureAsPdf,
                            contentDescription = "App Icon",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "PDF Reader",
                            style = Typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Search.route) }) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pullRefresh(pullRefreshState)
        ) {
        if (recentSections.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerLow),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.History,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "No Recent Files",
                    style = Typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Files you open will appear here. Tap a document in your files to get started.",
                    style = Typography.bodyLarge,
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Recent Files",
                    style = Typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            recentSections.forEach { section ->
                item {
                    Text(
                        text = section.title,
                        style = Typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                    ) {
                        section.files.forEachIndexed { index, file ->
                            val timeStr = section.formattedTime[index]
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        coroutineScope.launch(Dispatchers.IO) {
                                            AppDatabase.getDatabase(context).pdfHistoryDao().insertOrUpdate(
                                                PdfHistoryEntity(
                                                    uriString = file.uriString,
                                                    name = file.name,
                                                    size = file.size,
                                                    lastAccessed = System.currentTimeMillis()
                                                )
                                            )
                                        }
                                        try {
                                            val encodedUri = android.net.Uri.encode(file.uriString)
                                            navController.navigate("pdf_viewer?uri=$encodedUri")
                                        } catch (e: Exception) {
                                            // Ignore
                                        }
                                    }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                PdfThumbnail(
                                    uri = Uri.parse(file.uriString),
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceContainer),
                                    fallbackIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.PictureAsPdf,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = file.name,
                                        style = Typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = timeStr,
                                            style = Typography.labelSmall,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                        Text(
                                            text = " • ",
                                            style = Typography.labelSmall,
                                            color = MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        Text(
                                            text = file.size,
                                            style = Typography.labelSmall,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }
                                IconButton(onClick = { /*TODO*/ }) {
                                    Icon(
                                        imageVector = Icons.Filled.MoreVert,
                                        contentDescription = "More",
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                            if (index < section.files.size - 1) {
                                Spacer(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
        androidx.compose.material.pullrefresh.PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            backgroundColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        )
        }
    }
}
}
