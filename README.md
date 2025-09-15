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
Đề tài minh hoạ cách xây dựng một ứng dụng **truyền file qua giao thức UDP** dựa trên mô hình **Client/Server**.  
Ứng dụng cho phép:
- Client chia nhỏ file thành nhiều gói tin và gửi tới Server.
- Server nhận, ghép lại các gói tin và lưu thành file hoàn chỉnh.
- Minh họa lập trình mạng với **UDP socket** trong Java.


---

## 🔧 2. Ngôn ngữ lập trình sử dụng
[![Java](https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=java&logoColor=white)](https://www.java.com/)
- Công nghệ sử dụng
    - **Java 8+**
    - **Java Swing** (tạo giao diện)
    - **UDP DatagramSocket** (truyền dữ liệu)

---

## 🚀 3. Các chức năng chính
- Client chọn nhiều file và gửi tới Server qua UDP socket.
- File được chia nhỏ thành nhiều gói tin và truyền đi.
- Server nhận, ghép lại và lưu file vào thư mục chỉ định.
- Hiển thị tiến trình gửi/nhận file qua **JProgressBar**.
- Giao diện trực quan bằng **Java Swing**.
- **Có thể mở rộng**:
  - Cơ chế kiểm tra mất gói và gửi lại.
  - Truyền nhiều file liên tiếp.
  - Mã hoá dữ liệu trước khi gửi.

---

## 📂 4. Demo giao diện
Giao diện của Cient
<img width="728" height="484" alt="image" src="https://github.com/user-attachments/assets/6928b83a-7d87-4fee-8f33-8aef30b74fc4" />

Giao diện của Server
<img width="733" height="437" alt="image" src="https://github.com/user-attachments/assets/2649ec7c-8fff-475f-996f-2d2ca80f52f8" />


---

## ▶️ 5. Cách chạy chương trình
### 1️⃣ Chạy Server
- Mở `UDPFileServerGUI.java`
- Chọn **Port** (mặc định: `8888`)
- Bấm **Chọn thư mục lưu** để chỉ định nơi nhận file
- Nhấn **Bắt đầu lắng nghe**

### 2️⃣ Chạy Client
- Mở `UDPFileClientGUI.java`
- Nhập **IP** của Server (mặc định: `localhost`)
- Nhập **Port** (mặc định: `8888`)
- Chọn file cần gửi và nhấn **Gửi**

---

## 📌 Ghi chú
- Server phải được khởi động **trước khi Client gửi file**.
- Nếu chưa chọn thư mục lưu, file sẽ được lưu ngay tại thư mục chạy chương trình.
- UDP không đảm bảo toàn vẹn gói tin → chỉ phù hợp để demo, với file nhỏ/medium.

---
   

