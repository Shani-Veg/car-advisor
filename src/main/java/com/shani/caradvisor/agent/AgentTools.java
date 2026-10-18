package com.shani.caradvisor.agent;

import com.shani.caradvisor.data.ReferenceData;
import com.shani.caradvisor.engine.Occupation;
import com.shani.caradvisor.engine.Vehicle;
import com.shani.caradvisor.engine.Drive;
import com.shani.caradvisor.workflow.ApiException;
import com.shani.caradvisor.workflow.CarGuide;
import com.shani.caradvisor.workflow.CaseWorkflowService;
import com.shani.caradvisor.workflow.FactEntity;
import com.shani.caradvisor.workflow.FactKey;
import com.shani.caradvisor.workflow.FactValues;
import com.shani.caradvisor.workflow.Views.CaseView;
import com.shani.caradvisor.workflow.Views.RankingItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The only actions the model can take. Each one goes through CaseWorkflowService, so the chat is held to
 * exactly the same validation as the form. Tools never throw at the model: problems come back as text
 * it can relay or ask about.
 */
@Component
public class AgentTools {

    static final String RECORD_FACTS = "record_facts";
    static final String CHOOSE_ALTERNATIVE = "choose_alternative";
    static final String LIST_OPTIONS = "list_options";
    static final String REQUEST_REP_CONFIRMATION = "request_rep_confirmation";
    static final String FIND_CARS = "find_cars";
    static final String DESCRIBE_CAR = "describe_car";
    static final String COMPARE_CARS = "compare_cars";

    private static final Map<String, Drive> DRIVES = Map.of(
            "electric", Drive.ELECTRIC, "plug-in", Drive.PLUG_IN, "hybrid", Drive.HYBRID, "petrol", Drive.PETROL);

    private static final Pattern NUMBER = Pattern.compile("(\\d[\\d,]*(?:\\.\\d+)?)\\s*(אלף)?");

    private final ReferenceData data;
    private final CaseWorkflowService workflow;
    private final CarGuide guide;

    public AgentTools(ReferenceData data, CaseWorkflowService workflow, CarGuide guide) {
        this.data = data;
        this.workflow = workflow;
        this.guide = guide;
    }

    /** What happened during one employee turn, for the confirmation messages the server writes itself. */
    static final class TurnLog {
        final List<FactKey> recorded = new ArrayList<>();
        boolean alternativeChosen;
        boolean optionsListed;
        boolean repRequested;
        /** What the car tools found, for the server to write in the chat (with the numbers) after the model's reply. */
        final List<CarGuide.Answer> carAnswers = new ArrayList<>();
    }

