package client;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class HistoryDialog extends JDialog {
    private JTable table;
    private DefaultTableModel tableModel;
    private String username;

    // Cấu hình Server
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 8888;

    // Màu sắc
    public static final Color LIME_GREEN = new Color(144, 200, 75);
    public static final Color LIME_GREEN_LIGHT = new Color(245, 255, 240);

    public HistoryDialog(Frame parent, String username) {
        super(parent, "Lịch sử gửi file - " + username, true); // true = chặn cửa sổ chính khi đang mở
        this.username = username;

        setSize(700, 500);
        setLayout(new BorderLayout());
        setLocationRelativeTo(parent);

        // 1. Tạo Bảng
        String[] columns = {"Tên File", "Kích thước", "Người nhận", "Thời gian"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(25);
        table.getTableHeader().setBackground(LIME_GREEN);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));

        // Cột kích thước nhỏ lại
        table.getColumnModel().getColumn(1).setPreferredWidth(80);
        table.getColumnModel().getColumn(3).setPreferredWidth(150);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);

        // 2. Nút đóng
        JButton closeBtn = new JButton("Đóng");
        closeBtn.setBackground(new Color(220, 53, 69)); // Màu đỏ
        closeBtn.setForeground(Color.WHITE);
        closeBtn.addActionListener(e -> dispose());

        JPanel btnPanel = new JPanel();
        btnPanel.add(closeBtn);
        add(btnPanel, BorderLayout.SOUTH);

        // 3. Tải dữ liệu ngay khi mở
        loadHistoryFromServer();
    }

    private void loadHistoryFromServer() {
        new Thread(() -> {
            try (DatagramSocket socket = new DatagramSocket()) {
                socket.setSoTimeout(2000); // Chờ tối đa 2 giây

                // Gửi yêu cầu
                String request = "GET_HISTORY|" + username;
                byte[] reqData = request.getBytes(StandardCharsets.UTF_8);
                InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
                socket.send(new DatagramPacket(reqData, reqData.length, serverAddr, SERVER_PORT));

                // Nhận dữ liệu
                byte[] buffer = new byte[4096];
                while (true) {
                    try {
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                        socket.receive(packet);
                        String msg = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);

                        if (msg.startsWith("HISTORY_ITEM|")) {
                            // Format: HISTORY_ITEM|filename|size|receiver|timestamp
                            String[] parts = msg.split("\\|");
                            if (parts.length >= 5) {
                                String fName = parts[1];
                                long fSize = Long.parseLong(parts[2]);
                                String receiver = parts[3].contains("SERVER") ? "Server" : "Client Khác";
                                String time = parts[4];

                                SwingUtilities.invokeLater(() -> {
                                    tableModel.addRow(new Object[]{
                                            fName, formatFileSize(fSize), receiver, time
                                    });
                                });
                            }
                        }
                    } catch (SocketTimeoutException e) {
                        // Hết 2 giây không thấy tin nhắn mới -> Coi như xong
                        if (tableModel.getRowCount() == 0) {
                            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Chưa có lịch sử nào."));
                        }
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Lỗi kết nối Server!"));
            }
        }).start();
    }

    private static String formatFileSize(long size) {
        if (size < 1024) return size + " B";
        int exp = (int) (Math.log(size) / Math.log(1024));
        char unit = "KMGTPE".charAt(exp - 1);
        return String.format("%.1f %sB", size / Math.pow(1024, exp), unit);
    }
}