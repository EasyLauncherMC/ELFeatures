package org.easylauncher.mods.elfeatures.shared.mixin;

import java.util.function.IntPredicate;

public final class MixinConstraint {

    public static final MixinConstraint ALWAYS_TRUE = new MixinConstraint();

    private final IntPredicate[] bounds;

    public MixinConstraint(IntPredicate... bounds) {
        this.bounds = bounds;
    }

    public boolean pass(int dataVersion) {
        for (IntPredicate bound : bounds)
            if (!bound.test(dataVersion))
                return false;

        return true;
    }

    public static MixinConstraint parse(String input) {
        if (input == null || input.isEmpty() || "*".equals(input))
            return ALWAYS_TRUE;

        boolean leftStrict = isStrict(input.charAt(0));
        boolean rightStrict = isStrict(input.charAt(input.length() - 1));

        String expression = input.substring(1, input.length() - 1);
        boolean isRange = expression.contains(",");

        if (!isRange) {
            if (leftStrict || rightStrict)
                throw new IllegalArgumentException("Certain expression must only be inside []: " + input);

            int expected = Integer.parseInt(expression);
            return new MixinConstraint(actual -> actual == expected);
        }

        if (expression.indexOf(',') == -1 || expression.indexOf(',') != expression.lastIndexOf(','))
            throw new IllegalArgumentException("Range expression must have only 2 bounds: " + input);

        boolean leftFinite = expression.charAt(0) != ',';
        boolean rightFinite = expression.charAt(expression.length() - 1) != ',';

        if (leftFinite && rightFinite) {
            String[] bounds = expression.split(",");
            int leftBound = Integer.parseInt(bounds[0]);
            int rightBound = Integer.parseInt(bounds[1]);
            return new MixinConstraint(
                    leftStrict ? (actual -> leftBound < actual) : (actual -> leftBound <= actual),
                    rightStrict ? (actual -> actual < rightBound) : (actual -> actual <= rightBound)
            );
        } else if (leftFinite && !rightFinite && rightStrict) {
            int leftBound = Integer.parseInt(expression.substring(0, expression.length() - 1));
            return new MixinConstraint(leftStrict ? (actual -> leftBound < actual) : (actual -> leftBound <= actual));
        } else if (rightFinite && !leftFinite && leftStrict) {
            int rightBound = Integer.parseInt(expression.substring(1));
            return new MixinConstraint(rightStrict ? (actual -> actual < rightBound) : (actual -> actual <= rightBound));
        } else {
            throw new IllegalArgumentException("Unexpected expression: " + input);
        }
    }

    private static boolean isStrict(char input) {
        if (input == '(' || input == ')')
            return true;

        if (input == '[' || input == ']')
            return false;

        throw new IllegalArgumentException("Unexpected character: " + input);
    }

}
