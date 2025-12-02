package POS;

import javax.swing.table.AbstractTableModel;
import java.util.List;

// Table Model
public class CartTableModel extends AbstractTableModel {
    private final Cart cart;
    private final String[] columns = {"Item", "Qty", "Price", "Total", "Action"};

    public CartTableModel(Cart cart) {
        this.cart = cart;
    }

    @Override
    public int getRowCount() {
        return cart.getItems().size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    // Display
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        List<CartItem> items = cart.getItems();
        CartItem ci = items.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return ci.getProduct().getName();
            case 1:
                return ci.getQuantity();
            case 2:
                return String.format("₱%.2f", ci.getProduct().getPrice());
            case 3:
                return String.format("₱%.2f", ci.getTotalPrice());
            case 4:
                return "Remove";
            default:
                return "";
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        // Editable
        return columnIndex == 1 || columnIndex == 4;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        List<CartItem> items = cart.getItems();
        if (rowIndex < 0 || rowIndex >= items.size()) return;
        CartItem ci = items.get(rowIndex);
        if (columnIndex == 1) {
            try {
                int v = Integer.parseInt(aValue.toString());
                if (v <= 0) {
                    // Remove
                    cart.removeProduct(ci.getProduct());
                } else {
                    ci.setQuantity(v);
                }
                fireTableDataChanged();
            } catch (NumberFormatException ignored) {}
        }
    }
}
