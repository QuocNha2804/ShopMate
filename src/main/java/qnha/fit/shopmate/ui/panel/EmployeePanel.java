package qnha.fit.shopmate.ui.panel;

import qnha.fit.shopmate.dto.EmployeeRevenueDTO;
import qnha.fit.shopmate.model.User;
import qnha.fit.shopmate.service.AuthService;
import qnha.fit.shopmate.service.ReportService;
import qnha.fit.shopmate.ui.dialog.EmployeeDialog;
import qnha.fit.shopmate.util.FormatUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class EmployeePanel extends JPanel {
    private final AuthService authService = new AuthService();
    private final ReportService reportService = new ReportService();

    private final JTextField txtSearch = new JTextField(20);
    private final DefaultTableModel empTableModel;
    private final JTable tblEmployees;
    private final DefaultTableModel revTableModel;
    private List<User> employees;

    public EmployeePanel() {
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(16, 18, 16, 18));

        // Toolbar
        JPanel toolbar = new JPanel(new BorderLayout(10, 6));
        JPanel searchBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchBox.add(new JLabel("Tìm nhân viên:"));
        searchBox.add(txtSearch);
        JButton btnSearch = new JButton("Tìm kiếm");
        searchBox.add(btnSearch);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton btnAdd = new JButton("+ Thêm Nhân viên");
        btnAdd.setBackground(new Color(37, 99, 235));
        btnAdd.setForeground(Color.WHITE);
        JButton btnEdit = new JButton("Sửa / Phân quyền");
        JButton btnResetPass = new JButton("Reset Mật khẩu (123456)");
        btnResetPass.setForeground(new Color(217, 119, 6));
        JButton btnDeleteOrLock = new JButton("Xóa / Khóa TK");
        btnDeleteOrLock.setForeground(new Color(220, 38, 38));

        actions.add(btnAdd);
        actions.add(btnEdit);
        actions.add(btnResetPass);
        actions.add(btnDeleteOrLock);

        toolbar.add(searchBox, BorderLayout.WEST);
        toolbar.add(actions, BorderLayout.EAST);

        // Bảng danh sách nhân viên
        String[] empCols = {"ID", "Mã NV", "Username", "Họ và tên", "Số điện thoại", "Email", "Vai trò (Role)", "Trạng thái", "Ngày tạo"};
        empTableModel = new DefaultTableModel(empCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblEmployees = new JTable(empTableModel);
        tblEmployees.setRowHeight(28);
        JScrollPane empScroll = new JScrollPane(tblEmployees);
        empScroll.setBorder(BorderFactory.createTitledBorder("Danh sách Nhân viên hệ thống"));

        // Bảng doanh thu theo nhân viên
        String[] revCols = {"Mã NV", "Họ và tên nhân viên", "Vai trò", "Tổng số hóa đơn đã lập", "Tổng doanh thu mang về"};
        revTableModel = new DefaultTableModel(revCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tblRev = new JTable(revTableModel);
        tblRev.setRowHeight(28);
        JScrollPane revScroll = new JScrollPane(tblRev);
        revScroll.setBorder(BorderFactory.createTitledBorder("Thống kê Doanh thu theo Nhân viên"));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, empScroll, revScroll);
        splitPane.setResizeWeight(0.6);

        btnSearch.addActionListener(e -> refreshData());
        txtSearch.addActionListener(e -> refreshData());
        btnAdd.addActionListener(e -> openDialog(null));
        btnEdit.addActionListener(e -> {
            User sel = getSelectedUser();
            if (sel == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên cần sửa!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            openDialog(sel);
        });
        btnResetPass.addActionListener(e -> handleResetPassword());
        btnDeleteOrLock.addActionListener(e -> handleDeleteOrLock());

        add(toolbar, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);

        refreshData();
    }

    public void refreshData() {
        employees = authService.searchUsers(txtSearch.getText().trim());
        empTableModel.setRowCount(0);
        for (User u : employees) {
            empTableModel.addRow(new Object[]{
                    u.getId(),
                    u.getCode(),
                    u.getUsername(),
                    u.getFullName(),
                    u.getPhone(),
                    u.getEmail(),
                    u.getRole(),
                    u.isActive() ? "ACTIVE (Hoạt động)" : "LOCKED (Đã khóa)",
                    FormatUtil.formatDateTime(u.getCreatedAt())
            });
        }

        revTableModel.setRowCount(0);
        List<EmployeeRevenueDTO> revList = reportService.getEmployeeRevenueReport(null, null);
        for (EmployeeRevenueDTO dto : revList) {
            revTableModel.addRow(new Object[]{
                    dto.getEmployeeCode(),
                    dto.getFullName(),
                    dto.getRole(),
                    dto.getOrderCount(),
                    FormatUtil.formatCurrency(dto.getTotalRevenue())
            });
        }
    }

    private User getSelectedUser() {
        int row = tblEmployees.getSelectedRow();
        if (row < 0 || employees == null || row >= employees.size()) return null;
        return employees.get(row);
    }

    private void openDialog(User user) {
        EmployeeDialog dlg = new EmployeeDialog((Frame) SwingUtilities.getWindowAncestor(this), authService, user);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            refreshData();
        }
    }

    private void handleResetPassword() {
        User sel = getSelectedUser();
        if (sel == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên cần reset mật khẩu!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int cf = JOptionPane.showConfirmDialog(this,
                "Reset mật khẩu của nhân viên '" + sel.getFullName() + "' về mặc định (" + AuthService.DEFAULT_PASSWORD + ")?",
                "Xác nhận Reset mật khẩu", JOptionPane.YES_NO_OPTION);
        if (cf == JOptionPane.YES_OPTION) {
            authService.resetPasswordToDefault(sel.getId());
            JOptionPane.showMessageDialog(this,
                    "Đã đặt lại mật khẩu của " + sel.getUsername() + " về: " + AuthService.DEFAULT_PASSWORD,
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void handleDeleteOrLock() {
        User sel = getSelectedUser();
        if (sel == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int cf = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa hoặc khóa tài khoản nhân viên '" + sel.getFullName() + "'?",
                "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (cf == JOptionPane.YES_OPTION) {
            try {
                String msg = authService.deleteOrLockUser(sel.getId());
                refreshData();
                JOptionPane.showMessageDialog(this, msg, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
