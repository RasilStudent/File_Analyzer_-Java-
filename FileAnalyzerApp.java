import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.io.File;
import java.nio.file.*;

public class FileAnalyzerApp extends JFrame {

    private JTextArea resultArea;
    private JTree directoryTree;
    private JTextField pathTextField;

    public FileAnalyzerApp() {
        initializeUI();
    }

    private void initializeUI() {
        setTitle("File Analyzer");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        resultArea = new JTextArea();
        directoryTree = new JTree();
        pathTextField = new JTextField(30);

        JButton analyzeButton = new JButton("Analyze");
        analyzeButton.addActionListener(e -> analyzeFiles(pathTextField.getText()));

        JScrollPane treeScrollPane = new JScrollPane(directoryTree);
        JScrollPane resultScrollPane = new JScrollPane(resultArea);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScrollPane, resultScrollPane);
        splitPane.setDividerLocation(200);

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());
        controlPanel.add(new JLabel("Path:"));
        controlPanel.add(pathTextField);
        controlPanel.add(analyzeButton);

        setLayout(new BorderLayout());
        add(controlPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);

        directoryTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) directoryTree.getLastSelectedPathComponent();
                if (selectedNode != null) {
                    Object userObject = selectedNode.getUserObject();
                    if (userObject instanceof String) {
                        String fileName = (String) userObject;
                        String parentPath = getParentPath(selectedNode);
                        File selectedFile = new File(parentPath, fileName);
                        displayFileInfo(selectedFile);
                    } else if (userObject instanceof File) {
                        File selectedFile = (File) userObject;
                        displayFileInfo(selectedFile);
                    }
                }
            }
        });
    }

    private void analyzeFiles(String path) {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(new File(path));
        createDirectoryTree(rootNode, Paths.get(path));
        directoryTree.setModel(new DefaultTreeModel(rootNode));
    }

    private void createDirectoryTree(DefaultMutableTreeNode rootNode, Path directory) {
        try {
            Files.walk(directory, 1)
                    .filter(path -> !path.equals(directory))
                    .forEach(path -> {
                        File file = path.toFile();
                        DefaultMutableTreeNode node = new DefaultMutableTreeNode(file.getName());
                        rootNode.add(node);
                        if (Files.isDirectory(path)) {
                            createDirectoryTree(node, path);
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            resultArea.setText("Error analyzing files and directories.");
        }
    }

    private void displayFileInfo(File file) {
        FileInfo fileInfo = new FileInfo(file);
        resultArea.setText(fileInfo.toString());
    }

    private String getParentPath(DefaultMutableTreeNode node) {
        if (node.getParent() == null) {
            return "";
        }
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
        Object userObject = parent.getUserObject();
        if (userObject instanceof String) {
            return getParentPath(parent) + File.separator + (String) userObject;
        } else {
            return ((File) userObject).getPath();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FileAnalyzerApp fileAnalyzerApp = new FileAnalyzerApp();
            fileAnalyzerApp.setVisible(true);
        });
    }
}
