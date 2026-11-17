package qnha.fit.shopmate.service;

import qnha.fit.shopmate.config.DBConnection;
import qnha.fit.shopmate.dao.CategoryDAO;
import qnha.fit.shopmate.dao.ProductDAO;
import qnha.fit.shopmate.dao.StockImportDAO;
import qnha.fit.shopmate.exception.BusinessException;
import qnha.fit.shopmate.exception.DataAccessException;
import qnha.fit.shopmate.model.Category;
import qnha.fit.shopmate.model.Product;
import qnha.fit.shopmate.model.StockImport;
import qnha.fit.shopmate.util.Validator;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class ProductService {
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final StockImportDAO stockImportDAO = new StockImportDAO();

    // ================= CATEGORY =================

    public List<Category> getAllCategories() {
        return categoryDAO.findAll();
    }

    public List<Category> getActiveCategories() {
        return categoryDAO.findAllActive();
    }

    public void saveCategory(Category category) {
        Validator.requireNonBlank(category.getName(), "Tên danh mục");
        if (category.getId() == 0) {
            categoryDAO.insert(category);
        } else {
            categoryDAO.update(category);
        }
    }

    public void deleteCategory(int categoryId) {
        int count = categoryDAO.countProductsByCategoryId(categoryId);
        if (count > 0) {
            throw new BusinessException("Cảnh báo: Danh mục này đang chứa " + count
                    + " sản phẩm! Không thể xóa danh mục đang có sản phẩm. Vui lòng chuyển sản phẩm sang danh mục khác hoặc chuyển trạng thái sang Ngừng hoạt động.");
        }
        categoryDAO.deleteById(categoryId);
    }

    // ================= PRODUCT =================

    public List<Product> getAllProducts() {
        return productDAO.findAll();
    }

    public List<Product> searchProducts(String keyword, Integer categoryId, String sortOption, boolean activeOnly) {
        return productDAO.searchAndFilter(keyword, categoryId, sortOption, activeOnly);
    }

    public Product getProductById(int id) {
        return productDAO.findById(id);
    }

    public void saveProduct(Product p) {
        Validator.validateCode(p.getCode(), "Mã sản phẩm");
        Validator.requireNonBlank(p.getName(), "Tên sản phẩm");
        Validator.requireNonBlank(p.getUnit(), "Đơn vị tính");
        if (p.getCategoryId() <= 0) {
            throw new BusinessException("Vui lòng chọn danh mục sản phẩm!");
        }
        Validator.validateProductPrices(p.getCostPrice(), p.getSellingPrice());
        if (p.getStockQuantity() < 0) {
            throw new BusinessException("Số lượng tồn kho không được nhỏ hơn 0!");
        }

        if (p.getId() == 0) {
            productDAO.insert(p);
        } else {
            productDAO.update(p);
        }
    }

    public String deleteOrDiscontinueProduct(int productId) {
        if (productDAO.hasTransactions(productId)) {
            productDAO.updateStatus(productId, "DISCONTINUED");
            return "Sản phẩm đã phát sinh hóa đơn/nhập kho nên được chuyển sang trạng thái 'Ngừng kinh doanh' (DISCONTINUED) để giữ lịch sử.";
        } else {
            productDAO.deleteById(productId);
            return "Đã xóa sản phẩm khỏi hệ thống thành công.";
        }
    }

    public String generateNextProductCode() {
        return productDAO.generateNextCode();
    }

    // ================= STOCK IMPORT =================

    public void importStock(int userId, int productId, int quantity, BigDecimal importPrice, String note) {
        if (quantity <= 0) {
            throw new BusinessException("Số lượng nhập hàng phải lớn hơn 0!");
        }
        Validator.validatePositivePrice(importPrice, "Giá nhập kho");

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                StockImport si = new StockImport();
                si.setImportCode(stockImportDAO.generateNextCode());
                si.setUserId(userId);
                si.setProductId(productId);
                si.setQuantity(quantity);
                si.setImportPrice(importPrice);
                si.setNote(note);

                stockImportDAO.insertWithConnection(conn, si);
                productDAO.updateStockAndCostWithConnection(conn, productId, quantity, importPrice);

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw new DataAccessException("Lỗi khi nhập kho: " + e.getMessage(), e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi kết nối CSDL khi nhập kho: " + e.getMessage(), e);
        }
    }

    public List<StockImport> getAllStockImports() {
        return stockImportDAO.findAll();
    }
}
