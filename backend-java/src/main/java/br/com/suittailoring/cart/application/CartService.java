package br.com.suittailoring.cart.application;
import br.com.suittailoring.cart.domain.Cart;
import br.com.suittailoring.cart.infrastructure.CartRepository;
import br.com.suittailoring.catalog.domain.model.Product;
import br.com.suittailoring.catalog.infrastructure.VariantRepository;
import br.com.suittailoring.shared.BusinessException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.*;
@Service @Transactional
public class CartService {
    public record Item(UUID variantId,String name,String size,String color,BigDecimal unitPrice,int quantity,int availableStock){}
    public record View(UUID token,boolean consumed,List<Item> items,BigDecimal subtotal,String currency){}
    private final CartRepository carts; private final VariantRepository variants;
    public CartService(CartRepository c,VariantRepository v){carts=c;variants=v;}
    public View create(){return view(carts.save(new Cart()));}
    @Transactional(readOnly=true) public View get(UUID token,UUID customer){Cart c=carts.findById(token).orElseThrow(EntityNotFoundException::new);c.checkAccess(customer);return view(c);}
    public View set(UUID token,UUID variantId,int quantity,UUID customer){
        Cart c=carts.lock(token).orElseThrow(EntityNotFoundException::new);c.checkAccess(customer);
        if(quantity>0){
            var v=variants.findById(variantId).orElseThrow(EntityNotFoundException::new);
            if(v.getProduct().getStatus()!=Product.Status.PUBLISHED || quantity>v.getStock())throw new BusinessException("Produto indisponível ou estoque insuficiente");
        }
        c.setItem(variantId,quantity);return view(c);
    }
    private View view(Cart c){
        List<Item> items=c.getItems().entrySet().stream().sorted(Map.Entry.comparingByKey()).map(e->{
            var v=variants.findById(e.getKey()).orElseThrow(EntityNotFoundException::new);
            return new Item(v.getId(),v.getProduct().getName(),v.getSize(),v.getColor(),v.getProduct().sellingPrice(),e.getValue(),v.getStock());
        }).toList();
        return new View(c.getId(),c.isConsumed(),items,items.stream().map(i->i.unitPrice().multiply(BigDecimal.valueOf(i.quantity()))).reduce(BigDecimal.ZERO,BigDecimal::add),"BRL");
    }
}
