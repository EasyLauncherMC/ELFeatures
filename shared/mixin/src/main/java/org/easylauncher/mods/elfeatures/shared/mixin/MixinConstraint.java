package org.easylauncher.mods.elfeatures.shared.mixin;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.easylauncher.mods.elfeatures.version.MinecraftVersion;

import java.util.function.Consumer;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class MixinConstraint {

    private final MinecraftVersion expected;
    private final VersionPredicate predicate;

    public boolean isComparableWith(MinecraftVersion actual) {
        return expected.isComparableWith(actual);
    }

    public boolean pass(MinecraftVersion actual) {
        if (!isComparableWith(actual))
            throw new IllegalArgumentException(String.format("Value '%s' isn't comparable with '%s'", actual, expected));

        return predicate.test(expected, actual);
    }

    public static void parse(String input, Consumer<MixinConstraint> consumer) {
        if (input == null || input.isEmpty() || "*".equals(input))
            return;

        boolean leftStrict = isStrict(input.charAt(0));
        boolean rightStrict = isStrict(input.charAt(input.length() - 1));
        String expression = input.substring(1, input.length() - 1);

        if (parseEquationExpression(expression, leftStrict, rightStrict, consumer))
            return;

        if (expression.indexOf(',') == -1 || expression.indexOf(',') != expression.lastIndexOf(','))
            throw new IllegalArgumentException("Range expression must have only 2 bounds: " + input);

        if (parseRangeExpression(expression, leftStrict, rightStrict, consumer))
            return;

        throw new IllegalArgumentException("Unexpected expression: " + input);
    }

    private static boolean parseEquationExpression(String expression, boolean leftStrict, boolean rightStrict, Consumer<MixinConstraint> consumer) {
        if (expression.contains(","))
            return false;

        if (leftStrict || rightStrict)
            throw new IllegalArgumentException("Certain expression must only be inside []: " + expression);

        // [X]
        consumer.accept(new MixinConstraint(parseExpected(expression), MinecraftVersion::isEqual));
        return true;
    }

    private static boolean parseRangeExpression(String expression, boolean leftStrict, boolean rightStrict, Consumer<MixinConstraint> consumer) {
        boolean leftFinite = expression.charAt(0) != ',';
        boolean rightFinite = expression.charAt(expression.length() - 1) != ',';

        // [X,Y] or [X,Y) or (X,Y] or (X,Y)
        if (leftFinite && rightFinite) {
            String[] bounds = expression.split(",");
            MinecraftVersion leftBound = parseExpected(bounds[0]);
            MinecraftVersion rightBound = parseExpected(bounds[1]);

            if (!leftBound.isComparableWith(rightBound))
                throw new IllegalArgumentException(String.format(
                        "Left bound '%s' cannot be compared with right bound '%s'",
                        leftBound, rightBound
                ));

            consumer.accept(new MixinConstraint(leftBound, (e, a) -> e.isOlderThan(a, leftStrict)));
            consumer.accept(new MixinConstraint(rightBound, (e, a) -> e.isNewerThan(a, rightStrict)));
            return true;
        }

        // [X,) or (X,)
        if (leftFinite && rightStrict) {
            MinecraftVersion expected = parseExpected(expression.substring(0, expression.length() - 1));
            consumer.accept(new MixinConstraint(expected, (e, a) -> e.isOlderThan(a, leftStrict)));
            return true;
        }

        // (,X] or (,X)
        if (rightFinite && leftStrict) {
            MinecraftVersion expected = parseExpected(expression.substring(1));
            consumer.accept(new MixinConstraint(expected, (e, a) -> e.isNewerThan(a, rightStrict)));
            return true;
        }

        return false;
    }

    private static boolean isStrict(char input) {
        if (input == '(' || input == ')')
            return true;

        if (input == '[' || input == ']')
            return false;

        throw new IllegalArgumentException("Unexpected character: " + input);
    }

    private static MinecraftVersion parseExpected(String expression) {
        return MinecraftVersion.parse(expression).orElseThrow(() -> new IllegalArgumentException(
                String.format("Unresolved expression: '%s'", expression)
        ));
    }

    @FunctionalInterface
    public interface VersionPredicate {
        boolean test(MinecraftVersion expected, MinecraftVersion actual);
    }

}
