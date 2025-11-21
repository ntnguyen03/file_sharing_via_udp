package client;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.imageio.ImageIO;
import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.UIManager;

public class UDPFileClientGUI extends JFrame {
    public static final Color LIME_GREEN = new Color(144, 200, 75);
    public static final Color LIME_GREEN_HOVER = new Color(130, 180, 65);
    public static final Color LIGHT_GRAY_BG = new Color(240, 240, 240);
    public static final Color LIME_GREEN_LIGHT = new Color(245, 255, 240);

    private JTextField clientIPField, clientPortField;
    private JTextField senderNameField, messageField;

    // Thêm nút Lịch sử vào danh sách nút
    private JButton chooseFileBtn, sendFileBtn, sendAllBtn, clearAllBtn, toggleViewBtn, historyBtn;

    private JLabel statusLabel;
    private JProgressBar progressBar;
    private JComboBox<String> destinationBox;
    private JPanel dropPanel;

    private List<File> selectedFiles;
    private JTable fileTable;
    private DefaultTableModel tableModel;
    private JPanel thumbnailPanel;
    private JScrollPane tableScrollPane, thumbnailScrollPane;
    private boolean isTableView = true;
    private boolean isGuestMode = false;

    private JPanel previewPanel;
    private JLabel previewLabel;
    private JScrollPane previewScrollPane;

    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 8888;

    public UDPFileClientGUI(String loggedInUser, boolean isGuest) {
        this.isGuestMode = isGuest;
        setTitle("UDP File Client - User: " + loggedInUser + (isGuest ? " (GUEST)" : ""));
        setSize(900, 650);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        selectedFiles = new ArrayList<>();

        createDropPanel();
        add(dropPanel, BorderLayout.NORTH);
        createControlPanel();
        createCenterPanel();
        createBottomPanel();
        setupEventHandlers();

        // Tự động điền tên user
        senderNameField.setText(loggedInUser);
        senderNameField.setEditable(isGuest);

        // --- ĐÃ XÓA: Đoạn code tải lịch sử tự động ---
        // (Vì giờ chúng ta dùng nút bấm để xem ở trang riêng)
    }

    private void createControlPanel() {
        JPanel controlPanel = new JPanel(new GridBagLayout());
        controlPanel.setBorder(BorderFactory.createTitledBorder("Send Configuration"));
        controlPanel.setPreferredSize(new Dimension(220, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.anchor = GridBagConstraints.WEST;

        destinationBox = new JComboBox<>(new String[]{"Send to Server", "Send to Other Client"});
        clientIPField = new JTextField(12);
        clientPortField = new JTextField(12);
        senderNameField = new JTextField(12);
        messageField = new JTextField(12);

        chooseFileBtn = new JButton("Choose Files");
        sendFileBtn = new JButton("Send Selected");
        sendAllBtn = new JButton("Send All");
        clearAllBtn = new JButton("Clear All");
        toggleViewBtn = new JButton("🖼️ Thumbnails");

        // --- NÚT MỚI ---
        historyBtn = new JButton("📜 Lịch sử");
        // ---------------

        gbc.gridx = 0; gbc.gridy = 0;
        controlPanel.add(new JLabel("Send Mode:"), gbc);
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        controlPanel.add(destinationBox, gbc);

        gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
        controlPanel.add(new JLabel("Client IP:"), gbc);
        gbc.gridy = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        controlPanel.add(clientIPField, gbc);

        gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE;
        controlPanel.add(new JLabel("Client Port:"), gbc);
        gbc.gridy = 5; gbc.fill = GridBagConstraints.HORIZONTAL;
        controlPanel.add(clientPortField, gbc);

        gbc.gridy = 6; gbc.fill = GridBagConstraints.NONE;
        controlPanel.add(new JLabel("Sender Name:"), gbc);
        gbc.gridy = 7; gbc.fill = GridBagConstraints.HORIZONTAL;
        controlPanel.add(senderNameField, gbc);

        gbc.gridy = 8; gbc.fill = GridBagConstraints.NONE;
        controlPanel.add(new JLabel("Message:"), gbc);
        gbc.gridy = 9; gbc.fill = GridBagConstraints.HORIZONTAL;
        controlPanel.add(messageField, gbc);

        gbc.gridy = 10; gbc.insets = new Insets(10, 3, 3, 3);
        controlPanel.add(new JLabel("Actions:"), gbc);

        gbc.gridy = 11; gbc.insets = new Insets(3, 3, 3, 3);
        controlPanel.add(chooseFileBtn, gbc);
        gbc.gridy = 12;
        controlPanel.add(sendFileBtn, gbc);
        gbc.gridy = 13;
        controlPanel.add(sendAllBtn, gbc);
        gbc.gridy = 14;
        controlPanel.add(clearAllBtn, gbc);

        // --- THÊM NÚT LỊCH SỬ VÀO LAYOUT ---
        gbc.gridy = 15;
        controlPanel.add(historyBtn, gbc);
        // -----------------------------------

        gbc.gridy = 16; gbc.insets = new Insets(10, 3, 3, 3);
        controlPanel.add(new JLabel("View:"), gbc);
        gbc.gridy = 17; gbc.insets = new Insets(3, 3, 3, 3);
        controlPanel.add(toggleViewBtn, gbc);

        add(controlPanel, BorderLayout.WEST);
    }

    private void createCenterPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout());
        createTableView();
        createThumbnailView();
        createPreviewPanel();

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScrollPane, previewScrollPane);
        splitPane.setDividerLocation(220);
        splitPane.setResizeWeight(0.5);

