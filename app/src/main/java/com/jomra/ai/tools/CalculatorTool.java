package com.jomra.ai.tools;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CalculatorTool implements Tool {
    @Override public String getName() { return "calculator"; }
    @Override public String getDescription() { return "Perform mathematical calculations"; }
    @Override public ToolParameter[] getParameters() {
        return new ToolParameter[]{new ToolParameter("expression", "Math expression", String.class, true, "")};
    }

    @Override public ToolResult execute(Map<String, Object> params) throws ToolException {
        long startTime = System.currentTimeMillis();
        String expr = (String) params.get("expression");
        if (expr == null || expr.trim().isEmpty()) throw new ToolException("Expression cannot be empty");

        // Fix the syntax error: escape the backslash
        expr = expr.replaceAll("\\s", "");

        if (!expr.matches("[0-9+\\-*/().]+")) throw new ToolException("Invalid characters in expression");

        try {
            double result = evaluateExpression(expr);
            if (Double.isInfinite(result) || Double.isNaN(result)) {
                throw new ToolException("Calculation resulted in an invalid number");
            }

            return new ToolResult.Builder()
                .success(true)
                .resultText("Result: " + result)
                .data(Map.of("result", result, "expression", expr))
                .executionTime(System.currentTimeMillis() - startTime)
                .build();
        } catch (Exception e) {
            throw new ToolException("Calculation failed: " + e.getMessage());
        }
    }

    /**
     * Improved expression evaluator (still simple, but handles basic order of ops).
     */
    private double evaluateExpression(String expression) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < expression.length()) ? expression.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < expression.length()) throw new RuntimeException("Unexpected: " + (char)ch);
                return x;
            }

            double parseExpression() {
                double x = parseTerm();
                for (;;) {
                    if      (eat('+')) x += parseTerm(); // addition
                    else if (eat('-')) x -= parseTerm(); // subtraction
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (;;) {
                    if      (eat('*')) x *= parseFactor(); // multiplication
                    else if (eat('/')) x /= parseFactor(); // division
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor(); // unary plus
                if (eat('-')) return -parseFactor(); // unary minus

                double x;
                int startPos = this.pos;
                if (eat('(')) { // parentheses
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(expression.substring(startPos, this.pos));
                } else {
                    throw new RuntimeException("Unexpected: " + (char)ch);
                }

                return x;
            }
        }.parse();
    }

    @Override public long getEstimatedDurationMs() { return 10; }
    @Override public boolean isAvailable() { return true; }
}
