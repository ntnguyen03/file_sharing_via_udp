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
Đề tài minh họa cách xây dựng một ứng dụng truyền file qua giao thức UDP dựa trên mô hình Client/Server
Các tính năng chính:
- Xác thực người dùng: Hỗ trợ Đăng nhập, Đăng ký và chế độ Khách (Guest Login) không lưu lịch sử.
- Chia sẻ file qua UDP: Client chia nhỏ file thành các gói tin (4KB) và gửi đến Server hoặc Client khác.
- Chuyển tiếp file (P2P via Server): Server đóng vai trò trung gian chuyển tiếp file đến Client đích dựa trên IP/Port.
- Giao diện hiện đại: Sử dụng FlatLaf cho giao diện phẳng, hỗ trợ Kéo-Thả (Drag & Drop) file, xem trước nội dung (Ảnh/Text).
- Quản lý dữ liệu: Lưu trữ lịch sử truyền tải và thông tin người dùng bằng cơ sở dữ liệu SQLite.
- Chế độ hiển thị linh hoạt: Chuyển đổi giữa dạng Bảng (Table) chi tiết và dạng Lưới (Thumbnail).


---

## 🔧 2. Ngôn ngữ lập trình sử dụng
[![Java](https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=java&logoColor=white)](https://www.java.com/)
<img src="https://img.shields.io/badge/SQLite-003B57?style=for-the-badge&logo=sqlite&logoColor=white" alt="SQLite"/>
<img src="https://img.shields.io/badge/FlatLaf-UI-orange?style=for-the-badge" alt="FlatLaf"/>
- Công nghệ sử dụng
    - **Java Swing** (tạo giao diện)
    - **FlatLaf Library** Thư viện Look and Feel giúp giao diện hiện đại, đẹp mắt.
    - **UDP DatagramSocket** (truyền dữ liệu)
    - **SQLite JDBC** Hệ quản trị cơ sở dữ liệu nhúng để lưu User và Lịch sử.
    - **Multithreading** Xử lý đa luồng cho việc gửi và nhận file đồng thời không gây treo giao diện.
    - **Java DnD & ImageIO** Hỗ trợ kéo thả và xử lý hình ảnh thumbnail.

---

## 🖼 3. Hình ảnh các chức năng
Giao diện chức năng Đăng nhập/Đăng ký

<img width="533" height="291" alt="image" src="https://github.com/user-attachments/assets/b5a08fd6-7ceb-4d26-83c7-8eb9c8f36594" />

Giao diện của Cient gửi

<img width="880" height="639" alt="image" src="https://github.com/user-attachments/assets/2e586973-84e5-408c-aaaf-7949bd88cbb0" />

Giao diện của Server

<img width="884" height="588" alt="image" src="https://github.com/user-attachments/assets/ef3118b5-6afc-4e2a-a9ae-2bfd77e9e3eb" />

Giao diện của Client nhận file

<img width="785" height="592" alt="image" src="https://github.com/user-attachments/assets/b6483b58-5401-48d0-81fe-e3181cd69c86" />

Chọn file gửi từ Client

<img width="880" height="641" alt="image" src="https://github.com/user-attachments/assets/53806ac0-81ec-4dc0-a2a7-cdab432448d0" />

Chọn thư mục lưu trữ trên Client nhận file

<img width="785" height="592" alt="image" src="https://github.com/user-attachments/assets/8d65a3bd-63bc-4f7c-b5e8-99fe3f8618e4" />

Lịch sử và thông tin file đã được gửi (Hình ảnh ví dụ trên Server)

<img width="886" height="591" alt="image" src="https://github.com/user-attachments/assets/f9d0dc9f-445d-4467-ae02-75f8bda7edee" />



---

## ▶️ 4. Cách chạy chương trình
### 1️⃣ Chạy Server
- Mở file UDPFileServerGUI.java.
- Server tự động sử dụng port 8888 (mặc định).
- File nhận được sẽ được lưu vào thư mục server_received_files (cho file lưu tại server) hoặc edge_storage (cho file chuyển tiếp).
- Chuyển đổi giữa chế độ xem bảng và thumbnail bằng nút 🖼️ Thumbnails hoặc 📋 Table.

### 2️⃣ Chạy Client
- Mở file UDPFileClientGUI.java.
- Đăng nhập: Nhập tài khoản hoặc chọn "Dùng không cần Login" (Guest).
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
- Cơ sở dữ liệu: File udp_file_system.db được tạo tự động tại thư mục gốc của dự án.
- Giao thức: Sử dụng giao thức tự định nghĩa:
    - Header: HEADER | filename | size | dest | sender | msg
    - Data: Các gói tin binary 4KB.
    - End: END | filename
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
