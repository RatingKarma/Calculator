import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Calculator extends JFrame {
    private JButton[] NumberButton;     // number buttons: 1 - 9
    private JButton ZeroBtn;            // number button: 0
    private JButton[] OperatorButton;   // operator buttons
    private JRadioButton Oct;           // octal radix radio button
    private JRadioButton Dec;           // decimal radix radio button
    private JTextArea Text;             // text area
    private Font UniFont;               // universe font in calculator
    private int Radix = 10;             // radix system

    private enum OpIdx {    // operator buttons index
        CLEAR,      // for 'C' button index
        BACKSPACE,  // for '←' button index
        PLUS,       // for '+' button index
        MINUS,      // for '-' button index
        EQUAL,      // for '=' button index
    }

    public Calculator() {
        ComponentInit();
        SetComponentsLayout();
    }

    /* *
     * set frame and run
     * */
    public void Execution() {
        Text.requestFocusInWindow();
        this.setTitle("计算器");
        this.setLocation(600, 350);
        this.setSize(400, 500);
        this.setBackground(Color.WHITE);
        this.setResizable(false);
        this.setVisible(true);
    }

    /* *
     * initialize all components
     * */
    private void ComponentInit() {
        int initIdx;

        // set universal font in calculator
        UniFont = new Font("Consolas", Font.BOLD, 20);

        // number buttons initialize
        NumberButton = new JButton[9];
        for (initIdx = NumberButton.length - 1; initIdx >= 0; --initIdx) {
            int idx = NumberButton.length - initIdx - 1;
            NumberButton[idx] = new JButton(String.valueOf(initIdx + 1));
            NumberButton[idx].setFont(UniFont);
            NumberButton[idx].setBorderPainted(false);
            NumberButton[idx].setFocusPainted(false);
            NumberButton[idx].setBackground(Color.WHITE);
            NumberButton[idx].addActionListener(e -> {
                Text.append(NumberButton[idx].getText());
                Text.requestFocusInWindow();
            });
        }
        ZeroBtn = new JButton("0");
        ZeroBtn.setFont(UniFont);
        ZeroBtn.setBackground(Color.WHITE);
        ZeroBtn.setBorderPainted(false);
        ZeroBtn.setFocusPainted(false);
        ZeroBtn.addActionListener(e -> {
            Text.append("0");
            Text.requestFocusInWindow();
        });

        // operator buttons initialize
        OperatorButton = new JButton[5];
        OperatorButton[OpIdx.CLEAR.ordinal()] = new JButton("C");       // 'C' button
        OperatorButton[OpIdx.BACKSPACE.ordinal()] = new JButton("←");   // '←' button
        OperatorButton[OpIdx.PLUS.ordinal()] = new JButton("+");        // '+' button
        OperatorButton[OpIdx.MINUS.ordinal()] = new JButton("-");       // '-' button
        OperatorButton[OpIdx.EQUAL.ordinal()] = new JButton("=");       // '=' button
        for (initIdx = 0; initIdx != OperatorButton.length; ++initIdx) {
            int idx = initIdx;
            OperatorButton[idx].setFont(UniFont);
            OperatorButton[idx].setBorderPainted(false);
            OperatorButton[idx].setFocusPainted(false);
            OperatorButton[idx].setBackground(Color.WHITE);
            OperatorButton[idx].addActionListener(e -> {
                String str = OperatorButton[idx].getText();
                switch (str) {
                    case "=" ->                 // if users click '=' button, display answer
                            AnswerDisplay();
                    case "←" ->                 // if users click '←' button,
                            DropLastChar();         // drop the last character of Text
                    case "C" -> Text.setText("");       // if users click 'C' button, clear Text
                    default -> Text.append(OperatorButton[idx].getText());
                    // if users click other buttons,
                    // Text appends buttons' number or operator character directly
                }
                Text.requestFocusInWindow();
            });
        }

        // Oct and Dec JRadioButton initialize
        // use ButtonGroup to make sure that only one
        // radio button is selected at the same time
        ButtonGroup RadixGroup = new ButtonGroup();
        Oct = new JRadioButton("八进制");
        Dec = new JRadioButton("十进制");
        RadixGroup.add(Oct);
        RadixGroup.add(Dec);
        Oct.setFocusPainted(false);
        Dec.setFocusPainted(false);

        // Decimal is the default mode, so set radix = 10
        Radix = 10;
        Oct.setSelected(false);
        Dec.setSelected(true);
        Oct.setBackground(Color.WHITE);
        Dec.setBackground(Color.WHITE);

        Oct.addActionListener(e -> {
            if (Oct.isSelected()) {
                Radix = 8;                  // set radix = 8
                Text.setText("");           // clear text
                Dec.setSelected(false);     // clear Dec selected state

                // disable number button 8 and number button 9
                NumberButton[0].setEnabled(false);
                NumberButton[1].setEnabled(false);
            }
            Text.requestFocusInWindow();
        });

        Dec.addActionListener(e -> {
            if (Dec.isSelected()) {
                Radix = 10;                 // set radix = 10
                Text.setText("");           // clear text
                Oct.setSelected(false);     // clear Oct selected state

                // enable number button 8 and number button 9
                NumberButton[0].setEnabled(true);
                NumberButton[1].setEnabled(true);
            }
            Text.requestFocusInWindow();
        });

        // Text JTextArea initialize
        Text = new JTextArea();
        Text.setRows(1);
        Text.setFont(UniFont);
        // Text can't be edited by users themselves
        Text.setEditable(false);
        Text.setBackground(Color.WHITE);
        // set Text size
        Text.setPreferredSize(new Dimension(400, 60));
        Text.requestFocusInWindow();
        Text.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char Input = e.getKeyChar();
                switch (Radix) {
                    case 8 -> {     // octal input: 0 - 7 and +- operator are valid
                        if (Input >= '0' && Input <= '7' || Input == '+' || Input == '-') {
                            Text.append(String.valueOf(Input));
                        }
                    }
                    case 10 -> {    // decimal input: 0 - 9 and +- operator are valid
                        if (Input >= '0' && Input <= '9' || Input == '+' || Input == '-') {
                            Text.append(String.valueOf(Input));
                        }
                    }
                }
                if (Input == '\n' || Input == '=') { // get answer
                    AnswerDisplay();
                } else if (Input == '\b') {          // backspace
                    DropLastChar();
                } else if (Input == 'C' || Input == 'c') {   // clear Text
                    Text.setText("");
                } else if (Input == 31) {    // select all content in Text
                    Text.selectAll();
                } else if (Input == 3) {     // copy selected content in Text
                    if (!Text.getText().isEmpty()) {    // make sure the Text is not empty firstly
                        if (Text.getSelectedText() == null) {
                            // if nothing is selected, select all
                            Text.selectAll();
                        }
                        // get selected content by class StringSelection
                        StringSelection Selection = new StringSelection(Text.getSelectedText());
                        // copy selected content to clipboard by class Clipboard
                        Clipboard Clip = Toolkit.getDefaultToolkit().getSystemClipboard();
                        Clip.setContents(Selection, null);
                    }
                }
                Text.requestFocusInWindow();
            }
        });
    }

    /* *
     * set components layout
     * */
    private void SetComponentsLayout() {
        BorderLayout UILayout = new BorderLayout();
        this.setLayout(UILayout);

        // set two ratio buttons layout
        JPanel RadixPanel = new JPanel();
        GridLayout HexLayout = new GridLayout(6, 1);
        RadixPanel.setLayout(HexLayout);

        // two ratio buttons only take two places
        // the rest places are empty
        RadixPanel.add(Oct);
        RadixPanel.add(Dec);
        RadixPanel.setBackground(Color.WHITE);

        // ratio buttons are at BorderLayout.West
        this.add(RadixPanel, BorderLayout.WEST);

        // menu initialize
        JMenuBar MenuBar = new JMenuBar();
        JMenu Menu = new JMenu("帮助");
        JMenuItem MenuItem = new JMenuItem("关于");
        Menu.setBorderPainted(false);
        MenuItem.setBackground(Color.WHITE);
        // if users click Menu-About, create a new dialog window
        MenuItem.addActionListener(e -> CreateNewDialog("关于作者", "W"));
        Menu.add(MenuItem);
        MenuBar.add(Menu);
        MenuBar.setBorderPainted(false);
        MenuBar.setBackground(Color.WHITE);

        // add Text and MenuBar into JFrame
        JPanel TextPanel = new JPanel();
        GridLayout TextLayout = new GridLayout(2, 1);
        TextPanel.setLayout(TextLayout);
        TextPanel.add(MenuBar);
        TextPanel.add(Text);
        TextPanel.setBackground(Color.WHITE);
        this.add(TextPanel, BorderLayout.NORTH);

        // set number buttons grid layout
        JPanel NumbersPanel = new JPanel();
        GridLayout numbersLayout = new GridLayout(4, 3);
        NumbersPanel.setLayout(numbersLayout);
        /* add number buttons to panel as following position:
         *               7  8  9
         *               4  5  6
         *               1  2  3
         *                  0
         * */
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 3; ++col) {
                NumbersPanel.add(NumberButton[row * 3 + 2 - col]);
            }
        }
        NumbersPanel.add(new JLabel());
        NumbersPanel.add(ZeroBtn);      // set zero button at the middle position
        NumbersPanel.add(new JLabel());
        NumbersPanel.setBackground(Color.WHITE);

        // number buttons are at BorderLayout Center
        this.add(NumbersPanel, BorderLayout.CENTER);

        // set operator buttons grid layout
        JPanel OperatorPanel = new JPanel();
        OperatorPanel.setPreferredSize(new Dimension(70, 300));
        GridLayout OperatorLayout = new GridLayout(5, 1);
        OperatorPanel.setLayout(OperatorLayout);
        for (var btn : OperatorButton) {
            OperatorPanel.add(btn);
        }
        OperatorPanel.setBackground(Color.WHITE);

        // operator buttons are at BorderLayout East
        this.add(OperatorPanel, BorderLayout.EAST);
    }

    /* *
     * Expression parsing method, return the answer
     * @return the final answer of the expression
     * */
    private int GetTextAns() {
        char op;
        int lhs, rhs;
        String expression = Text.getText();
        // use regex to split numbers and operands from expression
        // then store numbers and operands into an array
        Pattern pattern = Pattern.compile("\\d+|[+-]");
        Matcher matcher = pattern.matcher(expression);
        String[] tokens = new String[0];
        while (matcher.find()) {
            String match = matcher.group();
            tokens = Arrays.copyOf(tokens, tokens.length + 1);
            tokens[tokens.length - 1] = match;
        }

        // parse expression
        int idx = 0;
        int Negative = 1;
        // judge whether the first number is negative
        try {
            if (tokens[0].equals("-")) {
                Negative = -1;
                ++idx;
            }
            lhs = Negative * Integer.parseInt(tokens[idx++], Radix);
            for (; idx < tokens.length; idx += 2) {
                op = tokens[idx].charAt(0);                     // get operator
                rhs = Integer.parseInt(tokens[idx + 1], Radix);
                switch (op) {
                    case '+' -> lhs += rhs;
                    case '-' -> lhs -= rhs;
                }
            }
        } catch (Exception exp) {
            CreateNewDialog("非法输入", exp.getMessage());
            return 0;
        }
        return lhs;
    }

    /* *
     * Display final answer on Text
     * */
    private void AnswerDisplay() {
        int ans = GetTextAns();
        Text.setText("");
        // Text displays different answer
        // based on the radix
        switch (Radix) {
            case 8 -> {
                if (ans < 0) {
                    ans = -ans;
                    Text.setText("-");
                }
                Text.append(Integer.toOctalString(ans));
            }
            case 10 -> Text.setText(Integer.toString(ans));
        }
    }

    /* *
     * Drop the last character of Text
     * */
    private void DropLastChar() {
        String str = Text.getText();
        if (!str.isEmpty()) {
            Text.setText(str.substring(0, str.length() - 1));
        }
    }

    /* *
     * When users behavior will create a new window,
     * create a new dialog window to display some information.
     * The new dialog window will not allow users interact with the frame window
     * until users close the new dialog window
     * @param1 String Dialog's title
     * @param2 String Dialog's content
     * */
    private void CreateNewDialog(String DialogTitle, String Content) {
        JDialog NewDialog = new JDialog(this, DialogTitle);
        JLabel ContentLabel = new JLabel(Content);
        BorderLayout DialogLayout = new BorderLayout();
        JPanel DialogPanel = new JPanel();
        JButton OKButton = new JButton("返回");
        OKButton.addActionListener(e -> NewDialog.dispose());
        OKButton.setBackground(Color.WHITE);
        OKButton.setBorderPainted(false);
        OKButton.setFocusPainted(false);
        ContentLabel.setBackground(Color.WHITE);
        ContentLabel.setHorizontalAlignment(JLabel.CENTER);
        DialogPanel.setLayout(DialogLayout);
        DialogPanel.add(ContentLabel, BorderLayout.NORTH);
        DialogPanel.add(OKButton, BorderLayout.SOUTH);
        DialogPanel.setBackground(Color.WHITE);

        // if users don't close the dialog, forbid interaction with the frame
        NewDialog.setModal(true);
        NewDialog.setFont(UniFont);
        NewDialog.setContentPane(DialogPanel);
        NewDialog.setSize(100, 100);
        NewDialog.setLocationRelativeTo(this);  // set dialog window occurs position
        NewDialog.add(ContentLabel);
        NewDialog.setResizable(false);
        NewDialog.setVisible(true);
    }

    public static void main(String[] args) {
        Calculator c = new Calculator();
        c.Execution();
    }
}
