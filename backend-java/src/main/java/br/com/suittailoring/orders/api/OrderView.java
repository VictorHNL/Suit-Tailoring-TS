package br.com.suittailoring.orders.api;
import br.com.suittailoring.orders.domain.*;
import java.util.*;
import java.math.BigDecimal;
import java.time.Instant;
public record OrderView(UUID id,UUID customerId,PurchaseOrder.Status status,BigDecimal total,String currency,String shippingAddress,String trackingCode,Instant createdAt,Instant expiresAt,List<OrderLine> lines){
    public static OrderView of(PurchaseOrder o){return new OrderView(o.getId(),o.getCustomerId(),o.getStatus(),o.getTotal(),"BRL",o.getShippingAddress(),o.getTrackingCode(),o.getCreatedAt(),o.getExpiresAt(),o.getLines());}
}
