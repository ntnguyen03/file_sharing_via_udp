package server;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

public class UDPFileServer {
    public static void main(String[] args) {
        int port = 8888; // cổng lắng nghe
        String outputFile = "received_file.txt";

        try (DatagramSocket socket = new DatagramSocket(port);
             FileOutputStream fos = new FileOutputStream(outputFile)) {

            byte[] buffer = new byte[1028]; // 4 byte cho seq + 1024 byte data
            System.out.println("📡 Server đang lắng nghe trên cổng " + port);

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                ByteArrayInputStream bais = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
                DataInputStream dis = new DataInputStream(bais);

                int seq = dis.readInt();
                if (seq == -1) { // tín hiệu kết thúc
                    System.out.println("File nhận thành công: " + outputFile);
                    break;
                }

                byte[] data = new byte[packet.getLength() - 4];
                dis.readFully(data);
                fos.write(data);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
