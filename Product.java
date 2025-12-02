package POS;

// Product
public abstract class Product {
    // Name
    private final String name;
    // Price
    private final double price;

    // Constructor
    public Product(String name, double price) {
        this.name = name;
        this.price = price;
    }

    // GetName
    public String getName() {
        return name;
    }

    // GetPrice
    public double getPrice() {
        return price;
    }

    // ToString
    @Override
    public String toString() {
        return name + " (₱" + String.format("%.2f", price) + ")";
    }
}
