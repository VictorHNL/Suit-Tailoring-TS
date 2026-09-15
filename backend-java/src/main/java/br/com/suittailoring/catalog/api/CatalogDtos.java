package br.com.suittailoring.catalog.api;
import br.com.suittailoring.catalog.domain.model.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.*;
public final class CatalogDtos {
    private CatalogDtos(){}
    public record Input(@NotBlank @Size(max=150) String name,@NotBlank @Size(max=4000) String description,
        @NotBlank @Size(max=180) @Pattern(regexp="[a-z0-9]+(?:-[a-z0-9]+)*") String slug,
        @NotBlank @Size(max=100) String category,@Size(max=150) String collectionName,@Size(max=2000) String care,
        @NotNull Product.Audience audience,@NotNull Product.BodyPart bodyPart,
        @NotNull @DecimalMin("0.01") @Digits(integer=12,fraction=2) BigDecimal price,
        @DecimalMin("0.01") @Digits(integer=12,fraction=2) BigDecimal promotionalPrice,
        @NotNull @Size(max=10) List<@NotBlank @Size(max=1000) @Pattern(regexp="(?:https://[^\\s]+|/api/media/[a-f0-9-]+\\.(?:png|jpg))") String> images,
        @Size(max=1000) @Pattern(regexp="https://[^\\s]+") String videoUrl){}
    public record VariantInput(@NotBlank @Size(max=100) String sku,@NotBlank @Size(max=30) String size,@NotBlank @Size(max=80) String color,@Min(0) int stock){}
    public record VariantView(UUID id,String sku,String size,String color,int stock){
        public static VariantView of(Variant v){return new VariantView(v.getId(),v.getSku(),v.getSize(),v.getColor(),v.getStock());}
    }
    public record View(UUID id,long version,String name,String description,String slug,String category,String collectionName,String care,Product.Audience audience,Product.BodyPart bodyPart,Product.Status status,BigDecimal price,BigDecimal promotionalPrice,String currency,List<String> images,String videoUrl,List<VariantView> variants){
        public static View of(Product p,List<Variant> variants){return new View(p.getId(),p.getVersion(),p.getName(),p.getDescription(),p.getSlug(),p.getCategory(),p.getCollectionName(),p.getCare(),p.getAudience(),p.getBodyPart(),p.getStatus(),p.getPrice(),p.getPromotionalPrice(),"BRL",p.getImages(),p.getVideoUrl(),variants.stream().map(VariantView::of).toList());}
    }
}

