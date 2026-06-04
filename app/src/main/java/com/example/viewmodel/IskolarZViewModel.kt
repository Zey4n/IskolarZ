package com.example.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

data class CalculatorRequirement(
    val name: String,
    val weightPercentage: Double,
    val scorePercentage: Double
)

class IskolarZViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: IskolarZRepository

    // GWA visibility state - shown by default on first launch, hidden on subsequent launch if records exist
    var isGwaVisible by mutableStateOf(true)

    fun toggleGwaVisibility() {
        isGwaVisible = !isGwaVisible
    }

    init {
        val db = IskolarZDatabase.getDatabase(application)
        repository = IskolarZRepository(db)
        
        viewModelScope.launch {
            val profile = repository.getUserProfileDirect()
            val count = repository.getGradesCountDirect()
            // If the user already has profile and grade records at app launch, hide GWA by default
            if (profile != null && count > 0) {
                isGwaVisible = false
            } else {
                isGwaVisible = true
            }
        }
    }

    // Tab state
    var currentTab by mutableStateOf(0)

    // Setup state
    val userProfile: StateFlow<UserProfileEntity?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Grade Records State
    val gradeRecords: StateFlow<List<GradeRecordEntity>> = repository.allGrades
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Calculator Drafts State
    val calculatorDrafts: StateFlow<List<CalculatorDraftEntity>> = repository.allDrafts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Max year to generate semesters for (can be incremented by the user)
    var maxYearEnd by mutableStateOf(2029)

    // Active track and profile details (for edit modal on dashboard)
    fun profileNickname(profile: UserProfileEntity?) = profile?.nickname ?: ""
    fun profileTrack(profile: UserProfileEntity?) = profile?.track ?: "Health Informatics"
    fun profileStartYear(profile: UserProfileEntity?) = profile?.startYear ?: "AY 2022-2023 First Semester"

    // Profile initialization / edit
    fun saveProfile(nickname: String, track: String, startYear: String) {
        viewModelScope.launch {
            repository.saveUserProfile(
                UserProfileEntity(
                    nickname = nickname.trim(),
                    track = track,
                    startYear = startYear
                )
            )
        }
    }

    // Extend selectable academic years beyond 2028-2029
    fun incrementMaxYear() {
        maxYearEnd += 1
    }

    fun parseSemesterToVal(semStr: String): Double {
        val cleaned = semStr.replace(" - START", "").trim()
        if (cleaned == "Credited Units" || cleaned.isEmpty()) return 0.0
        
        val match = Regex("""AY (\d{4})-\d{4}""").find(cleaned)
        val year = match?.groupValues?.get(1)?.toIntOrNull() ?: return 0.0
        
        val semVal = when {
            cleaned.contains("First Semester") -> 0.1
            cleaned.contains("Second Semester") -> 0.2
            cleaned.contains("Midyear") -> 0.3
            else -> 0.0
        }
        return year + semVal
    }

    // Get list of available academic years depending on setting
    fun getAvailableSemesters(profile: UserProfileEntity?): List<String> {
        val startYearAndSem = profile?.startYear ?: ""
        val startVal = parseSemesterToVal(startYearAndSem)
        
        val matchStart = Regex("""AY (\d{4})-\d{4}""").find(startYearAndSem)
        val startYear = matchStart?.groupValues?.get(1)?.toIntOrNull() ?: 2024
        
        val records = gradeRecords.value
        var maxRecVal = 0.0
        records.forEach { record ->
            val v = parseSemesterToVal(record.academicYear)
            if (v > maxRecVal) {
                maxRecVal = v
            }
        }
        
        val baseVal = maxOf(startVal, maxRecVal)
        val baseYear = if (baseVal > 0.0) baseVal.toInt() else startYear
        val finalMaxYearEnd = baseYear + 1
        
        val list = mutableListOf<String>()
        list.add("Credited Units")
        for (year in startYear..finalMaxYearEnd) {
            val ay = "AY $year-${year + 1}"
            listOf("First Semester", "Second Semester", "Midyear").forEach { sem ->
                val option = "$ay $sem"
                val optionVal = parseSemesterToVal(option)
                if (optionVal >= startVal) {
                    if (option == startYearAndSem.replace(" - START", "")) {
                        list.add("$option - START")
                    } else {
                        list.add(option)
                    }
                }
            }
        }
        return list
    }

    // Insert Grade
    fun addGrade(
        courseCode: String,
        courseName: String,
        units: Double,
        grade: String,
        academicYear: String,
        isPEorNSTP: Boolean,
        isGE: Boolean = false
    ) {
        viewModelScope.launch {
            repository.insertGrade(
                GradeRecordEntity(
                    courseCode = courseCode.trim(),
                    courseName = courseName.trim(),
                    units = units,
                    grade = grade,
                    academicYear = academicYear.replace(" - START", ""),
                    isPEorNSTP = isPEorNSTP,
                    isGE = isGE
                )
            )
        }
    }

    // Edit Grade (Update)
    fun updateGradeRecord(entity: GradeRecordEntity) {
        viewModelScope.launch {
            repository.updateGrade(entity)
        }
    }

    // Delete Grade
    fun deleteGrade(entity: GradeRecordEntity) {
        viewModelScope.launch {
            repository.deleteGradeById(entity.id)
        }
    }

    fun deleteGradeByCode(code: String) {
        viewModelScope.launch {
            repository.deleteGradeByCode(code)
        }
    }

    // Helpers to support grades logic
    fun isPassedGrade(grade: String): Boolean {
        if (grade == "DRP") return false
        val gDouble = grade.toDoubleOrNull() ?: return true // PASSED/SATISFACTORY are non-numeric but passed
        return gDouble in 1.00..3.00
    }

    fun isFailedGrade(grade: String): Boolean {
        return grade == "5.00" || grade == "5.0"
    }

    // GWA & Units computations
    val gwaStatsState: Flow<GwaStats> = gradeRecords.map { grades ->
        var totalWeightedGrades = 0.0
        var totalAcademicUnitsForGwa = 0.0
        var academicUnitsEarned = 0.0
        var failedAcademicUnits = 0.0

        grades.forEach { gradeRecord ->
            if (!gradeRecord.isPEorNSTP) {
                val gradeValue = gradeRecord.grade.toDoubleOrNull()
                if (gradeValue != null) {
                    // It is a valid numeric grade in UP
                    if (gradeValue in 1.00..5.00) {
                        totalWeightedGrades += (gradeValue * gradeRecord.units)
                        totalAcademicUnitsForGwa += gradeRecord.units
                    }
                    if (gradeValue in 1.00..3.00) {
                        academicUnitsEarned += gradeRecord.units
                    } else if (gradeValue == 5.0) {
                        failedAcademicUnits += gradeRecord.units
                    }
                }
            }
        }

        val gwa = if (totalAcademicUnitsForGwa > 0) totalWeightedGrades / totalAcademicUnitsForGwa else 0.00
        val remainingUnits = maxOf(0.0, 158.0 - academicUnitsEarned)

        GwaStats(
            overallGwa = gwa,
            totalAcademicUnitsForGwa = totalAcademicUnitsForGwa,
            academicUnitsEarned = academicUnitsEarned,
            failedUnits = failedAcademicUnits,
            remainingUnits = remainingUnits
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GwaStats(0.0, 0.0, 0.0, 0.0, 158.0))

    // Standings mappings
    fun getLatinHonor(gwa: Double, totalEarned: Double): String {
        if (totalEarned == 0.0 || gwa == 0.0) return ""
        return when {
            gwa in 1.00..1.25 -> "Summa Cum Laude"
            gwa > 1.25 && gwa <= 1.45 -> "Magna Cum Laude"
            gwa > 1.45 && gwa <= 1.75 -> "Cum Laude"
            else -> ""
        }
    }

    fun getSemestralStanding(gwa: Double, semesterName: String): String {
        val isRegular = semesterName.contains("First Semester") || semesterName.contains("Second Semester")
        if (!isRegular || gwa == 0.0) return ""
        return when {
            gwa in 1.00..1.45 -> "University Scholar"
            gwa > 1.45 && gwa <= 1.75 -> "College Scholar"
            gwa > 1.75 && gwa <= 2.00 -> "Honor Roll"
            else -> ""
        }
    }

    // Calculator Actions
    fun saveCalculatorDraft(id: Int = 0, name: String, requirements: List<CalculatorRequirement>) {
        val jsonArray = JSONArray()
        requirements.forEach {
            val obj = JSONObject().apply {
                put("name", it.name)
                put("weight", it.weightPercentage)
                put("score", it.scorePercentage)
            }
            jsonArray.put(obj)
        }

        viewModelScope.launch {
            repository.insertDraft(
                CalculatorDraftEntity(
                    id = id,
                    draftName = name.trim(),
                    requirementsJson = jsonArray.toString()
                )
            )
        }
    }

    fun deleteCalculatorDraft(draft: CalculatorDraftEntity) {
        viewModelScope.launch {
            repository.deleteDraftById(draft.id)
        }
    }

    fun parseDraftRequirements(json: String): List<CalculatorRequirement> {
        val list = mutableListOf<CalculatorRequirement>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    CalculatorRequirement(
                        name = obj.getString("name"),
                        weightPercentage = obj.getDouble("weight"),
                        scorePercentage = obj.getDouble("score")
                    )
                )
            }
        } catch (e: Exception) {
            // fallback
            list.add(CalculatorRequirement("Quizzes", 20.0, 18.0))
            list.add(CalculatorRequirement("Exams", 80.0, 68.0))
        }
        return list
    }

    fun getCalculatorUpGrade(score: Double): String {
        return when {
            score >= 93.0 -> "1.00"
            score >= 90.0 -> "1.25"
            score >= 87.0 -> "1.50"
            score >= 84.0 -> "1.75"
            score >= 80.0 -> "2.00"
            score >= 75.0 -> "2.25"
            score >= 70.0 -> "2.50"
            score >= 65.0 -> "2.75"
            score >= 60.0 -> "3.00"
            score >= 55.0 -> "4.00"
            else -> "5.00"
        }
    }
}

data class GwaStats(
    val overallGwa: Double,
    val totalAcademicUnitsForGwa: Double,
    val academicUnitsEarned: Double,
    val failedUnits: Double,
    val remainingUnits: Double
)
