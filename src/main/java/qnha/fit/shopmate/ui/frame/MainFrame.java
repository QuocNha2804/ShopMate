package qnha.fit.shopmate.ui.frame;

import qnha.fit.shopmate.config.AppConfig;
import qnha.fit.shopmate.config.SessionManager;
import qnha.fit.shopmate.model.User;
import qnha.fit.shopmate.ui.component.SidebarButton;
import qnha.fit.shopmate.ui.dialog.ChangePasswordDialog;
import qnha.fit.shopmate.ui.panel.*;
import qnha.fit.shopmate.util.UIUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame {
    private final User currentUser;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);
    private final List<SidebarButton> navButtons = new ArrayList<>();
    private final JLabel lblPageTitle = new JLabel("Tổng quan hoạt động kinh doanh");

    private DashboardPanel dashboardPanel;
    private POSPanel posPanel;
    private ProductPanel productPanel;
    private CategoryPanel categoryPanel;
    private CustomerPanel customerPanel;
    private VoucherPanel voucherPanel;
    private EmployeePanel employeePanel;
    private InvoiceHistoryPanel invoiceHistoryPanel;
    private ReportPanel reportPanel;
    private SettingPanel settingPanel;

    public MainFrame(User user) {
        this.currentUser = user;
        setTitle(AppConfig.getStoreName() + " — ShopMate POS [" + user.getFullName() + " - " + user.getRole() + "]");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1380, 840);
        setMinimumSize(new Dimension(1180, 720));
        setLocationRelativeTo(null);

        initUI();
    }

    private void initUI() {
        // 1. Sidebar trái (Dark Slate hiện đại)
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(new Color(15, 23, 42));
        sidebar.setPreferredSize(new Dimension(245, 0));

        // Brand Header trên Sidebar
        JPanel brandBox = new JPanel(new BorderLayout(10, 4));
        brandBox.setOpaque(false);
        brandBox.setBorder(new EmptyBorder(20, 18, 16, 16));

        JLabel logoIcon = new JLabel("SM", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIUtil.PRIMARY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        logoIcon.setFont(new Font("Segoe UI", Font.BOLD, 16));
        logoIcon.setForeground(Color.WHITE);
        logoIcon.setPreferredSize(new Dimension(40, 40));

        JPanel brandTexts = new JPanel(new GridLayout(2, 1, 0, 2));
        brandTexts.setOpaque(false);
        JLabel lblBrand = new JLabel("SHOPMATE POS");
        lblBrand.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblBrand.setForeground(Color.WHITE);

        JLabel lblRoleBadge = new JLabel(currentUser.getRole() + " • " + currentUser.getCode());
        lblRoleBadge.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblRoleBadge.setForeground(new Color(148, 163, 184));

        brandTexts.add(lblBrand);
        brandTexts.add(lblRoleBadge);

        brandBox.add(logoIcon, BorderLayout.WEST);
        brandBox.add(brandTexts, BorderLayout.CENTER);

        // Menu điều hướng
        JPanel menuBox = new JPanel();
        menuBox.setOpaque(false);
        menuBox.setLayout(new BoxLayout(menuBox, BoxLayout.Y_AXIS));
        menuBox.setBorder(new EmptyBorder(6, 0, 10, 0));

        dashboardPanel = new DashboardPanel();
        posPanel = new POSPanel();
        productPanel = new ProductPanel();
        categoryPanel = new CategoryPanel();
        customerPanel = new CustomerPanel();
        voucherPanel = new VoucherPanel();
        invoiceHistoryPanel = new InvoiceHistoryPanel();
        settingPanel = new SettingPanel();

        contentPanel.add(dashboardPanel, "DASHBOARD");
        contentPanel.add(posPanel, "POS");
        contentPanel.add(productPanel, "PRODUCTS");
        contentPanel.add(categoryPanel, "CATEGORIES");
        contentPanel.add(customerPanel, "CUSTOMERS");
        contentPanel.add(voucherPanel, "VOUCHERS");
        contentPanel.add(invoiceHistoryPanel, "INVOICES");
        contentPanel.add(settingPanel, "SETTINGS");

        if (currentUser.isAdmin()) {
            employeePanel = new EmployeePanel();
            reportPanel = new ReportPanel();
            contentPanel.add(employeePanel, "EMPLOYEES");
            contentPanel.add(reportPanel, "REPORTS");
        }

        addNavButton(menuBox, "📊  Dashboard", "DASHBOARD", "Tổng quan hoạt động kinh doanh", () -> dashboardPanel.refreshData());
        addNavButton(menuBox, "🛒  Bán hàng (POS)", "POS", "Màn hình Bán hàng tại quầy (POS)", () -> posPanel.refreshData());
        addNavButton(menuBox, "📦  Sản phẩm & Kho", "PRODUCTS", "Quản lý Sản phẩm & Nhập kho", () -> productPanel.refreshData());
        addNavButton(menuBox, "🗂️  Danh mục SP", "CATEGORIES", "Quản lý Danh mục sản phẩm", () -> categoryPanel.refreshData());
        addNavButton(menuBox, "👥  Khách hàng", "CUSTOMERS", "Quản lý Khách hàng & Điểm tích lũy", () -> customerPanel.refreshData());
        addNavButton(menuBox, "🎟️  Mã giảm giá", "VOUCHERS", "Quản lý Khuyến mãi & Mã giảm giá", () -> voucherPanel.refreshData());
        addNavButton(menuBox, "🧾  Lịch sử Hóa đơn", "INVOICES", "Tra cứu, In lại & Hủy Hóa đơn", () -> invoiceHistoryPanel.refreshData());

        if (currentUser.isAdmin()) {
            addNavButton(menuBox, "👨‍💼  Nhân viên (ADMIN)", "EMPLOYEES", "Quản lý Nhân viên & Phân quyền", () -> employeePanel.refreshData());
            addNavButton(menuBox, "📈  Thống kê & Báo cáo", "REPORTS", "Thống kê Doanh thu - Lợi nhuận & Xuất Báo cáo", () -> reportPanel.refreshData());
        }
        addNavButton(menuBox, "⚙️  Cài đặt hệ thống", "SETTINGS", "Cài đặt Cửa hàng, Giao diện & Backup DB", () -> settingPanel.refreshData());

        // Khu vực Đổi mật khẩu & Đăng xuất dưới cùng Sidebar
        JPanel bottomAccountBox = new JPanel(new GridLayout(2, 1, 6, 8));
        bottomAccountBox.setOpaque(false);
        bottomAccountBox.setBorder(new EmptyBorder(12, 16, 18, 16));

        JButton btnChangePass = new JButton("🔑 Đổi mật khẩu");
        UIUtil.styleSecondaryButton(btnChangePass);
        JButton btnLogout = new JButton("🚪 Đăng xuất");
        UIUtil.styleDangerButton(btnLogout);

        btnChangePass.addActionListener(e -> new ChangePasswordDialog(this).setVisible(true));
        btnLogout.addActionListener(e -> handleLogout());

        bottomAccountBox.add(btnChangePass);
        bottomAccountBox.add(btnLogout);

        JScrollPane menuScroll = new JScrollPane(menuBox);
        menuScroll.setOpaque(false);
        menuScroll.getViewport().setOpaque(false);
        menuScroll.setBorder(null);

        sidebar.add(brandBox, BorderLayout.NORTH);
        sidebar.add(menuScroll, BorderLayout.CENTER);
        sidebar.add(bottomAccountBox, BorderLayout.SOUTH);

        // 2. Khu vực bên phải (Header + CardLayout Content)
        JPanel rightContainer = new JPanel(new BorderLayout());

        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240, 140)),
                new EmptyBorder(14, 22, 14, 22)
        ));
        lblPageTitle.setFont(new Font("Segoe UI", Font.BOLD, 19));

        JPanel userBadgePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        userBadgePanel.setOpaque(false);

        JLabel lblStoreInfo = new JLabel(AppConfig.getStoreName() + "  •  " + currentUser.getFullName());
        lblStoreInfo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblStoreInfo.setForeground(UIUtil.SLATE_500);

        JLabel lblRolePill = new JLabel(" " + currentUser.getRole() + " ");
        lblRolePill.setOpaque(true);
        lblRolePill.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblRolePill.setForeground(Color.WHITE);
        lblRolePill.setBackground(currentUser.isAdmin() ? UIUtil.PRIMARY : UIUtil.SUCCESS);
        lblRolePill.setBorder(new EmptyBorder(3, 8, 3, 8));

        userBadgePanel.add(lblStoreInfo);
        userBadgePanel.add(lblRolePill);

        topHeader.add(lblPageTitle, BorderLayout.WEST);
        topHeader.add(userBadgePanel, BorderLayout.EAST);

        rightContainer.add(topHeader, BorderLayout.NORTH);
        rightContainer.add(contentPanel, BorderLayout.CENTER);

        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidebar, rightContainer);
        mainSplit.setDividerSize(0);
        mainSplit.setDividerLocation(245);
        mainSplit.setEnabled(false);

        setContentPane(mainSplit);

        if (!navButtons.isEmpty()) {
            navButtons.getFirst().setSelectedState(true);
        }
    }

    private void addNavButton(JPanel container, String text, String cardKey, String pageTitle, Runnable onShow) {
        SidebarButton btn = new SidebarButton(text);
        btn.addActionListener(e -> {
            for (SidebarButton b : navButtons) {
                b.setSelectedState(false);
            }
            btn.setSelectedState(true);
            lblPageTitle.setText(pageTitle);
            if (onShow != null) {
                onShow.run();
            }
            cardLayout.show(contentPanel, cardKey);
        });
        navButtons.add(btn);
        container.add(btn);
        container.add(Box.createVerticalStrut(4));
    }

    private void handleLogout() {
        int cf = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn đăng xuất khỏi hệ thống?",
                "Xác nhận đăng xuất", JOptionPane.YES_NO_OPTION);
        if (cf == JOptionPane.YES_OPTION) {
            SessionManager.logout();
            new LoginFrame(true).setVisible(true);
            dispose();
        }
    }
}