        centerPanel.add(splitPane, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);
    }

    private void createTableView() {
        String[] columns = {"File Name", "Size", "Type", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        fileTable = new JTable(tableModel);
        fileTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        fileTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) showFilePreview();
        });

        fileTable.getColumnModel().getColumn(0).setPreferredWidth(200);
        fileTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        fileTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        fileTable.getColumnModel().getColumn(3).setPreferredWidth(100);

        tableScrollPane = new JScrollPane(fileTable);
    }

    private void createThumbnailView() {
        thumbnailPanel = new JPanel();
        thumbnailPanel.setLayout(new WrapLayout(FlowLayout.LEFT, 10, 10));
        thumbnailPanel.setBackground(Color.WHITE);
        thumbnailScrollPane = new JScrollPane(thumbnailPanel);
        thumbnailScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        thumbnailScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    }

    private void createPreviewPanel() {
        previewPanel = new JPanel(new BorderLayout());
        previewPanel.setBorder(BorderFactory.createTitledBorder("File Preview"));
        previewLabel = new JLabel("Select a file to preview", JLabel.CENTER);
        previewLabel.setVerticalAlignment(JLabel.CENTER);
        previewPanel.add(previewLabel, BorderLayout.CENTER);
        previewScrollPane = new JScrollPane(previewPanel);
    }

    private void createBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        progressBar = new JProgressBar(0, 100);
        statusLabel = new JLabel("Status: Ready...");
        bottomPanel.add(progressBar, BorderLayout.CENTER);
        bottomPanel.add(statusLabel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void createDropPanel() {
        dropPanel = new JPanel();
        dropPanel.setBackground(new Color(245, 255, 240));
        dropPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LIME_GREEN, 2, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        dropPanel.setPreferredSize(new Dimension(0, 50));

        JLabel dropLabel = new JLabel("📁 Drag and drop files here (multiple files supported)", JLabel.CENTER);
        dropLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        dropLabel.setForeground(new Color(110, 160, 50));
        dropPanel.add(dropLabel);

        new DropTarget(dropPanel, new FileDropTargetListener());
        new DropTarget(this, new FileDropTargetListener());
    }

    private void setupEventHandlers() {
        chooseFileBtn.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setMultiSelectionEnabled(true);
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                addFilesToList(fileChooser.getSelectedFiles());
            }
        });

        sendFileBtn.addActionListener(e -> {
            int selectedIndex = getSelectedFileIndex();
            if (selectedIndex >= 0) {
                File file = selectedFiles.get(selectedIndex);
                new Thread(() -> sendSingleFile(file, selectedIndex)).start();
            } else {
                JOptionPane.showMessageDialog(this, "Please select a file to send!");
            }
        });

        sendAllBtn.addActionListener(e -> {
            if (selectedFiles.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No files to send!");
                return;
            }
            new Thread(this::sendAllFiles).start();
        });

        clearAllBtn.addActionListener(e -> {
            selectedFiles.clear();
            tableModel.setRowCount(0);
            thumbnailPanel.removeAll();
            thumbnailPanel.revalidate();
            thumbnailPanel.repaint();
            previewLabel.setText("Select a file to preview");
            previewLabel.setIcon(null);
            statusLabel.setText("Status: Ready...");
        });

        toggleViewBtn.addActionListener(e -> toggleView());

        // --- SỰ KIỆN NÚT LỊCH SỬ ---
        historyBtn.addActionListener(e -> {
            if (isGuestMode) {
                JOptionPane.showMessageDialog(this,
                        "Chức năng này chỉ dành cho thành viên đăng nhập!",
                        "Thông báo", JOptionPane.WARNING_MESSAGE);
            } else {
                // Mở trang HistoryDialog mới tạo
                String currentUser = senderNameField.getText();
                new HistoryDialog(this, currentUser).setVisible(true);
            }
        });
        // ---------------------------
    }

    private void toggleView() {
        isTableView = !isTableView;
        Component currentView = ((JSplitPane)((JPanel)getContentPane().getComponent(2)).getComponent(0)).getTopComponent();
        JSplitPane splitPane = (JSplitPane)((JPanel)getContentPane().getComponent(2)).getComponent(0);

        if (isTableView) {
            splitPane.setTopComponent(tableScrollPane);
            toggleViewBtn.setText("🖼️ Thumbnails");
        } else {
            splitPane.setTopComponent(thumbnailScrollPane);
            toggleViewBtn.setText("📋 Table");
        }
        splitPane.setDividerLocation(220);
        splitPane.revalidate();
        splitPane.repaint();
    }

    private int getSelectedFileIndex() {
        if (isTableView) return fileTable.getSelectedRow();
        Component[] components = thumbnailPanel.getComponents();
        for (int i = 0; i < components.length; i++) {
            if (components[i] instanceof ThumbnailPanel) {
                if (((ThumbnailPanel) components[i]).isSelected()) return i;
            }
        }
        return -1;
    }

    private void addFilesToList(File[] files) {
        for (File file : files) {
            if (!selectedFiles.contains(file)) {
                selectedFiles.add(file);
                String fileType = getFileType(file);
                tableModel.addRow(new Object[]{ file.getName(), formatFileSize(file.length()), fileType, "Ready" });
                addThumbnail(file, selectedFiles.size() - 1);
            }
        }
        statusLabel.setText("Status: " + selectedFiles.size() + " files ready");
        if (fileTable.getSelectedRow() == -1 && selectedFiles.size() > 0) {
            fileTable.setRowSelectionInterval(0, 0);
            if (!isTableView) selectThumbnail(0);
        }
    }

    private void addThumbnail(File file, int index) {
        ThumbnailPanel thumbnail = new ThumbnailPanel(file, index);
        thumbnail.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                selectThumbnail(index);
                showPreview(file);
            }
        });
        thumbnailPanel.add(thumbnail);
        thumbnailPanel.revalidate();
        thumbnailPanel.repaint();
    }

    private void selectThumbnail(int index) {
        Component[] components = thumbnailPanel.getComponents();
        for (int i = 0; i < components.length; i++) {
            if (components[i] instanceof ThumbnailPanel) {
                ((ThumbnailPanel) components[i]).setSelected(i == index);
            }
        }
    }

    private class ThumbnailPanel extends JPanel {
        private File file;
        private int index;
        private boolean selected = false;

        public ThumbnailPanel(File file, int index) {
            this.file = file;
            this.index = index;
            setPreferredSize(new Dimension(120, 140));
            setBorder(BorderFactory.createRaisedBevelBorder());
            setLayout(new BorderLayout());
            setBackground(LIME_GREEN_LIGHT);
            createThumbnail();
            JLabel nameLabel = new JLabel(truncateFileName(file.getName()), JLabel.CENTER);
            nameLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
            add(nameLabel, BorderLayout.SOUTH);
        }

        private void createThumbnail() {
            JLabel iconLabel = new JLabel();
            iconLabel.setHorizontalAlignment(JLabel.CENTER);
            iconLabel.setPreferredSize(new Dimension(100, 100));
            String fileType = getFileType(file).toLowerCase();
            if (fileType.contains("image")) {
                try {
                    BufferedImage image = ImageIO.read(file);
                    if (image != null) {
                        Image scaledImage = image.getScaledInstance(90, 90, Image.SCALE_SMOOTH);
                        iconLabel.setIcon(new ImageIcon(scaledImage));
                    } else {
                        iconLabel.setText("🖼️");
                        iconLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 40));
                    }
                } catch (Exception e) {
                    iconLabel.setText("🖼️");
                    iconLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 40));
                }
            } else {
                String icon = getFileIcon(fileType);
                iconLabel.setText(icon);
                iconLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 40));
            }
            add(iconLabel, BorderLayout.CENTER);
        }

        private String getFileIcon(String fileType) {
            if (fileType.contains("text") || fileType.contains("java")) return "📄";
            if (fileType.contains("pdf")) return "📋";
            if (fileType.contains("document")) return "📝";
            if (fileType.contains("media")) return "🎵";
            if (fileType.contains("archive")) return "🗜️";
            return "📁";
        }

        private String truncateFileName(String name) {
            if (name.length() > 15) return name.substring(0, 12) + "...";
            return name;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
            if (selected) {
                setBorder(BorderFactory.createLoweredBevelBorder());
                setBackground(LIME_GREEN);
            } else {
                setBorder(BorderFactory.createRaisedBevelBorder());
                setBackground(LIME_GREEN_LIGHT);
            }
            repaint();
        }
        public boolean isSelected() { return selected; }
    }

    private class WrapLayout extends FlowLayout {
        public WrapLayout(int align, int hgap, int vgap) { super(align, hgap, vgap); }
        @Override public Dimension preferredLayoutSize(Container target) { return layoutSize(target, true); }
        @Override public Dimension minimumLayoutSize(Container target) {
            Dimension minimum = layoutSize(target, false);
            minimum.width -= (getHgap() + 1);
            return minimum;
        }
        private Dimension layoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int targetWidth = target.getSize().width;
                Container container = target;
                while (container.getSize().width == 0 && container.getParent() != null) container = container.getParent();
                targetWidth = container.getSize().width;
                if (targetWidth == 0) targetWidth = Integer.MAX_VALUE;
                int hgap = getHgap(); int vgap = getVgap();
                Insets insets = target.getInsets();
                int horizontalInsetsAndGap = insets.left + insets.right + (hgap * 2);
                int maxWidth = targetWidth - horizontalInsetsAndGap;
                Dimension dim = new Dimension(0, 0);
                int rowWidth = 0; int rowHeight = 0;
                int nmembers = target.getComponentCount();
                for (int i = 0; i < nmembers; i++) {
                    Component m = target.getComponent(i);
                    if (m.isVisible()) {
                        Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();
                        if (rowWidth + d.width > maxWidth) {
                            addRow(dim, rowWidth, rowHeight);
                            rowWidth = d.width; rowHeight = d.height;
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

    private void showFilePreview() {
        int selectedIndex = getSelectedFileIndex();
        if (selectedIndex >= 0 && selectedIndex < selectedFiles.size()) {
            File file = selectedFiles.get(selectedIndex);
            showPreview(file);
        }
    }

    private void showPreview(File file) {
        previewLabel.setIcon(null);
        String fileType = getFileType(file).toLowerCase();
        if (fileType.contains("image")) showImagePreview(file);
        else if (fileType.contains("text")) showTextPreview(file);
        else showFileInfo(file);
    }

    private void showImagePreview(File file) {
        try {
            BufferedImage image = ImageIO.read(file);
            if (image != null) {
                int maxWidth = 350; int maxHeight = 250;
                int width = image.getWidth(); int height = image.getHeight();
                double scale = Math.min((double)maxWidth/width, (double)maxHeight/height);
                if (scale < 1.0) {
                    width = (int)(width * scale);
                    height = (int)(height * scale);
                    Image scaledImage = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                    previewLabel.setIcon(new ImageIcon(scaledImage));
                } else {
                    previewLabel.setIcon(new ImageIcon(image));
                }
                previewLabel.setText("");
            }
        } catch (Exception e) { previewLabel.setText("Cannot preview image: " + e.getMessage()); }
    }

    private void showTextPreview(File file) {
        try {
            StringBuilder content = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line; int lineCount = 0;
                while ((line = reader.readLine()) != null && lineCount < 15) {
                    content.append(line).append("\n"); lineCount++;
                }
                if (lineCount >= 15) content.append("\n... (file truncated for preview)");
            }
            JTextArea textArea = new JTextArea(content.toString());
            textArea.setEditable(false);
            textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
            textArea.setMargin(new Insets(10, 10, 10, 10));
            previewPanel.removeAll();
            previewPanel.add(new JScrollPane(textArea), BorderLayout.CENTER);
            previewPanel.revalidate();
            previewPanel.repaint();
        } catch (Exception e) { previewLabel.setText("Cannot preview text file: " + e.getMessage()); }
    }

    private void showFileInfo(File file) {
        previewPanel.removeAll();
        previewPanel.add(previewLabel, BorderLayout.CENTER);
        previewPanel.revalidate();
        StringBuilder info = new StringBuilder();
        info.append("<html><div style='padding: 15px; font-family: Arial; font-size: 12px;'>");
        info.append("<h3>📄 ").append(file.getName()).append("</h3>");
        info.append("<p><b>Size:</b> ").append(formatFileSize(file.length())).append("</p>");
        info.append("<p><b>Type:</b> ").append(getFileType(file)).append("</p>");
        info.append("<p><b>Location:</b> ").append(file.getParent()).append("</p>");
        info.append("<p><b>Last Modified:</b> ").append(new Date(file.lastModified())).append("</p>");
        info.append("<br><p><i>Preview not available for this file type</i></p></div></html>");
        previewLabel.setText(info.toString());
    }

    private String getFileType(File file) {
        String name = file.getName().toLowerCase();
        String extension = "";
        int dotIndex = name.lastIndexOf('.');
        if (dotIndex > 0) extension = name.substring(dotIndex + 1);
        if (extension.matches("jpg|jpeg|png|gif|bmp|webp")) return "Image";
        if (extension.matches("txt|java|cpp|c|py|js|html|css|xml|json|log")) return "Text";
        if (extension.matches("pdf")) return "PDF";
        if (extension.matches("doc|docx")) return "Document";
        if (extension.matches("mp3|wav|mp4|avi|mov")) return "Media";
        if (extension.matches("zip|rar|7z")) return "Archive";
        return extension.toUpperCase() + " File";
    }

    private class FileDropTargetListener extends DropTargetAdapter {
        @Override public void dragEnter(DropTargetDragEvent dtde) { dropPanel.setBackground(new Color(220, 255, 210)); }
        @Override public void dragExit(DropTargetEvent dte) { dropPanel.setBackground(new Color(245, 255, 240)); }
        @Override public void drop(DropTargetDropEvent dtde) {
            try {
                dtde.acceptDrop(DnDConstants.ACTION_COPY);
                Transferable transferable = dtde.getTransferable();
                if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    List<File> files = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                    addFilesToList(files.toArray(new File[0]));
                }
                dtde.dropComplete(true);
            } catch (Exception e) { dtde.dropComplete(false); }
            dropPanel.setBackground(new Color(240, 248, 255));
        }
    }

    private void sendSingleFile(File file, int rowIndex) {
        try (DatagramSocket socket = new DatagramSocket();
             FileInputStream fis = new FileInputStream(file)) {
            SwingUtilities.invokeLater(() -> {
                tableModel.setValueAt("Sending...", rowIndex, 3);
                statusLabel.setText("📤 Sending: " + file.getName());
            });
            InetAddress serverIP = InetAddress.getByName(SERVER_IP);
            String sender = senderNameField.getText().trim();
            String message = messageField.getText().trim();
            if (sender.isEmpty()) sender = "Unknown";
            if (message.isEmpty()) message = "(No message)";

            String header;
            if (destinationBox.getSelectedIndex() == 0) {
                header = "HEADER|" + file.getName() + "|" + file.length() + "|DEST:SERVER|" + sender + "|" + message;
            } else {
                String clientIP = clientIPField.getText().trim();
                String clientPort = clientPortField.getText().trim();
                if (clientIP.isEmpty() || clientPort.isEmpty()) {
                    SwingUtilities.invokeLater(() -> {
                        tableModel.setValueAt("Error", rowIndex, 3);
                        statusLabel.setText("❌ Client IP/Port not entered");
                    });
                    return;
                }
                header = "HEADER|" + file.getName() + "|" + file.length() + "|DEST:CLIENT|" + clientIP + ":" + clientPort + "|" + sender + "|" + message;
            }

            byte[] headerBytes = header.getBytes(StandardCharsets.UTF_8);
            socket.send(new DatagramPacket(headerBytes, headerBytes.length, serverIP, SERVER_PORT));

            byte[] buffer = new byte[4096];
            int bytesRead; long sent = 0;
            while ((bytesRead = fis.read(buffer)) != -1) {
                DatagramPacket packet = new DatagramPacket(buffer, bytesRead, serverIP, SERVER_PORT);
                socket.send(packet);
                sent += bytesRead;
                int progress = (int) ((sent * 100) / file.length());
                SwingUtilities.invokeLater(() -> progressBar.setValue(progress));
            }

            String endMsg = "END|" + file.getName();
            byte[] endBytes = endMsg.getBytes(StandardCharsets.UTF_8);
            socket.send(new DatagramPacket(endBytes, endBytes.length, serverIP, SERVER_PORT));

            SwingUtilities.invokeLater(() -> {
                tableModel.setValueAt("✅ Sent", rowIndex, 3);
                statusLabel.setText("✅ Sent: " + file.getName());
                progressBar.setValue(100);
            });
        } catch (Exception e) {
            e.printStackTrace();
            SwingUtilities.invokeLater(() -> {
                tableModel.setValueAt("❌ Error", rowIndex, 3);
                statusLabel.setText("❌ Error sending: " + file.getName());
            });
        }
    }

    private void sendAllFiles() {
        for (int i = 0; i < selectedFiles.size(); i++) {
            File file = selectedFiles.get(i);
            String status = (String) tableModel.getValueAt(i, 3);
            if (!status.contains("✅")) {
                sendSingleFile(file, i);
                try { Thread.sleep(500); } catch (InterruptedException e) { break; }
            }
        }
        SwingUtilities.invokeLater(() -> statusLabel.setText("✅ All files processing completed"));
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
            // Cài đặt màu sắc LimeWire
            UIManager.put("Button.background", LIME_GREEN);
            UIManager.put("Button.foreground", Color.WHITE);
            UIManager.put("Button.hoverBackground", LIME_GREEN_HOVER);
            UIManager.put("Button.focusedBackground", LIME_GREEN_HOVER);
            UIManager.put("Button.arc", 10);
            UIManager.put("Button.borderWidth", 0);
            UIManager.put("ComboBox.selectionBackground", LIME_GREEN);
            UIManager.put("ComboBox.selectionForeground", Color.WHITE);
            UIManager.put("ComboBox.arc", 10);
            UIManager.put("ComboBox.borderWidth", 0);
            UIManager.put("ProgressBar.foreground", LIME_GREEN);
            UIManager.put("ProgressBar.background", LIGHT_GRAY_BG);
            UIManager.put("ProgressBar.arc", 10);
            UIManager.put("Table.selectionBackground", LIME_GREEN);
            UIManager.put("Table.selectionForeground", Color.WHITE);
        } catch (Exception ex) {
            System.err.println("Cannot set LookAndFeel");
        }

        SwingUtilities.invokeLater(() -> {
            // 1. Hiện Login
            LoginDialog loginDlg = new LoginDialog(null);
            loginDlg.setVisible(true);

            // 2. Nếu Login thành công thì mở Main GUI
            if (loginDlg.isSucceeded()) {
                String user = loginDlg.getLoggedInUser();
                boolean isGuest = loginDlg.isGuest();
                new UDPFileClientGUI(user, isGuest).setVisible(true);
            } else {
                System.exit(0);
            }
        });
    }
}