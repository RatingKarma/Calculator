import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Calculator extends JFrame {
    static final int CLEAR = 0;         // for 'C' button index
    static final int BACKSPACE = 1;     // for '←' button index
    static final int PLUS = 2;          // for '+' button index
    static final int MINUS = 3;         // for '-' button index
    static final int EQUAL = 4;         // for '=' button index
    private final JFrame Self = this;   // reference for this object
    private JButton ZeroBtn;            // number button: 0
    private JButton[] NumberButton;     // number buttons: 1 - 9
    private JButton[] OperatorButton;   // operator buttons
    private JRadioButton Oct;           // octal radix radio button
    private JRadioButton Dec;           // decimal radix radio button
    private JRadioButton Bin;           // binary radix radio button
    private JTextArea Text;             // text area
    private Font UniFont;               // universe font in calculator
    private int Radix = 10;             // radix system

    public Calculator() {
        ComponentInit();
        SetComponentsLayout();
    }

    public static void main(String[] args) {
        Calculator c = new Calculator();
        c.Execution();
    }

    // set frame and run
    public void Execution() {
        Point PrevPos = new Point();
        // get frame location
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                PrevPos.x = e.getX();
                PrevPos.y = e.getY();
                Text.requestFocusInWindow();
            }
        });

        // when mouse drags the frame, frame will follow the mouse
        this.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                Point CurrentPos = Self.getLocation();
                Self.setLocation(CurrentPos.x + e.getX() - PrevPos.x,
                        CurrentPos.y + e.getY() - PrevPos.y);
                Text.requestFocusInWindow();
            }
        });
        this.setUndecorated(true);
        this.setLocation(600, 350);
        this.setSize(400, 500);
        this.setBackground(Color.WHITE);
        this.setResizable(false);
        this.setVisible(true);
        Text.requestFocusInWindow();
    }

    // Buttons initialization
    private void ButtonsInit() {
        int InitIdx;
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
                    case "=" ->             // if users click '=' button, display answer
                            AnswerDisplay();
                    case "←" ->             // if users click '←' button,
                            DropLastChar();     // drop the last character of Text
                    case "C" ->             // if users click 'C' button, clear Text
                            Text.setText("");
                    default -> Text.append(OperatorButton[idx].getText());
                    // if users click other buttons,
                    // Text appends button's number or operator directly
                }
                Text.requestFocusInWindow();
            });
        }
    }

    // Radio buttons initialization
    private void RadioButtonsInit() {
        // Bin, Oct and Dec JRadioButtons initialization
        // use ButtonGroup to make sure that only one radix
        // radio button is selected at the same time
        ButtonGroup RadixGroup = new ButtonGroup();
        Bin = new JRadioButton("二进制");
        Oct = new JRadioButton("八进制");
        Dec = new JRadioButton("十进制");
        Bin.setFocusPainted(false);
        Oct.setFocusPainted(false);
        Dec.setFocusPainted(false);
        RadixGroup.add(Bin);
        RadixGroup.add(Oct);
        RadixGroup.add(Dec);

        // Decimal is the default radix, so let radix = 10 firstly
        Radix = 10;
        Bin.setSelected(false);
        Oct.setSelected(false);
        Dec.setSelected(true);
        Bin.setBackground(Color.WHITE);
        Oct.setBackground(Color.WHITE);
        Dec.setBackground(Color.WHITE);

        Bin.addActionListener(e -> BinSelected());
        Oct.addActionListener(e -> OctSelected());
        Dec.addActionListener(e -> DecSelected());
    }

    // Text initialization
    private void TextInit() {
        // Text JTextArea initialization
        Text = new JTextArea();
        Text.setFont(UniFont);
        Text.setAutoscrolls(true);

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
                    case 2 -> {     // binary input: 0 - 1 and +,- operators are valid
                        if (Input == '0' || Input == '1' || Input == '+' || Input == '-') {
                            Text.append(String.valueOf(Input));
                        }
                    }
                    case 8 -> {     // octal input: 0 - 7 and +,- operators are valid
                        if (Input >= '0' && Input <= '7' || Input == '+' || Input == '-') {
                            Text.append(String.valueOf(Input));
                        }
                    }
                    case 10 -> {    // decimal input: 0 - 9 and +,- operators are valid
                        if (Input >= '0' && Input <= '9' || Input == '+' || Input == '-') {
                            Text.append(String.valueOf(Input));
                        }
                    }
                }
                if (Input == '\n' || Input == '=') { // show answer
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
                        Self.dispose();
                        CreateNewDialog("退出", "感谢使用", true, Color.PINK);
                        System.exit(0);
                    } else {
                        // if content is not empty, clear the Text
                        Text.setText("");
                    }
                }
                Text.requestFocusInWindow();
            }
        });
    }


    // initialization all components
    private void ComponentInit() {
        // set universal font in calculator
        UniFont = new Font("黑体", Font.PLAIN, 20);

        ButtonsInit();      // initial buttons
        RadioButtonsInit(); // initial radio buttons
        TextInit();         // initial Text
    }

    // set components layout
    private void SetComponentsLayout() {
        BorderLayout UILayout = new BorderLayout();
        this.setLayout(UILayout);

        // set two ratio buttons layout
        JPanel RadixPanel = new JPanel();
        GridLayout HexLayout = new GridLayout(6, 1);
        RadixPanel.setLayout(HexLayout);

        // two ratio buttons only take two places,
        // the rest places are empty
        RadixPanel.add(Dec);
        RadixPanel.add(Oct);
        RadixPanel.add(Bin);
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
        MenuItem.addActionListener(e -> CreateNewDialog("关于作者", "W", true, Color.PINK));
        Menu.add(MenuItem);
        MenuBar.add(Menu);
        MenuBar.setSize(50, 50);
        MenuBar.setBorderPainted(false);
        MenuBar.setBackground(Color.WHITE);

        // add Text and MenuBar into JFrame
        JPanel TextPanel = new JPanel();
        JPanel MenuPanel = new JPanel();
        JPanel MenuButtonPanel = new JPanel();
        JLabel EmptyLabel = new JLabel();
        JLabel FrameTitle = new JLabel("计算器");
        JButton MinimumButton = new JButton("_");
        JButton CloseButton = new JButton("X");
        GridLayout MenuLayout = new GridLayout();
        GridLayout MenuButtonLayout = new GridLayout(1, 5);
        GridLayout TextLayout = new GridLayout(2, 1);
        MenuLayout.setRows(2);
        FrameTitle.setFont(new Font("黑体", Font.PLAIN, 20));
        CloseButton.setFocusPainted(false);
        CloseButton.setBorderPainted(false);
        CloseButton.setBackground(Color.WHITE);
        CloseButton.addActionListener(actionEvent -> {
            Self.dispose();
            CreateNewDialog("退出", "感谢使用", true, Color.PINK);
            System.exit(0);     // exit the program
        });
        MinimumButton.setFocusPainted(false);
        MinimumButton.setBorderPainted(false);
        MinimumButton.setBackground(Color.WHITE);
        MinimumButton.addActionListener(e -> {
            if (Self.getExtendedState() == JFrame.ICONIFIED) {
                Self.setExtendedState(JFrame.NORMAL);
            } else {
                Self.setExtendedState(JFrame.ICONIFIED);
            }
        });
        MenuButtonPanel.setBackground(Color.WHITE);
        MenuButtonPanel.setLayout(MenuButtonLayout);
        MenuButtonPanel.add(EmptyLabel);
        MenuButtonPanel.add(MinimumButton);
        MenuButtonPanel.add(CloseButton);
        MenuPanel.setLayout(MenuLayout);
        MenuPanel.setBackground(Color.WHITE);
        MenuPanel.add(FrameTitle);
        MenuPanel.add(MenuButtonPanel);
        MenuPanel.add(MenuBar);
        TextPanel.add(MenuPanel);
        TextPanel.add(Text);
        TextPanel.setLayout(TextLayout);
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
        NumbersPanel.setBackground(Color.WHITE);

        // number buttons are at BorderLayout Center
        this.add(NumbersPanel, BorderLayout.CENTER);

        // set operator buttons grid layout
        JPanel OperatorPanel = new JPanel();
        GridLayout OperatorLayout = new GridLayout(5, 1);
        OperatorPanel.setPreferredSize(new Dimension(70, 300));
        OperatorPanel.setLayout(OperatorLayout);
        for (var btn : OperatorButton) {
            OperatorPanel.add(btn);
        }
        OperatorPanel.setBackground(Color.WHITE);

        // operator buttons are at BorderLayout East
        this.add(OperatorPanel, BorderLayout.EAST);
    }

    /* *
     * @brief Expression parsing method, return the answer
     *
     * @return:Optional<Integer> the final answer of the expression,
     *                           answer is empty when expression is invalid
     *
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

        try {   // try to parse expression
            if (tokens.length == 0) {
                throw new NumberFormatException("未检测到操作数");
            }
            int idx = 0;
            int negative = 1;
            if (tokens[0].equals("-")) {
                negative = -1;
                ++idx;
            }
            // valid expression means tokens.length is odd
            if ((tokens.length & 1) == 0) {
                throw new NumberFormatException("未检测到操作数");
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
        } catch (NumberFormatException exp) {
            String ExpStr = exp.getMessage();
            String ContentStr;
            if (ExpStr.startsWith("For")) {
                if (ExpStr.charAt(19) >= '0' && ExpStr.charAt(19) <= '9') {
                    ContentStr = "操作数超过范围";
                } else {
                    ContentStr = "非法操作符";
                }
            } else {
                ContentStr = ExpStr;
            }
            CreateNewDialog("错误", ContentStr, false, Color.orange);
            return Optional.empty();    // invalid expression, return empty object
        }
        return Optional.of(lhs);
    }

    // Display final answer on Text
    private void AnswerDisplay() {
        Optional<Integer> ret = GetTextAns();
        Text.setText("");
        // judge whether ret has value
        if (ret.isPresent()) {
            int result = ret.get();
            if (result < 0) {
                result = -result;
                Text.append("-");
            }
            switch (Radix) {
                // Text displays different answer according to the radix
                case 2 -> Text.append(Integer.toBinaryString(result));
                case 8 -> Text.append(Integer.toOctalString(result));
                case 10 -> Text.setText(Integer.toString(result));
            }
        }
    }

    // Drop the last character of Text
    private void DropLastChar() {
        String str = Text.getText();
        if (!str.isEmpty()) {
            Text.setText(str.substring(0, str.length() - 1));
        }
    }

    /* *
     * When users behavior will create a new dialog window,
     * create one to display some information.
     * The new dialog window will not allow users interact with the frame window
     * until users close the new dialog window
     *
     * @param1:String Dialog's title
     * @param2:String Dialog's content
     * @param3:boolean content align mode
     * @param4:Color NewDialog background color
     * */
    private void CreateNewDialog(String TitleStr, String Content, boolean MiddleAlign, Color BkgColor) {
        JDialog NewDialog = new JDialog(this);
        JTextPane ContentText = new JTextPane();
        JPanel DialogPanel = new JPanel();
        JButton OKButton = new JButton("确定");
        BorderLayout DialogLayout = new BorderLayout();
        ContentText.setFont(UniFont);
        ContentText.setText(Content);
        ContentText.setEditable(false);
        ContentText.setAutoscrolls(true);
        ContentText.setBackground(BkgColor);
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
                Text.requestFocusInWindow();
            }
        });

        // set content string middle alignment
        StyledDocument Doc = ContentText.getStyledDocument();
        if (MiddleAlign) {
            SimpleAttributeSet AttrSet = new SimpleAttributeSet();
            StyleConstants.setFontFamily(AttrSet, UniFont.getFamily());
            StyleConstants.setFontSize(AttrSet, UniFont.getSize());
            StyleConstants.setAlignment(AttrSet, StyleConstants.ALIGN_CENTER);
            Doc.setParagraphAttributes(0, Doc.getLength(), AttrSet, false);
        }
        ContentText.setDocument(Doc);
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
                Text.requestFocusInWindow();
            }
        });
        OKButton.setBackground(BkgColor);
        OKButton.setBorderPainted(false);
        OKButton.setFocusPainted(false);
        DialogPanel.setLayout(DialogLayout);
        DialogPanel.setBackground(BkgColor);
        DialogPanel.add(OKButton, BorderLayout.SOUTH);

        // add title and a close button at NewDialog upper side
        JButton CloseButton = new JButton("X");
        GridLayout MenuLayout = new GridLayout(1, 4);
        JPanel MenuPanel = new JPanel();
        CloseButton.setBackground(BkgColor);
        CloseButton.setFocusPainted(false);
        CloseButton.setBorderPainted(false);
        CloseButton.addActionListener(e -> {
            NewDialog.dispose();
            Text.requestFocusInWindow();
        });
        CloseButton.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                char Input = e.getKeyChar();
                if (Input == '\n' || Input == 27) {
                    // if users press ENTER or ESC,
                    // close the new dialog window
                    NewDialog.dispose();
                }
                Text.requestFocusInWindow();
            }
        });
        MenuPanel.setLayout(MenuLayout);
        MenuPanel.add(new JLabel(TitleStr));
        MenuPanel.add(new JLabel());
        MenuPanel.add(new JLabel());
        MenuPanel.add(CloseButton);
        MenuPanel.setBackground(BkgColor);
        DialogPanel.add(MenuPanel, BorderLayout.NORTH);

        // if users don't close the dialog, forbid interaction with the frame
        NewDialog.setModal(true);
        NewDialog.setFont(UniFont);
        NewDialog.setContentPane(DialogPanel);
        NewDialog.setSize(200, 130);

        // set dialog window occurs position
        // the dialog window will occur at the middle of the frame
        NewDialog.setLocationRelativeTo(this);
        NewDialog.setUndecorated(true);
        NewDialog.add(ContentText);
        NewDialog.setResizable(false);
        NewDialog.setVisible(true);
    }

    // if Dec radio button is selected, execute this method
    private void DecSelected() {
        Radix = 10;                 // set radix = 10
        Text.setText("");           // clear Text

        // enable all buttons
        for (int i = 0; i < 9; ++i) {
            NumberButton[i].setEnabled(true);
        }
        Text.requestFocusInWindow();
    }

    // if Oct radio button is selected, execute this method
    private void OctSelected() {
        Radix = 8;                  // set radix = 8
        Text.setText("");           // clear Text

        // disable number button 8 and number button 9
        NumberButton[0].setEnabled(false);
        NumberButton[1].setEnabled(false);
        for (int i = 2; i < 9; ++i) {
            NumberButton[i].setEnabled(true);
        }
        Text.requestFocusInWindow();
    }

    // if Bin radio button is selected, execute this method
    private void BinSelected() {
        Radix = 2;                  // set radix = 2
        Text.setText("");           // clear Text

        // disable number buttons from 2 to 9
        for (int i = 0; i != 8; ++i) {
            NumberButton[i].setEnabled(false);
        }
        Text.requestFocusInWindow();
    }
}
