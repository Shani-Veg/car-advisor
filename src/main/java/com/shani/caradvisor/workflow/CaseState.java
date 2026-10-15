package com.shani.caradvisor.workflow;

import com.shani.caradvisor.data.ReferenceData;
import com.shani.caradvisor.engine.CarChoice;
import com.shani.caradvisor.engine.Employee;
import com.shani.caradvisor.engine.Occupation;
import com.shani.caradvisor.engine.Vehicle;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A case with its facts and questions, and the single place that decides what is still missing.
 * The chat, the form and the rep screen all read the case through here, so they agree.
 */
final class CaseState {

    /** Details the system may assume and then asks the employee to confirm (or reads from a payslip). */
    static final List<FactKey> REVIEWABLE = List.of(
            FactKey.COMMISSIONS, FactKey.BONUS, FactKey.PENSION_PERCENT, FactKey.STUDY_FUND_PERCENT,
            FactKey.CREDIT_POINTS, FactKey.CURRENT_PARTICIPATION);

    private static final List<FactKey> ALWAYS_REQUIRED = List.of(
            FactKey.OCCUPATION, FactKey.GROUP, FactKey.BASE_SALARY, FactKey.COMMISSIONS, FactKey.BONUS,
            FactKey.PENSION_PERCENT, FactKey.STUDY_FUND_PERCENT, FactKey.CREDIT_POINTS,
            FactKey.CHILDREN_UP_TO_23, FactKey.CURRENT_CAR);

    private final CaseEntity caseEntity;
    private final Map<FactKey, FactEntity> facts;
    private final List<RepQuestionEntity> questions;
    private final ReferenceData data;

    CaseState(CaseEntity caseEntity, Map<FactKey, FactEntity> facts, List<RepQuestionEntity> questions,
              ReferenceData data) {
        this.caseEntity = caseEntity;
        this.facts = facts;
        this.questions = questions;
        this.data = data;
    }

    CaseEntity caseEntity() {
        return caseEntity;
    }

    Optional<FactEntity> fact(FactKey key) {
        return Optional.ofNullable(facts.get(key));
    }

    List<RepQuestionEntity> questions() {
        return questions;
    }

    /** Everything needed to compute today's net. */
    List<FactKey> missingForToday() {
        List<FactKey> required = new ArrayList<>(ALWAYS_REQUIRED);
        if (hasCarToday()) {
            required.add(FactKey.CURRENT_PARTICIPATION);
        }
        return required.stream().filter(k -> !facts.containsKey(k)).toList();
    }

    /** Everything needed to compare today with the chosen alternative. */
    List<FactKey> missingForComparison() {
        List<FactKey> missing = new ArrayList<>(missingForToday());
        if (caseEntity.alternative() == CaseEntity.Alternative.CAR && !facts.containsKey(FactKey.ALTERNATIVE_PARTICIPATION)) {
            missing.add(FactKey.ALTERNATIVE_PARTICIPATION);
        }
        return missing;
    }

    /** Reviewable details that are still the system's assumption, not the employee's word. */
    List<FactKey> assumedForReview() {
        return REVIEWABLE.stream()
                .filter(k -> facts.containsKey(k) && facts.get(k).status() == FactEntity.Status.DEFAULT)
                .toList();
    }

    /** The four questions are answered and the assumed details still wait for the employee's yes, payslip or "later". */
    boolean reviewPending() {
        return missingForToday().isEmpty() && !caseEntity.assumptionsReviewed() && !assumedForReview().isEmpty();
    }

    /** No rep question, and no offer to ask one, for a car the employee is known not to be allowed. */
    boolean mayTake(Vehicle vehicle) {
        if (vehicle.isFamilyTrack()) {
            return fact(FactKey.CHILDREN_UP_TO_23)
                    .map(f -> Integer.parseInt(f.value()) >= data.rules().familyTrack().minChildren())
                    .orElse(true);
        }
        return fact(FactKey.GROUP)
                .map(f -> data.rules().rankOf(vehicle.group()) <= data.rules().rankOf(f.value()))
                .orElse(true);
    }

    /** The compared car's participation is still the catalog's estimate and nobody has asked the rep about it yet. */
    boolean canAskRep() {
        if (caseEntity.alternative() != CaseEntity.Alternative.CAR || hasPendingQuestions()) {
            return false;
        }
        boolean estimate = fact(FactKey.ALTERNATIVE_PARTICIPATION)
                .map(f -> f.status() == FactEntity.Status.DEFAULT)
                .orElse(false);
        return estimate && mayTake(vehicle(caseEntity.alternativeVehicleId()));
    }

    boolean hasPendingQuestions() {
        return questions.stream().anyMatch(RepQuestionEntity::isPending);
    }

    boolean canCompare() {
        return caseEntity.alternative() != CaseEntity.Alternative.NONE && missingForComparison().isEmpty();
    }

    String status() {
        if (hasPendingQuestions()) {
            return "WAITING_FOR_REP";
        }
        return canCompare() ? "READY" : "COLLECTING";
    }

    /**
     * Amounts the current numbers rely on that are catalog defaults rather than given or confirmed.
     * The group taken from the occupation table is not an estimate: that table is Strauss's own.
     */
    List<String> estimates() {
        return facts.values().stream()
                .filter(f -> f.status() == FactEntity.Status.DEFAULT && f.factKey() != FactKey.GROUP)
                .map(f -> f.factKey().apiName())
                .toList();
    }

    Employee employee() {
        return new Employee(
                text(FactKey.OCCUPATION),
                text(FactKey.GROUP),
                decimal(FactKey.BASE_SALARY),
                decimal(FactKey.COMMISSIONS),
                decimal(FactKey.BONUS),
                decimal(FactKey.PENSION_PERCENT),
                decimal(FactKey.STUDY_FUND_PERCENT),
                decimal(FactKey.CREDIT_POINTS),
                Integer.parseInt(text(FactKey.CHILDREN_UP_TO_23)));
    }

    Occupation occupation() {
        return data.occupations().byName(text(FactKey.OCCUPATION)).orElseThrow();
    }

    CarChoice todayChoice() {
        if (!hasCarToday()) {
            return new CarChoice.NoCar();
        }
        return new CarChoice.CompanyCar(vehicle(text(FactKey.CURRENT_CAR)), decimal(FactKey.CURRENT_PARTICIPATION));
    }

    CarChoice alternativeChoice() {
        if (caseEntity.alternative() == CaseEntity.Alternative.WAIVER) {
            return new CarChoice.NoCar();
        }
        return new CarChoice.CompanyCar(vehicle(caseEntity.alternativeVehicleId()),
                decimal(FactKey.ALTERNATIVE_PARTICIPATION));
    }

    Vehicle vehicle(String id) {
        return data.catalog().byId(id).orElseThrow();
    }

    private boolean hasCarToday() {
        FactEntity current = facts.get(FactKey.CURRENT_CAR);
        return current == null || !FactValues.NO_CAR.equals(current.value());
    }

    private String text(FactKey key) {
        return facts.get(key).value();
    }

    private BigDecimal decimal(FactKey key) {
        return new BigDecimal(facts.get(key).value());
    }
}
