package com.shani.caradvisor.data;

import com.shani.caradvisor.engine.Drive;
import com.shani.caradvisor.engine.Rules;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads rules.json. Fields we added on top of Strauss's sample file have defaults,
 * so their original file can be swapped in unchanged.
 */
public final class RulesLoader {

    /** Reconstructed from the 6 reference employees: the credit applies to deposits up to 7% of the income ceiling. */
    private static final BigDecimal DEFAULT_MAX_DEPOSIT_RATE = new BigDecimal("0.07");

    private RulesLoader() {
    }

    @SuppressWarnings("unchecked")
    public static Rules load(Path file) {
        Map<String, Object> root = new ObjectMapper().readValue(file.toFile(), Map.class);

        Map<String, BigDecimal> budgets = new LinkedHashMap<>();
        map(root, "budgets").forEach((group, amount) -> budgets.put(group, decimal(amount)));
        List<String> groupOrder = List.copyOf((List<String>) root.get("groupOrder"));

        Map<String, Object> electric = map(root, "electric");
        Map<String, Object> family = map(root, "familyTrack");
        Map<String, Object> waiver = map(root, "waiver");
        Map<String, Object> net = map(root, "netMethod");
        Map<String, Object> pension = map(net, "pensionCredit");

        List<Rules.Bracket> brackets = new ArrayList<>();
        for (Object item : (List<Object>) net.get("brackets")) {
            List<Object> pair = (List<Object>) item;
            brackets.add(new Rules.Bracket(pair.get(0) == null ? null : decimal(pair.get(0)), decimal(pair.get(1))));
        }

        Map<Integer, Drive> driveCodes = new LinkedHashMap<>();
        map(map(root, "valueOfUse"), "driveField")
                .forEach((code, hebrew) -> driveCodes.put(Integer.parseInt(code), Drive.fromHebrew((String) hebrew)));

        return new Rules(
                String.valueOf(root.get("version")),
                Map.copyOf(budgets),
                groupOrder,
                new Rules.Electric(decimal(electric.get("netBonus")),
                        (String) electric.getOrDefault("method", "gross_up")),
                new Rules.FamilyTrack(
                        ((Number) family.get("minChildren")).intValue(),
                        ((Number) family.get("childMaxAge")).intValue(),
                        decimal(family.get("carPrice")),
                        decimal(family.get("gapSplitEmployee")),
                        !"fixed".equals(family.getOrDefault("costSource", "catalog"))),
                new Rules.Waiver(
                        (Boolean) waiver.getOrDefault("maintenanceIsTaxable", true),
                        (Boolean) waiver.getOrDefault("maintenanceIsPensionable", false)),
                new Rules.NetMethod(
                        ((Number) net.get("taxYear")).intValue(),
                        decimal(net.get("creditPointValue")),
                        List.copyOf(brackets),
                        socialRate(map(net, "nationalInsurance")),
                        socialRate(map(net, "health")),
                        new Rules.PensionCredit(
                                decimal(pension.get("rate")),
                                decimal(pension.get("incomeCeiling")),
                                pension.containsKey("maxDepositRate")
                                        ? decimal(pension.get("maxDepositRate"))
                                        : DEFAULT_MAX_DEPOSIT_RATE)),
                Map.copyOf(driveCodes));
    }

    private static Rules.SocialRate socialRate(Map<String, Object> node) {
        return new Rules.SocialRate(
                decimal(node.get("threshold")),
                decimal(node.get("ceiling")),
                decimal(node.get("lowRate")),
                decimal(node.get("highRate")));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Map<String, Object> parent, String key) {
        Object value = parent.get(key);
        if (!(value instanceof Map)) {
            throw new IllegalStateException("rules.json: missing object '" + key + "'");
        }
        return (Map<String, Object>) value;
    }

    /** Via the string form, so 0.1 stays exactly 0.1. */
    private static BigDecimal decimal(Object value) {
        if (value == null) {
            throw new IllegalStateException("rules.json: missing number");
        }
        return new BigDecimal(value.toString());
    }
}
