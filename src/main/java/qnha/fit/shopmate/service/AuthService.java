package qnha.fit.shopmate.service;

import org.mindrot.jbcrypt.BCrypt;
import qnha.fit.shopmate.config.SessionManager;
import qnha.fit.shopmate.dao.UserDAO;
import qnha.fit.shopmate.exception.BusinessException;
import qnha.fit.shopmate.model.User;
import qnha.fit.shopmate.util.Validator;

import java.util.List;

public class AuthService {
    public static final String DEFAULT_PASSWORD = "123456";
    private static final String SEED_PLACEHOLDER_HASH = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

    private final UserDAO userDAO = new UserDAO();

    public User login(String username, String password) {
        Validator.requireNonBlank(username, "Tên đăng nhập");
        Validator.requireNonBlank(password, "Mật khẩu");

        User user = userDAO.findByUsername(username.trim());
        if (user == null) {
            throw new BusinessException("Tài khoản không tồn tại!");
        }
        if (!user.isActive()) {
            throw new BusinessException("Tài khoản đã bị khóa. Vui lòng liên hệ Quản trị viên (ADMIN)!");
        }

        if (!verifyPassword(user, password)) {
            throw new BusinessException("Mật khẩu không chính xác!");
        }

        SessionManager.setCurrentUser(user);
        return user;
    }

    private boolean verifyPassword(User user, String rawPassword) {
        String storedHash = user.getPasswordHash();
        if (storedHash == null || storedHash.isEmpty()) {
            return false;
        }
        if (SEED_PLACEHOLDER_HASH.equals(storedHash) && DEFAULT_PASSWORD.equals(rawPassword)) {
            String realHash = BCrypt.hashpw(DEFAULT_PASSWORD, BCrypt.gensalt(10));
            userDAO.updatePassword(user.getId(), realHash);
            user.setPasswordHash(realHash);
            return true;
        }
        try {
            if (storedHash.startsWith("$2a$") || storedHash.startsWith("$2b$") || storedHash.startsWith("$2y$")) {
                return BCrypt.checkpw(rawPassword, storedHash);
            }
        } catch (Exception ignored) {
        }
        return storedHash.equals(rawPassword);
    }

    public void changePassword(int userId, String currentPassword, String newPassword, String confirmPassword) {
        Validator.requireNonBlank(currentPassword, "Mật khẩu hiện tại");
        Validator.requireNonBlank(newPassword, "Mật khẩu mới");
        if (newPassword.length() < 6) {
            throw new BusinessException("Mật khẩu mới phải có ít nhất 6 ký tự!");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new BusinessException("Mật khẩu xác nhận không khớp!");
        }

        User user = userDAO.findById(userId);
        if (user == null) {
            throw new BusinessException("Không tìm thấy thông tin tài khoản!");
        }
        if (!verifyPassword(user, currentPassword)) {
            throw new BusinessException("Mật khẩu hiện tại không đúng!");
        }

        String newHash = BCrypt.hashpw(newPassword, BCrypt.gensalt(10));
        userDAO.updatePassword(userId, newHash);
        if (SessionManager.getCurrentUser() != null && SessionManager.getCurrentUser().getId() == userId) {
            SessionManager.getCurrentUser().setPasswordHash(newHash);
        }
    }

    public void resetPasswordToDefault(int userId) {
        String defaultHash = BCrypt.hashpw(DEFAULT_PASSWORD, BCrypt.gensalt(10));
        userDAO.updatePassword(userId, defaultHash);
    }

    public List<User> getAllUsers() {
        return userDAO.findAll();
    }

    public List<User> searchUsers(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return userDAO.findAll();
        }
        return userDAO.search(keyword);
    }

    public void saveUser(User user, String rawPasswordIfNew) {
        Validator.validateCode(user.getCode(), "Mã nhân viên");
        Validator.requireNonBlank(user.getUsername(), "Username");
        Validator.requireNonBlank(user.getFullName(), "Họ tên nhân viên");
        if (user.getPhone() != null && !user.getPhone().isBlank()) {
            Validator.validatePhone(user.getPhone());
        }
        Validator.validateEmailOptional(user.getEmail());

        if (user.getId() == 0) {
            String pwd = (rawPasswordIfNew != null && !rawPasswordIfNew.isBlank()) ? rawPasswordIfNew : DEFAULT_PASSWORD;
            user.setPasswordHash(BCrypt.hashpw(pwd, BCrypt.gensalt(10)));
            userDAO.insert(user);
        } else {
            userDAO.update(user);
        }
    }

    public String deleteOrLockUser(int userId) {
        if (SessionManager.getCurrentUser() != null && SessionManager.getCurrentUser().getId() == userId) {
            throw new BusinessException("Không thể xóa hoặc khóa tài khoản đang đăng nhập!");
        }
        User user = userDAO.findById(userId);
        if (user == null) {
            throw new BusinessException("Không tìm thấy nhân viên!");
        }
        if (userDAO.hasInvoices(userId)) {
            user.setStatus("LOCKED");
            userDAO.update(user);
            return "Nhân viên đã có lịch sử lập hóa đơn nên hệ thống chuyển sang trạng thái ĐÃ KHÓA (LOCKED) để bảo toàn dữ liệu.";
        } else {
            userDAO.deleteById(userId);
            return "Đã xóa nhân viên thành công.";
        }
    }

    public String generateNextEmployeeCode() {
        return userDAO.generateNextCode();
    }
}
