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
import java.util.Optional;

public class Calculator extends JFrame {
    static final int CLEAR = 0;         // for 'C' button index
    static final int BACKSPACE = 1;     // for '←' button index
    static final int PLUS = 2;          // for '+' button index
    static final int MINUS = 3;         // for '-' button index
    static final int MULTI = 4;         // for '*' button index
    static final int DIVIDE = 5;        // for '/' button index
    static final int EQUAL = 6;         // for '=' button index
    private final JFrame Self = this;   // reference for this object
    private JButton LeftParBtn;         // left parentheses button
    private JButton RightParBtn;        // right parentheses button
    private JButton ZeroBtn;            // number button: 0
    private JButton[] NumberButton;     // number buttons: 1 - 9
    private JButton[] OperatorButton;   // operator buttons
    private JRadioButton Oct;           // octal radix radio button
    private JRadioButton Dec;           // decimal radix radio button
    private JRadioButton Bin;           // binary radix radio button
    private JTextPane Text;             // text area
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

    /**
     * set frame and run
     */
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

    /**
     * initialize buttons
     */
    private void ButtonsInit() {
        int InitIdx;
        NumberButton = new JButton[10];
        for (InitIdx = NumberButton.length - 1; InitIdx >= 0; --InitIdx) {
            int idx = NumberButton.length - InitIdx - 1;
            NumberButton[idx] = new JButton(String.valueOf(InitIdx));
            NumberButton[idx].setFont(UniFont);
            NumberButton[idx].setBorderPainted(false);
            NumberButton[idx].setFocusPainted(false);
            NumberButton[idx].setBackground(Color.WHITE);
            NumberButton[idx].addActionListener(e -> {
                Text.setText(Text.getText() + NumberButton[idx].getText());
                Text.requestFocusInWindow();
            });
        }
        ZeroBtn = new JButton("0");
        LeftParBtn = new JButton("(");
        RightParBtn = new JButton(")");

        ZeroBtn.setFont(UniFont);
        ZeroBtn.setBackground(Color.WHITE);
        ZeroBtn.setBorderPainted(false);
        ZeroBtn.setFocusPainted(false);
        ZeroBtn.addActionListener(e -> {
            Text.setText(Text.getText() + "0");
            Text.requestFocusInWindow();
        });

        LeftParBtn.setFont(UniFont);
        LeftParBtn.setBackground(Color.WHITE);
        LeftParBtn.setBorderPainted(false);
        LeftParBtn.setFocusPainted(false);
        LeftParBtn.addActionListener(e -> {
            Text.setText(Text.getText() + "(");
            Text.requestFocusInWindow();
        });

        RightParBtn.setFont(UniFont);
        RightParBtn.setBackground(Color.WHITE);
        RightParBtn.setBorderPainted(false);
        RightParBtn.setFocusPainted(false);
        RightParBtn.addActionListener(e -> {
            Text.setText(Text.getText() + ")");
            Text.requestFocusInWindow();
        });

        // operator buttons initialization
        OperatorButton = new JButton[7];
        OperatorButton[CLEAR] = new JButton("C");       // 'C' button
        OperatorButton[BACKSPACE] = new JButton("←");   // '←' button
        OperatorButton[PLUS] = new JButton("+");        // '+' button
        OperatorButton[MINUS] = new JButton("-");       // '-' button
        OperatorButton[MULTI] = new JButton("*");       // '*' button
        OperatorButton[DIVIDE] = new JButton("/");      // '/' button
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
                    default -> Text.setText(Text.getText() + OperatorButton[idx].getText());
                    // if users click other buttons,
                    // Text appends button's number or operator directly
                }
                Text.requestFocusInWindow();
            });
        }
    }

    /**
     * initialize radio buttons on the left panel
     */
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

    /**
     * initialize text component
     */
    private void TextInit() {
        // Text JTextPane initialization
        Text = new JTextPane();
        Text.setFont(UniFont);
        Text.setAutoscrolls(true);

        StyledDocument doc = Text.getStyledDocument();
        SimpleAttributeSet right = new SimpleAttributeSet();
        StyleConstants.setAlignment(right, StyleConstants.ALIGN_RIGHT);
        doc.setParagraphAttributes(0, doc.getLength(), right, false);

        // Text can't be edited by users themselves
        Text.setEditable(false);

        // Text auto line wrap
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
                            Text.setText(Text.getText() + Input);
                        }
                    }
                    case 8 -> {     // octal input: 0 - 7 and +,- operators are valid
                        if (Input >= '0' && Input <= '7' || Input == '+' || Input == '-') {
                            Text.setText(Text.getText() + Input);
                        }
                    }
                    case 10 -> {    // decimal input: 0 - 9 and +,- operators are valid
                        if (Input >= '0' && Input <= '9' || Input == '+' || Input == '-') {
                            Text.setText(Text.getText() + Input);
                        }
                    }
                }
                if (Input == '(' || Input == ')') {
                    Text.setText(Text.getText() + Input);
                } else if (Input == '\n' || Input == '=') { // show answer
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
                    // if content is not empty, clear the Text
                    Text.setText("");
                }
                Text.requestFocusInWindow();
            }
        });
    }


    /**
     * initialize all components
     */
    private void ComponentInit() {
        // set universal font in calculator
        UniFont = new Font("黑体", Font.PLAIN, 20);

        ButtonsInit();      // initial buttons
        RadioButtonsInit(); // initial radio buttons
        TextInit();         // initial Text
    }

    /**
     * set component layout
     */
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
         *               (  0  )
         * */
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 3; ++col) {
                NumbersPanel.add(NumberButton[row * 3 + 2 - col]);
            }
        }
        NumbersPanel.add(LeftParBtn);
        NumbersPanel.add(ZeroBtn);      // set '0' button at the middle position
        NumbersPanel.add(RightParBtn);
        NumbersPanel.setBackground(Color.WHITE);

        // number buttons are at BorderLayout Center
        this.add(NumbersPanel, BorderLayout.CENTER);

        // set operator buttons grid layout
        JPanel OperatorPanel = new JPanel();
        GridLayout OperatorLayout = new GridLayout(7, 1);
        OperatorPanel.setPreferredSize(new Dimension(70, 450));
        OperatorPanel.setLayout(OperatorLayout);
        for (var btn : OperatorButton) {
            OperatorPanel.add(btn);
        }
        OperatorPanel.setBackground(Color.WHITE);

        // operator buttons are at BorderLayout East
        this.add(OperatorPanel, BorderLayout.EAST);
    }

    /**
     * Expression parsing method, return the answer
     * @return Optional<Integer> the final answer of the expression,
     *                           answer is empty when expression is invalid
     */
    private Optional<Integer> GetTextAns() {
        return Calculation.calculate(Text.getText(), Radix);
    }

    /**
     * calculation and display result
     */
    private void AnswerDisplay() {
        Optional<Integer> ret = GetTextAns();
        Text.setText("");
        // judge whether ret has value
        if (ret.isPresent()) {
            int result = ret.get();
            if (result < 0) {
                Text.setText(Text.getText() + '-');
            }
            switch (Radix) {
                // Text displays different answer according to the radix
                case 2 -> Text.setText(Text.getText() + Integer.toBinaryString(result));
                case 8 -> Text.setText(Text.getText() + Integer.toOctalString(result));
                case 10 -> Text.setText(Integer.toString(result));
            }
        } else {
            Text.setText("无效表达式");
        }
    }

    /**
     * drop `Text` last character
     */
    private void DropLastChar() {
        String str = Text.getText();
        if (!str.isEmpty()) {
            Text.setText(str.substring(0, str.length() - 1));
        }
    }

    /**
     * When users behavior will create a new dialog window,
     * create one to display some information.
     * The new dialog window will not allow users interact with the frame window
     * until users close the new dialog window
     * @param TitleStr dialog window title string
     * @param Content dialog content string
     * @param MiddleAlign dialog middle align
     * @param BkgColor  dialog background color
     */
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

    /**
     * For decimal mode
     */
    private void DecSelected() {
        Radix = 10;                 // set radix = 10
        Text.setText("");           // clear Text

        // enable all buttons
        for (int i = 0; i < 9; ++i) {
            NumberButton[i].setEnabled(true);
        }
        Text.requestFocusInWindow();
    }

    /**
     * for octalhex mode
     */
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

    /**
     * for binary mode
     */
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
