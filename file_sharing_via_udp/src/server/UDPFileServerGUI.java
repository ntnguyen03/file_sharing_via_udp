package server;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.UIManager;

public class UDPFileServerGUI extends JFrame {
    public static final Color LIME_GREEN = new Color(144, 200, 75);
    public static final Color LIME_GREEN_HOVER = new Color(130, 180, 65);
    public static final Color LIGHT_GRAY_BG = new Color(240, 240, 240);
    public static final Color LIME_GREEN_LIGHT = new Color(245, 255, 240);

    private JTable table;
    private DefaultTableModel tableModel;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private UDPFileServer server;
    private JPanel thumbnailPanel;
    private JScrollPane tableScrollPane, thumbnailScrollPane;
    private JButton toggleViewBtn;
    private boolean isTableView = true;
    private List<File> receivedFiles;

    public UDPFileServerGUI() {
        setTitle("UDP File Server");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        receivedFiles = new ArrayList<>();

        createTopPanel();
        createCenterPanel();
        createBottomPanel();

        // Tải lịch sử cũ khi mở app
        loadHistoryFromDB();

        server = new UDPFileServer(8888, this);
        server.start();
    }

    private void loadHistoryFromDB() {
        List<Object[]> history = DatabaseManager.getAllHistory();
        for (Object[] row : history) {
            String fileName = (String) row[0];
            Long size = (Long) row[1];
            String sender = (String) row[2];
            String dest = (String) row[3];
            addFileToDisplay(fileName, size, sender, "(History)", dest);
        }
    }

    private void createTopPanel() {
        JPanel topPanel = new JPanel(new FlowLayout());
        toggleViewBtn = new JButton("🖼️ Thumbnails");
        toggleViewBtn.addActionListener(e -> toggleView());
        topPanel.add(new JLabel("View Mode:"));
        topPanel.add(toggleViewBtn);
        add(topPanel, BorderLayout.NORTH);
    }

    private void createCenterPanel() {
        createTableView();
        createThumbnailView();
        add(tableScrollPane, BorderLayout.CENTER);
    }

    private void createTableView() {
        String[] columns = {"File Name", "Size", "Sender", "Message", "Destination"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(150);
        tableScrollPane = new JScrollPane(table);
    }

    private void createThumbnailView() {
        thumbnailPanel = new JPanel();
        thumbnailPanel.setLayout(new WrapLayout(FlowLayout.LEFT, 10, 10));
        thumbnailPanel.setBackground(LIME_GREEN_LIGHT);
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

    public void addFileToDisplay(String fileName, long fileSize, String sender, String message, String destination) {
        SwingUtilities.invokeLater(() -> {
            tableModel.addRow(new Object[]{fileName, formatFileSize(fileSize), sender, message, destination});
            File file = findReceivedFile(fileName);
            if (file != null && file.exists()) {
                receivedFiles.add(file);
                addThumbnail(file, sender, destination);
            }
        });
    }

    private File findReceivedFile(String fileName) {
        File serverFile = new File("server_received_files", fileName);
        if (serverFile.exists()) return serverFile;
        File edgeFile = new File("edge_storage", fileName);
        if (edgeFile.exists()) return edgeFile;
        return null;
    }

    private void addThumbnail(File file, String sender, String destination) {
        ServerThumbnailPanel thumbnail = new ServerThumbnailPanel(file, sender, destination);
        thumbnailPanel.add(thumbnail);
        thumbnailPanel.revalidate();
        thumbnailPanel.repaint();
    }

    private class ServerThumbnailPanel extends JPanel {
        private File file;
        private String sender, destination;

        public ServerThumbnailPanel(File file, String sender, String destination) {
            this.file = file;
            this.sender = sender;
            this.destination = destination;
            setPreferredSize(new Dimension(160, 180));
            setBorder(BorderFactory.createRaisedBevelBorder());
            setLayout(new BorderLayout());
            setBackground(LIME_GREEN_LIGHT);
            createThumbnail();
            createInfoPanel();
        }

        private void createThumbnail() {
            JLabel iconLabel = new JLabel("", JLabel.CENTER);
            String fileType = getFileType(file).toLowerCase();
            if (fileType.contains("image")) {
                try {
                    BufferedImage image = ImageIO.read(file);
                    if (image != null)
                        iconLabel.setIcon(new ImageIcon(image.getScaledInstance(90, 90, Image.SCALE_SMOOTH)));
                    else iconLabel.setText("🖼️");
                } catch (Exception e) {
                    iconLabel.setText("🖼️");
                }
            } else {
                iconLabel.setText("📁");
                iconLabel.setFont(new Font("Sans", Font.PLAIN, 40));
            }
            add(iconLabel, BorderLayout.CENTER);
        }

        private void createInfoPanel() {
            JPanel p = new JPanel(new GridLayout(4, 1));
            p.setBackground(LIME_GREEN_LIGHT);
            p.add(new JLabel(file.getName(), JLabel.CENTER));
            p.add(new JLabel(formatFileSize(file.length()), JLabel.CENTER));
            p.add(new JLabel("From: " + sender, JLabel.CENTER));
            p.add(new JLabel(destination.contains("SERVER") ? "Server" : "Forward", JLabel.CENTER));
            add(p, BorderLayout.SOUTH);
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
            return layoutSize(target, false);
        }

        private Dimension layoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int targetWidth = target.getSize().width;
                if (targetWidth == 0) targetWidth = Integer.MAX_VALUE;
                int hgap = getHgap(), vgap = getVgap();
                Insets insets = target.getInsets();
                int maxWidth = targetWidth - (insets.left + insets.right + hgap * 2);
                Dimension dim = new Dimension(0, 0);
                int rowWidth = 0, rowHeight = 0;
                for (Component m : target.getComponents()) {
                    if (m.isVisible()) {
                        Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();
                        if (rowWidth + d.width > maxWidth) {
                            dim.width = Math.max(dim.width, rowWidth);
                            dim.height += rowHeight + vgap;
                            rowWidth = 0;
                            rowHeight = 0;
                        }
                        rowWidth += d.width + hgap;
                        rowHeight = Math.max(rowHeight, d.height);
                    }
                }
                dim.width = Math.max(dim.width, rowWidth);
                dim.height += rowHeight + insets.top + insets.bottom + vgap * 2;
                return dim;
            }
        }
    }

