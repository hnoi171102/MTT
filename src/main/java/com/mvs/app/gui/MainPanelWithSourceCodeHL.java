package com.mvs.app.gui;

import com.mvs.app.gui.component.EmptyNumberFormatter;
import com.mvs.app.gui.component.SourceCodeTextArea;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.NumberFormatter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.io.*;
import java.text.NumberFormat;
import java.util.List;

public class MainPanelWithSourceCodeHL extends JPanel {

    public MainPanelWithSourceCodeHL() {
        core = new Core();

        readDataFile();

        initUI();

        initLookAndFeel();
    }

    protected void initUI() {

        setLayout(new BorderLayout());

        setPreferredSize(new Dimension(1366, 768));

        headPn = createHeadPanel();
        add(headPn, BorderLayout.PAGE_START);

        list = new JList<>();
        list.addListSelectionListener(e -> {
            index = e.getFirstIndex();
            for (int i : lineNumberOfMethods) {
                System.out.println("line: " + i);
            }
            sourceView.scrollToLine(lineNumberOfMethods[index]);
            System.out.println("source: " + methodSignatures[index]);
        });
        list.setFont(new Font("Serif", Font.BOLD, 14));

        //file browser
        fileRoot = new File("/");
        root = new DefaultMutableTreeNode(new FileNode(fileRoot));
        treeModel = new DefaultTreeModel(root);

        tree = new JTree(treeModel);
        tree.setShowsRootHandles(true);
        tree.setModel(null);

        JScrollPane scrollPane = new JScrollPane(tree);

        // add title for scrollPane
        TitledBorder title = BorderFactory.createTitledBorder("File Browser");
        title.setTitleFont(new Font("Arial", Font.PLAIN, 14));
        // set center alignment for title
        title.setTitleJustification(TitledBorder.CENTER);
        scrollPane.setBorder(title);

        tree.addTreeSelectionListener(e -> {
            // TODO Auto-generated method stub
            TreePath tp = e.getPath();
            if (tp != null) {
                DefaultMutableTreeNode obj = (DefaultMutableTreeNode) tp.getLastPathComponent();
                Object file1 = obj.getUserObject();

                if (file1 instanceof FileNode) {

                    FileNode node = (FileNode) file1;
                    file = node.getFile();

                    if (file != null) {

                        if (file.getParent() != null) {
                            recentDirectory = file.getParent();
                            writeDataFile();
                        }

                        try {
                            loadSourceCode();

                            core = new Core(file.getAbsolutePath());
                            core.setLoop(loop);
                            core.create();

                            methodSignatures = core.getMethodSignatures();
                            lineNumberOfMethods = core.getLineNumberOfMethods();
                            list.setListData(methodSignatures);
                        } catch (FileNotFoundException e1) {
                            e1.printStackTrace();
                        }

                    }
                }
            }
        });

        JPanel constraintPanel = createConstraintsPanel();

        JPanel logPanel = createLogPanel();

        JPanel sourcePanel = createSourceViewPanel();

        JPanel functionList = createFunctionListPanel();

        JSplitPane splitPane0 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, functionList, sourcePanel);
        splitPane0.setDividerLocation(300);

