package br.com.suittailoring.catalog.application;
import br.com.suittailoring.catalog.domain.model.*;
import br.com.suittailoring.catalog.infrastructure.*;
import br.com.suittailoring.catalog.api.CatalogDtos.*;
import br.com.suittailoring.operations.application.AuditService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;
import java.util.*;
@Service @Transactional
public class CatalogService {
    private final ProductRepository products; private final VariantRepository variants; private final AuditService audit;
    public CatalogService(ProductRepository p,VariantRepository v,AuditService a){products=p;variants=v;audit=a;}
    @Transactional(readOnly=true) public Page<View> list(Product.Audience audience,String category,String search,int page){
        return products.search(Product.Status.PUBLISHED,audience,category,search,PageRequest.of(Math.max(0,page),20,Sort.by("name"))).map(this::view);
    }
    @Transactional(readOnly=true) public Page<View> adminList(int page){return products.findAll(PageRequest.of(Math.max(0,page),20,Sort.by("name"))).map(this::view);}
    @Transactional(readOnly=true) public View detail(UUID id,boolean admin){Product p=find(id);if(!admin && p.getStatus()!=Product.Status.PUBLISHED)throw new EntityNotFoundException();return view(p);}
    public View create(Input i){Product p=new Product(i.name(),i.description());apply(p,i);products.save(p);audit.record("PRODUCT_CREATED",p.getId());return view(p);}
    public View update(UUID id,Input i){Product p=products.lock(id).orElseThrow(EntityNotFoundException::new);apply(p,i);if(p.getStatus()==Product.Status.PUBLISHED)p.publish();audit.record("PRODUCT_UPDATED",id);return view(p);}
    public View publish(UUID id){Product p=products.lock(id).orElseThrow(EntityNotFoundException::new);if(variants.findByProductIdOrderBySku(id).isEmpty())throw new IllegalArgumentException("Cadastre uma variante antes de publicar");p.publish();audit.record("PRODUCT_PUBLISHED",id);return view(p);}
    public void archive(UUID id){Product p=products.lock(id).orElseThrow(EntityNotFoundException::new);p.archive();audit.record("PRODUCT_ARCHIVED",id);}
    public View cloneProduct(UUID id){
        Product p=find(id).copy();products.save(p);
        for(Variant v:variants.findByProductIdOrderBySku(id))variants.save(new Variant(p,v.getSku()+"-"+UUID.randomUUID().toString().substring(0,8),v.getSize(),v.getColor(),0));
        audit.record("PRODUCT_CLONED",p.getId());return view(p);
    }
    public VariantView addVariant(UUID id,VariantInput i){Product p=products.lock(id).orElseThrow(EntityNotFoundException::new);Variant v=variants.save(new Variant(p,i.sku(),i.size(),i.color(),i.stock()));audit.record("VARIANT_CREATED",v.getId());return VariantView.of(v);}
    public VariantView stock(UUID id,int stock){Variant v=variants.lock(id).orElseThrow(EntityNotFoundException::new);v.setStock(stock);audit.record("STOCK_SET_"+stock,id);return VariantView.of(v);}
    private Product find(UUID id){return products.findById(id).orElseThrow(EntityNotFoundException::new);}
    private View view(Product p){return View.of(p,variants.findByProductIdOrderBySku(p.getId()));}
    private void apply(Product p,Input i){p.update(i.name(),i.description(),i.slug(),i.category(),i.collectionName(),i.care(),i.audience(),i.bodyPart(),i.price(),i.promotionalPrice(),i.images(),i.videoUrl());}
}