    private String getFileType(File file) {
        String name = file.getName().toLowerCase();
        if (name.endsWith("jpg") || name.endsWith("png")) return "Image";
        return "File";
    }

    public void updateProgress(int progress) {
        SwingUtilities.invokeLater(() -> progressBar.setValue(progress));
    }

    public void updateStatus(String status) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(status));
    }

    private static String formatFileSize(long size) {
        if (size < 1024) return size + " B";
        int exp = (int) (Math.log(size) / Math.log(1024));
        return String.format("%.1f %cKB", size / Math.pow(1024, exp), "KMGTPE".charAt(exp - 1));
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());

            // --- CẤU HÌNH MÀU SẮC LIMEWIRE ---

            // 1. Cấu hình Nút bấm & Progress Bar (Cũ)
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

            // 2. --- MỚI: CẤU HÌNH HÀNG TIÊU ĐỀ (HEADER) ---
            UIManager.put("TableHeader.background", LIME_GREEN);      // Nền xanh
            UIManager.put("TableHeader.foreground", Color.WHITE);     // Chữ trắng
            UIManager.put("TableHeader.bottomSeparatorColor", LIME_GREEN); // Ẩn đường kẻ dưới
            UIManager.put("TableHeader.font", new Font("SansSerif", Font.BOLD, 12)); // Chữ đậm
            // ----------------------------------------------

        } catch (Exception ex) {
            System.err.println("Không thể cài đặt FlatLaf Look and Feel.");
        }

        // Khởi tạo Database
        DatabaseManager.init();

        SwingUtilities.invokeLater(() -> new UDPFileServerGUI().setVisible(true));
    }

    class UDPFileServer extends Thread {
        private DatagramSocket socket;
        private boolean running = true;
        private int port;
        private UDPFileServerGUI gui;
        private FileOutputStream fos;
        private String currentFileName, destination, senderName, message;
        private long expectedFileSize, receivedBytes;
        private InetAddress clientDestIP;
        private int clientDestPort;
        private final File edgeDir = new File("edge_storage");
        private final File serverDir = new File("server_received_files");

        public UDPFileServer(int port, UDPFileServerGUI gui) {
            this.port = port;
            this.gui = gui;
            if (!edgeDir.exists()) edgeDir.mkdirs();
            if (!serverDir.exists()) serverDir.mkdirs();
        }

        @Override
        public void run() {
            try {
                socket = new DatagramSocket(port);
                byte[] buffer = new byte[4096];
                while (running) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    String msg = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);

                    //Xử lý Đăng nhập/Đăng ký
                    if (msg.startsWith("LOGIN|")) {
                        String[] parts = msg.split("\\|");
                        boolean ok = DatabaseManager.checkLogin(parts[1], parts[2]);
                        byte[] resBytes = (ok ? "LOGIN_OK" : "LOGIN_FAIL").getBytes();
                        socket.send(new DatagramPacket(resBytes, resBytes.length, packet.getAddress(), packet.getPort()));
                        continue;
                    } else if (msg.startsWith("REGISTER|")) {
                        String[] parts = msg.split("\\|");
                        boolean ok = DatabaseManager.registerUser(parts[1], parts[2], parts[3]);
                        byte[] resBytes = (ok ? "REG_OK" : "REG_FAIL").getBytes();
                        socket.send(new DatagramPacket(resBytes, resBytes.length, packet.getAddress(), packet.getPort()));
                        continue;
                    }

                    if (msg.startsWith("HEADER|")) {
                        String[] parts = msg.split("\\|");
                        currentFileName = parts[1];
                        expectedFileSize = Long.parseLong(parts[2]);
                        destination = parts[3];
                        senderName = (parts.length >= 5) ? parts[4] : "Unknown"; // Lấy tên người gửi (vị trí 4)
                        message = (parts.length >= 6) ? parts[5] : "";

                        if (destination.startsWith("DEST:CLIENT")) {
                            String[] destInfo = parts[4].split(":");
                            clientDestIP = InetAddress.getByName(destInfo[0]);
                            clientDestPort = Integer.parseInt(destInfo[1]);
                            senderName = parts[5]; // Lấy lại tên nếu là chuyển tiếp
                            message = parts[6];
                        }
                        fos = new FileOutputStream(new File(edgeDir, currentFileName));
                        receivedBytes = 0;
                        gui.addFileToDisplay(currentFileName, expectedFileSize, senderName, message, destination);
                        gui.updateStatus("📥 Receiving: " + currentFileName);
                        gui.updateProgress(0);

                    } else if (msg.startsWith("END|")) {
                        if (fos != null) fos.close();
                        if ("DEST:SERVER".equals(destination)) {
                            Files.move(new File(edgeDir, currentFileName).toPath(), new File(serverDir, currentFileName).toPath(), StandardCopyOption.REPLACE_EXISTING);
                        } else if (destination.startsWith("DEST:CLIENT")) {
                            forwardFileToClient(currentFileName, clientDestIP, clientDestPort, senderName, message);
                        }
                        if (DatabaseManager.isRealUser(senderName)) {
                            DatabaseManager.saveFileLog(currentFileName, expectedFileSize, senderName, destination, "SUCCESS");
                            System.out.println("Đã lưu lịch sử cho user: " + senderName);
                        } else {
                            System.out.println("User '" + senderName + "' là khách hoặc không tồn tại. Không lưu lịch sử.");
                        }
                        gui.updateStatus("✅ Completed: " + currentFileName);
                        gui.updateProgress(100);
                        fos = null;
                        currentFileName = null;
                        destination = null;

                    } else {
                        if (fos != null) {
                            fos.write(packet.getData(), 0, packet.getLength());
                            receivedBytes += packet.getLength();
                            gui.updateProgress((int) ((receivedBytes * 100) / expectedFileSize));
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void forwardFileToClient(String fileName, InetAddress ip, int port, String sender, String msg) {
            // (Giữ nguyên code forward cũ của bạn, không thay đổi)
            new Thread(() -> {
                try {
                    Thread.sleep(300);
                    File fileToForward = new File(edgeDir, fileName);
                    if (!fileToForward.exists()) return;
                    try (DatagramSocket s = new DatagramSocket()) {
                        String header = "HEADER|" + fileName + "|" + fileToForward.length() + "|FROM:SERVER|" + sender + "|" + msg;
                        byte[] hb = header.getBytes(StandardCharsets.UTF_8);
                        s.send(new DatagramPacket(hb, hb.length, ip, port));
                        Thread.sleep(100);
                        try (FileInputStream fis = new FileInputStream(fileToForward)) {
                            byte[] buf = new byte[4096];
                            int read;
                            while ((read = fis.read(buf)) != -1) {
                                s.send(new DatagramPacket(buf, read, ip, port));
                                Thread.sleep(5);
                            }
                            Thread.sleep(150);
                            String end = "END|" + fileName;
                            s.send(new DatagramPacket(end.getBytes(), end.length(), ip, port));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}