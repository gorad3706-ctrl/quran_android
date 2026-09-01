package com.quran.mobile.education.domain

import com.google.common.truth.Truth.assertThat
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import org.junit.Test

class PedagogicalAlgorithmsTest {
  private val algorithms = PedagogicalAlgorithms(Clock.fixed(Instant.parse("2026-09-01T00:00:00Z"), ZoneOffset.UTC))

  @Test fun `study priorities favor the less mastered high coefficient chapter`() {
    val priorities = algorithms.rankStudyPriorities(
      listOf(
        ChapterStudyState("math", "mathematics", 0.2, 5.0, LocalDate.parse("2026-08-30")),
        ChapterStudyState("french", "french", 0.8, 5.0, LocalDate.parse("2026-08-30"))
      ),
      mapOf("mathematics" to 5, "french" to 2),
      LocalDate.parse("2026-10-01")
    )
    assertThat(priorities.map(StudyPriority::chapterId)).containsExactly("math", "french").inOrder()
  }

  @Test fun `spaced repetition resets failures and applies minimum ease`() {
    val failed = algorithms.updateSpacedRepetition(SpacedRepetitionState(20, 1.3, 4), 1)
    assertThat(failed).isEqualTo(SpacedRepetitionState(1, 1.3, 0))
    val passed = algorithms.updateSpacedRepetition(SpacedRepetitionState(6, 2.5, 1), 5)
    assertThat(passed.intervalDays).isEqualTo(15)
    assertThat(passed.repetitions).isEqualTo(2)
  }

  @Test fun `semester average weights composition twice and subjects by coefficient`() {
    val average = algorithms.calculateSemesterAverage(listOf(
      SubjectGradeInput("math", 3, listOf(10.0, 14.0), 15.0),
      SubjectGradeInput("french", 1, listOf(12.0), 9.0)
    ))
    assertThat(average.subjectAverages[0].value).isWithin(0.001).of(14.0)
    assertThat(average.value).isWithin(0.001).of(13.0)
  }

  @Test fun `oral recommendations return the two largest point gains`() {
    val recommendations = algorithms.recommendOralSubjects(listOf(
      SubjectGradeInput("physics", 6, emptyList(), 8.0),
      SubjectGradeInput("math", 5, emptyList(), 5.0),
      SubjectGradeInput("french", 2, emptyList(), 3.0)
    ))
    assertThat(recommendations.map(OralSubjectRecommendation::subjectId)).containsExactly("math", "physics").inOrder()
  }
}
