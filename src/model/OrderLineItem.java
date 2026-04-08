package model;

public class OrderLineItem {
    private int itemId;
    private String itemName;
    private int quantity;
    private double unitPrice;
    private double discount;

    public OrderLineItem(int itemId, String itemName, int quantity, double unitPrice, double discount) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.discount = discount;
    }

    public int getItemId() {
        return itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public double getDiscount() {
        return discount;
    }

    public double getLineTotal() {
        return (quantity * unitPrice) - discount;
    }
}