<h2 align="center">
    <a href="https://dainam.edu.vn/vi/khoa-cong-nghe-thong-tin">
    🎓 Faculty of Information Technology (DaiNam University)
    </a>
</h2>
<h2 align="center">
   TRUYỀN FILE QUA UDP
</h2>
<div align="center">
    <p align="center">
        <img src="docs/aiotlab_logo.png" alt="AIoTLab Logo" width="170"/>
        <img src="docs/fitdnu_logo.png" alt="AIoTLab Logo" width="180"/>
        <img src="docs/dnu_logo.png" alt="DaiNam University Logo" width="200"/>
    </p>

[![AIoTLab](https://img.shields.io/badge/AIoTLab-green?style=for-the-badge)](https://www.facebook.com/DNUAIoTLab)
[![Faculty of Information Technology](https://img.shields.io/badge/Faculty%20of%20Information%20Technology-blue?style=for-the-badge)](https://dainam.edu.vn/vi/khoa-cong-nghe-thong-tin)
[![DaiNam University](https://img.shields.io/badge/DaiNam%20University-orange?style=for-the-badge)](https://dainam.edu.vn)

</div>

# 📡 Truyền File Qua UDP

## 📖 1. Giới thiệu
Đề tài minh họa cách xây dựng một ứng dụng truyền file qua giao thức UDP dựa trên mô hình Client/Server. Ứng dụng hỗ trợ truyền file từ client đến server hoặc từ client này đến client khác thông qua server, với giao diện người dùng thân thiện và nhiều tính năng nâng cao.
Các tính năng chính:
- Chia sẻ file qua UDP: Client chia nhỏ file thành các gói tin và gửi đến server hoặc client khác.
- Hỗ trợ gửi file trực tiếp đến client khác: Server có thể chuyển tiếp file đến một client khác dựa trên địa chỉ IP và cổng được chỉ định.
- Giao diện người dùng: Hiển thị danh sách file ở dạng bảng hoặc thumbnail, hỗ trợ xem trước file (hình ảnh, văn bản) và kéo-thả file.
- Theo dõi tiến độ: Hiển thị thanh tiến độ và trạng thái truyền file.
- Lịch sử file: Lưu và hiển thị thông tin về các file đã nhận (tên, kích thước, người gửi, thông điệp).
- Chọn thư mục lưu trữ: Client nhận file có thể chọn thư mục lưu trữ, với cơ chế tránh ghi đè file trùng tên.
- Minh họa lập trình mạng với UDP socket trong Java.


---

## 🔧 2. Ngôn ngữ lập trình sử dụng
[![Java](https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=java&logoColor=white)](https://www.java.com/)
- Công nghệ sử dụng
    - **Java Swing** (tạo giao diện)
    - **UDP DatagramSocket** (truyền dữ liệu)
    - Java AWT/Swing DnD: Hỗ trợ kéo-thả file vào giao diện client.
    - Java ImageIO: Hỗ trợ xem trước hình ảnh và tạo thumbnail.

---

## 🖼 3. Hình ảnh các chức năng
Giao diện của Cient gửi
<img width="1111" height="798" alt="image" src="https://github.com/user-attachments/assets/79e1aacf-41d1-4b34-99a3-9c79fc7f51f8" />


Giao diện của Server
<img width="977" height="734" alt="image" src="https://github.com/user-attachments/assets/eae22505-f785-470f-aa5d-ff9722683616" />

Giao diện của Client nhận file
<img width="988" height="746" alt="image" src="https://github.com/user-attachments/assets/7f19ab5e-b44a-49c4-88f7-319a542e4210" />

Chọn file gửi từ Client
<img width="1108" height="804" alt="image" src="https://github.com/user-attachments/assets/af1113cd-9c66-464c-8422-9215bee89275" />

Chọn thư mục lưu trữ trên Client nhận file
<img width="992" height="744" alt="image" src="https://github.com/user-attachments/assets/488d1739-0662-464c-9fdc-f40250466a57" />

Lịch sử và thông tin file đã được gửi
<img width="1840" height="1010" alt="image" src="https://github.com/user-attachments/assets/22e1d881-0df1-473f-b6b1-090415ff3e36" />


---

## ▶️ 4. Cách chạy chương trình
### 1️⃣ Chạy Server
- Mở file UDPFileServerGUI.java.
- Server tự động sử dụng port 8888 (mặc định).
- File nhận được sẽ được lưu vào thư mục server_received_files (cho file lưu tại server) hoặc edge_storage (cho file chuyển tiếp).
- Chuyển đổi giữa chế độ xem bảng và thumbnail bằng nút 🖼️ Thumbnails hoặc 📋 Table.

### 2️⃣ Chạy Client
- Mở file UDPFileClientGUI.java.
- Nhập IP của server (mặc định: localhost) và port (mặc định: 8888).
- Chọn chế độ gửi:
    - Send to Server: Gửi file đến server để lưu trữ.
    - Send to Other Client: Nhập IP và port của client đích để chuyển tiếp file qua server.
- Nhập tên người gửi và thông điệp (tùy chọn).
- Chọn file bằng nút Choose Files hoặc kéo-thả file vào vùng được chỉ định.
- Nhấn Send Selected để gửi file đã chọn hoặc Send All để gửi tất cả file trong danh sách.
- Chuyển đổi giữa chế độ xem bảng và thumbnail bằng nút 🖼️ Thumbnails hoặc 📋 Table.
- Xem trước file bằng cách chọn file trong danh sách hoặc thumbnail (hỗ trợ hình ảnh và văn bản).

### 3️⃣ Chạy Client Nhận File
- Mở file UDPFileClientReceive.java.
- Client tự động lắng nghe trên port 9999 (mặc định).
- Khi nhận được file, người dùng được yêu cầu chọn thư mục lưu trữ (chỉ chọn một lần cho tất cả file).
- File nhận được hiển thị trong danh sách hoặc thumbnail, với thông tin về tên, kích thước, người gửi, và thông điệp.
- Nhấn đúp vào thumbnail để mở thư mục chứa file.
- Nhấn nút 🔄 Change Save Directory để thay đổi thư mục lưu trữ.
---

## 📌 Ghi chú
- Server phải được khởi động trước khi client gửi file.
- Client nhận file phải chạy và lắng nghe trên port được chỉ định (mặc định: 9999) để nhận file chuyển tiếp.
- File được chia thành các gói tin 4KB để truyền qua UDP.
- Ứng dụng hỗ trợ nhiều loại file (hình ảnh, văn bản, PDF, v.v.) với thumbnail và xem trước phù hợp.
- UDP không đảm bảo toàn vẹn gói tin, vì vậy ứng dụng phù hợp nhất cho các file nhỏ hoặc trung bình và môi trường mạng ổn định.
- Nếu không chọn thư mục lưu trên client nhận, file sẽ bị từ chối. Server lưu file vào thư mục mặc định nếu không chuyển tiếp.

---
   
## 📝 Thông tin cá nhân
- Nguyễn Trường Nam - CNTT 16-03
- Email: truongnam0304@gmail.com
- Phone: 0397367184
© 2025 AIoTLab, Faculty of Information Technology, DaiNam University. All rights reserved.
