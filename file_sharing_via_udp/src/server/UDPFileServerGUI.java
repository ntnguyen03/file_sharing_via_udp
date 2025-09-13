package server;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class UDPFileServerGUI extends JFrame {
    private JTable table;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JTextField portField;
    private JButton startButton, chooseFolderButton;
    private UDPFileServer server;
    private File saveDir; // Thư mục lưu

    public UDPFileServerGUI() {
        setTitle("UDP File Server");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Bảng lịch sử
        table = new JTable(new DefaultTableModel(new Object[]{"Tên file", "Dung lượng", "Người gửi"}, 0));
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Thanh trạng thái
        JPanel bottomPanel = new JPanel(new BorderLayout());
        progressBar = new JProgressBar(0, 100);
        bottomPanel.add(progressBar, BorderLayout.CENTER);
        statusLabel = new JLabel("Chưa nhận file nào");
        bottomPanel.add(statusLabel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);

        // Port + nút start
        JPanel topPanel = new JPanel(new FlowLayout());
        portField = new JTextField("8888", 10);
        startButton = new JButton("Bắt đầu lắng nghe");
        chooseFolderButton = new JButton("Chọn thư mục lưu");

        topPanel.add(new JLabel("Port:"));
        topPanel.add(portField);
        topPanel.add(startButton);
        topPanel.add(chooseFolderButton);
        add(topPanel, BorderLayout.NORTH);

        // Chọn thư mục lưu file
        chooseFolderButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                saveDir = chooser.getSelectedFile();
                JOptionPane.showMessageDialog(this, "Đã chọn thư mục: " + saveDir.getAbsolutePath());
            }
        });

        startButton.addActionListener(e -> {
            int port = Integer.parseInt(portField.getText());
            server = new UDPFileServer(port, table, progressBar, statusLabel, saveDir);
            server.start();
            startButton.setEnabled(false);
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new UDPFileServerGUI().setVisible(true));
    }
}

class UDPFileServer extends Thread {
    private DatagramSocket socket;
    private boolean running = true;
    private int port;
    private JTable table;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private File saveDir;

    private FileOutputStream fos;
    private String currentFileName;
    private long expectedFileSize;
    private long receivedBytes;

    public UDPFileServer(int port, JTable table, JProgressBar progressBar, JLabel statusLabel, File saveDir) {
        this.port = port;
        this.table = table;
        this.progressBar = progressBar;
        this.statusLabel = statusLabel;
        this.saveDir = saveDir;
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

                // HEADER
                if (msg.startsWith("HEADER|")) {
                    String[] parts = msg.split("\\|");
                    currentFileName = parts[1];
                    expectedFileSize = Long.parseLong(parts[2]);
                    receivedBytes = 0;

                    File dir = (saveDir != null) ? saveDir : new File(".");
                    fos = new FileOutputStream(new File(dir, "received_" + currentFileName));

                    SwingUtilities.invokeLater(() -> {
                        DefaultTableModel model = (DefaultTableModel) table.getModel();
                        model.addRow(new Object[]{currentFileName, expectedFileSize + " bytes", packet.getAddress()});
                        statusLabel.setText("Đang nhận: " + currentFileName);
                        progressBar.setValue(0);
                    });
                }
                // END
                else if (msg.startsWith("END|")) {
                    if (fos != null) {
                        fos.close();
                    }
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("Hoàn tất nhận: " + currentFileName);
                        progressBar.setValue(100);
                    });
                }
                // DATA
                else {
                    if (fos != null) {
                        fos.write(packet.getData(), 0, packet.getLength());
                        receivedBytes += packet.getLength();

                        int progress = (int) ((receivedBytes * 100) / expectedFileSize);

                        SwingUtilities.invokeLater(() -> {
                            progressBar.setValue(progress);
                        });
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopServer() {
        running = false;
        if (socket != null && !socket.isClosed()) socket.close();
    }
}
