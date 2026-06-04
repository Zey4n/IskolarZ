package com.example.data

import kotlinx.coroutines.flow.Flow

class IskolarZRepository(private val db: IskolarZDatabase) {
    val userProfile: Flow<UserProfileEntity?> = db.userProfileDao.getUserProfileFlow()
    val allGrades: Flow<List<GradeRecordEntity>> = db.gradeRecordDao.getAllGradesFlow()
    val allDrafts: Flow<List<CalculatorDraftEntity>> = db.calculatorDraftDao.getAllDraftsFlow()

    suspend fun getUserProfileDirect(): UserProfileEntity? {
        return db.userProfileDao.getUserProfile()
    }

    suspend fun getGradesCountDirect(): Int {
        return db.gradeRecordDao.getGradesCount()
    }

    suspend fun saveUserProfile(profile: UserProfileEntity) {
        db.userProfileDao.insertOrUpdateProfile(profile)
    }

    suspend fun insertGrade(record: GradeRecordEntity) {
        db.gradeRecordDao.insertGrade(record)
    }

    suspend fun updateGrade(record: GradeRecordEntity) {
        db.gradeRecordDao.updateGrade(record)
    }

    suspend fun deleteGradeById(id: Int) {
        db.gradeRecordDao.deleteGradeById(id)
    }

    suspend fun deleteGradeByCode(code: String) {
        db.gradeRecordDao.deleteGradeByCode(code)
    }

    suspend fun insertDraft(draft: CalculatorDraftEntity) {
        db.calculatorDraftDao.insertDraft(draft)
    }

    suspend fun deleteDraftById(id: Int) {
        db.calculatorDraftDao.deleteDraftById(id)
    }
}