    List<Map<String, Object>> declarations() {
        Map<String, Object> facts = new LinkedHashMap<>();
        facts.put("occupation", Map.of("type", "string", "enum", occupationNames(),
                "description", "The employee's role, exactly as in the company's occupation table."));
        facts.put("group", Map.of("type", "string", "enum", data.rules().groupOrder(),
                "description", "Car eligibility group, only if the employee states it."));
        facts.put("baseSalary", money("Monthly base salary in NIS."));
        facts.put("commissions", money("Monthly commissions in NIS. 0 if the employee says there are none."));
        facts.put("bonus", money("Monthly bonus in NIS. 0 if the employee says there is none."));
        facts.put("pensionPercent", Map.of("type", "number", "description", "The employee's own pension deduction, in percent."));
        facts.put("studyFundPercent", Map.of("type", "number", "description", "The employee's own study-fund deduction, in percent. 0 if none."));
        facts.put("creditPoints", Map.of("type", "number", "description", "Tax credit points."));
        facts.put("childrenUpTo23", Map.of("type", "integer", "description", "Number of children up to age 23."));
        facts.put("currentCar", Map.of("type", "string", "enum", vehicleIdsWithNone(),
                "description", "The car the employee has today. NONE = no company car today. " + catalogLegend()));
        facts.put("currentParticipation", money("What the employee pays per month today for the current car, in NIS."));

        return List.of(
                Map.of("name", RECORD_FACTS,
                        "description", "Store details the employee stated in their message. Only include fields the employee actually said.",
                        "parameters", Map.of("type", "object", "properties", facts)),
                Map.of("name", CHOOSE_ALTERNATIVE,
                        "description", "Set the option the employee wants to compare with today: a catalog car, or giving up the car.",
                        "parameters", Map.of("type", "object",
                                "properties", Map.of(
                                        "type", Map.of("type", "string", "enum", List.of("car", "waiver")),
                                        "vehicleId", Map.of("type", "string", "enum", vehicleIds(),
                                                "description", "Required when type is car. " + catalogLegend())),
                                "required", List.of("type"))),
                Map.of("name", LIST_OPTIONS,
                        "description", "All options the employee may take, best for their net first, with each car's drive type "
                                + "(electric, hybrid...). Use when they ask what is worthwhile or which cars of a kind they can take."),
                Map.of("name", FIND_CARS,
                        "description", "Find catalog cars of a kind: by drive type, up to a list price, or losing at most "
                                + "some amount of net a month. Use when the employee asks which cars exist, e.g. "
                                + "'I want an electric car', 'a plug-in up to 200 thousand', 'what won't cost me more than 500'. "
                                + "The system shows the cars and their numbers in the chat.",
                        "parameters", Map.of("type", "object", "properties", Map.of(
                                "drives", Map.of("type", "array", "items", Map.of("type", "string", "enum", List.copyOf(DRIVES.keySet())),
                                        "description", "Drive types the employee asked for. Empty or missing = any."),
                                "maxListPrice", money("Highest list price in NIS, only if the employee wrote it."),
                                "maxNetDecrease", money("The most net pay the employee agrees to lose per month, in NIS, only if they wrote it."),
                                "order", Map.of("type", "string", "enum", List.of("best_for_net", "most_luxurious", "cheapest"),
                                        "description", "most_luxurious for luxury or premium requests (highest list price first), "
                                                + "cheapest for cheap ones, otherwise best_for_net.")))),
                Map.of("name", DESCRIBE_CAR,
                        "description", "Explain one catalog car: drive type, group, list price, use value, participation, and what it "
                                + "does to this employee's net. Use when they ask about a specific model.",
                        "parameters", Map.of("type", "object",
                                "properties", Map.of("vehicleId", Map.of("type", "string", "enum", vehicleIds(),
                                        "description", catalogLegend())),
                                "required", List.of("vehicleId"))),
                Map.of("name", COMPARE_CARS,
                        "description", "Put two to four catalog cars side by side, when the employee hesitates between models.",
                        "parameters", Map.of("type", "object",
                                "properties", Map.of("vehicleIds", Map.of("type", "array",
                                        "items", Map.of("type", "string", "enum", vehicleIds()),
                                        "description", "Two to four cars. " + catalogLegend())),
                                "required", List.of("vehicleIds"))),
                Map.of("name", REQUEST_REP_CONFIRMATION,
                        "description", "Ask the transport rep to confirm the participation for the car being compared. "
                                + "Only when the employee asks for it (for example: they want this car, or want the number confirmed)."));
    }

    Map<String, Object> execute(String caseId, long messageId, String employeeText, LlmClient.FunctionCall call,
                                TurnLog log) {
        return switch (call.name()) {
            case RECORD_FACTS -> recordFacts(caseId, messageId, employeeText, call.args(), log);
            case CHOOSE_ALTERNATIVE -> chooseAlternative(caseId, call.args(), log);
            case LIST_OPTIONS -> {
                log.optionsListed = true;
                yield listOptions(caseId, log);
            }
            case REQUEST_REP_CONFIRMATION -> requestRep(caseId, log);
            case FIND_CARS -> findCars(caseId, employeeText, call.args(), log);
            case DESCRIBE_CAR -> carAnswer(log, () -> guide.describe(caseId, String.valueOf(call.args().get("vehicleId"))));
            case COMPARE_CARS -> carAnswer(log, () -> guide.compare(caseId, strings(call.args().get("vehicleIds"))));
            default -> Map.of("error", "Unknown tool " + call.name());
        };
    }

