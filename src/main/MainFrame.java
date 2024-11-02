package main;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import hdfs.HDFSUtils;
import org.apache.hadoop.fs.FileStatus;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

public class MainFrame extends JFrame {

    private JTree directoryTree;
    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode root;
    private String selectedFilePath = null; // 用于保存选中的文件路径
    private JTextArea fileContentArea; // 用于显示文件内容

    public MainFrame() {
        setTitle("简易云盘系统");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 顶部工具栏：包含上传和下载按钮
        JPanel toolbar = new JPanel();
        JButton uploadButton = new JButton("上传");
        JButton downloadButton = new JButton("下载");
        JButton deleteButton = new JButton("删除");
        JButton uploadFolderButton = new JButton("上传文件夹");

        toolbar.add(uploadFolderButton);
        toolbar.add(uploadButton);
        toolbar.add(downloadButton);
        toolbar.add(deleteButton);

        // 左侧目录树初始化
        root = new DefaultMutableTreeNode("HDFS根目录");
        treeModel = new DefaultTreeModel(root);
        directoryTree = new JTree(treeModel);
        JScrollPane treeScrollPane = new JScrollPane(directoryTree);

        // 右侧文本区域初始化
        fileContentArea = new JTextArea();
        fileContentArea.setEditable(false); // 文件内容不可编辑
        JScrollPane contentScrollPane = new JScrollPane(fileContentArea);

        // 使用JSplitPane分割
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScrollPane, contentScrollPane);
        splitPane.setDividerLocation(250);

        // 添加组件到主窗口
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(toolbar, BorderLayout.NORTH);
        getContentPane().add(splitPane, BorderLayout.CENTER);

        // 加载HDFS目录结构
        loadDirectoryTree(root, "/");

        // 上传和下载按钮的事件监听
        uploadButton.addActionListener(e -> {
            try {
                uploadFile();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });
        downloadButton.addActionListener(e -> downloadFile());
        deleteButton.addActionListener(e -> deleteFileOrDirectory());

        // 添加目录树节点选择监听器
        directoryTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) directoryTree.getLastSelectedPathComponent();
                if (selectedNode != null) {
                    // 更新选中文件路径
                    selectedFilePath = getFullPath(selectedNode);
                }
            }
        });
        uploadFolderButton.addActionListener(e -> {
            try {
                uploadDir();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });


        // 添加双击事件监听器
        directoryTree.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) directoryTree.getLastSelectedPathComponent();
                    if (selectedNode != null && selectedFilePath != null) {
                        try {
                            if (!HDFSUtils.isDirectory(selectedFilePath)) {
                                String content = HDFSUtils.readFileContent(selectedFilePath);
                                fileContentArea.setText(content);
                                fileContentArea.setCaretPosition(0); // 滚动到顶部
                            } else {
                                JOptionPane.showMessageDialog(MainFrame.this, "无法显示目录内容，请选择一个文件。", "提示", JOptionPane.WARNING_MESSAGE);
                            }
                        } catch (IOException ex) {
                            JOptionPane.showMessageDialog(MainFrame.this, "无法读取文件内容: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        });
    }

    // 获取选中节点的完整HDFS路径
    private String getFullPath(DefaultMutableTreeNode node) {
        StringBuilder path = new StringBuilder();
        Object[] nodes = node.getUserObjectPath();
        for (int i = 1; i < nodes.length; i++) { // 跳过根节点
            path.append("/").append(nodes[i].toString());
        }
        return path.toString();
    }

    // 加载目录树的具体实现
    private void loadDirectoryTree(DefaultMutableTreeNode node, String path) {
        try {
            for (FileStatus status : HDFSUtils.listFileStatus(path)) {
                DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(status.getPath().getName());
                node.add(childNode);
                if (status.isDirectory()) {
                    loadDirectoryTree(childNode, status.getPath().toString());
                }
            }
            treeModel.reload();
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "加载目录失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 上传文件功能
    private void uploadFile() throws IOException {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("选择要上传的文件");
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            String localPath = fileChooser.getSelectedFile().getAbsolutePath();
            String remotePath = selectedFilePath != null && HDFSUtils.isDirectory(selectedFilePath) ? selectedFilePath : "/";

            try {
                HDFSUtils.upload(localPath, remotePath);
                JOptionPane.showMessageDialog(this, "文件上传成功！", "上传", JOptionPane.INFORMATION_MESSAGE);
                root.removeAllChildren();
                loadDirectoryTree(root, "/");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "上传文件失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    private void uploadDir() throws IOException {
        JFileChooser folderChooser = new JFileChooser();
        folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        folderChooser.setDialogTitle("选择要上传的文件夹");

        int result = folderChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            String localFolderPath = folderChooser.getSelectedFile().getAbsolutePath();

            // 判断上传路径：如果选中的是目录，上传到该目录；否则上传到根目录
            String remotePath;
            try {
                if (selectedFilePath != null && HDFSUtils.isDirectory(selectedFilePath)) {
                    remotePath = selectedFilePath; // 上传到选中的目录
                } else {
                    remotePath = ""; // 没有选中目录时，默认上传到根目录
                }
                HDFSUtils.uploadDirectoryRecursively(localFolderPath, remotePath);
                JOptionPane.showMessageDialog(this, "文件夹上传成功！", "上传", JOptionPane.INFORMATION_MESSAGE);
                // 更新目录树
                root.removeAllChildren();
                loadDirectoryTree(root, "/");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "上传文件夹失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // 下载文件功能
    private void downloadFile() {
        if (selectedFilePath == null) {
            JOptionPane.showMessageDialog(this, "请先选择一个文件进行下载。", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("选择文件保存位置");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            String localPath = fileChooser.getSelectedFile().getAbsolutePath();
            try {
                HDFSUtils.download(selectedFilePath, localPath);
                JOptionPane.showMessageDialog(this, "文件下载成功！", "下载", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "下载文件失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // 删除文件或目录功能
    private void deleteFileOrDirectory() {
        if (selectedFilePath == null || selectedFilePath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请先选择一个文件或目录！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "确定要删除该文件或目录吗？", "删除确认", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                HDFSUtils.delete(selectedFilePath, true);
                JOptionPane.showMessageDialog(this, "删除成功！", "删除", JOptionPane.INFORMATION_MESSAGE);
                root.removeAllChildren();
                loadDirectoryTree(root, "/");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "删除失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
        });
    }
}
