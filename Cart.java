import java.util.ArrayList;
import java.util.List;

// Cart
public class Cart {
    // Items
    private final List<CartItem> items = new ArrayList<>();

    // Add
    public void addProduct(Product p) {
        for (CartItem ci : items) {
            if (ci.getProduct().getName().equals(p.getName())) {
                ci.incrementQuantity();
                return;
            }
        }
        items.add(new CartItem(p, 1));
    }

    // Remove
    public void removeProduct(Product p) {
        // RemoveByName
        items.removeIf(ci -> ci.getProduct().getName().equals(p.getName()));
    }

    // Clear
    public void clear() {
        items.clear();
    }

    // GetItems
    public List<CartItem> getItems() {
        return items;
    }

    // Subtotal
    public double getSubtotal() {
        double sum = 0.0;
        for (CartItem ci : items) {
            sum = sum + ci.getTotalPrice();
        }
        return sum;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}
import java.util.ArrayList;
import java.util.List;

// Cart
public class Cart {
    // Items
    private final List<CartItem> items = new ArrayList<>();

    // Add
    public void addProduct(Product p) {
        for (CartItem ci : items) {
            if (ci.getProduct().getName().equals(p.getName())) {
                ci.incrementQuantity();
                return;
            }
        }
        items.add(new CartItem(p, 1));
    }

    // Remove
    public void removeProduct(Product p) {
        // RemoveByName
        items.removeIf(ci -> ci.getProduct().getName().equals(p.getName()));
    }

    // Clear
    public void clear() {
        items.clear();
    }

    // GetItems
    public List<CartItem> getItems() {
        return items;
    }

    // Subtotal
    public double getSubtotal() {
        double sum = 0.0;
        for (CartItem ci : items) {
            sum = sum + ci.getTotalPrice();
        }
        return sum;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}
import java.util.ArrayList;
import java.util.List;

// Cart
public class Cart {
    // Items
    private final List<CartItem> items = new ArrayList<>();

    // Add
    public void addProduct(Product p) {
        for (CartItem ci : items) {
            if (ci.getProduct().getName().equals(p.getName())) {
                ci.incrementQuantity();
                return;
            }
        }
        items.add(new CartItem(p, 1));
    }

    // Remove
    public void removeProduct(Product p) {
        // RemoveByName
        items.removeIf(ci -> ci.getProduct().getName().equals(p.getName()));
    }

    // Clear
    public void clear() {
        items.clear();
    }

    // GetItems
    public List<CartItem> getItems() {
        return items;
    }

    // Subtotal
    public double getSubtotal() {
        double sum = 0.0;
        for (CartItem ci : items) {
            sum = sum + ci.getTotalPrice();
        }
        return sum;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}
import java.util.ArrayList;
import java.util.List;

// Cart
public class Cart {
    // Items
    private final List<CartItem> items = new ArrayList<>();

    // Add
    public void addProduct(Product p) {
        for (CartItem ci : items) {
            if (ci.getProduct().getName().equals(p.getName())) {
                ci.incrementQuantity();
                return;
            }
        }
        items.add(new CartItem(p, 1));
    }

    // Remove
    public void removeProduct(Product p) {
        // RemoveByName
        items.removeIf(ci -> ci.getProduct().getName().equals(p.getName()));
    }

    // Clear
    public void clear() {
        items.clear();
    }

    // GetItems
    public List<CartItem> getItems() {
        return items;
    }

    // Subtotal
    public double getSubtotal() {
        double sum = 0.0;
        for (CartItem ci : items) {
            sum = sum + ci.getTotalPrice();
        }
        return sum;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}
