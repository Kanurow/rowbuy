package com.rowland.engineering.ecommerce.dto;

import com.rowland.engineering.ecommerce.model.Category;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.NaturalId;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponse {
    private Long id;
    private String productName;
    private Category category;
    private Double sellingPrice;
    private Double amountDiscounted;
    private Integer percentageDiscount;
    private String description;
    private String imageUrl;
    private Integer quantity;

}
