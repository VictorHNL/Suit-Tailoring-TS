package br.com.suittailoring.catalog.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.*;

@Entity
@Table(name = "products")
public class Product {
  public enum Audience {
    WOMEN,
    MEN,
    UNISEX
  }

  public enum Status {
    DRAFT,
    PUBLISHED,
    ARCHIVED
  }

  public enum BodyPart {
    TORSO,
    LEGS,
    FEET,
    FULL_BODY,
    ACCESSORIES
  }

  @Id private UUID id = UUID.randomUUID();
  @Version private long version;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false, length = 4000)
  private String description;

  @Column(nullable = false, unique = true)
  private String slug;

  @Column(nullable = false)
  private String category;

  private String collectionName;

  @Column(length = 2000)
  private String care;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Audience audience = Audience.UNISEX;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private BodyPart bodyPart = BodyPart.TORSO;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Status status = Status.DRAFT;

  @Column(nullable = false, precision = 14, scale = 2)
  private BigDecimal price = BigDecimal.ZERO;

  @Column(precision = 14, scale = 2)
  private BigDecimal promotionalPrice;

  @ElementCollection
  @CollectionTable(name = "product_media", joinColumns = @JoinColumn(name = "product_id"))
  @OrderColumn(name = "position")
  @Column(name = "url", length = 1000)
  private List<String> images = new ArrayList<>();

  @Column(length = 1000)
  private String videoUrl;

  protected Product() {}

  public Product(String name, String description) {
    changeName(name);
    this.description = description;
    this.slug = UUID.randomUUID().toString();
    this.category = "Sem categoria";
  }

  public void changeName(String name) {
    if (name == null || name.isBlank())
      throw new IllegalArgumentException("O nome do produto não pode ficar vazio");
    this.name = name.strip();
  }

  public void update(
      String name,
      String description,
      String slug,
      String category,
      String collectionName,
      String care,
      Audience audience,
      BodyPart bodyPart,
      BigDecimal price,
      BigDecimal promotion,
      List<String> images,
      String videoUrl) {
    changeName(name);
    if (price == null || price.signum() <= 0 || price.scale() > 2)
      throw new IllegalArgumentException("Preço deve ser positivo com até duas casas decimais");
    if (promotion != null
        && (promotion.signum() <= 0 || promotion.compareTo(price) > 0 || promotion.scale() > 2))
      throw new IllegalArgumentException("Preço promocional inválido");
    this.description = description;
    this.slug = slug;
    this.category = category;
    this.collectionName = collectionName;
    this.care = care;
    this.audience = audience;
    this.bodyPart = bodyPart;
    this.price = price;
    this.promotionalPrice = promotion;
    this.images.clear();
    this.images.addAll(images);
    this.videoUrl = videoUrl;
  }

  public void publish() {
    if (images.isEmpty() || price.signum() <= 0)
      throw new IllegalArgumentException("Publicação exige imagem e preço");
    status = Status.PUBLISHED;
  }

  public void archive() {
    status = Status.ARCHIVED;
  }

  public Product copy() {
    Product copy = new Product(name + " (cópia)", description);
    copy.update(
        copy.name,
        description,
        slug + "-" + UUID.randomUUID().toString().substring(0, 8),
        category,
        collectionName,
        care,
        audience,
        bodyPart,
        price,
        promotionalPrice,
        images,
        videoUrl);
    return copy;
  }

  public BigDecimal sellingPrice() {
    return promotionalPrice == null ? price : promotionalPrice;
  }

  public UUID getId() {
    return id;
  }

  public long getVersion() {
    return version;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public String getSlug() {
    return slug;
  }

  public String getCategory() {
    return category;
  }

  public String getCollectionName() {
    return collectionName;
  }

  public String getCare() {
    return care;
  }

  public Audience getAudience() {
    return audience;
  }

  public BodyPart getBodyPart() {
    return bodyPart;
  }

  public Status getStatus() {
    return status;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public BigDecimal getPromotionalPrice() {
    return promotionalPrice;
  }

  public List<String> getImages() {
    return List.copyOf(images);
  }

  public String getVideoUrl() {
    return videoUrl;
  }
}