        JSplitPane splitPane1 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, logPanel, constraintPanel);
        splitPane1.setDividerLocation(300);

        JSplitPane splitPane2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, splitPane0, splitPane1);
        splitPane2.setDividerLocation(400);

        JPanel temp = new JPanel(new BorderLayout());
        temp.setPreferredSize(new Dimension(800, 900));
        temp.add(splitPane2, BorderLayout.CENTER);

        JSplitPane splitPane3 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane, temp);
        splitPane3.setDividerLocation(300);

        add(splitPane3, BorderLayout.CENTER);
    }

    private JPanel createHeadPanel() {
        JPanel head = new JPanel();
        openBtn = new JButton("Open Folder/File");

        openBtn.addActionListener(e -> {

            JFileChooser chooser = new JFileChooser();

            chooser.setCurrentDirectory(new File(recentDirectory));

            chooser.setDialogTitle("Please choose a file or project");
            chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            FileNameExtensionFilter filter = new FileNameExtensionFilter("C file", "c");
            chooser.addChoosableFileFilter(filter);
            chooser.setAcceptAllFileFilterUsed(false);

            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                fileRoot = chooser.getSelectedFile();
            } else {
                System.out.println("No Selection ");
                return;
            }

            if (fileRoot.isDirectory())
                recentDirectory = fileRoot.getAbsolutePath();
            else if (fileRoot.getParent() != null)
                recentDirectory = fileRoot.getParent();
            writeDataFile();

            root = new DefaultMutableTreeNode(new FileNode(fileRoot));
            treeModel = new DefaultTreeModel(root);

            tree.setModel(treeModel);
            tree.setShowsRootHandles(true);
            //      JScrollPane scrollPane = new JScrollPane(tree);
            CreateChildNodes ccn =
                    new CreateChildNodes(fileRoot, root);
            new Thread(ccn).start();
        });

        head.add(openBtn);

        refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refresh());
        head.add(refreshBtn);

        verificationBtn = new JButton("Verification");
        verificationBtn.addActionListener(arg0 -> verification());
        head.add(verificationBtn);

        NumberFormat format = NumberFormat.getInstance();
        //    NumberFormatter formatter = new NumberFormatter(format);
        NumberFormatter formatter = new EmptyNumberFormatter(format);
        formatter.setValueClass(Integer.class);
        formatter.setMinimum(01);
        formatter.setMaximum(Integer.MAX_VALUE);
        formatter.setAllowsInvalid(false);
        // If you want the value to be committed on each keystroke instead of focus lost
        formatter.setCommitsOnValidEdit(true);
        /*loopField = new JFormattedTextField(formatter);
        loopField.setColumns(4);
        loopField.setText(loop + "");

        JLabel label = new JLabel("Loop: ");

        head.add(label);
        head.add(loopField);

        loopField.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyReleased(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == Event.ENTER) {


                    String text = loopField.getText();

                    if (text.equals("")) {
                        loopField.setText(loop + "");
                    } else {
                        loop = Integer.parseInt(text);
                        System.out.println("loop: " + loop);
                    }

                    MainPanelWithSourceCodeHL.this.requestFocusInWindow();
                }
            }
        });*/

        return head;
    }

    private JPanel createConstraintsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        resultTA = new JTextArea();
        //	resultTA.setEditable(false);

        smtInput = new JTextArea();
        //	smtInput.setEditable(false);

        smtLog = new JTextArea();
        //	smtLog.setEditable(false);

        metaSMT = new JTextArea();
        //	metaSMT.setEditable(false);

        aboutMe = new JTextArea();
        aboutMe.setEditable(false);

        JTabbedPane tabbedPane = new JTabbedPane();

        try {

            Font font = new Font("Arial", Font.PLAIN, 14);
            UIManager.getDefaults().put("TabbedPane.font", new FontUIResource(font));
        } catch (Exception e) {
            e.printStackTrace();
        }

        tabbedPane.add("Report", new JScrollPane(resultTA));
        tabbedPane.add("SMT input", new JScrollPane(smtInput));
        tabbedPane.add("Solver log", new JScrollPane(smtLog));
        tabbedPane.add("MetaSMT", new JScrollPane(metaSMT));
        tabbedPane.add("About me", new JScrollPane(aboutMe));
        aboutMe.setText("This tool is developed by Kieu Van Tuyen, \n"
                + "a student of University of Engineering and Technology - VNU.\n"
                + "This tool is used for verifying C programs and refer to the GUI from the VTSE tool.\n"
                + "Version: 1.0\n"
                + "Contact: 19021387@vnu.edu.vn");

        panel.add(tabbedPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createFunctionListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Function List");
        // set center alignment of title
        title.setHorizontalAlignment(JLabel.CENTER);
        panel.add(title, BorderLayout.PAGE_START);

        title.setFont(new Font("Arial", Font.PLAIN, 14));
        JScrollPane spList = new JScrollPane(list);

        panel.add(spList, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createLogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Constraints");
        // set center alignment of title
        title.setHorizontalAlignment(JLabel.CENTER);
        title.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(title, BorderLayout.PAGE_START);

        JLabel label1 = new JLabel("Pre-condition:");
        label1.setFont(new Font("Arial", Font.ITALIC, 14));
        JLabel label2 = new JLabel("User's Assertion");
        label2.setFont(new Font("Arial", Font.ITALIC, 14));
        preconditionTA = new JTextArea();
        userAssertionTA = new JTextArea();
        JScrollPane spConstraint1 = new JScrollPane(preconditionTA);

        JScrollPane spConstraint = new JScrollPane(userAssertionTA);

        JSplitPane tmp1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, label1, spConstraint1);
        tmp1.setDividerLocation(30);
        JSplitPane tmp2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, label2, spConstraint);
        tmp2.setDividerLocation(30);
        JSplitPane tmp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tmp1, tmp2);
        tmp.setDividerLocation(200);
        panel.add(tmp, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createSourceViewPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Source code");
        // set center alignment for title
        title.setHorizontalAlignment(JLabel.CENTER);
        title.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(title, BorderLayout.PAGE_START);

        //	sourceView = new RSyntaxTextArea();
        sourceView = new SourceCodeTextArea();
        sourceView.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_C);
        sourceView.setCodeFoldingEnabled(true);
        //sourceView.setEditable(false);
        RTextScrollPane sp = new RTextScrollPane(sourceView);

        panel.add(sp, BorderLayout.CENTER);
