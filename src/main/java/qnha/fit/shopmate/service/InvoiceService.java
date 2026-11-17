package qnha.fit.shopmate.service;

import qnha.fit.shopmate.config.AppConfig;
import qnha.fit.shopmate.config.DBConnection;
import qnha.fit.shopmate.dao.CustomerDAO;
import qnha.fit.shopmate.dao.InvoiceDAO;
import qnha.fit.shopmate.dao.ProductDAO;
import qnha.fit.shopmate.dao.VoucherDAO;
import qnha.fit.shopmate.exception.BusinessException;
import qnha.fit.shopmate.exception.DataAccessException;
import qnha.fit.shopmate.model.CartItem;
import qnha.fit.shopmate.model.Invoice;
import qnha.fit.shopmate.model.InvoiceDetail;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class InvoiceService {
    private final InvoiceDAO invoiceDAO = new InvoiceDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final VoucherDAO voucherDAO = new VoucherDAO();

    public Invoice checkout(Invoice invoice, List<CartItem> cartItems) {
        if (cartItems == null || cartItems.isEmpty()) {
            throw new BusinessException("Giỏ hàng đang trống, không thể thanh toán!");
        }
        if (invoice.getCashReceived().compareTo(invoice.getTotalAmount()) < 0) {
            throw new BusinessException("Số tiền khách đưa chưa đủ để thanh toán!");
        }

        String invoiceCode = invoiceDAO.generateNextInvoiceCode();
        invoice.setInvoiceCode(invoiceCode);
        invoice.setStatus("PAID");
        invoice.setCreatedAt(LocalDateTime.now());

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Lưu hóa đơn
                invoiceDAO.insertInvoiceWithConnection(conn, invoice);

                // 2. Lưu chi tiết & trừ tồn kho
                List<InvoiceDetail> savedDetails = new ArrayList<>();
                for (CartItem item : cartItems) {
                    boolean stockOk = productDAO.deductStockWithConnection(
                            conn,
                            item.getProduct().getId(),
                            item.getQuantity()
                    );
                    if (!stockOk) {
                        throw new BusinessException("Sản phẩm '" + item.getProduct().getName()
                                + "' không đủ số lượng tồn kho để bán!");
                    }

                    InvoiceDetail detail = new InvoiceDetail();
                    detail.setInvoiceId(invoice.getId());
                    detail.setProductId(item.getProduct().getId());
                    detail.setProductCode(item.getProduct().getCode());
                    detail.setProductName(item.getProduct().getName());
                    detail.setUnit(item.getProduct().getUnit());
                    detail.setQuantity(item.getQuantity());
                    detail.setCostPrice(item.getCostPrice());
                    detail.setUnitPrice(item.getUnitPrice());
                    detail.setLineTotal(item.getLineTotal());

                    invoiceDAO.insertDetailWithConnection(conn, detail);
                    savedDetails.add(detail);
                }
                invoice.setDetails(savedDetails);

                // 3. Tăng số lần dùng Voucher (nếu có)
                if (invoice.getVoucherId() != null && invoice.getVoucherId() > 0) {
                    voucherDAO.incrementUsedCountWithConnection(conn, invoice.getVoucherId());
                }

                // 4. Cộng điểm tích lũy cho khách hàng (nếu có chọn khách)
                if (invoice.getCustomerId() != null && invoice.getCustomerId() > 0) {
                    int rate = AppConfig.getPointsRate();
                    int earnedPoints = invoice.getTotalAmount()
                            .divideToIntegralValue(BigDecimal.valueOf(rate))
                            .intValue();
                    if (earnedPoints > 0) {
                        customerDAO.updatePointsWithConnection(conn, invoice.getCustomerId(), earnedPoints);
                    }
                }

                conn.commit();
                return invoice;
            } catch (BusinessException be) {
                conn.rollback();
                throw be;
            } catch (Exception e) {
                conn.rollback();
                throw new DataAccessException("Lỗi khi lưu hóa đơn: " + e.getMessage(), e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi kết nối CSDL khi thanh toán: " + e.getMessage(), e);
        }
    }

    public void cancelInvoice(int invoiceId, String cancelReason) {
        if (cancelReason == null || cancelReason.trim().isEmpty()) {
            throw new BusinessException("Vui lòng nhập lý do hủy hóa đơn!");
        }
        Invoice inv = invoiceDAO.findByIdWithDetails(invoiceId);
        if (inv == null) {
            throw new BusinessException("Không tìm thấy hóa đơn cần hủy!");
        }
        if ("CANCELLED".equalsIgnoreCase(inv.getStatus())) {
            throw new BusinessException("Hóa đơn này đã được hủy trước đó!");
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Cập nhật trạng thái hủy
                invoiceDAO.updateCancelStatusWithConnection(conn, invoiceId, cancelReason.trim());

                // 2. Hoàn lại số lượng tồn kho cho từng mặt hàng
                for (InvoiceDetail detail : inv.getDetails()) {
                    productDAO.restoreStockWithConnection(conn, detail.getProductId(), detail.getQuantity());
                }

                // 3. Hoàn lại lượt dùng Voucher
                if (inv.getVoucherId() != null && inv.getVoucherId() > 0) {
                    voucherDAO.decrementUsedCountWithConnection(conn, inv.getVoucherId());
                }

                // 4. Trừ lại điểm tích lũy đã cộng của khách
                if (inv.getCustomerId() != null && inv.getCustomerId() > 0) {
                    int rate = AppConfig.getPointsRate();
                    int pointsToDeduct = inv.getTotalAmount()
                            .divideToIntegralValue(BigDecimal.valueOf(rate))
                            .intValue();
                    if (pointsToDeduct > 0) {
                        customerDAO.updatePointsWithConnection(conn, inv.getCustomerId(), -pointsToDeduct);
                    }
                }

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw new DataAccessException("Lỗi khi hủy hóa đơn và hoàn kho: " + e.getMessage(), e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi kết nối CSDL khi hủy hóa đơn: " + e.getMessage(), e);
        }
    }

    public Invoice getInvoiceWithDetails(int invoiceId) {
        return invoiceDAO.findByIdWithDetails(invoiceId);
    }

    public List<Invoice> getRecentInvoices(int limit) {
        return invoiceDAO.findRecentInvoices(limit);
    }

    public List<Invoice> getInvoicesByCustomer(int customerId) {
        return invoiceDAO.findByCustomerId(customerId);
    }

    public List<Invoice> searchInvoices(String keyword, LocalDate fromDate, LocalDate toDate, Integer userId, String status) {
        return invoiceDAO.searchInvoices(keyword, fromDate, toDate, userId, status);
    }
}
