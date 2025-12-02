package POS;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

// Checkout
public class CheckoutDialog extends JDialog {
    private final Cart cart;
    private final double taxRate;

    public CheckoutDialog(Frame owner, Cart cart, double taxRate) {
        super(owner, "Checkout", true);
        this.cart = cart;
        this.taxRate = taxRate;
        initUI();
        setLocationRelativeTo(owner);
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // header
        JPanel header = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                int w = getWidth();
                int h = getHeight();
                g2.setPaint(new GradientPaint(0, 0, new Color(90, 140, 200), w, 0, new Color(140, 190, 240)));
                g2.fillRect(0, 0, w, h);
            }
        };
        header.setPreferredSize(new Dimension(0, 64));
        header.setLayout(new BorderLayout());
        JLabel title = new JLabel("Checkout", SwingConstants.CENTER);
        title.setForeground(Color.white);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        header.add(title, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        // center
        JPanel center = new JPanel(new BorderLayout());
        center.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));

        // items list
        JPanel itemsPanel = new JPanel();
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
        JScrollPane scroll = new JScrollPane(itemsPanel);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(220,220,220)));
        center.add(scroll, BorderLayout.CENTER);

        // build data
        java.util.List<CartItem> items = cart.getItems();
        double subtotal = cart.getSubtotal();
        double tax = subtotal * taxRate;
        double total = subtotal + tax;

        // Build items
        java.util.List<JLabel> iconLabels = new java.util.ArrayList<>();
        java.util.List<String> iconNames = new java.util.ArrayList<>();
        if (items.isEmpty()) {
            // Empty view
            JPanel emptyPanel = new JPanel(new BorderLayout());
            emptyPanel.setBorder(BorderFactory.createEmptyBorder(24,24,24,24));
            emptyPanel.setBackground(new Color(250,250,255));
            JLabel cartIcon = new JLabel("🛒", SwingConstants.CENTER);
            cartIcon.setFont(cartIcon.getFont().deriveFont(Font.BOLD, 56f));
            cartIcon.setForeground(new Color(80,140,200));
            JLabel msg = new JLabel("Your cart is empty", SwingConstants.CENTER);
            msg.setFont(msg.getFont().deriveFont(Font.PLAIN, 14f));
            msg.setForeground(new Color(90,90,100));
            emptyPanel.add(cartIcon, BorderLayout.CENTER);
            emptyPanel.add(msg, BorderLayout.SOUTH);
            center.add(emptyPanel, BorderLayout.CENTER);
        } else {
            // Add rows
            for (int i = 0; i < items.size(); i++) {
                CartItem ci = items.get(i);
                JPanel row = createItemRow(ci, i);
                itemsPanel.add(row);
                // register icon
                Component[] comps = row.getComponents();
                for (Component c : comps) {
                    if (c instanceof JPanel) {
                        for (Component inner : ((JPanel) c).getComponents()) {
                            if (inner instanceof JLabel && ((JLabel) inner).getIcon() != null) {
                                iconLabels.add((JLabel) inner);
                                iconNames.add(ci.getProduct().getName());
                                break;
                            }
                        }
                        break;
                    }
                }
            }
            itemsPanel.revalidate();
            itemsPanel.repaint();
        }

        // totals
        JPanel totals = new JPanel(new GridLayout(1, 3, 8, 8));
        totals.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JLabel subL = new JLabel("Subtotal", SwingConstants.CENTER);
        JLabel taxL = new JLabel("Tax", SwingConstants.CENTER);
        JLabel totL = new JLabel("Total", SwingConstants.CENTER);
        JLabel subV = new JLabel(String.format("₱%.2f", subtotal), SwingConstants.CENTER);
        JLabel taxV = new JLabel(String.format("₱%.2f", tax), SwingConstants.CENTER);
        JLabel totV = new JLabel(String.format("₱%.2f", total), SwingConstants.CENTER);
        subL.setFont(subL.getFont().deriveFont(Font.PLAIN, 12f));
        taxL.setFont(taxL.getFont().deriveFont(Font.PLAIN, 12f));
        totL.setFont(totL.getFont().deriveFont(Font.PLAIN, 12f));
        subV.setFont(subV.getFont().deriveFont(Font.BOLD, 14f));
        taxV.setFont(taxV.getFont().deriveFont(Font.PLAIN, 13f));
        totV.setFont(totV.getFont().deriveFont(Font.BOLD, 16f));
        JPanel s = new JPanel(new BorderLayout()); s.add(subL, BorderLayout.NORTH); s.add(subV, BorderLayout.CENTER); s.setBackground(new Color(255,250,240));
        JPanel t = new JPanel(new BorderLayout()); t.add(taxL, BorderLayout.NORTH); t.add(taxV, BorderLayout.CENTER); t.setBackground(new Color(245,250,255));
        JPanel to = new JPanel(new BorderLayout()); to.add(totL, BorderLayout.NORTH); to.add(totV, BorderLayout.CENTER); to.setBackground(new Color(240,255,245));
        totals.add(s); totals.add(t); totals.add(to);
        center.add(totals, BorderLayout.SOUTH);

        add(center, BorderLayout.CENTER);

        // south
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton confirm = new JButton("Confirm");
        JButton close = new JButton("Close");
        confirm.setBackground(new Color(80,160,220)); confirm.setForeground(Color.white);
        close.setBackground(new Color(200,80,80)); close.setForeground(Color.white);
        // No focus
        confirm.setFocusPainted(false);
        confirm.setOpaque(true);
        close.setFocusPainted(false);
        close.setBorderPainted(false);
        close.setOpaque(true);

        south.add(close);
        south.add(confirm);
        add(south, BorderLayout.SOUTH);

        // confirm action
        confirm.addActionListener(new ActionListener() {
            private Timer t;
            private int dots = 0;
            @Override
            public void actionPerformed(ActionEvent e) {
                // empty guard
                if (cart.getItems().isEmpty()) {
                    showStyledInfo("Empty Cart", "You did not select any product to buy");
                    return;
                }
                confirm.setEnabled(false);
                close.setEnabled(false);
                // processing label
                JLabel proc = new JLabel("Processing", SwingConstants.CENTER);
                proc.setFont(proc.getFont().deriveFont(Font.BOLD, 14f));
                center.removeAll();
                center.add(proc, BorderLayout.CENTER);
                center.revalidate(); center.repaint();

                t = new Timer(400, ev -> {
                    dots = (dots + 1) % 4;
                    String dotsStr = new String(new char[dots]).replace('\0', '.');
                    proc.setText("Processing" + dotsStr);
                });
                t.start();

                // finish
                new Timer(2000, ev2 -> {
                    t.stop();
                    // success
                    center.removeAll();
                    JPanel okp = new JPanel(new BorderLayout());
                    okp.setBackground(new Color(240, 255, 245));
                    JLabel check = new JLabel("✔", SwingConstants.CENTER);
                    check.setFont(check.getFont().deriveFont(Font.BOLD, 48f));
                    check.setForeground(new Color(40,160,60));
                    JLabel msg = new JLabel("Payment complete", SwingConstants.CENTER);
                    msg.setFont(msg.getFont().deriveFont(Font.BOLD, 16f));
                    okp.add(check, BorderLayout.CENTER);
                    okp.add(msg, BorderLayout.SOUTH);
                    center.add(okp, BorderLayout.CENTER);
                    center.revalidate(); center.repaint();

                    // done
                    close.setText("Done");
                    close.setEnabled(true);
                    ((Timer) ev2.getSource()).stop();
                }).start();
            }
        });

        close.addActionListener(e -> {
            dispose();
        });

        setSize(420, 420);
    }

    // row
    private JPanel createItemRow(CartItem ci, int idx) {
        String name = ci.getProduct().getName();
        int qty = ci.getQuantity();
        double total = ci.getTotalPrice();

        Color even = new Color(250, 250, 255);
        Color odd = new Color(245, 245, 245);
        JPanel row = new JPanel(new BorderLayout());
        row.setBorder(BorderFactory.createEmptyBorder(6,8,6,8));
        row.setBackground((idx % 2 == 0) ? even : odd);

        // icon
        JLabel icon = new JLabel(new ImageIcon(drawReceiptImage(name, 34, 34, 0f)));

        // name
        JLabel nameLbl = new JLabel(name);
        nameLbl.setFont(nameLbl.getFont().deriveFont(Font.PLAIN, 13f));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        left.setOpaque(false);
        left.add(icon);
        left.add(nameLbl);

        // qty
        JLabel qtyLbl = new JLabel("x" + qty, SwingConstants.CENTER);
        qtyLbl.setFont(qtyLbl.getFont().deriveFont(Font.PLAIN, 13f));

        // price
        JLabel priceLbl = new JLabel(String.format("₱%.2f", total), SwingConstants.RIGHT);
        priceLbl.setFont(priceLbl.getFont().deriveFont(Font.BOLD, 13f));

        row.add(left, BorderLayout.WEST);
        row.add(qtyLbl, BorderLayout.CENTER);
        row.add(priceLbl, BorderLayout.EAST);

        return row;
    }

    // icon
    // draw
    private BufferedImage drawReceiptImage(String name, int w, int h, float phase) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(0,0,0,0));
        g.fillRect(0,0,w,h);
        String lower = name.toLowerCase();
        if (lower.contains("burger")) {
            g.setColor(new Color(205,133,63));
            g.fillRoundRect(4, 4, w-8, h/2, 8, 8);
            g.setColor(new Color(124,198,80));
            g.fillRect(6, h/2 - 2, w-12, 4);
        } else if (lower.contains("fries")) {
            g.setColor(new Color(200,30,45));
            int start = 6 + (int)(phase*2);
            for (int i=0;i<4;i++) g.fillRect(start + i*6, 6 + (i%2), 4, h-12 - (i%3));
        } else if (lower.contains("hotdog")) {
            g.setColor(new Color(222,163,92));
            g.fillRoundRect(4, h/2 - 6, w-8, 12, 8, 8);
            g.setColor(new Color(180,40,40));
            g.fillRect(6, h/2 - 4, w-12, 6);
        } else if (lower.contains("coke") || lower.contains("water")) {
            g.setColor(new Color(230,240,255));
            g.fillRoundRect(w/2 - 10, 4, 20, h-8, 6,6);
            g.setColor(lower.contains("coke") ? new Color(80,40,20,200) : new Color(100,170,255,200));
            int lh = 6 + (int)(phase*6);
            g.fillRoundRect(w/2 - 8, h - lh - 6, 16, lh, 6,6);
        } else if (lower.contains("coffee")) {
            g.setColor(new Color(240,230,210));
            g.fillRoundRect(6, h/2 - 6, w-12, 12, 6,6);
            g.setColor(new Color(150,90,60));
            g.fillOval(w/2 - 8, h/2 - 10 - (int)(phase*2), 16, 8);
        } else {
            g.setColor(new Color(200,220,200));
            g.fillOval(4, 6, w-8, h-12);
        }
        g.dispose();
        return img;
    }

    // Info
    private void showStyledInfo(String title, String message) {
        JDialog d = new JDialog(this, title, true);
        d.setLayout(new BorderLayout());

        // small header
        JPanel head = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                int w = getWidth();
                g2.setPaint(new GradientPaint(0, 0, new Color(100,130,170), w, 0, new Color(150,190,220)));
                g2.fillRect(0, 0, w, getHeight());
            }
        };
        head.setPreferredSize(new Dimension(0, 36));
        head.setLayout(new BorderLayout());
        JLabel t = new JLabel(title, SwingConstants.CENTER);
        t.setForeground(Color.white);
        t.setFont(t.getFont().deriveFont(Font.BOLD, 14f));
        head.add(t, BorderLayout.CENTER);
        d.add(head, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout());
        body.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        body.setBackground(new Color(250,250,252));

        JLabel icon = new JLabel(new ImageIcon(drawReceiptImage("info", 48, 48, 0f)));
        icon.setBorder(BorderFactory.createEmptyBorder(6,6,6,6));
        body.add(icon, BorderLayout.WEST);

        JLabel lab = new JLabel(message);
        lab.setFont(lab.getFont().deriveFont(Font.BOLD, 13f));
        lab.setForeground(new Color(60,60,70));
        body.add(lab, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        JButton ok = new JButton("OK");
        ok.setBackground(new Color(80,160,220)); ok.setForeground(Color.white);
        ok.setFocusPainted(false); ok.setOpaque(true);
        ok.addActionListener(ev -> d.dispose());
        actions.add(ok);
        body.add(actions, BorderLayout.SOUTH);

        d.add(body, BorderLayout.CENTER);
        d.setSize(380, 160);
        d.setResizable(false);
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }
}
