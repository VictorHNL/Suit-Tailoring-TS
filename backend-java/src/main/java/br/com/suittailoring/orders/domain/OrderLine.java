package br.com.suittailoring.orders.domain;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;
@Embeddable
public class OrderLine {
    @Column(nullable=false) private UUID variantId;
    @Column(nullable=false) private String productName;
    @Column(nullable=false) private String sku;
    @Column(nullable=false) private int quantity;
    @Column(nullable=false,precision=14,scale=2) private BigDecimal unitPrice;
    protected OrderLine(){}
    public OrderLine(UUID variantId,String productName,String sku,int quantity,BigDecimal unitPrice){
        this.variantId=variantId;this.productName=productName;this.sku=sku;this.quantity=quantity;this.unitPrice=unitPrice;
    }
    public UUID getVariantId(){return variantId;}public String getProductName(){return productName;}public String getSku(){return sku;}
    public int getQuantity(){return quantity;}public BigDecimal getUnitPrice(){return unitPrice;}
    public BigDecimal subtotal(){return unitPrice.multiply(BigDecimal.valueOf(quantity));}
}
