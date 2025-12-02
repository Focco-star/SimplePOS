// Cart Item
public class CartItem {
    private final Product product; // Product
    private int quantity; // Qty

    // Create
    public CartItem(Product product, int quantity) {
        this.product = product;
        if (quantity < 1) quantity = 1;
        this.quantity = quantity;
    }

    // Get product
    public Product getProduct() {
        return product;
    }

    // Get qty
    public int getQuantity() {
        return quantity;
    }

    // Set qty
    public void setQuantity(int quantity) {
        if (quantity < 0) quantity = 0;
        this.quantity = quantity;
    }

    // Increment
    public void incrementQuantity() {
        quantity = quantity + 1;
    }

    // Total
    public double getTotalPrice() {
        return product.getPrice() * quantity;
    }

}
