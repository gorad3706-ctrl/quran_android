package com.quran.mobile.education.data

import com.quran.mobile.education.domain.ChapterStudyState
import com.quran.mobile.education.domain.SubjectGradeInput
import java.time.LocalDate

/**
 * Data boundary for remotely administered, versioned Senegalese curricula. It prevents UI and
 * algorithm code from embedding subject lists, coefficients, or exam rules.
 */
interface CurriculumRepository {
  suspend fun subjectCoefficients(curriculumVersion: String, programmeId: String): Map<String, Int>
  suspend fun examDate(studentId: String): LocalDate
}

interface StudentLearningRepository {
  suspend fun chapterStates(studentId: String): List<ChapterStudyState>
  suspend fun semesterGrades(studentId: String, semester: Int): List<SubjectGradeInput>
}