    private Map<String, Object> recordFacts(String caseId, long messageId, String employeeText, Map<String, Object> args,
                                            TurnLog log) {
        List<String> saved = new ArrayList<>();
        List<String> unchanged = new ArrayList<>();
        Map<String, String> rejected = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : args.entrySet()) {
            FactKey key;
            try {
                key = FactKey.fromApiName(entry.getKey());
            } catch (ApiException e) {
                rejected.put(entry.getKey(), "unknown field");
                continue;
            }
            if (key.type() == FactKey.ValueType.MONEY && !statedInMessage(entry.getValue(), employeeText)) {
                rejected.put(key.apiName(), "This amount does not appear in the employee's message. Ask them to write it in digits.");
                continue;
            }
            try {
                if (workflow.setFactIfChanged(caseId, key, entry.getValue(), FactEntity.Source.EMPLOYEE_MESSAGE,
                        "m-" + messageId)) {
                    saved.add(key.apiName());
                    log.recorded.add(key);
                } else {
                    unchanged.add(key.apiName());
                }
            } catch (ApiException e) {
                rejected.put(key.apiName(), e.getMessage());
            }
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("saved", saved);
        result.put("alreadyKnown", unchanged);
        result.put("rejected", rejected);
        result.put("caseState", Prompts.caseState(workflow.view(caseId)));
        return result;
    }

    private Map<String, Object> chooseAlternative(String caseId, Map<String, Object> args, TurnLog log) {
        try {
            workflow.chooseAlternative(caseId, String.valueOf(args.get("type")),
                    args.get("vehicleId") == null ? null : String.valueOf(args.get("vehicleId")), null);
            log.alternativeChosen = true;
            return Map.of("ok", true, "caseState", Prompts.caseState(workflow.view(caseId)));
        } catch (ApiException e) {
            return Map.of("ok", false, "error", e.getMessage());
        }
    }

    /** Amount filters count only if the employee wrote the amount; otherwise the search runs without them, and says so. */
    private Map<String, Object> findCars(String caseId, String employeeText, Map<String, Object> args, TurnLog log) {
        Set<Drive> drives = new java.util.HashSet<>();
        for (String name : strings(args.get("drives"))) {
            Drive drive = DRIVES.get(name);
            if (drive != null) {
                drives.add(drive);
            }
        }
        List<String> ignored = new ArrayList<>();
        BigDecimal maxPrice = statedAmount(args.get("maxListPrice"), employeeText, "maxListPrice", ignored);
        BigDecimal maxDecrease = statedAmount(args.get("maxNetDecrease"), employeeText, "maxNetDecrease", ignored);
        CarGuide.Order order = switch (String.valueOf(args.getOrDefault("order", "best_for_net"))) {
            case "most_luxurious" -> CarGuide.Order.MOST_LUXURIOUS;
            case "cheapest" -> CarGuide.Order.CHEAPEST;
            default -> CarGuide.Order.BEST_FOR_NET;
        };
        Map<String, Object> result = new LinkedHashMap<>(carAnswer(log, () -> guide.find(caseId, drives, maxPrice, maxDecrease, order)));
        if (!ignored.isEmpty()) {
            result.put("ignored", ignored + ": the amount does not appear in the employee's message");
        }
        return result;
    }

    private static BigDecimal statedAmount(Object value, String employeeText, String name, List<String> ignored) {
        if (value == null) {
            return null;
        }
        if (!statedInMessage(value, employeeText)) {
            ignored.add(name);
            return null;
        }
        return new BigDecimal(String.valueOf(value).replace(",", "").trim());
    }

