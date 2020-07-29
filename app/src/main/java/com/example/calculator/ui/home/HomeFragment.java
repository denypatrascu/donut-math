package com.example.calculator.ui.home;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import com.example.calculator.R;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HomeFragment extends Fragment {
    private static String LOG_TAG = "Donut Calculator";
    private final String OPERATIONS = "+-*/^";
    private final String DIGITS = "0123456789";
    private final String BRACKETS_ERROR = "Wrong expression, brackets do not correspond!";
    private final String FUNCTIONS_ERROR = "Wrong expression, check functions parameters!";
    private final String CONSTANTS = "πe";

    private View view;
    private TextView display, displayError;
    private Button[] btnDigit = new Button[10]; // 0, 1, 2, 3, 4, 5, 6, 7, 8, 9
    private Map<String, Button> btnOperation = new HashMap<>(); // +, -, *, /
    private Button btnClear, btnDelete, btnEqual; // C, DEL, 🍩
    private Button[] btnBrackets = new Button[2]; // (, )
    private Map<String, Button> btnTrigonometric = new HashMap<>(); // sin, cos, tan, cot
    private Map<String, Button> btnOtherFunctions = new HashMap<>(); // sqrt, log, ln
    private Button btnPoint; // .
    private  Map<String, Button> btnConstants = new HashMap<>(); // π, e

    private boolean digitSelected = false;
    private boolean constantSelected = false;
    private boolean pointSelected = false;
    private boolean nonZeroBefore = false;
    private int zeroCount = 0;
    private int[] countBrackets = new int[2];

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);
        setIDs(view);
        buttonsForDigits();
        buttonsForPrimaryOperations();
        buttonDelete();
        buttonClear();
        buttonsForBrackets();
        buttonsForFunctions(btnTrigonometric);
        buttonsForFunctions(btnOtherFunctions);
        buttonsForConstants();
        buttonPoint();
        buttonEqual();
        return view;
    }

    private void setIDs(@NonNull View view) {
        display = view.findViewById(R.id.displayNumber);
        displayError = view.findViewById(R.id.displayError);

        btnClear = view.findViewById(R.id.button00);
        btnDelete = view.findViewById(R.id.button01);
        btnEqual = view.findViewById(R.id.button04);

        btnBrackets[0] = view.findViewById(R.id.button02);
        btnBrackets[1] = view.findViewById(R.id.button03);

        btnOperation.put("+", (Button) view.findViewById(R.id.button44));
        btnOperation.put("-", (Button) view.findViewById(R.id.button34));
        btnOperation.put("*", (Button) view.findViewById(R.id.button24));
        btnOperation.put("/", (Button) view.findViewById(R.id.button14));
        btnOperation.put("^", (Button) view.findViewById(R.id.button60));

        btnDigit[0] = view.findViewById(R.id.button42);
        btnDigit[1] = view.findViewById(R.id.button31);
        btnDigit[2] = view.findViewById(R.id.button32);
        btnDigit[3] = view.findViewById(R.id.button33);
        btnDigit[4] = view.findViewById(R.id.button21);
        btnDigit[5] = view.findViewById(R.id.button22);
        btnDigit[6] = view.findViewById(R.id.button23);
        btnDigit[7] = view.findViewById(R.id.button11);
        btnDigit[8] = view.findViewById(R.id.button12);
        btnDigit[9] = view.findViewById(R.id.button13);

        btnTrigonometric.put("sin", (Button) view.findViewById(R.id.button10));
        btnTrigonometric.put("cos", (Button) view.findViewById(R.id.button20));
        btnTrigonometric.put("tan", (Button) view.findViewById(R.id.button30));
        btnTrigonometric.put("cot", (Button) view.findViewById(R.id.button40));

        btnOtherFunctions.put("sqrt", (Button) view.findViewById(R.id.button50));
        btnOtherFunctions.put("log", (Button) view.findViewById(R.id.button70));
        btnOtherFunctions.put("ln", (Button) view.findViewById(R.id.button80));

        btnConstants.put("π", (Button) view.findViewById(R.id.button41));
        btnConstants.put("e", (Button) view.findViewById(R.id.button51));

        btnPoint = view.findViewById(R.id.button43);
    }

    private void scrollToRight() {
        view.post(new Runnable() {
            @Override
            public void run() {
            ((HorizontalScrollView) view
                    .findViewById(R.id.displayScrollerView))
                    .fullScroll(HorizontalScrollView.FOCUS_RIGHT);
            }
        });
    }

    private void scrollToLeft() {
        view.post(new Runnable() {
            @Override
            public void run() {
                ((HorizontalScrollView) view
                        .findViewById(R.id.displayScrollerView))
                        .fullScroll(HorizontalScrollView.FOCUS_LEFT);
            }
        });
    }

    private void appendToDisplay(String string) {
        String text = display.getText() + string;
        display.setText(text);
        scrollToRight();
    }

    private void popFromDisplay() {
        String text = display.getText().toString();
        if (text.length() > 0) {
            char lastChar = getLastChar();

            switch (lastChar) {
                case '(':
                    if (countBrackets[0] > 0) countBrackets[0]--;
                    popFunctionFromDisplay();
                    return;
                case ')':
                    if (countBrackets[1] > 0) countBrackets[1]--;
                    break;
                case '.':
                    pointSelected = false;
                    break;
            }

            if (CONSTANTS.indexOf(lastChar) >= 0) constantSelected = false;

            text = text.substring(0, text.length() - 1);
            display.setText(text);
        }
    }

    private void popFunctionFromDisplay() {
        String text = display.getText().toString();

        Map<Integer, String[]> functions = new HashMap<>();
        functions.put(5, new String[] {"sqrt"});
        functions.put(4, new String[] {"sin", "cos", "tan", "cot", "log"});
        functions.put(3, new String[] {"ln"});
        int length = 1;

        for (Map.Entry<Integer, String[]> possibilities : functions.entrySet()) {
            for (String function : possibilities.getValue()) {
                if (text.endsWith(function + "(")) {
                    length = possibilities.getKey();
                    break;
                }
            }

            if (length != 1) break;
        }

        text = text.substring(0, text.length() - length);
        display.setText(text);
        if (countBrackets[0] > 0) countBrackets[0]--;
    }

    private void resetConditions() {
        countBrackets[0] = 0;
        countBrackets[1] = 0;
        constantSelected = false;
        digitSelected = false;
        pointSelected = false;
        nonZeroBefore = false;
        zeroCount = 0;
    }

    private void clearDisplay() {
        display.setText("");
        resetConditions();
    }

    private void clearDisplayError() {
        displayError.setText("");
    }

    private char getLastChar() {
        String text = display.getText().toString();
        if (text.length() > 0) {
            return text.charAt(text.length() - 1);
        }
        return '#';
    }

    private boolean isLastCharDigit() {
        return DIGITS.indexOf(getLastChar()) >= 0;
    }

    private boolean isLastCharConstant() { return CONSTANTS.indexOf(getLastChar()) >= 0; }

    private void logButton(String identifier) {
        Log.d(LOG_TAG, "Button \"" + identifier + "\" touched");
    }

    private void buttonsForPrimaryOperations() {
        Set<Map.Entry<String, Button>> operations = btnOperation.entrySet();
        for (Map.Entry<String, Button> operation : operations) {
            final String key = operation.getKey();

            operation.getValue().setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    logButton(key);
                    char lastChar = getLastChar();

                    if (".".indexOf(lastChar) >= 0)
                        return;

                    if (display.getText().toString().length() == 1
                        && (key.equals("+") || key.equals("-")))
                        return;

                    if (!digitSelected && OPERATIONS.indexOf(lastChar) >= 0) popFromDisplay();

                    if ("#(".indexOf(lastChar) < 0 ||
                        (key.equals("+") || key.equals("-"))) {
                        appendToDisplay(key);
                        constantSelected = false;
                        digitSelected = false;
                        pointSelected = false;
                        nonZeroBefore = false;
                        zeroCount = 0;
                    }
                }
            });
        }
    }

    private void buttonsForDigits() {
        for (int i = 0; i < 10; i++) {
            final String number = String.valueOf(i);
            final int index = i;
            btnDigit[i].setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    logButton(number);
                    char lastChar = getLastChar();
                    String text = number;
                    if (constantSelected || lastChar == ')') text = "*" + number;
                    if (index != 0) nonZeroBefore = true;
                    if (!pointSelected && index == 0 && zeroCount >= 1 && !nonZeroBefore) return;
                    appendToDisplay(text);
                    if (index == 0) zeroCount++;
                    constantSelected = false;
                    digitSelected = true;
                }
            });
        }
    }

    private void buttonDelete() {
        btnDelete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                logButton("delete");
                digitSelected = isLastCharDigit();
                constantSelected = isLastCharConstant();
                popFromDisplay();
            }
        });
    }

    private void buttonClear() {
        btnClear.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                logButton("clear");
                clearDisplay();
                clearDisplayError();
                digitSelected = true;
            }
        });
    }

    private void buttonsForBrackets() {
        countBrackets[0] = 0;
        countBrackets[1] = 0;

        btnBrackets[0].setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                logButton("(");
                char lastChar = getLastChar();
                boolean correct = false;
                String bracket = "(";

                if ("#(".indexOf(lastChar) >= 0 || OPERATIONS.indexOf(lastChar) >= 0) {
                    correct = true;
                }
                if (lastChar == ')'
                    || DIGITS.indexOf(lastChar) >= 0
                    || CONSTANTS.indexOf(lastChar) >= 0) {
                    bracket = "*(";
                    correct = true;
                }

                if (correct) {
                    appendToDisplay(bracket);
                    countBrackets[0]++;
                    digitSelected = false;
                    constantSelected = false;
                    nonZeroBefore = false;
                    zeroCount = 0;
                }
            }
        });

        btnBrackets[1].setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                logButton(")");
                char lastChar = getLastChar();
                boolean correct = false;
                String bracket = ")";

                if ((lastChar == ')'
                    || DIGITS.indexOf(lastChar) >= 0
                    || CONSTANTS.indexOf(lastChar) >= 0)
                    && countBrackets[0] > countBrackets[1]) {
                    correct = true;
                }

                if (correct) {
                    appendToDisplay(bracket);
                    countBrackets[1]++;
                    digitSelected = false;
                    constantSelected = false;
                    nonZeroBefore = false;
                    zeroCount = 0;
                }
            }
        });
    }

    private void buttonsForFunctions(@NonNull Map<String, Button> buttons) {
        Set<Map.Entry<String, Button>> operations = buttons.entrySet();
        for (Map.Entry<String, Button> operation : operations) {
            final String key = operation.getKey();

            operation.getValue().setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    logButton(key);
                    char lastChar = getLastChar();
                    String text = key + "(";
                    boolean correct = false;

                    if ("#(".indexOf(lastChar) >= 0
                        || OPERATIONS.indexOf(lastChar) >= 0) {
                        correct = true;
                    }

                    if (lastChar == ')'
                        || DIGITS.indexOf(lastChar) >= 0
                        || CONSTANTS.indexOf(lastChar) >= 0) {
                        text = "*" + key + "(";
                        correct = true;
                    }

                    if (correct) {
                        appendToDisplay(text);
                        countBrackets[0]++;
                        constantSelected = false;
                        digitSelected = false;
                        pointSelected = false;
                        nonZeroBefore = false;
                        zeroCount = 0;
                    }
                }
            });
        }
    }

    private void buttonPoint() {
        btnPoint.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                logButton("point");
                String text = ".";
                char lastChar = getLastChar();
                boolean correct = false;

                if (!pointSelected) {
                    if (DIGITS.indexOf(lastChar) < 0) text = "0.";
                    if (CONSTANTS.indexOf(lastChar) >= 0) text = "*" + text;
                    correct = true;
                }

                if (correct) {
                    pointSelected = true;
                    appendToDisplay(text);
                    digitSelected = false;
                    constantSelected = false;
                    nonZeroBefore = false;
                    if (text.indexOf('0') >= 0) zeroCount = 1;
                }
            }
        });
    }

    private void buttonsForConstants() {
        Set<Map.Entry<String, Button>> constants = btnConstants.entrySet();
        for (Map.Entry<String, Button> constant : constants) {
            final String key = constant.getKey();

            constant.getValue().setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    logButton(key);
                    char lastChar = getLastChar();
                    String text = key;

                    if (DIGITS.indexOf(lastChar) >= 0
                        || CONSTANTS.indexOf(lastChar) >= 0) {
                        text = "*" + text;
                    }

                    if (lastChar != '.') {
                        appendToDisplay(text);
                        constantSelected = true;
                        digitSelected = false;
                        pointSelected = false;
                        nonZeroBefore = false;
                        zeroCount = 0;
                    }
                }
            });
        }
    }

    private void buttonEqual() {
        btnEqual.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("DefaultLocale")
            public void onClick(View v) {
                logButton("equal");

                if (countBrackets[0] != countBrackets[1]) {
                    displayError.setText(BRACKETS_ERROR);
                    Log.e(LOG_TAG, BRACKETS_ERROR);
                    return;
                }

                String expressionString = display.getText().toString();

                if (expressionString.equals("")) return;

                expressionString = expressionString.replace("log", "log2");
                expressionString = expressionString.replace("ln", "log");

                Log.d(LOG_TAG, "Final expression => " + expressionString);

                Expression expression = new ExpressionBuilder(expressionString).build();

                try {
                    double floatingValue = expression.evaluate();

                    if (Double.isNaN(floatingValue)) {
                        displayError.setText(FUNCTIONS_ERROR);
                        Log.e(LOG_TAG, FUNCTIONS_ERROR);
                        return;
                    }

                    long integerValue = (long) floatingValue;

                    if (floatingValue == integerValue) {
                        display.setText(String.format("%d", integerValue));
                    } else {
                        display.setText(String.format("%s", floatingValue));
                    }

                    constantSelected = false;
                    digitSelected = true;
                    pointSelected = false;
                    nonZeroBefore = false;

                    if (integerValue == 0) zeroCount = 1;
                    else zeroCount = 0;

                    clearDisplayError();
                    scrollToLeft();
                } catch (Exception e) {
                    if (e.getMessage() != null) {
                        displayError.setText(e.getMessage());
                    }
                    Log.e(LOG_TAG, e.toString());
                }
            }
        });
    }
}
