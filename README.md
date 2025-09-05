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

⚠️ Do UDP không đảm bảo tính tin cậy, đề tài có thể mở rộng thêm cơ chế **ACK/NACK**, đánh số thứ tự gói tin hoặc phát hiện gói tin mất để đảm bảo truyền tải chính xác hơn.

---

## 🔧 2. Ngôn ngữ lập trình sử dụng
[![Java](https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=java&logoColor=white)](https://www.java.com/)

---

## 🚀 3. Các chức năng chính
- 📤 **Client**: đọc file, chia thành gói tin, gửi qua UDP.
- 📥 **Server**: nhận gói tin, sắp xếp và ghi thành file đầu ra.
- 🔢 **Đánh số gói tin**: đảm bảo dữ liệu ghép đúng thứ tự.
- 🛠️ **Có thể mở rộng**:
  - Cơ chế kiểm tra mất gói và gửi lại.
  - Truyền nhiều file liên tiếp.
  - Mã hoá dữ liệu trước khi gửi.

---

## 📂 4. Cấu trúc thư mục

├── client/
│ └── UDPFileClient.java
├── server/
│ └── UDPFileServer.java
├── docs/
│ └── README.md
└── README.md

---

## ▶️ 5. Cách chạy chương trình
1. **Biên dịch chương trình**:
   ```bash
   javac client/UDPFileClient.java server/UDPFileServer.java

