package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// 1. User Profile Entity
@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val nickname: String,
    val track: String, // "Health Informatics" or "Statistical Computing"
    val startYear: String
)

// 2. Grade Record Entity
@Entity(tableName = "grade_records")
data class GradeRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val courseCode: String,
    val courseName: String,
    val units: Double,
    val grade: String,         // "1.00", "1.25", ..., "DRP"
    val academicYear: String,  // e.g. "AY 2022-2023 First Semester", "Credited Units"
    val isPEorNSTP: Boolean,
    val isGE: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

// 3. Calculator Draft Entity
@Entity(tableName = "calculator_drafts")
data class CalculatorDraftEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val draftName: String,
    val requirementsJson: String, // JSON structure serializing requirement name, weight%, score%
    val timestamp: Long = System.currentTimeMillis()
)

// DAOs
@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfileFlow(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfile(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfileEntity)
}

@Dao
interface GradeRecordDao {
    @Query("SELECT * FROM grade_records ORDER BY timestamp DESC")
    fun getAllGradesFlow(): Flow<List<GradeRecordEntity>>

    @Query("SELECT COUNT(*) FROM grade_records")
    suspend fun getGradesCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrade(gradeRecord: GradeRecordEntity)

    @Update
    suspend fun updateGrade(gradeRecord: GradeRecordEntity)

    @Query("DELETE FROM grade_records WHERE id = :id")
    suspend fun deleteGradeById(id: Int)

    @Query("DELETE FROM grade_records WHERE courseCode = :code")
    suspend fun deleteGradeByCode(code: String)
}

@Dao
interface CalculatorDraftDao {
    @Query("SELECT * FROM calculator_drafts ORDER BY timestamp DESC")
    fun getAllDraftsFlow(): Flow<List<CalculatorDraftEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDraft(draft: CalculatorDraftEntity)

    @Query("DELETE FROM calculator_drafts WHERE id = :id")
    suspend fun deleteDraftById(id: Int)
}

@Database(
    entities = [
        UserProfileEntity::class,
        GradeRecordEntity::class,
        CalculatorDraftEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class IskolarZDatabase : RoomDatabase() {
    abstract val userProfileDao: UserProfileDao
    abstract val gradeRecordDao: GradeRecordDao
    abstract val calculatorDraftDao: CalculatorDraftDao

    companion object {
        @Volatile
        private var INSTANCE: IskolarZDatabase? = null

        fun getDatabase(context: Context): IskolarZDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    IskolarZDatabase::class.java,
                    "iskolarz_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
