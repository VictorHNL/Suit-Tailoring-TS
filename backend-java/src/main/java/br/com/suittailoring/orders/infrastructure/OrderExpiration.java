package br.com.suittailoring.orders.infrastructure;
import br.com.suittailoring.orders.application.OrderService;
import br.com.suittailoring.orders.domain.PurchaseOrder;
import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import java.time.Instant;
@Component @EnableScheduling @ConditionalOnProperty(name="app.jobs.enabled",havingValue="true",matchIfMissing=true)
public class OrderExpiration {
    private final OrderRepository orders;private final OrderService service;
    public OrderExpiration(OrderRepository o,OrderService s){orders=o;service=s;}
    @Scheduled(fixedDelay=60000,initialDelay=60000)
    public void expire(){
        for(var order:orders.findTop100ByStatusAndExpiresAtBefore(PurchaseOrder.Status.AWAITING_PAYMENT,Instant.now())){
            try{service.expire(order.getId());}
            catch(RuntimeException e){org.slf4j.LoggerFactory.getLogger(getClass()).warn("Falha ao expirar pedido {}",order.getId());}
        }
    }
}
