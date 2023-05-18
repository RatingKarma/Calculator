import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Calculator extends JFrame {
    private JButton ZeroBtn;            // number button: 0
    private JButton[] NumberButton;     // number buttons: 1 - 9
    private JButton[] OperatorButton;   // operator buttons
    private JRadioButton Oct;           // octal radix radio button
    private JRadioButton Dec;           // decimal radix radio button
    private JTextArea Text;             // text area
    private Font UniFont;               // universe font in calculator
    private int Radix = 10;             // radix system

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
     * initialization all components
     * */
    private void ComponentInit() {
        int InitIdx;

        // set universal font in calculator
        UniFont = new Font("Consolas", Font.BOLD, 20);

        // number buttons initialization
        NumberButton = new JButton[9];
        for (InitIdx = NumberButton.length - 1; InitIdx >= 0; --InitIdx) {
            int idx = NumberButton.length - InitIdx - 1;
            NumberButton[idx] = new JButton(String.valueOf(InitIdx + 1));
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

        // operator buttons initialization
        int CLEAR = 0;          // for 'C' button index
        int BACKSPACE = 1;      // for '←' button index
        int PLUS = 2;           // for '+' button index
        int MINUS = 3;          // for '-' button index
        int EQUAL = 4;          // for '=' button index
        OperatorButton = new JButton[5];
        OperatorButton[CLEAR] = new JButton("C");       // 'C' button
        OperatorButton[BACKSPACE] = new JButton("←");   // '←' button
        OperatorButton[PLUS] = new JButton("+");        // '+' button
        OperatorButton[MINUS] = new JButton("-");       // '-' button
        OperatorButton[EQUAL] = new JButton("=");       // '=' button
        for (InitIdx = 0; InitIdx != OperatorButton.length; ++InitIdx) {
            int idx = InitIdx;
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
                            DropLastChar();     // drop the last character of Text
                    case "C" ->                 // if users click 'C' button, clear Text
                            Text.setText("");
                    default -> Text.append(OperatorButton[idx].getText());
                    // if users click other buttons,
                    // Text appends button's number or operator directly
                }
                Text.requestFocusInWindow();
            });
        }

        // Oct and Dec JRadioButtons initialization
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

        Oct.addActionListener(e -> OctSelected());
        Dec.addActionListener(e -> DecSelected());

        // Text JTextArea initialization
        Text = new JTextArea();
        Text.setRows(1);
        Text.setFont(UniFont);

        // Text can't be edited by users themselves
        Text.setEditable(false);

        // Text auto line wrap
        Text.setLineWrap(true);
        Text.setBackground(Color.WHITE);

        // set Text size
        Text.setPreferredSize(new Dimension(400, 60));
        Text.requestFocusInWindow();
        Text.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                char Input = e.getKeyChar();
                switch (Radix) {
                    case 8 -> {     // octal input: 0 - 7 and +,- operators are valid
                        if (Input >= '0' && Input <= '7' || Input == '+' || Input == '-') {
                            Text.append(String.valueOf(Input));
                        }
                    }
                    case 10 -> {    // decimal input: 0 - 9 and +- operators are valid
                        if (Input >= '0' && Input <= '9' || Input == '+' || Input == '-') {
                            Text.append(String.valueOf(Input));
                        }
                    }
                }
                if (Input == '\n' || Input == '=') { // get answer
                    AnswerDisplay();
                } else if (Input == '\b') {          // backspace
                    DropLastChar();
                } else if (Input == 'c' || Input == 'C') {   // clear Text
                    Text.setText("");
                } else if (Input == 31) {
                    // Ctrl + A == (ASCII)31
                    // select all content in Text
                    Text.selectAll();
                } else if (Input == 3) {
                    // Ctrl + C == (ASCII)3
                    // copy selected content in Text
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
                } else if (Input == 27) {
                    // ESC == (ASCII)27
                    // if users press ESC and Text content is empty, close the frame
                    if (Text.getText().isEmpty()) {
                        dispose();
                    } else {
                        // if content is not empty, clear the Text
                        Text.setText("");
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

        // two ratio buttons only take two places,
        // the rest places are empty
        RadixPanel.add(Oct);
        RadixPanel.add(Dec);
        RadixPanel.setBackground(Color.WHITE);

        // ratio buttons are at BorderLayout.West
        this.add(RadixPanel, BorderLayout.WEST);

        // menu initialization
        JMenuBar MenuBar = new JMenuBar();
        JMenuItem MenuItem = new JMenuItem("关于");
        JMenu Menu = new JMenu("帮助");
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
        NumbersPanel.add(ZeroBtn);      // set '0' button at the middle position
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
     * @return Optional<Integer> the final answer of the expression
     * @exception expression may be invalid
     * */
    private Optional<Integer> GetTextAns() {
        char op;
        int lhs, rhs;
        String expression = Text.getText();

        // use regex to split numbers and operands from expression,
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
        int negative = 1;
        try {
            if (tokens[0].equals("-")) {
                negative = -1;
                ++idx;
            }
            lhs = negative * Integer.parseInt(tokens[idx++], Radix);
            for (; idx < tokens.length; idx += 2) {
                op = tokens[idx].charAt(0);
                rhs = Integer.parseInt(tokens[idx + 1], Radix);
                switch (op) {
                    case '+' -> lhs += rhs;
                    case '-' -> lhs -= rhs;
                }
            }
        } catch (Exception exp) {
            CreateNewDialog("非法输入", "in " + exp.getMessage().substring(10));
            return Optional.empty();
        }
        return Optional.of(lhs);
    }

    /* *
     * Display final answer on Text
     * */
    private void AnswerDisplay() {
        Optional<Integer> ret = GetTextAns();
        Text.setText("");
        // judge whether ret has value
        if (ret.isPresent()) {
            int result = ret.get();
            switch (Radix) {
                // Text displays different answer according to the radix
                case 8 -> {
                    if (result < 0) {
                        result = -result;
                        Text.append("-");
                    }
                    Text.append(Integer.toOctalString(result));
                }
                case 10 -> Text.setText(Integer.toString(result));
            }
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
        JTextArea ContentText = new JTextArea(Content);
        BorderLayout DialogLayout = new BorderLayout();
        JPanel DialogPanel = new JPanel();
        JButton OKButton = new JButton("返回");
        ContentText.setRows(3);
        ContentText.setColumns(15);
        ContentText.setLineWrap(true);
        ContentText.setFont(UniFont);
        ContentText.setEditable(false);
        ContentText.setBackground(Color.WHITE);
        ContentText.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                char Input = e.getKeyChar();
                if (Input == '\n' || Input == 27) {
                    // if users press ENTER or ESC,
                    // close the new dialog window
                    NewDialog.dispose();
                } else if (Input == 31) {
                    ContentText.selectAll();
                } else if (Input == 3) {
                    // copy selected content in ContentText
                    if (!ContentText.getText().isEmpty()) {
                        if (ContentText.getSelectedText() == null) {
                            // if nothing is selected, select all
                            ContentText.selectAll();
                        }
                        // get selected content by class StringSelection
                        StringSelection Selection = new StringSelection(ContentText.getSelectedText());
                        // copy selected content to clipboard by class Clipboard
                        Clipboard Clip = Toolkit.getDefaultToolkit().getSystemClipboard();
                        Clip.setContents(Selection, null);
                    }
                }
            }
        });
        OKButton.addActionListener(e -> NewDialog.dispose());
        OKButton.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                char Input = e.getKeyChar();
                if (Input == '\n' || Input == 27) {
                    // if users press ENTER or ESC,
                    // close the new dialog window
                    NewDialog.dispose();
                }
                ContentText.requestFocusInWindow();
            }
        });
        OKButton.setBackground(Color.WHITE);
        OKButton.setBorderPainted(false);
        OKButton.setFocusPainted(false);
        DialogPanel.setLayout(DialogLayout);
        DialogPanel.add(ContentText, BorderLayout.NORTH);
        DialogPanel.add(OKButton, BorderLayout.SOUTH);
        DialogPanel.setBackground(Color.WHITE);

        // if users don't close the dialog, forbid interaction with the frame
        NewDialog.setModal(true);
        NewDialog.setFont(UniFont);
        NewDialog.setContentPane(DialogPanel);
        NewDialog.setSize(200, 200);

        // set dialog window occurs position
        NewDialog.setLocationRelativeTo(this);
        NewDialog.add(ContentText);
        NewDialog.setResizable(false);
        NewDialog.setVisible(true);
    }

    private void OctSelected() {
        Radix = 8;                  // set radix = 8
        Text.setText("");           // clear Text
        Dec.setSelected(false);     // clear Dec selected state

        // disable number button 8 and number button 9
        NumberButton[0].setEnabled(false);
        NumberButton[1].setEnabled(false);
        Text.requestFocusInWindow();
    }

    private void DecSelected() {
        Radix = 10;                 // set radix = 10
        Text.setText("");           // clear Text
        Oct.setSelected(false);     // clear Oct selected state

        // enable number button 8 and number button 9
        NumberButton[0].setEnabled(true);
        NumberButton[1].setEnabled(true);
        Text.requestFocusInWindow();
    }

    public static void main(String[] args) {
        Calculator c = new Calculator();
        c.Execution();
    }
}
