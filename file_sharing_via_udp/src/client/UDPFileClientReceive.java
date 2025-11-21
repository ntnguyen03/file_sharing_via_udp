package client;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.UIManager;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class UDPFileClientReceive extends JFrame {
    public static final Color LIME_GREEN = new Color(144, 200, 75);
    public static final Color LIME_GREEN_HOVER = new Color(130, 180, 65);
    public static final Color LIGHT_GRAY_BG = new Color(240, 240, 240);
    public static final Color LIME_GREEN_LIGHT = new Color(245, 255, 240);

    private JTable table;
    private DefaultTableModel tableModel;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JLabel saveDirLabel;

    private DatagramSocket socket;
    private boolean running = true;

    private FileOutputStream fos;
    private String currentFileName;
    private long expectedFileSize;
    private long receivedBytes;
    private File currentSaveDir;
    private String senderName;
    private String messageText;

    private JPanel thumbnailPanel;
    private JScrollPane tableScrollPane, thumbnailScrollPane;
    private JButton toggleViewBtn;
    private boolean isTableView = true;
    private List<ReceivedFileInfo> receivedFiles;

    // Biến quản lý thư mục lưu
    private File saveDirectory = null;
    private boolean saveDirectorySelected = false;

    public UDPFileClientReceive(int listenPort) {
        setTitle("UDP File Client - Receive Files");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        receivedFiles = new ArrayList<>();

        createTopPanel();
        createCenterPanel();
        createBottomPanel();

        new Thread(() -> startListening(listenPort)).start();
    }

    private void createTopPanel() {
        JPanel topPanel = new JPanel(new FlowLayout());
        toggleViewBtn = new JButton("🖼️ Thumbnails");
        toggleViewBtn.addActionListener(e -> toggleView());

        JButton resetDirBtn = new JButton("🔄 Change Save Directory");
        // --- SỬA LỖI: Gọi hàm mở cửa sổ chọn thư mục ngay ---
        resetDirBtn.addActionListener(e -> promptForSaveDirectory());
        // ---------------------------------------------------

        JLabel portLabel = new JLabel("Listening on port: 9999");
        portLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));

        saveDirLabel = new JLabel("Save Dir: Not selected");
        saveDirLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 10));
        saveDirLabel.setForeground(Color.GRAY);

        topPanel.add(portLabel);
        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(saveDirLabel);
        topPanel.add(Box.createHorizontalGlue());
        topPanel.add(resetDirBtn);
        topPanel.add(new JLabel("View Mode:"));
        topPanel.add(toggleViewBtn);

        add(topPanel, BorderLayout.NORTH);
    }

    // --- MỚI: Hàm mở JFileChooser ---
    private void promptForSaveDirectory() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose folder to save ALL incoming files");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        if (saveDirectory != null) {
            chooser.setCurrentDirectory(saveDirectory);
        }

        int result = chooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            saveDirectory = chooser.getSelectedFile();
            saveDirectorySelected = true;

            statusLabel.setText("Save directory set: " + saveDirectory.getAbsolutePath());
            if (saveDirLabel != null) {
                saveDirLabel.setText("Save Dir: " + saveDirectory.getName());
                saveDirLabel.setForeground(LIME_GREEN);
            }
        }
    }
    // -------------------------------

    private void createCenterPanel() {
        createTableView();
        createThumbnailView();
        add(tableScrollPane, BorderLayout.CENTER);
    }

    private void createTableView() {
        String[] columns = {"File Name", "Size", "Sender", "Message"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(200);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(180);
        tableScrollPane = new JScrollPane(table);
    }

    private void createThumbnailView() {
        thumbnailPanel = new JPanel();
        thumbnailPanel.setLayout(new WrapLayout(FlowLayout.LEFT, 10, 10));
        thumbnailPanel.setBackground(Color.WHITE);
        thumbnailScrollPane = new JScrollPane(thumbnailPanel);
        thumbnailScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        thumbnailScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    }

    private void createBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        progressBar = new JProgressBar(0, 100);
        statusLabel = new JLabel("No files received");
        bottomPanel.add(progressBar, BorderLayout.CENTER);
        bottomPanel.add(statusLabel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void toggleView() {
        isTableView = !isTableView;
        remove(getContentPane().getComponent(1));
        if (isTableView) {
            add(tableScrollPane, BorderLayout.CENTER);
            toggleViewBtn.setText("🖼️ Thumbnails");
        } else {
            add(thumbnailScrollPane, BorderLayout.CENTER);
            toggleViewBtn.setText("📋 Table");
        }
        revalidate();
        repaint();
    }

    private class ReceivedFileInfo {
        File file;
        String sender;
        String message;
        long size;

        public ReceivedFileInfo(File file, String sender, String message, long size) {
            this.file = file;
            this.sender = sender;
            this.message = message;
            this.size = size;
        }
    }

    private void addThumbnail(File file, String sender, String message, long size) {
        if (!file.exists()) return;
        SwingUtilities.invokeLater(() -> {
            try {
                ReceivedFileInfo info = new ReceivedFileInfo(file, sender, message, size);
                receivedFiles.add(info);
                ClientThumbnailPanel thumbnail = new ClientThumbnailPanel(info);
                thumbnailPanel.add(thumbnail);
                thumbnailPanel.revalidate();
                thumbnailPanel.repaint();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private class ClientThumbnailPanel extends JPanel {
        private ReceivedFileInfo fileInfo;

        public ClientThumbnailPanel(ReceivedFileInfo fileInfo) {
            this.fileInfo = fileInfo;
            setPreferredSize(new Dimension(160, 200));
            setBorder(BorderFactory.createRaisedBevelBorder());
            setLayout(new BorderLayout());
            setBackground(LIME_GREEN_LIGHT);
            createThumbnail();
            createInfoPanel();
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) openFileLocation();
                }
            });
        }

        private void createThumbnail() {
            JLabel iconLabel = new JLabel();
            iconLabel.setHorizontalAlignment(JLabel.CENTER);
            iconLabel.setPreferredSize(new Dimension(140, 100));
            String fileType = getFileType(fileInfo.file).toLowerCase();
            if (fileType.contains("image")) {
                try {
                    BufferedImage image = ImageIO.read(fileInfo.file);
                    if (image != null) {
                        Image scaledImage = image.getScaledInstance(90, 90, Image.SCALE_SMOOTH);
                        iconLabel.setIcon(new ImageIcon(scaledImage));
                    } else iconLabel.setText("🖼️");
                } catch (Exception e) {
                    iconLabel.setText("🖼️");
                }
            } else {
                iconLabel.setText(getFileIcon(fileType));
                iconLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 40));
            }
            add(iconLabel, BorderLayout.CENTER);
        }

        private void createInfoPanel() {
            JPanel infoPanel = new JPanel(new GridLayout(5, 1, 2, 1));
            infoPanel.setBackground(LIME_GREEN_LIGHT);
            infoPanel.add(new JLabel(truncateText(fileInfo.file.getName(), 18), JLabel.CENTER));
            infoPanel.add(new JLabel(formatFileSize(fileInfo.size), JLabel.CENTER));
            infoPanel.add(new JLabel("From: " + truncateText(fileInfo.sender, 12), JLabel.CENTER));
            infoPanel.add(new JLabel(truncateText(fileInfo.message, 15), JLabel.CENTER));
            infoPanel.add(new JLabel("Double-click to open", JLabel.CENTER));
            add(infoPanel, BorderLayout.SOUTH);
        }

        private void openFileLocation() {
            try {
                if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(fileInfo.file.getParentFile());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(UDPFileClientReceive.this, "Cannot open location.");
            }
        }

        private String getFileIcon(String fileType) {
            if (fileType.contains("text")) return "📄";
            if (fileType.contains("pdf")) return "📋";
            return "📁";
        }

        private String truncateText(String text, int maxLength) {
            if (text.length() > maxLength) return text.substring(0, maxLength - 3) + "...";
            return text;
        }
    }

    private class WrapLayout extends FlowLayout {
        public WrapLayout(int align, int hgap, int vgap) {
            super(align, hgap, vgap);
        }

        @Override
        public Dimension preferredLayoutSize(Container target) {
            return layoutSize(target, true);
        }

        @Override
        public Dimension minimumLayoutSize(Container target) {
            Dimension minimum = layoutSize(target, false);
            minimum.width -= (getHgap() + 1);
            return minimum;
        }

        private Dimension layoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int targetWidth = target.getSize().width;
                Container container = target;
                while (container.getSize().width == 0 && container.getParent() != null)
                    container = container.getParent();
                targetWidth = container.getSize().width;
                if (targetWidth == 0) targetWidth = Integer.MAX_VALUE;
                int hgap = getHgap();
                int vgap = getVgap();
                Insets insets = target.getInsets();
                int horizontalInsetsAndGap = insets.left + insets.right + (hgap * 2);
                int maxWidth = targetWidth - horizontalInsetsAndGap;
                Dimension dim = new Dimension(0, 0);
                int rowWidth = 0;
                int rowHeight = 0;
                int nmembers = target.getComponentCount();
                for (int i = 0; i < nmembers; i++) {
                    Component m = target.getComponent(i);
                    if (m.isVisible()) {
                        Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();
                        if (rowWidth + d.width > maxWidth) {
                            addRow(dim, rowWidth, rowHeight);
                            rowWidth = d.width;
                            rowHeight = d.height;
                        } else {
                            rowWidth += hgap + d.width;
                            rowHeight = Math.max(rowHeight, d.height);
                        }
                    }
                }
                addRow(dim, rowWidth, rowHeight);
                dim.width = Math.max(dim.width, rowWidth);
                dim.height += insets.top + insets.bottom + vgap * 2;
                return dim;
            }
        }

        private void addRow(Dimension dim, int rowWidth, int rowHeight) {
            dim.width = Math.max(dim.width, rowWidth);
            if (dim.height > 0) dim.height += getVgap();
            dim.height += rowHeight;
        }
    }

    private void startListening(int port) {
        try {
            socket = new DatagramSocket(port);
            byte[] buffer = new byte[4096];
            System.out.println("Client listening on port: " + port);
            while (running) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String msg = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
                if (msg.startsWith("HEADER|")) handleHeaderMessage(msg);
                else if (msg.startsWith("END|")) handleEndMessage(msg);
                else handleDataMessage(packet);
            }
        } catch (Exception e) {
            if (running) e.printStackTrace();
        }
    }

    private void handleHeaderMessage(String msg) {
        try {
            String[] parts = msg.split("\\|");
            String incomingFileName = parts[1];
            long incomingFileSize = Long.parseLong(parts[2]);
            String incomingSender = parts.length > 4 ? parts[4] : "Unknown";
            String incomingMessage = parts.length > 5 ? parts[5] : "(No message)";

            if (fos != null) {
                fos.close();
                fos = null;
            }

            currentFileName = incomingFileName;
            expectedFileSize = incomingFileSize;
            senderName = incomingSender;
            messageText = incomingMessage;
            receivedBytes = 0;

            // --- SỬA ĐỔI: Sử dụng promptForSaveDirectory ---
            if (!saveDirectorySelected) {
                SwingUtilities.invokeAndWait(() -> promptForSaveDirectory());
            }
            // -----------------------------------------------

            if (saveDirectorySelected && saveDirectory != null) {
                currentSaveDir = saveDirectory;
                File outputFile = new File(currentSaveDir, currentFileName);
                int counter = 1;
                String baseName = currentFileName;
                String extension = "";
                int dotIndex = baseName.lastIndexOf('.');
                if (dotIndex > 0) {
                    extension = baseName.substring(dotIndex);
                    baseName = baseName.substring(0, dotIndex);
                }
                while (outputFile.exists()) {
                    outputFile = new File(currentSaveDir, baseName + "_" + counter + extension);
                    counter++;
                }
                currentFileName = outputFile.getName();
                outputFile.getParentFile().mkdirs();
                fos = new FileOutputStream(outputFile);

                SwingUtilities.invokeLater(() -> {
                    tableModel.addRow(new Object[]{currentFileName, formatFileSize(expectedFileSize), senderName, messageText});
                    statusLabel.setText("📥 Receiving: " + currentFileName);
                    progressBar.setValue(0);
                });
            } else {
                SwingUtilities.invokeLater(() -> statusLabel.setText("❌ Skipped: " + currentFileName));
                fos = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleEndMessage(String msg) {
        if (fos != null) {
            try {
                fos.flush();
                fos.close();
                File completedFile = new File(currentSaveDir, currentFileName);
                if (completedFile.exists() && completedFile.length() > 0) {
                    addThumbnail(completedFile, senderName, messageText, completedFile.length());
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("✅ Completed: " + currentFileName);
                        progressBar.setValue(100);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        fos = null;
        currentFileName = null;
        receivedBytes = 0;
    }

    private void handleDataMessage(DatagramPacket packet) {
        if (fos != null) {
            try {
                fos.write(packet.getData(), 0, packet.getLength());
                receivedBytes += packet.getLength();
                int progress = expectedFileSize > 0 ? (int) ((receivedBytes * 100) / expectedFileSize) : 0;
                SwingUtilities.invokeLater(() -> progressBar.setValue(Math.min(progress, 100)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String getFileType(File file) {
        String name = file.getName().toLowerCase();
        if (name.endsWith(".jpg") || name.endsWith(".png")) return "Image";
        if (name.endsWith(".pdf")) return "PDF";
        if (name.endsWith(".txt")) return "Text";
        return "File";
    }

    private static String formatFileSize(long size) {
        if (size < 1024) return size + " B";
        int exp = (int) (Math.log(size) / Math.log(1024));
        char unit = "KMGTPE".charAt(exp - 1);
        return String.format("%.1f %sB", size / Math.pow(1024, exp), unit);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());

            // Copy y hệt cấu hình màu của Server
            UIManager.put("Button.background", LIME_GREEN);
            UIManager.put("Button.foreground", Color.WHITE);
            UIManager.put("Button.hoverBackground", LIME_GREEN_HOVER);
            UIManager.put("Button.focusedBackground", LIME_GREEN_HOVER);
            UIManager.put("Button.arc", 10);
            UIManager.put("Button.borderWidth", 0);

            UIManager.put("ProgressBar.foreground", LIME_GREEN);
            UIManager.put("ProgressBar.background", LIGHT_GRAY_BG);
            UIManager.put("ProgressBar.arc", 10);

            UIManager.put("Table.selectionBackground", LIME_GREEN);
            UIManager.put("Table.selectionForeground", Color.WHITE);

            // --- MỚI: HEADER MÀU XANH ---
            UIManager.put("TableHeader.background", LIME_GREEN);
            UIManager.put("TableHeader.foreground", Color.WHITE);
            UIManager.put("TableHeader.bottomSeparatorColor", LIME_GREEN);
            UIManager.put("TableHeader.font", new Font("SansSerif", Font.BOLD, 12));
            // ----------------------------

        } catch (Exception ex) {
            System.err.println("Cannot set LookAndFeel");
        }

        SwingUtilities.invokeLater(() -> new UDPFileClientReceive(9999).setVisible(true));
    }
}