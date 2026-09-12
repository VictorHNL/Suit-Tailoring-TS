package br.com.suittailoring.catalog.domain.model;

public class Product {
    
    private String name;
    private String description;


    public Product(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void changeName(String newName) {
        if (newName == null || newName.isBlank()){
            throw new IllegalArgumentException(
                "O nome do produto não pode ficar vazio"
            );
        }

        this.name = newName;
    }
}