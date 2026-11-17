package qnha.fit.shopmate.service;

import qnha.fit.shopmate.dao.CustomerDAO;
import qnha.fit.shopmate.dao.VoucherDAO;
import qnha.fit.shopmate.exception.BusinessException;
import qnha.fit.shopmate.model.Customer;
import qnha.fit.shopmate.model.Voucher;
import qnha.fit.shopmate.util.Validator;

import java.math.BigDecimal;
import java.util.List;

public class CustomerService {
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final VoucherDAO voucherDAO = new VoucherDAO();

    // ================= CUSTOMER =================

    public List<Customer> getAllCustomers() {
        return customerDAO.findAll();
    }

    public List<Customer> searchCustomers(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return customerDAO.findAll();
        }
        return customerDAO.search(keyword);
    }

    public Customer findByPhone(String phone) {
        if (phone == null || phone.isBlank()) return null;
        return customerDAO.findByPhone(phone.trim());
    }

    public void saveCustomer(Customer customer) {
        Validator.requireNonBlank(customer.getName(), "Tên khách hàng");
        Validator.validatePhone(customer.getPhone());
        Validator.validateEmailOptional(customer.getEmail());
        if (customer.getPoints() < 0) {
            throw new BusinessException("Điểm tích lũy không được âm!");
        }
        if (customer.getId() == 0) {
            customerDAO.insert(customer);
        } else {
            customerDAO.update(customer);
        }
    }

    public void deleteCustomer(int customerId) {
        customerDAO.deleteById(customerId);
    }

    // ================= VOUCHER =================

    public List<Voucher> getAllVouchers() {
        return voucherDAO.findAll();
    }

    public Voucher validateAndGetVoucher(String code, BigDecimal subtotal) {
        Validator.requireNonBlank(code, "Mã giảm giá");
        Voucher v = voucherDAO.findByCode(code.trim());
        if (v == null) {
            throw new BusinessException("Mã giảm giá '" + code + "' không tồn tại!");
        }
        if (!v.isActive()) {
            throw new BusinessException("Mã giảm giá '" + code + "' đã ngưng kích hoạt!");
        }
        if (v.getUsedCount() >= v.getUsageLimit()) {
            throw new BusinessException("Mã giảm giá '" + code + "' đã hết lượt sử dụng!");
        }
        if (!v.isValidForOrder(subtotal)) {
            throw new BusinessException("Đơn hàng chưa đủ điều kiện hoặc mã giảm giá đã hết hạn (Đơn tối thiểu: "
                    + qnha.fit.shopmate.util.FormatUtil.formatCurrency(v.getMinOrderAmount()) + ")!");
        }
        return v;
    }

    public void saveVoucher(Voucher v) {
        Validator.validateCode(v.getCode(), "Mã giảm giá");
        if (v.getDiscountValue() == null || v.getDiscountValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Giá trị giảm phải lớn hơn 0!");
        }
        if ("PERCENT".equalsIgnoreCase(v.getDiscountType()) && v.getDiscountValue().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new BusinessException("Phần trăm giảm giá không được vượt quá 100%!");
        }
        if (v.getStartDate() == null || v.getEndDate() == null || v.getEndDate().isBefore(v.getStartDate())) {
            throw new BusinessException("Ngày kết thúc phải lớn hơn hoặc bằng ngày bắt đầu!");
        }
        if (v.getId() == 0) {
            voucherDAO.insert(v);
        } else {
            voucherDAO.update(v);
        }
    }

    public void deleteVoucher(int id) {
        voucherDAO.deleteById(id);
    }
}
