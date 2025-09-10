package client;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.*;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

public class UDPFileClient {
    public static void main(String[] args) {
        String serverAddress = "localhost"; // hoặc IP server nếu chạy khác máy
        int port = 8888;
        String inputFile = "send_file.txt";

        try (DatagramSocket socket = new DatagramSocket();
             FileInputStream fis = new FileInputStream(inputFile)) {

            InetAddress serverIP = InetAddress.getByName(serverAddress);
            byte[] buffer = new byte[1024];
            int bytesRead, seq = 0;

            while ((bytesRead = fis.read(buffer)) != -1) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(baos);

                dos.writeInt(seq++); // ghi số thứ tự
                dos.write(buffer, 0, bytesRead);

                byte[] sendData = baos.toByteArray();
                DatagramPacket packet = new DatagramPacket(sendData, sendData.length, serverIP, port);
                socket.send(packet);
            }

            // gửi gói kết thúc
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeInt(-1);
            byte[] endData = baos.toByteArray();
            DatagramPacket endPacket = new DatagramPacket(endData, endData.length, serverIP, port);
            socket.send(endPacket);

            System.out.println("File đã được gửi!");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
