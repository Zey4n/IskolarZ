package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.*
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: IskolarZViewModel = viewModel()
                val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
                var hasEnteredDashboard by remember { mutableStateOf(false) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (userProfile != null && hasEnteredDashboard) {
                            BottomNavigationBar(
                                currentTab = viewModel.currentTab,
                                onTabSelected = { viewModel.currentTab = it }
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(innerPadding)
                    ) {
                        if (userProfile == null || !hasEnteredDashboard) {
                            LaunchOnboardingScreen(
                                profile = userProfile,
                                onSaveProfile = { nickname, track, startYear ->
                                    viewModel.saveProfile(nickname, track, startYear)
                                    viewModel.currentTab = 0
                                    hasEnteredDashboard = true
                                },
                                onEnterDashboard = {
                                    viewModel.currentTab = 0
                                    hasEnteredDashboard = true
                                },
                                viewModel = viewModel
                            )
                        } else {
                            when (viewModel.currentTab) {
                                0 -> DashboardTab(viewModel = viewModel, profile = userProfile!!)
                                1 -> GradesTab(viewModel = viewModel)
                                2 -> CurriculumTab(viewModel = viewModel, profile = userProfile!!)
                                3 -> CalculatorTab(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}

data class NavItem(val index: Int, val label: String, val icon: ImageVector)

@Composable
fun BottomNavigationBar(currentTab: Int, onTabSelected: (Int) -> Unit) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        modifier = Modifier.height(80.dp)
    ) {
        val items = listOf(
            NavItem(0, "Dashboard", Icons.Default.Dashboard),
            NavItem(1, "Grades", Icons.Default.Assessment),
            NavItem(2, "Curriculum", Icons.Default.ListAlt),
            NavItem(3, "Calculator", Icons.Default.Calculate)
        )
        items.forEach { item ->
            val isSelected = currentTab == item.index
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(item.index) },
                icon = { Icon(item.icon, contentDescription = item.label, tint = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )
            )
        }
    }
}

// ==================== ONBOARDING LAUNCH SCREEN ====================
@Composable
fun LaunchOnboardingScreen(
    profile: UserProfileEntity?,
    onSaveProfile: (String, String, String) -> Unit,
    onEnterDashboard: () -> Unit,
    viewModel: IskolarZViewModel
) {
    // Choose start semester
    val startSemesters = remember {
        val list = mutableListOf<String>()
        for (year in 2022..2029) {
            list.add("AY $year-${year + 1} First Semester")
            list.add("AY $year-${year + 1} Second Semester")
        }
        list
    }

    var nickname by remember(profile) { mutableStateOf(profile?.nickname ?: "") }
    var selectedTrack by remember(profile) { mutableStateOf(profile?.track ?: "Health Informatics") }
    var selectedStartYear by remember(profile) { mutableStateOf(profile?.startYear ?: startSemesters.first()) }
    var isYearDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .testTag("onboarding_screen"),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.School,
            contentDescription = "IskolarZ Logo",
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "IskolarZ",
            fontSize = 38.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.secondary
        )
        Text(
            text = "University of the Philippines Manila • BS Computer Science Curriculum Checklist",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(40.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = if (profile != null) "PROFILE & ACADEMICS:" else "TELL US ABOUT YOURSELF",
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    label = { Text("Nickname / Alias") },
                    singleLine = true,
                    enabled = profile == null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("nickname_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.secondary,
                        focusedLabelColor = MaterialTheme.colorScheme.secondary
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Academic Start Year", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { isYearDropdownExpanded = true },
                        enabled = profile == null,
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Text(text = selectedStartYear, color = if (profile != null) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onBackground)
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "dropdown", tint = MaterialTheme.colorScheme.secondary)
                    }
                    if (profile == null) {
                        DropdownMenu(
                            expanded = isYearDropdownExpanded,
                            onDismissRequest = { isYearDropdownExpanded = false }
                        ) {
                            startSemesters.forEach { sem ->
                                DropdownMenuItem(
                                    text = { Text(sem) },
                                    onClick = {
                                        selectedStartYear = sem
                                        isYearDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (profile != null) "Track Specialization" else "Choose Your Track Specialization",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    listOf("Health Informatics", "Statistical Computing").forEach { track ->
                        val selected = selectedTrack == track
                        Button(
                            onClick = { selectedTrack = track },
                            enabled = profile == null,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                contentColor = if (selected) Color.Black else MaterialTheme.colorScheme.onBackground,
                                disabledContainerColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                                disabledContentColor = if (selected) Color.Black.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            border = if (!selected) BorderStroke(1.dp, MaterialTheme.colorScheme.outline) else null
                        ) {
                            Text(text = track, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (profile != null) {
                    onEnterDashboard()
                } else {
                    if (nickname.trim().isNotBlank()) {
                        onSaveProfile(nickname, selectedTrack, selectedStartYear)
                    }
                }
            },
            enabled = profile != null || nickname.trim().isNotBlank(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                disabledContainerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                contentColor = Color.Black
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("submit_button"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("ENTER DASHBOARD", fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
        }
    }
}

// ==================== DASHBOARD TAB ====================
@Composable
fun DashboardTab(
    viewModel: IskolarZViewModel,
    profile: UserProfileEntity
) {
    val grades by viewModel.gradeRecords.collectAsStateWithLifecycle()
    val stats by viewModel.gwaStatsState.collectAsStateWithLifecycle(initialValue = GwaStats(0.0, 0.0, 0.0, 0.0, 158.0))
    
    var isEditProfileDialogShown by remember { mutableStateOf(false) }
    var isAddGradeDialogShown by remember { mutableStateOf(false) }
    var isViewAllGradesDialogShown by remember { mutableStateOf(false) }
    var gradeToDelete by remember { mutableStateOf<GradeRecordEntity?>(null) }
    var gradeToEdit by remember { mutableStateOf<GradeRecordEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Welcome and Edit button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "IskolarZ Tracker",
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp
                )
                Text(
                    text = "Welcome, ${profile.nickname.uppercase()}!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
            }
            IconButton(
                onClick = { isEditProfileDialogShown = true },
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
            ) {
                Icon(Icons.Default.Settings, contentDescription = "Edit Profile", tint = MaterialTheme.colorScheme.secondary)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // First Card: General Standing
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "CUMULATIVE GWA",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = { viewModel.toggleGwaVisibility() },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (viewModel.isGwaVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle GWA Visibility",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (viewModel.isGwaVisible) {
                        if (stats.totalAcademicUnitsForGwa > 0) String.format(Locale.US, "%.4f", stats.overallGwa) else "0.0000"
                    } else {
                        "• • • •"
                    },
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.secondary
                )
                
                // Latin Honors standing
                val honor = viewModel.getLatinHonor(stats.overallGwa, stats.academicUnitsEarned)
                if (honor.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (viewModel.isGwaVisible) honor else "• • • •",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (viewModel.isGwaVisible) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier
                            .background(
                                color = if (viewModel.isGwaVisible) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("BS Computer Science", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("PROGRAM", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(profile.track, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                        Text("TRACK", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Second Card: Metric Overview
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "CURRICULUM METRICS",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(12.dp))

                val totalLabel = if (stats.failedUnits > 0) "158(+${stats.failedUnits.toInt()})" else "158"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Academic Units Target", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    Text(totalLabel, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Academic Units Earned", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    Text("${stats.academicUnitsEarned.toInt()} units", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Remaining Units", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    Text("${stats.remainingUnits.toInt()} units", fontWeight = FontWeight.Bold, color = if (stats.remainingUnits > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.secondary)
                }

                Spacer(modifier = Modifier.height(12.dp))
                val progress = if (stats.academicUnitsEarned >= 158) 1f else (stats.academicUnitsEarned / 158.0).toFloat()
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.outline
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Recent Grades List Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "RECENT GRADES ADDED",
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
            if (grades.isNotEmpty()) {
                Text(
                    text = "View All",
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { isViewAllGradesDialogShown = true }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Recent Grades List
        if (grades.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.HourglassEmpty, contentDescription = "Empty", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No grades added yet", fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { isAddGradeDialogShown = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary, contentColor = Color.Black)
                    ) {
                        Text("Add Your First Grade")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(grades.take(5)) { grade ->
                    GradeItemRow(
                        gradeRecord = grade,
                        onEdit = { gradeToEdit = grade },
                        onDelete = { gradeToDelete = grade }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { isAddGradeDialogShown = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary, contentColor = Color.Black),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("add_grade_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Icon")
                Spacer(modifier = Modifier.width(8.dp))
                Text("ADD NEW GRADE", fontWeight = FontWeight.Bold)
            }
        }
    }

    // Modal dialogs
    if (isEditProfileDialogShown) {
        EditProfileDialog(
            profile = profile,
            viewModel = viewModel,
            onDismiss = { isEditProfileDialogShown = false }
        )
    }

    if (isAddGradeDialogShown) {
        AddNewGradeDialog(
            viewModel = viewModel,
            profile = profile,
            onDismiss = { isAddGradeDialogShown = false }
        )
    }

    if (isViewAllGradesDialogShown) {
        ViewAllGradesDialog(
            viewModel = viewModel,
            onDismiss = { isViewAllGradesDialogShown = false }
        )
    }

    if (gradeToEdit != null) {
        EditGradeDialog(
            gradeRecord = gradeToEdit!!,
            viewModel = viewModel,
            profile = profile,
            onDismiss = { gradeToEdit = null }
        )
    }

    if (gradeToDelete != null) {
        AlertDialog(
            onDismissRequest = { gradeToDelete = null },
            title = {
                Text(
                    text = "CONFIRM DELETE",
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete this grade record: ${gradeToDelete!!.courseCode}?\n\nThis will permanently remove the record and cannot be undone.",
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteGrade(gradeToDelete!!)
                        gradeToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = Color.White
                    )
                ) {
                    Text("DELETE", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { gradeToDelete = null }
                ) {
                    Text("CANCEL")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(12.dp)
        )
    }
}

// ==================== RECENT GRADE ITEM ROW ====================
@Composable
fun GradeItemRow(
    gradeRecord: GradeRecordEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (gradeRecord.isPEorNSTP) "${gradeRecord.courseCode}" else gradeRecord.courseCode,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    if (gradeRecord.isPEorNSTP) {
                        Text(
                            text = "(NON-ACAD)",
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = gradeRecord.courseName,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${gradeRecord.academicYear} • " + 
                            if (gradeRecord.isPEorNSTP) "(${String.format(Locale.US, "%.1f", gradeRecord.units)}) Units" else "${gradeRecord.units} Units",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (gradeRecord.isPEorNSTP) "(${gradeRecord.grade})" else gradeRecord.grade,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (gradeRecord.grade == "5.00" || gradeRecord.grade == "DRP") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f))
                }
            }
        }
    }
}

// ==================== ADD NEW GRADE DIALOG WITH AUTOCOMPLETE ====================
@Composable
fun AddNewGradeDialog(
    viewModel: IskolarZViewModel,
    profile: UserProfileEntity,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val grades by viewModel.gradeRecords.collectAsStateWithLifecycle()
    
    var courseCodeInput by remember { mutableStateOf("") }
    var courseNameInput by remember { mutableStateOf("") }
    var unitsInput by remember { mutableStateOf("3.0") }
    var isPEorNSTPInput by remember { mutableStateOf(false) }
    var isGEInput by remember { mutableStateOf(false) }

    // Selected items
    var selectedGrade by remember { mutableStateOf("1.00") }
    val semesters = viewModel.getAvailableSemesters(profile)
    var selectedSemester by remember { mutableStateOf(semesters.first()) }

    var isGradeDropdownExpanded by remember { mutableStateOf(false) }
    var isSemDropdownExpanded by remember { mutableStateOf(false) }

    // Core list search feature
    var searchQuery by remember { mutableStateOf("") }
    var isSearchFocused by remember { mutableStateOf(false) }

    // All available master courses from curriculum (excluding subjects completely passed)
    val passedSubjectCodes = remember(grades) { 
        grades.filter { viewModel.isPassedGrade(it.grade) }.map { it.courseCode.uppercase() }.toSet()
    }

    val searchResults = remember(searchQuery, profile) {
        val all = CurriculumData.getAllSearchedCourses(profile.track)
        if (searchQuery.isBlank()) {
            // suggest non-passed subjects
            all.filter { !passedSubjectCodes.contains(it.code.uppercase()) }.take(5)
        } else {
            all.filter {
                (it.code.contains(searchQuery, ignoreCase = true) || it.name.contains(searchQuery, ignoreCase = true)) &&
                !passedSubjectCodes.contains(it.code.uppercase())
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary)
        ) {
            LazyColumn(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                item {
                    Text(
                        text = "ADD GRADE RECORD",
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Live search field
                    Text("Search Course (Type Code or Name)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            isSearchFocused = true
                        },
                        placeholder = { Text("e.g. CMSC 11") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // Show search suggestions
                if (searchResults.isNotEmpty()) {
                    item {
                        Text("Curriculum Suggestions (Click to Auto-fill):", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    items(searchResults.take(4)) { course ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                                .clickable {
                                    courseCodeInput = course.code
                                    courseNameInput = course.name
                                    unitsInput = course.units.toString()
                                    isPEorNSTPInput = course.category == "PE"
                                    isGEInput = course.category == "GE"
                                    searchQuery = course.code
                                    isSearchFocused = false
                                },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Row(modifier = Modifier.padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(course.code, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(course.name, fontSize = 11.sp, maxLines = 1)
                                }
                                Text("${course.units} units", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))

                    val matchedCourse = remember(courseCodeInput, profile) {
                        CurriculumData.getAllSearchedCourses(profile.track).find {
                            it.code.trim().equals(courseCodeInput.trim(), ignoreCase = true)
                        }
                    }
                    val isLockedCourse = remember(matchedCourse) {
                        matchedCourse != null &&
                        !matchedCourse.code.trim().equals("CMSC Elective 1", ignoreCase = true) &&
                        !matchedCourse.code.trim().equals("CMSC Elective 2", ignoreCase = true) &&
                        matchedCourse.category != "PE"
                    }
                    val isElectiveCode = courseCodeInput.trim().equals("CMSC Elective 1", ignoreCase = true) || courseCodeInput.trim().equals("CMSC Elective 2", ignoreCase = true)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Selected Course Details", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        if (courseCodeInput.isNotEmpty()) {
                            TextButton(
                                onClick = {
                                    courseCodeInput = ""
                                    courseNameInput = ""
                                    unitsInput = "3.0"
                                    isPEorNSTPInput = false
                                    isGEInput = false
                                    searchQuery = ""
                                },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Clear Selection", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = courseCodeInput,
                        onValueChange = {
                            courseCodeInput = it
                            val isPe = it.uppercase().contains("PE") && !it.uppercase().contains("NSTP") && !it.uppercase().contains("CWTS") && !it.uppercase().contains("LTS") && !it.uppercase().contains("PEOPLE") && !it.uppercase().contains("SPEC") && !it.uppercase().contains("ELECT")
                            isPEorNSTPInput = isPe
                            isGEInput = it.uppercase().startsWith("GE ") || it.uppercase() == "GE"
                            if (isPe) {
                                isGEInput = false
                                unitsInput = "2.0"
                            }
                        },
                        label = { Text("Course Code") },
                        singleLine = true,
                        enabled = !isElectiveCode && !isLockedCourse,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = courseNameInput,
                        onValueChange = { courseNameInput = it },
                        label = { Text("Course Name") },
                        enabled = !isLockedCourse,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = unitsInput,
                        onValueChange = { unitsInput = it },
                        label = { Text("Units") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        enabled = !isLockedCourse,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Non-academic category indicator
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                isPEorNSTPInput = !isPEorNSTPInput
                                if (isPEorNSTPInput) {
                                    isGEInput = false
                                    unitsInput = "2.0"
                                }
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isPEorNSTPInput,
                            onCheckedChange = {
                                isPEorNSTPInput = it
                                if (it) {
                                    isGEInput = false
                                    unitsInput = "2.0"
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("This is a PE subject", fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                isGEInput = !isGEInput
                                if (isGEInput) {
                                    isPEorNSTPInput = false
                                }
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isGEInput,
                            onCheckedChange = {
                                isGEInput = it
                                if (it) {
                                    isPEorNSTPInput = false
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("This is a GE subject", fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Grade dropdown
                    Text("Select Grade", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { isGradeDropdownExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Text(selectedGrade, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "dropdown")
                        }
                        DropdownMenu(
                            expanded = isGradeDropdownExpanded,
                            onDismissRequest = { isGradeDropdownExpanded = false }
                        ) {
                            listOf("1.00", "1.25", "1.50", "1.75", "2.00", "2.25", "2.50", "2.75", "3.00", "4.00", "5.00", "DRP").forEach { gr ->
                                DropdownMenuItem(
                                    text = { Text(gr) },
                                    onClick = {
                                        selectedGrade = gr
                                        isGradeDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Semester dropdown
                    Text("Academic Year & Semester Taken", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { isSemDropdownExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Text(selectedSemester, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "dropdown")
                        }
                        DropdownMenu(
                            expanded = isSemDropdownExpanded,
                            onDismissRequest = { isSemDropdownExpanded = false }
                        ) {
                            semesters.forEach { sem ->
                                DropdownMenuItem(
                                    text = { Text(sem) },
                                    onClick = {
                                        selectedSemester = sem
                                        isSemDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                val dUnits = unitsInput.toDoubleOrNull()
                                if (courseCodeInput.trim().isBlank() || courseNameInput.trim().isBlank() || dUnits == null || dUnits <= 0.0) {
                                    Toast.makeText(context, "Please fill out all fields correctly", Toast.LENGTH_SHORT).show()
                                } else {
                                    val isNstp = courseCodeInput.uppercase().contains("NSTP") ||
                                                 courseCodeInput.uppercase().contains("CWTS") ||
                                                 courseCodeInput.uppercase().contains("LTS")
                                    val finalIsPEorNSTP = isPEorNSTPInput || isNstp

                                    viewModel.addGrade(
                                        courseCode = courseCodeInput.trim(),
                                        courseName = courseNameInput.trim(),
                                        units = dUnits,
                                        grade = selectedGrade,
                                        academicYear = selectedSemester,
                                        isPEorNSTP = finalIsPEorNSTP,
                                        isGE = if (isNstp) false else isGEInput
                                    )
                                    onDismiss()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary, contentColor = Color.Black),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Save", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ==================== EDIT GRADE DIALOG ====================
@Composable
fun EditGradeDialog(
    gradeRecord: GradeRecordEntity,
    viewModel: IskolarZViewModel,
    profile: UserProfileEntity,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    
    var courseCodeInput by remember { mutableStateOf(gradeRecord.courseCode) }
    var courseNameInput by remember { mutableStateOf(gradeRecord.courseName) }
    var unitsInput by remember { mutableStateOf(gradeRecord.units.toString()) }
    var isPEorNSTPInput by remember { mutableStateOf(gradeRecord.isPEorNSTP) }
    var isGEInput by remember { mutableStateOf(gradeRecord.isGE) }

    // Selected items
    var selectedGrade by remember { mutableStateOf(gradeRecord.grade) }
    val semesters = viewModel.getAvailableSemesters(profile)
    var selectedSemester by remember { mutableStateOf(
        if (gradeRecord.academicYear.contains(" - START")) gradeRecord.academicYear else {
            semesters.find { it == gradeRecord.academicYear || it.replace(" - START", "") == gradeRecord.academicYear } ?: semesters.first()
        }
    ) }

    var isGradeDropdownExpanded by remember { mutableStateOf(false) }
    var isSemDropdownExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "EDIT GRADE RECORD",
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 16.sp
                    )
                    if (courseCodeInput.isNotEmpty()) {
                        TextButton(
                            onClick = {
                                courseCodeInput = ""
                                courseNameInput = ""
                                unitsInput = "3.0"
                                isPEorNSTPInput = false
                                isGEInput = false
                            },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Clear", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                val matchedCourse = remember(courseCodeInput, profile) {
                    CurriculumData.getAllSearchedCourses(profile.track).find {
                        it.code.trim().equals(courseCodeInput.trim(), ignoreCase = true)
                    }
                }
                val isLockedCourse = remember(matchedCourse) {
                    matchedCourse != null &&
                    !matchedCourse.code.trim().equals("CMSC Elective 1", ignoreCase = true) &&
                    !matchedCourse.code.trim().equals("CMSC Elective 2", ignoreCase = true) &&
                    matchedCourse.category != "PE"
                }
                val isElectiveCode = courseCodeInput.trim().equals("CMSC Elective 1", ignoreCase = true) || courseCodeInput.trim().equals("CMSC Elective 2", ignoreCase = true)

                OutlinedTextField(
                    value = courseCodeInput,
                    onValueChange = {
                        courseCodeInput = it
                        val isPe = it.uppercase().contains("PE") && !it.uppercase().contains("NSTP") && !it.uppercase().contains("CWTS") && !it.uppercase().contains("LTS") && !it.uppercase().contains("PEOPLE") && !it.uppercase().contains("SPEC") && !it.uppercase().contains("ELECT")
                        isPEorNSTPInput = isPe
                        isGEInput = it.uppercase().startsWith("GE ") || it.uppercase() == "GE"
                        if (isPe) {
                            isGEInput = false
                            unitsInput = "2.0"
                        }
                    },
                    label = { Text("Course Code") },
                    singleLine = true,
                    enabled = !isElectiveCode && !isLockedCourse,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = courseNameInput,
                    onValueChange = { courseNameInput = it },
                    label = { Text("Course Name") },
                    enabled = !isLockedCourse,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = unitsInput,
                    onValueChange = { unitsInput = it },
                    label = { Text("Units") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    enabled = !isLockedCourse,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Non-academic category indicator
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            isPEorNSTPInput = !isPEorNSTPInput
                            if (isPEorNSTPInput) {
                                isGEInput = false
                                unitsInput = "2.0"
                            }
                        }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isPEorNSTPInput,
                        onCheckedChange = {
                            isPEorNSTPInput = it
                            if (it) {
                                isGEInput = false
                                unitsInput = "2.0"
                            }
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("This is a PE subject", fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            isGEInput = !isGEInput
                            if (isGEInput) {
                                isPEorNSTPInput = false
                            }
                        }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isGEInput,
                        onCheckedChange = {
                            isGEInput = it
                            if (it) {
                                isPEorNSTPInput = false
                            }
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("This is a GE subject", fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Grade dropdown
                Text("Select Grade", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { isGradeDropdownExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Text(selectedGrade, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "dropdown")
                    }
                    DropdownMenu(
                        expanded = isGradeDropdownExpanded,
                        onDismissRequest = { isGradeDropdownExpanded = false }
                    ) {
                        listOf("1.00", "1.25", "1.50", "1.75", "2.00", "2.25", "2.50", "2.75", "3.00", "4.00", "5.00", "DRP").forEach { gr ->
                            DropdownMenuItem(
                                text = { Text(gr) },
                                onClick = {
                                    selectedGrade = gr
                                    isGradeDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Semester dropdown
                Text("Academic Year & Semester Taken", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { isSemDropdownExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Text(selectedSemester, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "dropdown")
                    }
                    DropdownMenu(
                        expanded = isSemDropdownExpanded,
                        onDismissRequest = { isSemDropdownExpanded = false }
                    ) {
                        semesters.forEach { sem ->
                            DropdownMenuItem(
                                text = { Text(sem) },
                                onClick = {
                                    selectedSemester = sem
                                    isSemDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            val dUnits = unitsInput.toDoubleOrNull()
                            if (courseCodeInput.trim().isBlank() || courseNameInput.trim().isBlank() || dUnits == null || dUnits <= 0.0) {
                                Toast.makeText(context, "Please fill out all fields correctly", Toast.LENGTH_SHORT).show()
                            } else {
                                val isNstp = courseCodeInput.uppercase().contains("NSTP") ||
                                             courseCodeInput.uppercase().contains("CWTS") ||
                                             courseCodeInput.uppercase().contains("LTS")
                                val finalIsPEorNSTP = isPEorNSTPInput || isNstp

                                val updated = gradeRecord.copy(
                                    courseCode = courseCodeInput.trim(),
                                    courseName = courseNameInput.trim(),
                                    units = dUnits,
                                    grade = selectedGrade,
                                    academicYear = selectedSemester.replace(" - START", ""),
                                    isPEorNSTP = finalIsPEorNSTP,
                                    isGE = if (isNstp) false else isGEInput
                                )
                                viewModel.updateGradeRecord(updated)
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary, contentColor = Color.Black),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Update", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==================== EDIT PROFILE DIALOG ====================
@Composable
fun EditProfileDialog(
    profile: UserProfileEntity,
    viewModel: IskolarZViewModel,
    onDismiss: () -> Unit
) {
    var nickname by remember { mutableStateOf(profile.nickname) }
    var selectedTrack by remember { mutableStateOf(profile.track) }
    var selectedStartYear by remember { mutableStateOf(profile.startYear) }
    var isYearDropdownExpanded by remember { mutableStateOf(false) }

    val startSemesters = remember(viewModel.maxYearEnd) {
        val list = mutableListOf<String>()
        for (year in 2022..viewModel.maxYearEnd) {
            list.add("AY $year-${year + 1} First Semester")
            list.add("AY $year-${year + 1} Second Semester")
        }
        list
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "EDIT STUDENT PROFILE",
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    label = { Text("Nickname") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Academic Start Year", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { isYearDropdownExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = selectedStartYear, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "dropdown")
                    }
                    DropdownMenu(
                        expanded = isYearDropdownExpanded,
                        onDismissRequest = { isYearDropdownExpanded = false }
                    ) {
                        startSemesters.forEach { sem ->
                            DropdownMenuItem(
                                text = { Text(sem) },
                                onClick = {
                                    selectedStartYear = sem
                                    isYearDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Choose Your Track Specialization", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    listOf("Health Informatics", "Statistical Computing").forEach { track ->
                        val selected = selectedTrack == track
                        Button(
                            onClick = { selectedTrack = track },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                contentColor = if (selected) Color.Black else MaterialTheme.colorScheme.onBackground
                            ),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(text = track, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                // Add new academic year button
                Button(
                    onClick = { viewModel.incrementMaxYear() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.secondary
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("+ Extend Academic Years Choices", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            if (nickname.trim().isNotBlank()) {
                                viewModel.saveProfile(nickname, selectedTrack, selectedStartYear)
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary, contentColor = Color.Black),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save Changes", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==================== VIEW ALL GRADES DIALOG_EXPANSION ====================
@Composable
fun ViewAllGradesDialog(viewModel: IskolarZViewModel, onDismiss: () -> Unit) {
    val grades by viewModel.gradeRecords.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()

    var gradeToDelete by remember { mutableStateOf<GradeRecordEntity?>(null) }
    var gradeToEdit by remember { mutableStateOf<GradeRecordEntity?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "ALL RECORDED GRADES",
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(grades) { grade ->
                        GradeItemRow(
                            gradeRecord = grade,
                            onEdit = { gradeToEdit = grade },
                            onDelete = { gradeToDelete = grade }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary, contentColor = Color.Black),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close")
                }
            }
        }
    }

    if (gradeToEdit != null && profile != null) {
        EditGradeDialog(
            gradeRecord = gradeToEdit!!,
            viewModel = viewModel,
            profile = profile!!,
            onDismiss = { gradeToEdit = null }
        )
    }

    if (gradeToDelete != null) {
        AlertDialog(
            onDismissRequest = { gradeToDelete = null },
            title = {
                Text(
                    text = "CONFIRM DELETE",
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete this grade record: ${gradeToDelete!!.courseCode}?\n\nThis will permanently remove the record and cannot be undone.",
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteGrade(gradeToDelete!!)
                        gradeToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = Color.White
                    )
                ) {
                    Text("DELETE", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { gradeToDelete = null }
                ) {
                    Text("CANCEL")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(12.dp)
        )
    }
}


// ==================== GRADES TAB ====================
@Composable
fun GradesTab(viewModel: IskolarZViewModel) {
    val grades by viewModel.gradeRecords.collectAsStateWithLifecycle()
    val semestersWithGrades = remember(grades) {
        grades.map { it.academicYear }.distinct().sortedBy { it }
    }

    if (semestersWithGrades.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                Icon(Icons.Default.Feed, contentDescription = "History", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text("No Semester Record Found", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Please add subject grades from the Dashboard tab to view your semestral summaries here.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), textAlign = TextAlign.Center)
            }
        }
        return
    }

    var selectedSemester by remember { mutableStateOf(semestersWithGrades.first()) }
    // Auto sync when list changes
    LaunchedEffect(semestersWithGrades) {
        if (!semestersWithGrades.contains(selectedSemester) && semestersWithGrades.isNotEmpty()) {
            selectedSemester = semestersWithGrades.first()
        }
    }

    var isDropdownExpanded by remember { mutableStateOf(false) }

    // Filter grades for selected semester
    val semesterGrades = remember(grades, selectedSemester) {
        grades.filter { it.academicYear == selectedSemester }
    }

    // Sort according to priority:
    // 1. CMSC Majors first
    // 2. Other majors (Math, Stat, Chem, Phys, HI etc)
    // 3. GEs
    // 4. PE subjects
    // 5. NSTP subjects
    val sortedGrades = remember(semesterGrades) {
        semesterGrades.sortedWith(compareBy(
            { getSortCategory(it.courseCode) },
            { it.courseCode }
        ))
    }

    // GWA calculation for selected semester ONLY
    val semAcademicUnits = sortedGrades.filter { !it.isPEorNSTP && it.grade != "DRP" }.sumOf { it.units }
    val semTotalWeighted = sortedGrades.filter { !it.isPEorNSTP && it.grade != "DRP" }.sumOf {
        val valDouble = it.grade.toDoubleOrNull() ?: 0.0
        valDouble * it.units
    }
    val semGwa = if (semAcademicUnits > 0) semTotalWeighted / semAcademicUnits else 0.00
    
    // PE/NSTP sum
    val semPeNstpUnits = sortedGrades.filter { it.isPEorNSTP && it.grade != "DRP" }.sumOf { it.units }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "GRADES VIEWING",
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            fontSize = 14.sp
        )
        Text(text = "Semestral Breakdown", fontWeight = FontWeight.Bold, fontSize = 22.sp)

        Spacer(modifier = Modifier.height(16.dp))

        // Semestral Selector Box
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "ACADEMIC PERIOD",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                
                Box(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.background,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { isDropdownExpanded = true }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = "Academic Selector",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = selectedSemester,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "dropdown arrow",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = isDropdownExpanded, 
                        onDismissRequest = { isDropdownExpanded = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        semestersWithGrades.forEach { sem ->
                            DropdownMenuItem(
                                text = { 
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.School,
                                            contentDescription = null,
                                            tint = if (sem == selectedSemester) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = sem,
                                            fontWeight = if (sem == selectedSemester) FontWeight.Bold else FontWeight.Normal,
                                            color = if (sem == selectedSemester) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                },
                                onClick = {
                                    selectedSemester = sem
                                    isDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1.3f)
                    ) {
                        Text(
                            text = "STUDENT STANDING & STATUS",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        val standing = viewModel.getSemestralStanding(semGwa, selectedSemester)
                        if (standing.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "🎖️ $standing",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.secondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Regular Academic Standing",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (semAcademicUnits > 0) String.format(Locale.US, "%.4f", semGwa) else "N/A",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.secondary,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "SEMESTRAL GWA",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Table List
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Table header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        .padding(8.dp)
                ) {
                    Text("Subject", Modifier.weight(1.8f), fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Text("Units", Modifier.weight(0.7f), fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = FontFamily.Monospace, textAlign = TextAlign.Center)
                    Text("Grade", Modifier.weight(0.7f), fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = FontFamily.Monospace, textAlign = TextAlign.End)
                }

                Spacer(modifier = Modifier.height(4.dp))

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(sortedGrades) { gr ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1.8f)) {
                                Text(
                                    text = if (gr.isPEorNSTP) "${gr.courseCode}" else gr.courseCode,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (gr.isPEorNSTP) MaterialTheme.colorScheme.textSecondary() else MaterialTheme.colorScheme.secondary
                                )
                                Text(gr.courseName, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), maxLines = 1)
                            }
                            Text(
                                text = if (gr.isPEorNSTP) String.format(Locale.US, "(%.1f)", gr.units) else "${gr.units}",
                                modifier = Modifier.weight(0.7f),
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = if (gr.isPEorNSTP) "(${gr.grade})" else gr.grade,
                                modifier = Modifier.weight(0.7f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                textAlign = TextAlign.End,
                                color = if (gr.grade == "5.00" || gr.grade == "DRP") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    }
                }

                // Table Summary Bottom Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val acadTotalStr = if (semAcademicUnits % 1.0 == 0.0) "${semAcademicUnits.toInt()}" else "$semAcademicUnits"
                    val unitsText = if (semPeNstpUnits > 0) {
                        "$acadTotalStr(${String.format(Locale.US, "%.1f", semPeNstpUnits)})"
                    } else {
                        acadTotalStr
                    }
                    Text("Total Units Taken: $unitsText", fontWeight = FontWeight.Bold, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                    
                    Text(
                        text = if (semAcademicUnits > 0) "GWA: " + String.format(Locale.US, "%.4f", semGwa) else "GWA: N/A",
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

// ==================== HELPER SORT CATEGORY ====================
fun getSortCategory(courseCode: String): Int {
    val code = courseCode.uppercase().trim()
    return when {
        code.startsWith("CMSC") || code.startsWith("CMCS") -> 1
        code.startsWith("MATH") || code.startsWith("STAT") || code.startsWith("CHEM") || code.startsWith("PHYS") || code.startsWith("HI") -> 2
        code.startsWith("PE") -> 4
        code.startsWith("NSTP") || code.startsWith("CWTS") || code.startsWith("LTS") -> 5
        // GEs
        code.startsWith("GE") || code.startsWith("WIKA") || code.startsWith("COMM") || code.startsWith("KAS") || code.startsWith("ETHICS") || code.startsWith("STS") || code.startsWith("PHILARTS") || code.startsWith("SAS") || code.startsWith("SCIENCE") || code.startsWith("NATSCI") || code.startsWith("NATURAL") || code.startsWith("ARTS") || code.startsWith("PI") -> 3
        else -> 2 // Other major
    }
}

@Composable
fun ColorScheme.textSecondary(): Color = this.onSurface.copy(alpha = 0.5f)


// ==================== CURRICULUM TAB ====================
@Composable
fun CurriculumTab(
    viewModel: IskolarZViewModel,
    profile: UserProfileEntity
) {
    val grades by viewModel.gradeRecords.collectAsStateWithLifecycle()
    
    // Filters: 0 = All, 1 = GEs, 2 = PEs, 3 = NSTPs
    var selectedFilterIndex by remember { mutableStateOf(0) }
    val filters = listOf("All Checklist", "GE courses", "PE courses", "NSTP courses")

    var searchQuery by remember { mutableStateOf("") }
    var selectedCourseForView by remember { mutableStateOf<CourseInfo?>(null) }
    var isSearchFocused by remember { mutableStateOf(false) }

    val searchResults = remember(searchQuery, profile.track) {
        val all = CurriculumData.getAllSearchedCourses(profile.track)
        if (searchQuery.isBlank()) {
            emptyList<CourseInfo>()
        } else {
            all.filter {
                it.code.contains(searchQuery, ignoreCase = true) ||
                it.name.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // Mapping passed courses for checks
    val passedCoursesSet = remember(grades) {
        grades.filter { viewModel.isPassedGrade(it.grade) }.map { it.courseCode.uppercase().trim() }.toSet()
    }

    // Get track specific courses
    val originalCurriculum = remember(profile.track) {
        CurriculumData.getCourseListForTrack(profile.track)
    }

    val mappedCurriculum = remember(originalCurriculum, grades) {
        val takenGEs = grades.filter { it.isGE && viewModel.isPassedGrade(it.grade) }
            .sortedBy { it.timestamp }
        val takenPEs = grades.filter {
            it.isPEorNSTP &&
            !it.courseCode.uppercase().contains("NSTP") &&
            !it.courseCode.uppercase().contains("CWTS") &&
            !it.courseCode.uppercase().contains("LTS") &&
            viewModel.isPassedGrade(it.grade)
        }.sortedBy { it.timestamp }
        val takenNSTPs = grades.filter {
            it.isPEorNSTP &&
            (it.courseCode.uppercase().contains("NSTP") ||
             it.courseCode.uppercase().contains("CWTS") ||
             it.courseCode.uppercase().contains("LTS")) &&
            viewModel.isPassedGrade(it.grade)
        }.sortedBy { it.timestamp }

        var geCount = 0
        var peCount = 0
        var nstpCount = 0

        originalCurriculum.map { item ->
            when (item.category) {
                "GE" -> {
                    if (geCount < takenGEs.size) {
                        val record = takenGEs[geCount]
                        geCount++
                        val matchedGE = CurriculumData.REQUIRED_GES.find {
                            it.code.uppercase().trim() == record.courseCode.uppercase().trim()
                        } ?: CurriculumData.ELECTIVE_GES.find {
                            it.code.uppercase().trim() == record.courseCode.uppercase().trim() ||
                            (it.code == "Natural Science II" && record.courseCode.uppercase().trim() == "NATSCI II")
                        }
                        val matchingDesc = matchedGE?.description ?: "General Education recorded subject."
                        item.copy(
                            code = record.courseCode,
                            name = record.courseName,
                            units = record.units,
                            description = matchingDesc
                        )
                    } else {
                        item
                    }
                }
                "PE" -> {
                    if (peCount < takenPEs.size) {
                        val record = takenPEs[peCount]
                        peCount++
                        item.copy(
                            code = record.courseCode,
                            name = record.courseName,
                            units = record.units,
                            description = "Physical Education recorded subject."
                        )
                    } else {
                        item
                    }
                }
                "NSTP" -> {
                    if (nstpCount < takenNSTPs.size) {
                        val record = takenNSTPs[nstpCount]
                        nstpCount++
                        item.copy(
                            code = record.courseCode,
                            name = record.courseName,
                            units = record.units,
                            description = "National Service Training Program recorded subject."
                        )
                    } else {
                        item
                    }
                }
                "CMSC_MAJOR" -> {
                    if (item.code == "CMSC Elective 1" || item.code == "CMSC Elective 2") {
                        val record = grades.find { it.courseCode.uppercase().trim() == item.code.uppercase().trim() }
                        if (record != null) {
                            item.copy(
                                name = record.courseName,
                                units = record.units
                            )
                        } else {
                            item
                        }
                    } else {
                        item
                    }
                }
                else -> item
            }
        }
    }

    // Apply filters
    val filteredCurriculum = remember(mappedCurriculum, selectedFilterIndex) {
        when (selectedFilterIndex) {
            1 -> mappedCurriculum.filter { it.category == "GE" }
            2 -> mappedCurriculum.filter { it.category == "PE" }
            3 -> mappedCurriculum.filter { it.category == "NSTP" }
            else -> mappedCurriculum
        }
    }

    // Grouping filtered list by Year-Semester
    val groupedCurriculum = remember(filteredCurriculum) {
        filteredCurriculum.groupBy { "YEAR ${it.year} • ${it.semester.uppercase()}" }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "CURRICULUM CHECKLIST",
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            fontSize = 14.sp
        )
        Text(text = "Course Syllabus Tracker", fontWeight = FontWeight.Bold, fontSize = 22.sp)
        
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Based on the official BS Computer Science curriculum (effective AY 2018–2019), issued by the Department of Physical Sciences and Mathematics, College of Arts and Sciences, University of the Philippines Manila.",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.primary,
            lineHeight = 14.sp,
            textAlign = TextAlign.Justify
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Live Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                isSearchFocused = true
            },
            placeholder = { Text("Search Course Code or Name...") },
            label = { Text("Search Curriculum Subject") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("curriculum_search_field"),
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search icon")
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty() || selectedCourseForView != null) {
                    IconButton(onClick = {
                        searchQuery = ""
                        selectedCourseForView = null
                        isSearchFocused = false
                    }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search")
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Search Autocomplete Suggestions
        if (isSearchFocused && searchResults.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                LazyColumn(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(searchResults) { course ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedCourseForView = course
                                    searchQuery = course.code
                                    isSearchFocused = false
                                },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(course.code, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(course.name, fontSize = 11.sp, maxLines = 1)
                                }
                                Text("${course.units.toInt()} units", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Selected Subject Detailed View & Taken Status Indicator
        if (selectedCourseForView != null) {
            val course = selectedCourseForView!!
            val gradeRecordForCourse = remember(grades, course.code) {
                grades.find { it.courseCode.uppercase().trim() == course.code.uppercase().trim() }
            }
            val isPassed = gradeRecordForCourse != null && viewModel.isPassedGrade(gradeRecordForCourse.grade)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(
                    width = 2.dp,
                    color = if (isPassed) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = course.code,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = if (isPassed) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                val badgeLabel = when {
                                    course.category == "GE" -> "GE"
                                    course.category == "PE" -> "PE"
                                    course.category == "NSTP" -> "NSTP"
                                    course.code.startsWith("CMSC Elective") -> "ELECTIVE"
                                    else -> null
                                }
                                if (badgeLabel != null) {
                                    Box(
                                        modifier = Modifier
                                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                            .border(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = badgeLabel,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.secondary,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                            Text(
                                text = course.name,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }
                        Text(
                            text = "${course.units.toInt()} units",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "COURSE DESCRIPTION",
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = course.description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f),
                        textAlign = TextAlign.Justify
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "PREREQUISITES",
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = course.prerequisites,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isPassed) MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isPassed) Icons.Default.CheckCircle else Icons.Default.Info,
                            contentDescription = "Status",
                            tint = if (isPassed) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isPassed) "STATUS: TAKEN & COMPLETED" else "STATUS: NOT YET TAKEN",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isPassed) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                fontFamily = FontFamily.Monospace
                            )
                            if (gradeRecordForCourse != null) {
                                Text(
                                    text = if (isPassed) "Grade: ${gradeRecordForCourse.grade} (${gradeRecordForCourse.academicYear})" else "Recorded Grade: ${gradeRecordForCourse.grade} (${gradeRecordForCourse.academicYear})",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Filter chips list
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            filters.forEachIndexed { index, title ->
                val selected = selectedFilterIndex == index
                Card(
                    modifier = Modifier
                        .clickable { selectedFilterIndex = index }
                        .weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Box(modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = title,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selected) Color.Black else MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Group list of courses
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            groupedCurriculum.forEach { (semesterHeader, courses) ->
                item {
                    Text(
                        text = semesterHeader,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                items(courses) { course ->
                    val isPassed = passedCoursesSet.contains(course.code.uppercase().trim())
                    
                    // Allow UI expansion
                    var isExpanded by remember { mutableStateOf(false) }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isExpanded = !isExpanded },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isPassed) MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = if (isPassed) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = "Status",
                                        tint = if (isPassed) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    val gradeRecordForCourse = remember(grades, course.code) {
                                        grades.find { it.courseCode.uppercase().trim() == course.code.uppercase().trim() }
                                    }
                                    val badgeLabel = when {
                                        gradeRecordForCourse?.isGE == true -> "GE"
                                        gradeRecordForCourse?.isPEorNSTP == true -> {
                                            val cCode = course.code.uppercase().trim()
                                            if (cCode.contains("NSTP") || cCode.contains("CWTS") || cCode.contains("LTS")) "NSTP" else "PE"
                                        }
                                        course.code.startsWith("CMSC Elective") -> "ELECTIVE"
                                        else -> null
                                    }

                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = course.code,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = if (isPassed) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface
                                            )
                                            if (badgeLabel != null) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                        .border(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = badgeLabel,
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.secondary,
                                                        fontFamily = FontFamily.Monospace
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = course.name,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                            maxLines = 1
                                        )
                                    }
                                }

                                Text(
                                    text = "${course.units.toInt()}u",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            AnimatedVisibility(visible = isExpanded) {
                                Column(modifier = Modifier.padding(top = 12.dp)) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Text(
                                        text = "COURSE DESCRIPTION",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = course.description,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f),
                                        textAlign = TextAlign.Justify
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text(
                                        text = "PREREQUISITES",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = course.prerequisites,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


// ==================== CALCULATOR TAB ====================
@Composable
fun CalculatorTab(viewModel: IskolarZViewModel) {
    val drafts by viewModel.calculatorDrafts.collectAsStateWithLifecycle()
    
    var isEditDraftDialogShown by remember { mutableStateOf(false) }
    var activeDraftToEdit by remember { mutableStateOf<CalculatorDraftEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "GRADES CALCULATOR SCREEN",
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp
                )
                Text(text = "Weighted Scratchpad", fontWeight = FontWeight.Bold, fontSize = 22.sp)
            }
            Button(
                onClick = {
                    activeDraftToEdit = null
                    isEditDraftDialogShown = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary, contentColor = Color.Black)
            ) {
                Text("+ New Draft", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "This is a local weighted calculator tool helping you plan assessments. The table system complies with raw scale grading definitions defined in UPM syllabus guides.",
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (drafts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Calculate, contentDescription = "Calc", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No calculation drafts saved yet", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Create templates of courses containing percentages of Quizzes, Exams, and Assignments to model targets easily.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f), textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            activeDraftToEdit = null
                            isEditDraftDialogShown = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary, contentColor = Color.Black)
                    ) {
                        Text("Add New Scratchpad Template")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(drafts) { draft ->
                    val requirements = remember(draft.requirementsJson) {
                        viewModel.parseDraftRequirements(draft.requirementsJson)
                    }
                    val totalWeight = requirements.sumOf { it.weightPercentage }
                    val finalScore = if (totalWeight > 0) {
                        (requirements.sumOf { it.scorePercentage } / totalWeight) * 100.0
                    } else 0.00
                    val upGrade = viewModel.getCalculatorUpGrade(finalScore)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                activeDraftToEdit = draft
                                isEditDraftDialogShown = true
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth()
                        ) {
                            // Header Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = "Draft Icon",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = draft.draftName.uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 15.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                
                                // Delete action
                                IconButton(
                                    onClick = { viewModel.deleteCalculatorDraft(draft) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Draft",
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                            Spacer(modifier = Modifier.height(10.dp))

                            // List of all component requirements
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                requirements.forEach { req ->
                                    val reqScore = req.scorePercentage
                                    val reqWeight = req.weightPercentage
                                    val percentage = if (reqWeight > 0) (reqScore / reqWeight) * 100.0 else 0.0
                                    
                                    val scoreText = if (reqScore % 1.0 == 0.0) reqScore.toInt().toString() else String.format(Locale.US, "%.1f", reqScore)
                                    val weightText = if (reqWeight % 1.0 == 0.0) reqWeight.toInt().toString() else String.format(Locale.US, "%.1f", reqWeight)
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = req.name,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                            modifier = Modifier.weight(1.2f)
                                        )
                                        
                                        // Visual bar
                                        Box(
                                            modifier = Modifier
                                                .weight(2f)
                                                .height(6.dp)
                                                .background(
                                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                                    RoundedCornerShape(3.dp)
                                                )
                                        ) {
                                            val fraction = if (reqWeight > 0) (reqScore / reqWeight).toFloat().coerceIn(0f, 1f) else 0f
                                            if (fraction > 0f) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxHeight()
                                                        .fillMaxWidth(fraction)
                                                        .background(
                                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                                            RoundedCornerShape(3.dp)
                                                        )
                                                )
                                            }
                                        }
                                        
                                        Spacer(modifier = Modifier.width(10.dp))
                                        
                                        Text(
                                            text = "$scoreText / $weightText",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(1f),
                                            textAlign = TextAlign.End,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        
                                        Text(
                                            text = "(${String.format(Locale.US, "%.0f", percentage)}%)",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.width(48.dp),
                                            textAlign = TextAlign.End
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                            Spacer(modifier = Modifier.height(10.dp))

                            // Overall computations output row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Weighted Score: ",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = "${String.format(Locale.US, "%.2f", finalScore)}%",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Column(
                                    horizontalAlignment = Alignment.End,
                                    modifier = Modifier
                                        .background(
                                            MaterialTheme.colorScheme.secondary,
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = upGrade,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.Black
                                    )
                                    Text(
                                        text = "EST. GRADE",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (isEditDraftDialogShown) {
        CalculatorDraftEditDialog(
            draft = activeDraftToEdit,
            viewModel = viewModel,
            onDismiss = { isEditDraftDialogShown = false }
        )
    }
}

// ==================== CALCULATOR DRAFT EDIT DIALOG ====================
data class RequirementRowState(
    val name: String,
    val weightInput: String,
    val scoreInput: String
)

@Composable
fun CalculatorDraftEditDialog(
    draft: CalculatorDraftEntity?,
    viewModel: IskolarZViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var draftName by remember { mutableStateOf(draft?.draftName ?: "") }
    
    // Default requirements if creating new
    val rawRequirements = remember {
        if (draft != null) {
            viewModel.parseDraftRequirements(draft.requirementsJson)
        } else {
            listOf(
                CalculatorRequirement("Quizzes", 20.0, 18.0),
                CalculatorRequirement("Exams", 80.0, 68.0)
            )
        }
    }

    val rowStates = remember {
        mutableStateListOf<RequirementRowState>().apply {
            addAll(
                rawRequirements.map { req ->
                    RequirementRowState(
                        name = req.name,
                        weightInput = if (req.weightPercentage == 0.0) "" else {
                            if (req.weightPercentage % 1.0 == 0.0) req.weightPercentage.toInt().toString() else req.weightPercentage.toString()
                        },
                        scoreInput = if (req.scorePercentage == 0.0) "" else {
                            if (req.scorePercentage % 1.0 == 0.0) req.scorePercentage.toInt().toString() else req.scorePercentage.toString()
                        }
                    )
                }
            )
        }
    }

    fun updateWeight(index: Int, newWeightStr: String) {
        val row = rowStates[index]
        val parsedWeight = newWeightStr.toDoubleOrNull() ?: 0.0
        if (parsedWeight > 100.0) {
            val cappedWeightStr = "100"
            rowStates[index] = row.copy(weightInput = cappedWeightStr)
            
            val currentScore = row.scoreInput.toDoubleOrNull() ?: 0.0
            if (currentScore > 100.0) {
                rowStates[index] = rowStates[index].copy(scoreInput = "100")
            }
            return
        }
        
        rowStates[index] = row.copy(weightInput = newWeightStr)
        
        val parsedScore = row.scoreInput.toDoubleOrNull() ?: 0.0
        if (parsedScore > parsedWeight) {
            val cappedScoreStr = if (parsedWeight % 1.0 == 0.0) parsedWeight.toInt().toString() else parsedWeight.toString()
            rowStates[index] = rowStates[index].copy(scoreInput = if (parsedWeight == 0.0) "" else cappedScoreStr)
        }
    }

    fun updateScore(index: Int, newScoreStr: String) {
        val row = rowStates[index]
        val parsedWeight = row.weightInput.toDoubleOrNull() ?: 0.0
        val parsedScore = newScoreStr.toDoubleOrNull() ?: 0.0
        
        if (newScoreStr.isEmpty()) {
            rowStates[index] = row.copy(scoreInput = "")
        } else if (parsedScore <= parsedWeight) {
            rowStates[index] = row.copy(scoreInput = newScoreStr)
        } else {
            val cappedScoreStr = if (parsedWeight % 1.0 == 0.0) parsedWeight.toInt().toString() else parsedWeight.toString()
            rowStates[index] = row.copy(scoreInput = cappedScoreStr)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (draft == null) "NEW DRAFT" else "EDIT DRAFT",
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = draftName,
                    onValueChange = { draftName = it },
                    label = { Text("Draft Name (e.g. CMSC 123)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("ASSESSMENTS REQUIREMENTS", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.secondary)
                    TextButton(
                        onClick = {
                            rowStates.add(
                                RequirementRowState(
                                    name = "Requirement ${rowStates.size + 1}",
                                    weightInput = "10",
                                    scoreInput = "8.5"
                                )
                            )
                        }
                    ) {
                        Text("+ Add Row", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Scrollable weight requirements lists
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(rowStates.size) { index ->
                        val item = rowStates[index]
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = item.name,
                                        onValueChange = {
                                            rowStates[index] = item.copy(name = it)
                                        },
                                        placeholder = { Text("Name") },
                                        singleLine = true,
                                        modifier = Modifier.weight(1.5f),
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.secondary)
                                    )
                                    
                                    Spacer(modifier = Modifier.width(8.dp))

                                    IconButton(
                                        onClick = {
                                            if (rowStates.size > 2) {
                                                rowStates.removeAt(index)
                                            } else {
                                                Toast.makeText(context, "At least 2 requirements are required by default.", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Delete Row", tint = MaterialTheme.colorScheme.error)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = item.weightInput,
                                        onValueChange = {
                                            updateWeight(index, it)
                                        },
                                        label = { Text("Weight") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    OutlinedTextField(
                                        value = item.scoreInput,
                                        onValueChange = {
                                            updateScore(index, it)
                                        },
                                        label = { Text("Your Score") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Calculations Outputs
                val totalWeight = rowStates.sumOf { it.weightInput.toDoubleOrNull() ?: 0.0 }
                val calculatedAverage = if (totalWeight > 0) {
                    (rowStates.sumOf { it.scoreInput.toDoubleOrNull() ?: 0.0 } / totalWeight) * 100.0
                } else 0.00
                val estimatedGrade = viewModel.getCalculatorUpGrade(calculatedAverage)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Weight Sum:", fontSize = 12.sp)
                            Text(
                                text = "${totalWeight.toInt()}% / 100%",
                                fontWeight = FontWeight.Bold,
                                color = if (totalWeight == 100.0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Weighted Average:", fontSize = 12.sp)
                            Text("${String.format(Locale.US, "%.2f", calculatedAverage)}%", fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Equivalent UP Grade:", fontSize = 12.sp)
                            Text(estimatedGrade, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            if (draftName.trim().isBlank()) {
                                Toast.makeText(context, "Draft name is required", Toast.LENGTH_SHORT).show()
                            } else {
                                val totalWeight = rowStates.sumOf { it.weightInput.toDoubleOrNull() ?: 0.0 }
                                if (totalWeight != 100.0) {
                                    val currentStr = if (totalWeight % 1.0 == 0.0) totalWeight.toInt().toString() else String.format(Locale.US, "%.1f", totalWeight)
                                    Toast.makeText(context, "Total weight sum must be exactly 100%. Current: ${currentStr}%", Toast.LENGTH_SHORT).show()
                                } else {
                                    val requirementRows = rowStates.map {
                                        val weight = it.weightInput.toDoubleOrNull() ?: 0.0
                                        val score = it.scoreInput.toDoubleOrNull() ?: 0.0
                                        CalculatorRequirement(
                                            name = it.name,
                                            weightPercentage = weight,
                                            scorePercentage = score
                                        )
                                    }
                                    viewModel.saveCalculatorDraft(id = draft?.id ?: 0, name = draftName, requirements = requirementRows)
                                    onDismiss()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary, contentColor = Color.Black),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save Draft", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
