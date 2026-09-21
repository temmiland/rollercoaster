package land.temmi.rollercoaster.event;

/** Evaluates a Condition against GameState - the one piece of interpretation Condition itself
 * deliberately doesn't carry. */
public final class ConditionEvaluator {
    private ConditionEvaluator() {
    }

    public static boolean evaluate(Condition condition, GameState state) {
        String actual;
        switch (condition.type) {
            case FLAG: actual = state.getFlag(condition.key); break;
            case VARIABLE: actual = state.getVariable(condition.key); break;
            case TIME_OF_DAY: actual = Float.toString(state.getTimeOfDay()); break;
            default: throw new IllegalArgumentException("Unknown condition type: " + condition.type);
        }
        return compare(actual, condition.comparison, condition.value);
    }

    private static boolean compare(String actual, Condition.Comparison comparison, String expected) {
        if (comparison == Condition.Comparison.EQUALS) return expected.equals(actual);
        if (comparison == Condition.Comparison.NOT_EQUALS) return !expected.equals(actual);
        if (actual == null) return false;
        float actualNumber;
        float expectedNumber;
        try {
            actualNumber = Float.parseFloat(actual);
            expectedNumber = Float.parseFloat(expected);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                "Comparison '" + comparison + "' needs numeric values, got '" + actual + "' vs '" + expected + "'");
        }
        switch (comparison) {
            case GREATER_THAN: return actualNumber > expectedNumber;
            case LESS_THAN: return actualNumber < expectedNumber;
            case GREATER_OR_EQUAL: return actualNumber >= expectedNumber;
            case LESS_OR_EQUAL: return actualNumber <= expectedNumber;
            default: throw new IllegalArgumentException("Unknown comparison: " + comparison);
        }
    }
}
