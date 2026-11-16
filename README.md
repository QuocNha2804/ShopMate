# 🛒 ShopMate — Phần mềm Quản lý Bán hàng (POS) Desktop

Phần mềm Quản lý Bán hàng (Point of Sale - POS) trên nền tảng Desktop được xây dựng bằng **Java Swing (Java 21)**, giao diện hiện đại với **FlatLaf**, cơ sở dữ liệu **MariaDB**, biểu đồ trực quan **JFreeChart**, xuất hóa đơn **iText PDF** và xuất báo cáo **Apache POI Excel**.

---

## 📦 Tính năng nổi bật

1. **Đăng nhập & Phân quyền (`ADMIN` / `STAFF`):**
   - Mã hóa mật khẩu an toàn bằng `jBCrypt`.
   - Tính năng **Remember Me** và **Đổi mật khẩu**.
   - Phân quyền chặt chẽ giữa Quản trị viên (`ADMIN`) và Thu ngân (`STAFF`).
2. **Dashboard Tổng quan:**
   - 4 thẻ thống kê realtime: Doanh thu & Lợi nhuận hôm nay, Số đơn hôm nay, Sản phẩm sắp hết hàng (`tồn < 10`), Khách hàng mới.
   - Biểu đồ cột doanh thu 7 ngày gần nhất (`JFreeChart`).
   - Top 5 sản phẩm bán chạy & 10 đơn hàng gần nhất (double-click xem và in hóa đơn).
3. **Bán hàng tại quầy (POS) — Màn hình 2 cột:**
   - **Cột trái:** Lưới sản phẩm (`ProductCard` có ảnh, giá bán, tồn kho), tìm kiếm nhanh và lọc theo danh mục.
   - **Cột phải:** Giỏ hàng `JTable`, tăng/giảm số lượng hoặc double-click nhập số lượng, tìm khách hàng theo SĐT (hoặc thêm nhanh khách mới), áp dụng **Mã giảm giá (`Voucher`)** hoặc giảm trực tiếp `%` / `VNĐ`, tự động tính thuế VAT.
   - **Thanh toán (`CheckoutDialog`):** Hỗ trợ Tiền mặt (tự tính tiền thừa trả khách) và Chuyển khoản QR, thực thi bằng **JDBC Transaction** (tạo hóa đơn + trừ tồn kho + cộng điểm tích lũy khách hàng).
4. **Quản lý Sản phẩm, Danh mục & Nhập kho:**
   - CRUD danh mục (cảnh báo khi xóa danh mục đang chứa sản phẩm).
   - CRUD sản phẩm kèm upload ảnh (`JFileChooser` lưu vào `./data/images/`).
   - **Cảnh báo tồn kho thấp (`tồn < 10`):** Tự động tô đỏ dòng sản phẩm trên bảng.
   - **Nhập hàng vào kho (`StockImportDialog`):** Tăng tồn kho, cập nhật giá nhập và lưu lịch sử phiếu nhập kho.
5. **Quản lý Khách hàng, Nhân viên & Khuyến mãi:**
   - Tra cứu lịch sử mua hàng của từng khách hàng và điểm tích lũy.
   - Quản lý nhân viên, gán quyền `ADMIN`/`STAFF`, khóa tài khoản và reset mật khẩu.
   - Quản lý mã giảm giá (`Voucher`).
6. **Lịch sử Hóa đơn, Thống kê & Cài đặt:**
   - Xem trước hóa đơn K80, in ra máy in hoặc xuất PDF tiếng Việt (`iText`).
   - Hủy hóa đơn (dành cho `ADMIN`, tự động **hoàn lại tồn kho** và trừ lại điểm tích lũy).
   - Báo cáo Doanh thu - Giá vốn - Lợi nhuận theo Ngày / Tháng / Năm (`JFreeChart` Bar + Line), xuất Excel 4 sheets (`Apache POI`) & PDF.
   - Cài đặt thông tin cửa hàng, đổi giao diện (`FlatLaf Light / Dark / IntelliJ`), Sao lưu & Phục hồi Database (`.sql`).

---

## 🗄️ Khởi tạo Cơ sở dữ liệu (MariaDB)

Ứng dụng sẽ **tự động khởi tạo database `shopmate_db`, chạy `schema.sql` và nạp dữ liệu mẫu `data.sql`** ngay trong lần đầu tiên khởi động nếu bảng chưa tồn tại!

Bạn cũng có thể chạy thủ công 2 file script trong thư mục `src/main/resources/`:
1. `src/main/resources/schema.sql` — Tạo 8 bảng dữ liệu chuẩn hóa.
2. `src/main/resources/data.sql` — Nạp dữ liệu mẫu đầy đủ (Nhân viên, Danh mục, 18 Sản phẩm, Khách hàng, Voucher, Phiếu nhập kho, Hóa đơn 7 ngày gần nhất).

### 🔑 Tài khoản đăng nhập mặc định
| Vai trò | Username | Mật khẩu |
| :--- | :--- | :--- |
| **Quản trị viên (ADMIN)** | `admin` | `123456` |
| **Nhân viên thu ngân (STAFF)** | `staff` | `123456` |

---

## 🚀 Hướng dẫn Chạy & Đóng gói `.jar`

1. Cấu hình thông tin kết nối MariaDB tại `src/main/resources/db.properties` (mặc định `localhost:3306`, user `root`, password rỗng).
2. Chạy trực tiếp class `qnha.fit.shopmate.Main` trên IntelliJ IDEA, hoặc đóng gói file `.jar` chạy độc lập bằng Maven:
   ```bash
   mvn clean package
   java -jar target/ShopMate-POS.jar
   ```
