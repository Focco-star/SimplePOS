package POS;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractCellEditor;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

// App
public class Main {
    // Window
    private final JFrame frame = new JFrame("Simple POS");

    // Model
    private final Cart cart = new Cart();
    private final CartTableModel tableModel = new CartTableModel(cart);

    // Totals
    private final JLabel lblSubtotal = new JLabel("Subtotal");
    private final JLabel lblTax = new JLabel("Tax");
    private final JLabel lblTotal = new JLabel("Total");
    private final JLabel lblSubtotalAmt = new JLabel("₱0.00", SwingConstants.CENTER);
    private final JLabel lblTaxAmt = new JLabel("₱0.00", SwingConstants.CENTER);
    private final JLabel lblTotalAmt = new JLabel("₱0.00", SwingConstants.CENTER);

    // Tax
    private final double TAX_RATE = 0.12; // 12%

    // Catalog
    private final List<Product> catalog = new ArrayList<>();
    // tiles
    private JPanel tileSubtotal;
    private JPanel tileTax;
    private JPanel tileTotal;
    // header phase
    private float headerPhase = 0f;
    // Flash
    private JTable cartTable;
    private int flashRow = -1;
    private float flashPhase = 0f;
    private Timer flashTimer;


    public Main() {
        createSampleCatalog();
        initUI();
    }

    // Samples
    private void createSampleCatalog() {
        catalog.add(new FoodItem("Burger", 5.99));
        catalog.add(new FoodItem("Fries", 2.49));
        catalog.add(new FoodItem("Hotdog", 3.25));
        catalog.add(new DrinkItem("Coke", 1.50));
        catalog.add(new DrinkItem("Coffee", 2.25));
        catalog.add(new DrinkItem("Water", 1.00));
    }

