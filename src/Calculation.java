import java.util.Map;
import java.util.Optional;
import java.util.Stack;
import java.util.function.BiFunction;

public class Calculation {
    /**
     * operator priority definition
     */
    private static final Map<Character, Integer> OP_PRIORITY = Map.of(
            '(', 0,
            '+', 1,
            '-', 1,
            '*', 2,
            '/', 2
    );

    /**
     * operator function
     */
    private static final Map<Character, BiFunction<Integer, Integer, Integer>> OP_FUNC = Map.of(
            '+', Integer::sum,
            '-', (a, b) -> a - b,
            '*', (a, b) -> a * b,
            '/', (a, b) -> a / b
    );

    /**
     * number stack
     */
    private static final Stack<Integer> nums = new Stack<>();

    /**
     * operator stack
     */
    private static final Stack<Character> ops = new Stack<>();

    /**
     * clarify the unary minus operator
     * @param expr original mathematical expression
     * @param i `-` position
     * @return true - unary minus; false - binary minus
     */
    private static boolean is_unary_minus(String expr, int i) {
        if (expr.charAt(i) != '-') {
            return false;
        }

        if (i == 0) {
            return true;
        }

        char prev = expr.charAt(i - 1);

        return is_operator(prev) || prev == '(';
    }

    /**
     * handle binary minus
     * @param c binary operator
     */
    private static void handle_binary_minus(char c) {
        while (!ops.isEmpty()
                && ops.peek() != '('
                && OP_PRIORITY.get(c) <= OP_PRIORITY.get(ops.peek())) {
            int result = do_calculation(ops.pop());
            nums.push(result);
        }
        ops.push(c);
    }

    /**
     *
     * @param expression original mathematical expression
     * @param base expression radix
     * @return if expression is valid, return result; otherwise return Optional.empty()
     */
    public static Optional<Integer> calculate(String expression, int base) {
        nums.clear();
        ops.clear();

        try {
            for (int i = 0; i < expression.length(); i++) {
                char c = expression.charAt(i);

                if (is_digit(c, base)) {
                    int j = i;
                    while (j < expression.length() && is_digit(expression.charAt(j), base)) {
                        j++;
                    }
                    int value = Integer.parseInt(expression.substring(i, j), base);
                    nums.push(value);
                    i = j - 1;
                } else if (c == '-') {
                    if (is_unary_minus(expression, i)) {
                        nums.push(0);
                        ops.push('-');
                    } else {
                        handle_binary_minus(c);
                    }
                } else if (is_operator(c)) {
                    if (ops.isEmpty()) {
                        ops.push(c);
                    } else {
                        char prevOp = ops.peek();
                        if (OP_PRIORITY.get(c) <= OP_PRIORITY.get(prevOp)) {
                            int result = do_calculation(prevOp);
                            ops.pop();
                            nums.push(result);
                        }
                        ops.push(c);
                    }
                } else if (c == '(') {
                    ops.push(c);
                } else if (c == ')') {
                    while (!ops.isEmpty() && ops.peek() != '(') {
                        char op = ops.pop();
                        int result = do_calculation(op);
                        nums.push(result);
                    }
                    if (ops.isEmpty() || ops.pop() != '(') {
                        return Optional.empty();
                    }
                }
            }

            while (!ops.isEmpty() && nums.size() > 1) {
                char op = ops.pop();
                int result = do_calculation(op);
                nums.push(result);
            }

            if (!ops.isEmpty() || nums.size() != 1) {
                return Optional.empty();
            }

            return Optional.of(nums.pop());

        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * do binary calculation
     * @param op operator character
     * @return calculation result
     */
    private static int do_calculation(char op) {
        BiFunction<Integer, Integer, Integer> func = OP_FUNC.get(op);
        int rhs = nums.pop();
        int lhs = nums.pop();
        return func.apply(lhs, rhs);
    }

    /**
     * clarify `c` is a operator character
     * @param c operator character
     * @return true - operator character; false - not a operator character
     */
    private static boolean is_operator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }

    /**
     * clarify `c` is a digital character
     * @param c character
     * @param base digit radix
     * @return true - is digital character; false - not a digital character
     */
    private static boolean is_digit(char c, int base) {
        return Character.digit(c, base) != -1;
    }
}