    private Map<String, Object> carAnswer(TurnLog log, java.util.function.Supplier<CarGuide.Answer> question) {
        try {
            CarGuide.Answer answer = question.get();
            log.carAnswers.add(answer);
            return Map.of("shownToEmployee", answer.forModel(),
                    "note", "The chat already shows these cars with their numbers. Add one or two sentences in words.");
        } catch (ApiException e) {
            return Map.of("error", e.getMessage());
        }
    }

    private static List<String> strings(Object value) {
        if (value instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return value == null ? List.of() : List.of(String.valueOf(value));
    }

    private Map<String, Object> requestRep(String caseId, TurnLog log) {
        try {
            workflow.requestRepConfirmation(caseId);
            log.repRequested = true;
            return Map.of("ok", true, "caseState", Prompts.caseState(workflow.view(caseId)));
        } catch (ApiException e) {
            return Map.of("ok", false, "error", e.getMessage());
        }
    }

    /** The best options go to the chat as cards (CarGuide.best); the model gets the order and directions only. */
    private Map<String, Object> listOptions(String caseId, TurnLog log) {
        CaseView view = workflow.view(caseId);
        if (view.ranking() == null) {
            return Map.of("error", "Some details are still missing", "caseState", Prompts.caseState(view));
        }
        log.carAnswers.add(guide.best(caseId));
        List<Map<String, Object>> options = new ArrayList<>();
        int rank = 1;
        for (RankingItem item : view.ranking()) {
            if ("waiver".equals(item.alternative().type())) {
                continue; // giving up the car is discussed only when the employee raises it
            }
            options.add(Map.of(
                    "rank", rank++,
                    "option", item.alternative().label(),
                    "drive", item.alternative().drive() == null ? "no car" : item.alternative().drive(),
                    "effectOnNet", item.difference().signum() > 0 ? "higher than today"
                            : item.difference().signum() < 0 ? "lower than today" : "same as today",
                    "participationIsEstimate", item.participationEstimated()));
        }
        return Map.of("options", options,
                "note", "The chat shows the best options with their amounts right above your reply. Add one short sentence.");
    }

    /** An amount counts only if the employee wrote it (or said there is none). Stops the model from inventing salaries. */
    public static boolean statedInMessage(Object value, String text) {
        BigDecimal wanted;
        try {
            wanted = new BigDecimal(String.valueOf(value).replace(",", "").trim());
        } catch (NumberFormatException e) {
            return false;
        }
        if (wanted.signum() == 0) {
            return true;
        }
        Matcher m = NUMBER.matcher(text);
        while (m.find()) {
            BigDecimal found = new BigDecimal(m.group(1).replace(",", ""));
            if (m.group(2) != null) {
                found = found.multiply(BigDecimal.valueOf(1000));
            }
            if (found.compareTo(wanted) == 0) {
                return true;
            }
        }
        return false;
    }

    /** Catalog names the model may write even though they contain digits. */
    public List<String> safeNames() {
        List<String> names = new ArrayList<>();
        data.catalog().vehicles().forEach(v -> {
            names.add(v.label());
            names.add(v.commercialName());
        });
        return names;
    }

    private static Map<String, Object> money(String description) {
        return Map.of("type", "number", "description", description);
    }

    private List<String> occupationNames() {
        return data.occupations().all().stream().map(Occupation::name).toList();
    }

    private List<String> vehicleIds() {
        return data.catalog().vehicles().stream().map(Vehicle::id).toList();
    }

    private List<String> vehicleIdsWithNone() {
        List<String> ids = new ArrayList<>(vehicleIds());
        ids.add(FactValues.NO_CAR);
        return ids;
    }

    private String catalogLegend() {
        return "Catalog: " + data.catalog().vehicles().stream()
                .map(v -> v.id() + " = " + v.label() + " " + v.maker() + ", group " + v.group() + ", " + v.drive().hebrew())
                .collect(Collectors.joining("; "));
    }
}