    // UI
    private void initUI() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // root panel
        JPanel root = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                int w = getWidth();
                int h = getHeight();
                // gradient
                g2.setPaint(new GradientPaint(0, 0, new Color(255, 250, 245), w, h, new Color(245, 255, 255)));
                g2.fillRect(0, 0, w, h);
                // watermark
                g2.setFont(g2.getFont().deriveFont(Font.BOLD, 72f));
                g2.setColor(new Color(0, 0, 0, 12));
                FontMetrics fm = g2.getFontMetrics();
                String wm = "POS";
                int sx = (w - fm.stringWidth(wm)) / 2;
                int sy = h / 2 + fm.getAscent() / 2 - 30;
                g2.drawString(wm, sx, sy);
                // sketch
                int crw = 120, crh = 64;
                int cx = w - crw - 24;
                int cy = h - crh - 24;
                g2.setColor(new Color(0,0,0,20));
                g2.fillRoundRect(cx, cy, crw, crh, 8, 8);
                g2.setColor(new Color(255,255,255,40));
                g2.fillRect(cx + 10, cy + 10, crw - 20, 28);
            }
        };
        frame.setContentPane(root);

        // Products
        JPanel productPanel = new JPanel(new GridLayout(0, 3, 12, 12)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                int w = getWidth();
                int h = getHeight();
                Color c1 = new Color(255, 245, 238);
                Color c2 = new Color(255, 230, 240);
                g2.setPaint(new GradientPaint(0, 0, c1, w, h, c2));
                g2.fillRect(0, 0, w, h);
            }
        };
        productPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        for (Product p : catalog) {
            JButton b = createProductButton(p);
            productPanel.add(b);
        }

        JPanel left = new JPanel(new BorderLayout());
        left.setOpaque(false);
        JPanel productsTitlePanel = createTitlePanel("Products", new Color(255, 200, 160));
        left.add(productsTitlePanel, BorderLayout.NORTH);
        left.add(productPanel, BorderLayout.CENTER);

        // Cart table
        cartTable = new JTable(tableModel);
        JScrollPane tablePane = new JScrollPane(cartTable);
        JPanel right = new JPanel(new BorderLayout());
        right.setOpaque(false);
        JPanel cartTitlePanel = createTitlePanel("Cart", new Color(180, 220, 255));
        right.add(cartTitlePanel, BorderLayout.NORTH);
        // Table
        cartTable.setRowHeight(52);
        // Grid
        cartTable.setShowGrid(true);
        cartTable.setGridColor(new Color(220, 220, 220));
        cartTable.setIntercellSpacing(new Dimension(1, 1));
        cartTable.setSelectionBackground(new Color(190, 220, 255));
        cartTable.setSelectionForeground(Color.black);
        cartTable.getTableHeader().setReorderingAllowed(false);
        cartTable.getTableHeader().setPreferredSize(new Dimension(0, 34));
        cartTable.setDefaultRenderer(Object.class, new RowRenderer());
        cartTable.getColumnModel().getColumn(0).setCellRenderer(new CartCellRenderer());
        // widths
        cartTable.getColumnModel().getColumn(0).setPreferredWidth(180);
        cartTable.getColumnModel().getColumn(1).setPreferredWidth(40);
        cartTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        cartTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        // Action column
        if (cartTable.getColumnCount() > 4) {
            cartTable.getColumnModel().getColumn(4).setCellRenderer(new ActionButtonRenderer());
            cartTable.getColumnModel().getColumn(4).setCellEditor(new ActionButtonEditor(cartTable));
            cartTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        }
        right.add(tablePane, BorderLayout.CENTER);

        // Header
        cartTable.getTableHeader().setDefaultRenderer(new HeaderRenderer());
        Timer headerTimer = new Timer(80, ev -> {
            headerPhase += 0.03f;
            if (headerPhase > 1f) headerPhase = 0f;
            cartTable.getTableHeader().repaint();
        });
        headerTimer.start();

        // Totals
        JPanel totals = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));

        tileSubtotal = new JPanel(new BorderLayout());
        tileSubtotal.setBackground(new Color(255, 250, 240));
        tileSubtotal.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(240, 200, 150), 1, true), BorderFactory.createEmptyBorder(8,12,8,12)));
        lblSubtotal.setFont(lblSubtotal.getFont().deriveFont(Font.PLAIN, 12f));
        lblSubtotalAmt.setFont(lblSubtotalAmt.getFont().deriveFont(Font.BOLD, 16f));
        tileSubtotal.add(lblSubtotal, BorderLayout.NORTH);
        tileSubtotal.add(lblSubtotalAmt, BorderLayout.CENTER);

        tileTax = new JPanel(new BorderLayout());
        tileTax.setBackground(new Color(245, 250, 255));
        tileTax.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(160, 190, 240), 1, true), BorderFactory.createEmptyBorder(8,12,8,12)));
        lblTax.setFont(lblTax.getFont().deriveFont(Font.PLAIN, 12f));
        lblTaxAmt.setFont(lblTaxAmt.getFont().deriveFont(Font.PLAIN, 14f));
        tileTax.add(lblTax, BorderLayout.NORTH);
        tileTax.add(lblTaxAmt, BorderLayout.CENTER);

        tileTotal = new JPanel(new BorderLayout());
        tileTotal.setBackground(new Color(240, 255, 245));
        tileTotal.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(160, 220, 180), 1, true), BorderFactory.createEmptyBorder(8,12,8,12)));
        lblTotal.setFont(lblTotal.getFont().deriveFont(Font.PLAIN, 12f));
        lblTotalAmt.setFont(lblTotalAmt.getFont().deriveFont(Font.BOLD, 18f));
        tileTotal.add(lblTotal, BorderLayout.NORTH);
        tileTotal.add(lblTotalAmt, BorderLayout.CENTER);

        totals.add(tileSubtotal);
        totals.add(tileTax);
        totals.add(tileTotal);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        StyledButton btnCheckout = new StyledButton("Checkout", new Color(80, 160, 220), new Color(40,120,200));
        StyledButton btnClear = new StyledButton("Clear Cart", new Color(220, 80, 80), new Color(200, 40, 40));

        btnCheckout.addActionListener(e -> {
            // Checkout
            CheckoutDialog dlg = new CheckoutDialog(frame, cart, TAX_RATE);
            dlg.setVisible(true);
        });

        btnClear.addActionListener(e -> {
            cart.clear();
            refreshCartView();
        });

        controls.add(btnClear);
        controls.add(btnCheckout);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(totals, BorderLayout.WEST);
        bottom.add(controls, BorderLayout.EAST);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        split.setResizeWeight(0.5);
        frame.add(split, BorderLayout.CENTER);
        frame.add(bottom, BorderLayout.SOUTH);

        frame.setSize(800, 500);
        frame.setLocationRelativeTo(null);
    }

    // product button
    private JButton createProductButton(Product p) {
        int w = 140, h = 110;
        BufferedImage img = drawProductImage(p.getName(), w, h, 0f);
        JButton b = new JButton(new ImageIcon(img));
        b.setPreferredSize(new Dimension(w + 20, h + 40));
        b.setToolTipText(p.getName() + " — ₱" + String.format("%.2f", p.getPrice()));
        b.setFocusPainted(false);
        b.setContentAreaFilled(true);
        b.setBackground(new Color(250, 250, 250));
        b.setBorder(new LineBorder(new Color(220, 220, 220), 2, true));

        b.addActionListener(e -> {
            cart.addProduct(p);
            refreshCartView();
            // flash row
            int idx = -1;
            for (int i = 0; i < cart.getItems().size(); i++) {
                if (cart.getItems().get(i).getProduct().getName().equals(p.getName())) {
                    idx = i;
                    break;
                }
            }
            if (idx >= 0) {
                flashRow = idx;
                flashPhase = 0f;
                if (flashTimer == null) {
                    flashTimer = new Timer(80, ev2 -> {
                        flashPhase += 0.16f;
                        cartTable.repaint();
                        if (flashPhase > 1f) {
                            ((Timer) ev2.getSource()).stop();
                            flashRow = -1;
                            flashPhase = 0f;
                            flashTimer = null;
                        }
                    });
                    flashTimer.start();
                } else {
                    flashTimer.restart();
                }
            }
        });

        // hover
        JLabel nameLabel = new JLabel(p.getName() + "  ₱" + String.format("%.2f", p.getPrice()), SwingConstants.CENTER);
        nameLabel.setFont(nameLabel.getFont().deriveFont(12f));
        nameLabel.setForeground(new Color(70, 70, 70));
        nameLabel.setOpaque(false);

        b.setLayout(new BorderLayout());
        b.add(nameLabel, BorderLayout.SOUTH);



        return b;
    }

    // draw image
    private BufferedImage drawProductImage(String name, int w, int h, float phase) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // bg
        g.setColor(new Color(255, 255, 255, 0));
        g.fillRect(0, 0, w, h);

        String lower = name.toLowerCase();
        if (lower.contains("burger")) {
            // bun top
            g.setColor(new Color(205, 133, 63));
            g.fillRoundRect(12, 10, w - 24, 42, 30, 30);
            // lettuce
            g.setColor(new Color(124, 198, 80));
            g.fillRoundRect(18, 34, w - 36, 12, 12, 12);
            // patty
            g.setColor(new Color(120, 60, 40));
            g.fillRoundRect(18, 44, w - 36, 14, 8, 8);
            // bun bottom
            g.setColor(new Color(222, 163, 92));
            g.fillRoundRect(12, 56, w - 24, 24, 20, 20);
        } else if (lower.contains("fries")) {
            g.setColor(new Color(200, 30, 45));
            g.fillRoundRect(w/4 - 6, 28, w/2 + 12, 48, 10, 10);
            g.setColor(new Color(255, 220, 120));
            int start = w/4 + 4;
            for (int i = 0; i < 5; i++) {
                int x = start + i*10 - (int)(phase*4);
                g.fillRoundRect(x, 22 - i%2, 6, 34 + i%3, 4, 4);
            }
        } else if (lower.contains("hotdog")) {
            g.setColor(new Color(222, 163, 92));
            g.fillRoundRect(10, 36, w - 20, 22, 20, 20);
            g.setColor(new Color(180, 40, 40));
            g.fillRoundRect(14, 40, w - 28, 14, 12, 12);
            g.setColor(new Color(255, 200, 50));
            for (int i = 0; i < 5; i++) {
                g.fillOval(18 + i*12, 34 - (i%2==0?2:0), 8, 8);
            }
        } else if (lower.contains("coke") || lower.contains("water")) {
            // cup
            g.setColor(new Color(230, 240, 255));
            g.fillRoundRect(w/2 - 18, 18, 36, 56, 10, 10);
            g.setColor(new Color(190, 200, 220));
            g.drawRoundRect(w/2 - 18, 18, 36, 56, 10, 10);
            // liquid
            if (lower.contains("coke")) g.setColor(new Color(80, 40, 20, 200)); else g.setColor(new Color(100, 170, 255, 200));
            int liquidH = 30 + (int)(phase*8);
            g.fillRoundRect(w/2 - 16, 40 - liquidH/6, 32, liquidH, 8, 8);
            // straw
            g.setColor(new Color(255, 60, 90));
            g.fillRect(w/2 + 6, 8, 6, 28);
        } else if (lower.contains("coffee")) {
            // cup
            g.setColor(new Color(240, 230, 210));
            g.fillRoundRect(w/2 - 22, 32, 44, 36, 10, 10);
            g.setColor(new Color(150, 90, 60));
            g.fillOval(w/2 - 18, 28, 36, 18);
            // steam
            g.setColor(new Color(200,200,200, (int)(150 + phase*100)));
            int sx = w/2 - 6;
            for (int i = 0; i < 3; i++) {
                int y = 18 - (int)(phase * 18) - i*6;
                g.drawArc(sx - i*6, y, 20 + i*6, 20, 0, 180);
            }
        } else {
            // generic plate
            g.setColor(new Color(220, 240, 220));
            g.fillOval(10, 28, w - 20, 44);
            g.setColor(new Color(180, 120, 80));
            g.fillOval(w/2 - 12, 36, 24, 24);
        }

        g.dispose();
        return img;
    }

    // cart renderer
    private class CartCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String name = value != null ? value.toString() : "";
            BufferedImage ic = drawProductImage(name, 48, 48, 0f);
            lbl.setIcon(new ImageIcon(ic));
            lbl.setText(" " + name);
            lbl.setHorizontalTextPosition(SwingConstants.RIGHT);
            lbl.setVerticalTextPosition(SwingConstants.CENTER);
            lbl.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
            return lbl;
        }
    }

    // action renderer
    private class ActionButtonRenderer extends JButton implements TableCellRenderer {
        public ActionButtonRenderer() {
            setOpaque(true);
            setForeground(Color.white);
            setText("−");
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setBackground(new Color(200, 100, 60));
            setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
            return this;
        }
    }

    // action editor
    private class ActionButtonEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {
        private final JButton button = new JButton("−");
        private final JTable tableRef;
        private int currentRow = -1;

        public ActionButtonEditor(JTable table) {
            this.tableRef = table;
            button.setForeground(Color.white);
            button.setBackground(new Color(200, 60, 60));
            button.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
            button.addActionListener(this);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            currentRow = row;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return "Remove";
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int modelRow = tableRef.convertRowIndexToModel(currentRow);
            if (modelRow >= 0 && modelRow < cart.getItems().size()) {
                // decrement
                CartItem ci = cart.getItems().get(modelRow);
                int q = ci.getQuantity();
                if (q > 1) {
                    ci.setQuantity(q - 1);
                } else {
                    cart.removeProduct(ci.getProduct());
                }
                refreshCartView();
            }
            fireEditingStopped();
        }
    }

    // button
    private class StyledButton extends JButton {
        private final Color base;
        private final Color accent;
        private boolean hover = false;

        public StyledButton(String text, Color base, Color accent) {
            super(text);
            this.base = base;
            this.accent = accent;
            setForeground(Color.white);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            Color c1 = hover ? accent : base;
            Color c2 = c1.darker();
            g2.setPaint(new GradientPaint(0, 0, c1, 0, h, c2));
            g2.fillRoundRect(0, 0, w, h, 14, 14);
            FontMetrics fm = g2.getFontMetrics();
            int strW = fm.stringWidth(getText());
            int strH = fm.getAscent();
            g2.setColor(Color.white);
            g2.drawString(getText(), (w - strW) / 2, (h + strH) / 2 - 2);
            g2.dispose();
        }
    }

    // title panel
    private JPanel createTitlePanel(String text, Color accent) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 18f));
        lbl.setForeground(new Color(40, 40, 40));
        lbl.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        // rounded bg
        JPanel bg = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                g2.setPaint(new GradientPaint(0, 0, accent, w, 0, accent.darker()));
                g2.fillRoundRect(0, 0, w, h, 12, 12);
                super.paintComponent(g);
            }
        };
        bg.setOpaque(false);
        bg.add(lbl, BorderLayout.CENTER);
        bg.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        p.add(bg, BorderLayout.CENTER);
        p.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        return p;
    }

    // pulse
    private void pulsePanel(JPanel panel, Color highlight) {
        Color original = panel.getBackground();
        int steps = 8;
        final int[] s = {0};
        Timer t = new Timer(30, ev -> {
            float f = (float) s[0] / (float) steps;
            int r = (int) (original.getRed() * (1 - f) + highlight.getRed() * f);
            int g = (int) (original.getGreen() * (1 - f) + highlight.getGreen() * f);
            int b = (int) (original.getBlue() * (1 - f) + highlight.getBlue() * f);
            panel.setBackground(new Color(r, g, b));
            s[0]++;
            if (s[0] > steps) {
                ((Timer) ev.getSource()).stop();
                panel.setBackground(original);
            }
        });
        t.start();
    }

    // header renderer
    private class HeaderRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            lbl.setOpaque(true);
            lbl.setForeground(Color.white);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 13f));
            // animated color
            Color cA = new Color(100, 140, 200);
            Color cB = new Color(140, 180, 220);
            float p = headerPhase;
            int r = (int) (cA.getRed() * (1 - p) + cB.getRed() * p);
            int g = (int) (cA.getGreen() * (1 - p) + cB.getGreen() * p);
            int b = (int) (cA.getBlue() * (1 - p) + cB.getBlue() * p);
            lbl.setBackground(new Color(r, g, b));
            lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, new Color(80, 110, 150)));
            return lbl;
        }
    }

    // row renderer
    private class RowRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            // alternating rows
            Color even = new Color(250, 250, 255);
            Color odd = new Color(245, 245, 245);
            int modelRow = table.convertRowIndexToModel(row);
            Color bg = (modelRow % 2 == 0) ? even : odd;
            // flash highlight
            if (modelRow == flashRow) {
                float p = (float) Math.sin(flashPhase * Math.PI);
                bg = blend(bg, new Color(200, 255, 200), p);
            }
            lbl.setBackground(bg);
            // numeric align
            if (column == 1 || column == 2 || column == 3) {
                lbl.setHorizontalAlignment(SwingConstants.RIGHT);
                lbl.setForeground(new Color(40, 40, 40));
            } else {
                lbl.setHorizontalAlignment(SwingConstants.LEFT);
                lbl.setForeground(new Color(30, 30, 30));
            }
            // separator
            lbl.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0,0,1,0,new Color(230,230,230)), BorderFactory.createEmptyBorder(6, 8, 6, 8)));
            return lbl;
        }
    }

    // blend colors
    private Color blend(Color a, Color b, float p) {
        p = Math.max(0f, Math.min(1f, p));
        int r = (int) (a.getRed() * (1 - p) + b.getRed() * p);
        int g = (int) (a.getGreen() * (1 - p) + b.getGreen() * p);
        int bl = (int) (a.getBlue() * (1 - p) + b.getBlue() * p);
        return new Color(r, g, bl);
    }

    // refresh
    private void refreshCartView() {
        tableModel.fireTableDataChanged();
        double subtotal = cart.getSubtotal();
        double tax = subtotal * TAX_RATE;
        double total = subtotal + tax;
        lblSubtotalAmt.setText(String.format("₱%.2f", subtotal));
        lblTaxAmt.setText(String.format("₱%.2f", tax));
        lblTotalAmt.setText(String.format("₱%.2f", total));
        // pulse total
        if (tileTotal != null) {
            pulsePanel(tileTotal, new Color(200, 255, 200));
        }
    }

    // show
    public void show() {
        SwingUtilities.invokeLater(() -> frame.setVisible(true));
    }

    public static void main(String[] args) {
        new Main().show();
    }
}
