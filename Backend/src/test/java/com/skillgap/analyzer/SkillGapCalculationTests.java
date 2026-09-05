package com.skillgap.analyzer;

import com.skillgap.analyzer.dto.*;
import com.skillgap.analyzer.service.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class SkillGapCalculationTests {
    private final SkillGapService calculator = new SkillGapService(null, null, null, null);
    private final RecommendationService recommendations = new RecommendationService(null, null, null, null, calculator);
    private final JobResponse job = new JobResponse(1L, "ABC", "Java Developer", "Chennai");
    private JobSkillResponse required(long id, int level, boolean mandatory) {
        return new JobSkillResponse(id, id, "Skill " + id, level, mandatory);
    }

    @Test void weightedScoreGivesPartialCredit() {
        var result = calculator.calculate(job, Map.of(1L, 2, 2L, 3), List.of(required(1, 4, true), required(2, 3, false)));
        assertThat(result.overallMatchPercent()).isEqualByComparingTo("66.67");
        assertThat(result.skills().getFirst().matchPercent()).isEqualByComparingTo("50");
        assertThat(result.skills().getFirst().gap()).isEqualTo(2);
        assertThat(result.mandatorySkillsMet()).isFalse();
    }
    @Test void followsFormulaWhenSampleOverallIsInconsistent() {
        var result = calculator.calculate(job, Map.of(1L, 3, 2L, 4), List.of(required(1, 4, true), required(2, 3, true)));
        assertThat(result.overallMatchPercent()).isEqualByComparingTo("87.50");
    }
    @Test void capsExcessProficiencyAndIgnoresUnrelatedSkills() {
        var result = calculator.calculate(job, Map.of(1L, 5, 99L, 5), List.of(required(1, 2, true)));
        assertThat(result.overallMatchPercent()).isEqualByComparingTo("100");
        assertThat(result.skills().getFirst().gap()).isZero();
        assertThat(result.skills().getFirst().status()).isEqualTo("MATCHED");
        assertThat(recommendations.generate(result)).isEmpty();
    }
    @Test void absentSkillsAreLevelZeroAndPrioritiesMatchSpecification() {
        var result = calculator.calculate(job, Map.of(),
                List.of(required(1, 5, false), required(2, 1, true), required(3, 2, true)));
        assertThat(result.overallMatchPercent()).isEqualByComparingTo("0");
        assertThat(result.skills()).allSatisfy(s -> assertThat(s.currentLevel()).isZero());
        var suggestions = recommendations.generate(result);
        assertThat(suggestions).extracting(RecommendationResponse::priority).containsExactly(1, 2, 3);
        assertThat(suggestions).extracting(RecommendationResponse::skillId).containsExactly(3L, 2L, 1L);
    }
    @Test void optionalGapDoesNotFailMandatoryRequirement() {
        var result = calculator.calculate(job, Map.of(1L, 4), List.of(required(1, 4, true), required(2, 2, false)));
        assertThat(result.mandatorySkillsMet()).isTrue();
        assertThat(result.overallMatchPercent()).isEqualByComparingTo("66.67");
    }
    @Test void emptyRequirementsHaveExplicitZeroScoreWithoutDivisionByZero() {
        var result = calculator.calculate(job, Map.of(), List.of());
        assertThat(result.evaluable()).isFalse();
        assertThat(result.mandatorySkillsMet()).isFalse();
        assertThat(result.overallMatchPercent()).isEqualByComparingTo("0");
        assertThat(result.skills()).isEmpty();
    }
}
