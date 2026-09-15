package br.com.suittailoring.catalog.domain.model;
import jakarta.persistence.*;
import br.com.suittailoring.shared.BusinessException;
import java.util.UUID;
@Entity @Table(name="variants")
public class Variant {
    @Id private UUID id=UUID.randomUUID();
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="product_id") private Product product;
    @Column(nullable=false,unique=true) private String sku;
    @Column(nullable=false) private String size;
    @Column(nullable=false) private String color;
    @Column(nullable=false) private int stock;
    protected Variant(){}
    public Variant(Product product,String sku,String size,String color,int stock){this.product=product;this.sku=sku;this.size=size;this.color=color;setStock(stock);}
    public void setStock(int stock){if(stock<0)throw new BusinessException("Estoque não pode ser negativo");this.stock=stock;}
    public void reserve(int quantity){if(quantity<=0 || quantity>stock)throw new BusinessException("Estoque insuficiente");stock-=quantity;}
    public void release(int quantity){if(quantity<=0)throw new BusinessException("Quantidade inválida");stock=Math.addExact(stock,quantity);}
    public UUID getId(){return id;} public Product getProduct(){return product;} public String getSku(){return sku;}
    public String getSize(){return size;} public String getColor(){return color;} public int getStock(){return stock;}
}

