package com.quran.mobile.education.domain

import java.time.Clock
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.exp
import kotlin.math.log2

data class ChapterStudyState(
  val chapterId: String,
  val subjectId: String,
  val masteryScore: Double,
  val stabilityDays: Double,
  val lastStudiedOn: LocalDate?
)

data class StudyPriority(
  val chapterId: String,
  val score: Double
)

data class SpacedRepetitionState(
  val intervalDays: Int,
  val easeFactor: Double,
  val repetitions: Int
)

data class SubjectGradeInput(
  val subjectId: String,
  val coefficient: Int,
  val homeworkScores: List<Double>,
  val compositionScore: Double
)

data class SubjectAverage(val subjectId: String, val value: Double)

data class SemesterAverage(
  val subjectAverages: List<SubjectAverage>,
  val value: Double
)

data class OralSubjectRecommendation(
  val subjectId: String,
  val potentialPointGain: Double
)

/**
 * Stateless implementation of the pedagogical formulas. Curriculum coefficients and student
 * records are inputs from repositories; this layer deliberately contains no exam-specific data.
 */
class PedagogicalAlgorithms(private val clock: Clock = Clock.systemUTC()) {
  fun rankStudyPriorities(
    chapters: List<ChapterStudyState>,
    subjectCoefficients: Map<String, Int>,
    examDate: LocalDate
  ): List<StudyPriority> {
    val today = LocalDate.now(clock)
    val daysUntilExam = ChronoUnit.DAYS.between(today, examDate).coerceAtLeast(0)
    val deadlineWeight = 1.0 / log2(daysUntilExam + 2.0)

    return chapters.map { chapter ->
      require(chapter.masteryScore in 0.0..1.0) { "masteryScore must be between 0 and 1" }
      require(chapter.stabilityDays > 0) { "stabilityDays must be positive" }
      val coefficient = requireNotNull(subjectCoefficients[chapter.subjectId]) {
        "Missing coefficient for subject ${chapter.subjectId}"
      }
      require(coefficient > 0) { "Subject coefficient must be positive" }
      val elapsedDays = chapter.lastStudiedOn?.let { ChronoUnit.DAYS.between(it, today).coerceAtLeast(0) } ?: 0
      val score = coefficient * (1 - chapter.masteryScore) *
        exp(elapsedDays / chapter.stabilityDays) * deadlineWeight
      StudyPriority(chapter.chapterId, score)
    }.sortedByDescending(StudyPriority::score)
  }

  fun updateSpacedRepetition(previous: SpacedRepetitionState, quality: Int): SpacedRepetitionState {
    require(quality in 1..5) { "quality must be between 1 and 5" }
    val ease = maxOf(1.3, previous.easeFactor + 0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02))
    if (quality < 3) return SpacedRepetitionState(intervalDays = 1, easeFactor = ease, repetitions = 0)

    val repetitions = previous.repetitions + 1
    val interval = when (repetitions) {
      1 -> 6
      else -> (previous.intervalDays * ease).toInt().coerceAtLeast(1)
    }
    return SpacedRepetitionState(interval, ease, repetitions)
  }

  fun calculateSemesterAverage(subjects: List<SubjectGradeInput>): SemesterAverage {
    require(subjects.isNotEmpty()) { "At least one subject is required" }
    val averages = subjects.map { subject ->
      require(subject.coefficient > 0) { "Subject coefficient must be positive" }
      require(subject.homeworkScores.isNotEmpty()) { "At least one homework score is required" }
      validateScore(subject.compositionScore)
      subject.homeworkScores.forEach(::validateScore)
      val homeworkAverage = subject.homeworkScores.average()
      SubjectAverage(subject.subjectId, (homeworkAverage + subject.compositionScore * 2) / 3)
    }
    val coefficients = subjects.sumOf(SubjectGradeInput::coefficient)
    val weighted = averages.zip(subjects).sumOf { (average, subject) -> average.value * subject.coefficient }
    return SemesterAverage(averages, weighted / coefficients)
  }

  fun recommendOralSubjects(subjectScores: List<SubjectGradeInput>): List<OralSubjectRecommendation> =
    subjectScores.map { subject ->
      require(subject.coefficient > 0) { "Subject coefficient must be positive" }
      validateScore(subject.compositionScore)
      OralSubjectRecommendation(subject.subjectId, subject.coefficient * (20 - subject.compositionScore))
    }.sortedByDescending(OralSubjectRecommendation::potentialPointGain).take(2)

  private fun validateScore(score: Double) = require(score in 0.0..20.0) { "Score must be between 0 and 20" }
}
