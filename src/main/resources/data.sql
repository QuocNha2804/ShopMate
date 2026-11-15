-- =====================================================================
-- SHOPMATE POS - SAMPLE DATA SCRIPT (data.sql)
-- Mật khẩu mặc định cho tất cả tài khoản: 123456
-- Hash BCrypt ($2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy)
-- =====================================================================

USE shopmate_db;

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE stock_imports;
TRUNCATE TABLE invoice_details;
TRUNCATE TABLE invoices;
TRUNCATE TABLE vouchers;
TRUNCATE TABLE customers;
TRUNCATE TABLE products;
TRUNCATE TABLE categories;
TRUNCATE TABLE users;
SET FOREIGN_KEY_CHECKS = 1;

-- 1. Dữ liệu Nhân viên (users) - Mật khẩu: 123456
INSERT INTO users (id, code, username, password_hash, full_name, phone, email, role, status, created_at) VALUES
(1, 'NV001', 'admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Nguyễn Quốc Nhã (Admin)', '0901234567', 'admin@shopmate.vn', 'ADMIN', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 60 DAY)),
(2, 'NV002', 'staff', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Trần Thị Thu Ngân', '0912345678', 'thungan@shopmate.vn', 'STAFF', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 45 DAY)),
(3, 'NV003', 'minhanh', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Lê Minh Anh', '0923456789', 'minhanh@shopmate.vn', 'STAFF', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 30 DAY)),
(4, 'NV004', 'hoangnam', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Phạm Hoàng Nam', '0934567890', 'hoangnam@shopmate.vn', 'STAFF', 'LOCKED', DATE_SUB(NOW(), INTERVAL 20 DAY));

-- 2. Dữ liệu Danh mục (categories)
INSERT INTO categories (id, name, description, is_active) VALUES
(1, 'Đồ uống & Giải khát', 'Nước ngọt, trà đóng chai, cà phê, sữa các loại', 1),
(2, 'Bánh kẹo & Đồ ăn vặt', 'Snack, bánh quy, kẹo dẻo, socola nhập khẩu', 1),
(3, 'Thực phẩm khô & Gia vị', 'Mì ăn liền, dầu ăn, nước mắm, hạt nêm, gạo', 1),
(4, 'Chăm sóc cá nhân', 'Dầu gội, sữa tắm, kem đánh răng, khăn giấy', 1),
(5, 'Đồ gia dụng tiện ích', 'Pin, túi rác, màng bọc thực phẩm, nước rửa chén', 1);

-- 3. Dữ liệu Sản phẩm (products) - Có các SP tồn < 10 để test cảnh báo đỏ
INSERT INTO products (id, code, category_id, name, unit, cost_price, selling_price, stock_quantity, image_path, description, status) VALUES
(1, 'SP001', 1, 'Nước ngọt Coca-Cola Lon 320ml', 'Lon', 7500, 10000, 120, 'coca_320ml.png', 'Nước giải khát có gas Coca-Cola chính hãng', 'ACTIVE'),
(2, 'SP002', 1, 'Trà Ô Long Tea+ Plus 450ml', 'Chai', 8000, 12000, 85, 'olong_teaplus.png', 'Trà Ô Long giúp hạn chế hấp thu chất béo', 'ACTIVE'),
(3, 'SP003', 1, 'Sữa tươi tiệt trùng Vinamilk 100% (Lốc 4 hộp)', 'Lốc', 26000, 32000, 45, 'vinamilk_loc4.png', 'Sữa tươi tiệt trùng có đường 180ml x 4', 'ACTIVE'),
(4, 'SP004', 1, 'Cà phê hòa tan G7 3in1 (Hộp 18 gói)', 'Hộp', 48000, 58000, 6, 'cafe_g7.png', 'Cà phê thứ thiệt Trung Nguyên Legend (Sắp hết hàng)', 'ACTIVE'),
(5, 'SP005', 1, 'Nước khoáng LaVie 500ml', 'Chai', 3500, 6000, 150, 'lavie_500ml.png', 'Nước khoáng thiên nhiên LaVie', 'ACTIVE'),
(6, 'SP006', 2, 'Bánh ChocoPie Orion (Hộp 12 cái)', 'Hộp', 45000, 56000, 38, 'chocopie_12.png', 'Bánh bông lan phủ socola nhân marshmallow', 'ACTIVE'),
(7, 'SP007', 2, 'Snack Khoai tây Lay''s Vị Tự Nhiên 95g', 'Gói', 16000, 22000, 8, 'snack_lays.png', 'Snack khoai tây lát mỏng giòn rụm (Sắp hết hàng)', 'ACTIVE'),
(8, 'SP008', 2, 'Bánh quy bơ Cosy Marie (Gói 240g)', 'Gói', 22000, 29000, 52, 'cosy_marie.png', 'Bánh quy bơ truyền thống thơm ngon', 'ACTIVE'),
(9, 'SP009', 2, 'Kẹo dẻo Haribo Goldbears 80g', 'Gói', 24000, 32000, 4, 'haribo_80g.png', 'Kẹo dẻo trái cây hình gấu Đức (Sắp hết hàng)', 'ACTIVE'),
(10, 'SP010', 3, 'Mì Hảo Hảo Tôm Chua Cay (Thùng 30 gói)', 'Thùng', 105000, 125000, 25, 'mi_haohao_thung.png', 'Mì quốc dân Hảo Hảo Acecook', 'ACTIVE'),
(11, 'SP011', 3, 'Dầu ăn thực vật Neptune Light 1 Lít', 'Chai', 48000, 59000, 30, 'dau_neptune_1l.png', 'Dầu ăn thượng hạng tốt cho sức khỏe', 'ACTIVE'),
(12, 'SP012', 3, 'Nước mắm Nam Ngư Đệ Nhị 900ml', 'Chai', 35000, 44000, 7, 'nuocmam_namngu.png', 'Nước mắm thơm ngon đậm đà (Sắp hết hàng)', 'ACTIVE'),
(13, 'SP013', 3, 'Hạt nêm Knorr Thịt Thăn Xương Ống 400g', 'Gói', 31000, 39000, 40, 'hatnem_knorr.png', 'Hạt nêm từ thịt thăn và xương ống', 'ACTIVE'),
(14, 'SP014', 4, 'Dầu gội Clear Mát Lạnh Bạc Hà 630g', 'Chai', 135000, 165000, 18, 'daugoi_clear.png', 'Dầu gội sạch gàu mát lạnh sảng khoái', 'ACTIVE'),
(15, 'SP015', 4, 'Kem đánh răng PS Bảo Vệ 123 230g', 'Tuýp', 25000, 34000, 60, 'kdr_ps.png', 'Kem đánh răng ngừa sâu răng vượt trội', 'ACTIVE'),
(16, 'SP016', 4, 'Khăn giấy rút Pulppy 2 lớp (Lốc 3 gói)', 'Lốc', 42000, 52000, 5, 'khangiay_pulppy.png', 'Giấy lụa cao cấp mềm mịn (Sắp hết hàng)', 'ACTIVE'),
(17, 'SP017', 5, 'Nước rửa chén Sunlight Chanh 750g', 'Chai', 24000, 31000, 42, 'sunlight_chanh.png', 'Sạch dầu mỡ nhanh chóng hương chanh', 'ACTIVE'),
(18, 'SP018', 5, 'Pin Tiểu AA Energizer Max (Vỉ 2 viên)', 'Vỉ', 21000, 28000, 3, 'pin_energizer.png', 'Pin kiềm dung lượng cao chống rò rỉ (Sắp hết hàng)', 'ACTIVE');

-- 4. Dữ liệu Khách hàng (customers)
INSERT INTO customers (id, name, phone, email, address, points, created_at) VALUES
(1, 'Nguyễn Văn An', '0987654321', 'nguyenvanan@gmail.com', '15 Lê Lợi, Q. Gò Vấp, TP.HCM', 35, DATE_SUB(NOW(), INTERVAL 25 DAY)),
(2, 'Trần Ngọc Bích', '0976543210', 'ngocbich.tran@gmail.com', '88 Quang Trung, Q. Gò Vấp, TP.HCM', 52, DATE_SUB(NOW(), INTERVAL 18 DAY)),
(3, 'Lê Hoàng Cường', '0965432109', 'cuongle@yahoo.com', '210 Phan Văn Trị, Q. Bình Thạnh, TP.HCM', 18, DATE_SUB(NOW(), INTERVAL 10 DAY)),
(4, 'Phạm Thanh Dung', '0954321098', 'thanhdung@gmail.com', '42 Nguyễn Oanh, Q. Gò Vấp, TP.HCM', 12, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(5, 'Võ Minh Khôi', '0943210987', 'minhkhoi.vo@gmail.com', '105 Hoàng Hoa Thám, Q. Tân Bình, TP.HCM', 8, NOW()),
(6, 'Đặng Thu Thảo', '0932109876', 'thuthao.dang@gmail.com', '79 Nguyễn Kiệm, Q. Phú Nhuận, TP.HCM', 4, NOW());

-- 5. Dữ liệu Mã giảm giá (vouchers)
INSERT INTO vouchers (id, code, discount_type, discount_value, min_order_amount, max_discount, usage_limit, used_count, start_date, end_date, is_active) VALUES
(1, 'WELCOME10', 'PERCENT', 10.00, 100000, 50000, 100, 5, DATE_SUB(CURDATE(), INTERVAL 30 DAY), DATE_ADD(CURDATE(), INTERVAL 60 DAY), 1),
(2, 'GIAM20K', 'FIXED', 20000.00, 150000, 20000, 50, 3, DATE_SUB(CURDATE(), INTERVAL 15 DAY), DATE_ADD(CURDATE(), INTERVAL 45 DAY), 1),
(3, 'VIP50K', 'FIXED', 50000.00, 400000, 50000, 30, 1, DATE_SUB(CURDATE(), INTERVAL 10 DAY), DATE_ADD(CURDATE(), INTERVAL 90 DAY), 1);

-- 6. Dữ liệu Lịch sử Nhập kho (stock_imports)
INSERT INTO stock_imports (import_code, user_id, product_id, quantity, import_price, note, created_at) VALUES
('NK2026001', 1, 1, 100, 7500, 'Nhập hàng đầu kỳ Coca-Cola', DATE_SUB(NOW(), INTERVAL 10 DAY)),
('NK2026002', 1, 10, 30, 105000, 'Nhập thùng mì Hảo Hảo', DATE_SUB(NOW(), INTERVAL 7 DAY)),
('NK2026003', 1, 14, 20, 135000, 'Nhập bổ sung dầu gội Clear', DATE_SUB(NOW(), INTERVAL 3 DAY));

-- 7. Dữ liệu Hóa đơn mẫu trong 7 ngày gần đây (invoices)
INSERT INTO invoices (id, invoice_code, customer_id, user_id, voucher_id, subtotal, discount_amount, vat_amount, total_amount, cash_received, change_amount, payment_method, status, created_at) VALUES
(1, 'HD00001', 1, 2, NULL, 250000, 0, 20000, 270000, 300000, 30000, 'CASH', 'PAID', DATE_SUB(NOW(), INTERVAL 6 DAY)),
(2, 'HD00002', 2, 2, 1, 320000, 32000, 23040, 311040, 311040, 0, 'TRANSFER', 'PAID', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(3, 'HD00003', NULL, 3, NULL, 184000, 0, 14720, 198720, 200000, 1280, 'CASH', 'PAID', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(4, 'HD00004', 3, 2, 2, 415000, 20000, 31600, 426600, 450000, 23400, 'CASH', 'PAID', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(5, 'HD00005', 1, 3, NULL, 293000, 0, 23440, 316440, 316440, 0, 'TRANSFER', 'PAID', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(6, 'HD00006', 4, 2, 1, 510000, 50000, 36800, 496800, 500000, 3200, 'CASH', 'PAID', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(7, 'HD00007', 2, 1, NULL, 353000, 0, 28240, 381240, 400000, 18760, 'CASH', 'PAID', NOW()),
(8, 'HD00008', 5, 2, NULL, 218000, 0, 17440, 235440, 235440, 0, 'TRANSFER', 'PAID', NOW());

-- 8. Chi tiết hóa đơn (invoice_details)
INSERT INTO invoice_details (invoice_id, product_id, quantity, cost_price, unit_price, line_total) VALUES
(1, 10, 2, 105000, 125000, 250000),
(2, 3, 5, 26000, 32000, 160000),
(2, 1, 10, 7500, 10000, 100000),
(2, 2, 5, 8000, 12000, 60000),
(3, 6, 2, 45000, 56000, 112000),
(3, 2, 6, 8000, 12000, 72000),
(4, 14, 1, 135000, 165000, 165000),
(4, 10, 2, 105000, 125000, 250000),
(5, 11, 2, 48000, 59000, 118000),
(5, 1, 10, 7500, 10000, 100000),
(5, 5, 5, 3500, 6000, 30000),
(5, 7, 1, 16000, 22000, 22000),
(5, 8, 1, 22000, 29000, 23000),
(6, 10, 2, 105000, 125000, 250000),
(6, 14, 1, 135000, 165000, 165000),
(6, 3, 2, 26000, 32000, 64000),
(6, 17, 1, 24000, 31000, 31000),
(7, 4, 2, 48000, 58000, 116000),
(7, 6, 2, 45000, 56000, 112000),
(7, 10, 1, 105000, 125000, 125000),
(8, 3, 3, 26000, 32000, 96000),
(8, 15, 2, 25000, 34000, 68000),
(8, 1, 3, 7500, 10000, 30000),
(8, 2, 2, 8000, 12000, 24000);