//		panel.setPreferredSize(new Dimension(600, 400));

        return panel;
    }

    private void loadSourceCode() {
        loadFileToTextArea(file, sourceView);
    }

    private void loadSMTInput() {
        loadFileToTextArea(SMTINPUT_DIR + function.getDeclarator().getName().toString() + ".txt", smtInput);
    }

    private void loadMetaFile() {
        loadFileToTextArea("metaSMT.txt", metaSMT);
    }

    private void loadFileToTextArea(String path, JTextArea textArea) {
        File file = new File(path);
        loadFileToTextArea(file, textArea);
    }

    private void loadFileToTextArea(File file, JTextArea textArea) {
        System.out.println("file: " + file.getName());
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String nextLine = "";
            textArea.setText("");
            while (true) {
                nextLine = br.readLine();
                if (nextLine != null)
                    textArea.append(nextLine + "\n");
                else
                    break;

            }
            textArea.setCaretPosition(0);
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readDataFile() {
        dataFile = new File(dateFilePath);

        try {
            BufferedReader bf = new BufferedReader(new FileReader(dataFile));
            recentDirectory = bf.readLine();
            bf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeDataFile() {
        dataFile = new File(dateFilePath);
        if (recentDirectory == null)
            return;

        try {
            BufferedWriter bf = new BufferedWriter(new FileWriter(dataFile));
            System.out.println(recentDirectory);
            bf.write(recentDirectory);
            bf.flush();
            bf.close();
        } catch (IOException e) {
            //		e.printStackTrace();
        }
    }

    private void verification() {
        /*String text = loopField.getText();

        if (text.equals("")) {
            loopField.setText(loop + "");
        } else {
            loop = Integer.parseInt(text);
            System.out.println("loop: " + loop);
        }*/

        String precondition = preconditionTA.getText();
        /*String userAssertion = userAssertionTA.getText();*/
        String userAssertion = "return = 0";
        userAssertionTA.setText(userAssertion);

        /*if (userAssertion.equals("")) {
            JOptionPane.showMessageDialog(MainPanelWithSourceCodeHL.this,
                    "User's assertion aren't empty");
        }

        if (index < 0) {
            JOptionPane.showMessageDialog(MainPanelWithSourceCodeHL.this,
                    "You must choose a method!");
            return;
        }*/
        try {
            resultTA.setText("");
            smtInput.setText("");
            smtLog.setText("");

            List<String> outputList = core.runSolver(userAssertion, precondition);
            List<String> solverLog = core.getSolverLog();

            String state = outputList.get(0);
            System.out.println("state: " + state);
            if (precondition.equals("")) {    // has not precondition
                if (state.equals("unsat")) {
                    resultTA.setText(ALWAYS_TRUE_REPORT);
                    if (outputList.size() == 2) {
                        resultTA.append("\nand bound is " + loop);
                    }
                } else if (state.equals("unknown")) {

                    resultTA.setText("Unknown");
                } else {
                    resultTA.append(NOT_ALWAYS_TRUE_REPORT + "\n");
                    for (int i = 1; i < outputList.size(); i++) {
                        resultTA.append(outputList.get(i) + "\n");
                    }
                }
            } else {        //  has precondition
                if (state.equals("unsat")) {
                    resultTA.setText(ALWAYS_TRUE_REPORT_WITH_PRECONDITION);
                    if (outputList.size() == 2) {
                        resultTA.append("\nand bound is " + loop);
                    }
                } else if (state.equals("unknown")) {
                    resultTA.setText("Unknown");
                } else {
                    resultTA.append(NOT_ALWAYS_TRUE_REPORT_WITH_PRECONDITION + "\n");
                    for (int i = 1; i < outputList.size(); i++) {
                        resultTA.append(outputList.get(i) + "\n");
                    }
                }
            }

            loadSMTInput();

            loadMetaFile();

            smtLog.setText("");
            for (String str : solverLog) {
                smtLog.append(str + "\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(MainPanelWithSourceCodeHL.this, e.getMessage());
        }

    }

    private void refresh() {
        if (file == null)
            return;
        try {
            loadSourceCode();

            core = new Core(file.getAbsolutePath());
            core.setLoop(loop);
            core.create();

            methodSignatures = core.getMethodSignatures();
            lineNumberOfMethods = core.getLineNumberOfMethods();
            list.setListData(methodSignatures);

            resultTA.setText("");
            smtInput.setText("");
            smtLog.setText("");
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
    }

    /*public int getLinePosition(int lineNumber) {
        Vector lineLength = new Vector();
        String txt = sourceView.getText();
        int width = sourceView.getWidth();
        StringTokenizer st = new StringTokenizer(txt, "\n ", true);
        String str = " ";
        int len = 0;
        lineLength.addElement(new Integer(0)); //position of the first line
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            //get the width of the string
            int w = sourceView.getGraphics().getFontMetrics(sourceView.getGraphics().getFont()).stringWidth(str + token);
            if (w > width || token.charAt(0) == '\n') {
                len = len + str.length();
                if (token.charAt(0) == '\n') {
                    lineLength.addElement(new Integer(len)); //position of the line
                } else {
                    lineLength.addElement(new Integer(len - 1)); //position of the line
                }
                str = token;
            } else {
                str = str + token;
            }

        }

        return len;
    }*/

    private void initLookAndFeel() {

        UIManager.LookAndFeelInfo[] infos = UIManager.getInstalledLookAndFeels();
/*        for (UIManager.LookAndFeelInfo lak : infos) {
            System.out.println("class name: " + lak.getClassName());
        }*/
        try {
            UIManager.setLookAndFeel(infos[3].getClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                 | UnsupportedLookAndFeelException e1) {
            e1.printStackTrace();
        }
        SwingUtilities.updateComponentTreeUI(MainPanelWithSourceCodeHL.this);
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame(title);
            frame.setLayout(new BorderLayout());
            JPanel panel = new MainPanelWithSourceCodeHL();

            frame.add(panel);
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

            //	frame.setUndecorated(true);		// full screen
            //	frame.setPreferredSize(new Dimension(1000, 400));
            frame.pack();
            frame.setVisible(true);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        });
    }

    // ham de thuc hien file browser
    public static class FileNode {

        private final File file;

        public FileNode(File file) {
            this.file = file;
        }

        public File getFile() {
            return file;
        }

        @Override
        public String toString() {
            String name = file.getName();
            if (name.equals("")) {
                return file.getAbsolutePath();
            } else {
                return name;
            }
        }
    }

    public class CreateChildNodes implements Runnable {

        private final DefaultMutableTreeNode root;

        private final File fileRoot;

        public CreateChildNodes(File fileRoot,
                                DefaultMutableTreeNode root) {
            this.fileRoot = fileRoot;
            this.root = root;
        }

        @Override
        public void run() {
            createChildren(fileRoot, root);
        }

        private void createChildren(File fileRoot,
                                    DefaultMutableTreeNode node) {
            File[] files = fileRoot.listFiles();
            if (files == null) return;

            for (File file : files) {
                if (!isCFile(file) && !file.isDirectory())
                    continue;

                DefaultMutableTreeNode childNode =
                        new DefaultMutableTreeNode(new FileNode(file));
                node.add(childNode);
                if (file.isDirectory()) {
                    createChildren(file, childNode);
                }
            }
        }
    }

    private boolean isCFile(File file) {
        if (file == null || file.isDirectory())
            return false;
        String name = file.getName();
        String extension;

        String[] temp = name.split("\\.");

        if (temp.length < 2)    // file ko co tail
            return false;
        extension = temp[temp.length - 1];
        return extension.equals("c");
    }

    Core core;

    String dateFilePath = "data";
    File dataFile;
    String recentDirectory;
    File file;

    JPanel headPn;
    JButton openBtn;
    JButton verificationBtn;
    JButton refreshBtn;

    JList<String> list;
    String[] methodSignatures;    // list tên các method
    int[] lineNumberOfMethods;    // line bắt đầu của các method

    JTextArea preconditionTA;
    JTextArea userAssertionTA;
    JTextArea resultTA;
    SourceCodeTextArea sourceView;
    JTextArea smtInput;
    JTextArea smtLog;
    JTextArea metaSMT;
    JTextArea aboutMe;

    private DefaultMutableTreeNode root;

    private DefaultTreeModel treeModel;

    private JTree tree;

    File fileRoot;
    int index = -1;

    static String title = "MVS Tool";

    int loop = 1;

    static IASTFunctionDefinition function;

    static String SMTINPUT_DIR = "./src/main/resources/smt/";

    static String ALWAYS_TRUE_REPORT = "YES, There is no execution path that violates the user's condition.";
    static String NOT_ALWAYS_TRUE_REPORT = "NO, There exists an execution path that violates the user's condition.\n" +
            "A counterexample is generated in the Solver log tab.";
    static String ALWAYS_TRUE_REPORT_WITH_PRECONDITION = "YES, User's assertion is always true with pre-condition";
    static String NOT_ALWAYS_TRUE_REPORT_WITH_PRECONDITION = "NO, User's assertion is not always true with precondition, by a counter example: ";
}