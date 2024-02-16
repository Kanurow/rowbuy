package com.rowland.engineering.ecommerce.controller;


import com.rowland.engineering.ecommerce.dto.*;
import com.rowland.engineering.ecommerce.model.*;
import com.rowland.engineering.ecommerce.service.ProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Product")
public class ProductController {
    private final ProductService productService;


    @Operation(
            description = "Post request for checking out selected products",
            summary = "Checking out shopping cart"
    )
    @PostMapping("/checkout/{userId}")
    public ResponseEntity<ApiResponse> checkoutCart(
            @RequestBody CartCheckoutRequest checkoutRequest,
            @PathVariable(value = "userId") Long userId) {
        return new ResponseEntity<ApiResponse>(productService.checkoutCart(checkoutRequest,userId), HttpStatus.ACCEPTED);
    }

    @Operation(
            description = "Get request for retrieving products in a user shopping cart",
            summary = "Returns list of user cart items"
    )
    @GetMapping("/cart/{userId}")
    public List<ShoppingCart> getUserCart(
            @PathVariable(value = "userId") Long userId
    ) {
        return productService.getUserCart(userId);
    }


    @Operation(
            description = "Returns list of all products",
            summary = "Retrieves all products"
    )
    @GetMapping("/all")
    public ResponseEntity<List<ProductResponse>> getAllProducts() {

        List<ProductResponse> allProducts = productService.getAllProducts();
        return ResponseEntity.status(HttpStatus.OK).body(allProducts);
    }


    @Operation(
            description = "Returns list of all checked out product by user",
            summary = "Retrieves all checked out goods"
    )
    @GetMapping("/checkedout/{id}")
    public List<CartCheckout> getUserCartsByUserId(@PathVariable(value = "id") Long userId) {
        return productService.getCheckedOutCart(userId);
    }



    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(
            description = "Post request for product creation",
            summary = "Enables authorized users with admin role to create products"
    )
    @PostMapping("/createProduct")
    public ResponseEntity<String> createProductWithImg(@Valid @RequestParam("imageFile") MultipartFile imageFile,
                                                @RequestParam("productName") String productName,
                                                @RequestParam("price") Double price,
                                                @RequestParam("percentageDiscount") String percentageDiscount,
                                                @RequestParam("category") Category category,
                                                @RequestParam("quantity") Integer quantity,
                                                @RequestParam("userId") String userId,
                                                @RequestParam("description") String description) throws IOException {
        return productService.createProduct(imageFile,  productName,  price, Integer.valueOf(percentageDiscount),  category, quantity, userId, description);
    }


    @Operation(
            description = "Allows users view product complete information",
            summary = "View complete product information of selected product"
    )
    @GetMapping("/view/{productId}")
    public ResponseEntity<Product> getProduct(@PathVariable Long productId) {
        Product allProducts = productService.getProduct(productId);
        return ResponseEntity.status(HttpStatus.OK).body(allProducts);
    }

    @Operation(
            description = "Allows users view product created under supermarket category",
            summary = "Returns products under supermarket category"
    )
    @GetMapping("/supermarket")
    public ResponseEntity<List<ProductResponse>> getAllSupermarketProducts() {
        List<ProductResponse> allProducts = productService.getAllSupermarketProducts();
        return ResponseEntity.status(HttpStatus.OK).body(allProducts);
    }

    @Operation(
            description = "Allows users view product created under computing category",
            summary = "Returns products under computing category"
    )
    @GetMapping("/computing")
    public ResponseEntity<List<ProductResponse>> getAllComputingProducts() {
        List<ProductResponse> allProducts = productService.getAllComputingProducts();
        return ResponseEntity.status(HttpStatus.OK).body(allProducts);
    }

    @Operation(
            description = "Allows users view product created under travels category",
            summary = "Returns products under travels category"
    )
    @GetMapping("/travels")
    public ResponseEntity<List<ProductResponse>> getAllTravelsProducts() {
        List<ProductResponse> allProducts = productService.getAllTravelsProducts();
        return ResponseEntity.status(HttpStatus.OK).body(allProducts);
    }

    @Operation(
            description = "Allows users view product created under baby products category",
            summary = "Returns products under baby products category"
    )
    @GetMapping("/babyProducts")
    public ResponseEntity<List<ProductResponse>> getAllBabyProducts() {
        List<ProductResponse> allProducts = productService.getAllBabyProducts();
        return ResponseEntity.status(HttpStatus.OK).body(allProducts);
    }

    @Operation(
            description = "Allows users view product created under appliances category",
            summary = "Returns products under appliances category"
    )
    @GetMapping("/appliances")
    public ResponseEntity<List<ProductResponse>> getAllAppliancesProducts() {

        List<ProductResponse> allProducts = productService.getAllAppliancesProducts();
        return ResponseEntity.status(HttpStatus.OK).body(allProducts);
    }

    @Operation(
            description = "Allows users view product created under books category",
            summary = "Returns products under books category"
    )
    @GetMapping("/books")
    public ResponseEntity<List<ProductResponse>> getAllBooksProducts() {

        List<ProductResponse> allProducts = productService.getAllBooksProduct();
        return ResponseEntity.status(HttpStatus.OK).body(allProducts);
    }

    @Operation(
            description = "Allows users view product created under electronics category",
            summary = "Returns products under electronics category"
    )
    @GetMapping("/electronics")
    public ResponseEntity<List<ProductResponse>> getAllElectronicsProducts() {

        List<ProductResponse> allProducts = productService.getAllElectronicsProduct();
        return ResponseEntity.status(HttpStatus.OK).body(allProducts);
    }

    @Operation(
            description = "Allows users view product created under others category which can be regarded as a random category",
            summary = "Returns products under others category"
    )
    @GetMapping("/others")
    public ResponseEntity<List<ProductResponse>> getAllOtherProducts() {

        List<ProductResponse> allProducts = productService.getAllOtherProduct();
        return ResponseEntity.status(HttpStatus.OK).body(allProducts);
    }

    @Operation(
            description = "Allows users view product created under others phones and tablets category",
            summary = "Returns products under phones and tablets category"
    )
    @GetMapping("/phonesAndTablets")
    public ResponseEntity<List<ProductResponse>> getAllPhonesProducts() {

        List<ProductResponse> allProducts = productService.getAllPhonesAndTabletsProduct();
        return ResponseEntity.status(HttpStatus.OK).body(allProducts);
    }


    @Operation(
            description = "Post request for adding a product to shopping cart",
            summary = "Adds a product to shopping cart"
    )
    @PostMapping("/addtocart/{productId}/{userId}")
    public ResponseEntity<ApiResponse> addToCart(
            @PathVariable(value = "productId") Long productId,
            @PathVariable(value = "userId") Long userId) {
        return new ResponseEntity<ApiResponse>(productService.addToCart(productId,userId),HttpStatus.ACCEPTED);
    }


    @Operation(
            description = "Delete request for removing a product from cart",
            summary = "Removes a product from cart"
    )
    @DeleteMapping("/removefromcart/{id}")
    public ResponseEntity<ApiResponse> removeFromCart(
            @PathVariable(value = "id") Long id) {
        return new ResponseEntity<ApiResponse>(productService.removeFromCart(id),HttpStatus.ACCEPTED);
    }


    @Operation(
            description = "Gets all products the user has purchased",
            summary = "Returns order history of products purchased by user"
    )
    @GetMapping("/orderHistory/{userId}")
    public ResponseEntity<List<OrderHistoryResponse>> getUserOrderHistory(
            @PathVariable(value = "userId") Long id
    ) {
        List<OrderHistoryResponse> orders = productService.getUserOrderHistory(id);
        return ResponseEntity.status(HttpStatus.OK).body(orders);
    }


    @Operation(
            description = "Gets paged products based on selected number of display and sorted based on customer choice",
            summary = "Gets paged products for users based on sort preference and display size"
    )
    @GetMapping("/paged/{offset}/{pageSize}/{field}")
    public ResponseEntity<List<ProductResponse>> getAllPagedProducts(
            @PathVariable int offset,
            @PathVariable int pageSize,
            @PathVariable String field) {
        List<ProductResponse> allProducts = productService.getAllProductsWithPagination(offset, pageSize, field);
        return ResponseEntity.status(HttpStatus.OK).body(allProducts);
    }

    @Operation(
            description = "Gets products created by a given vendor",
            summary = "Users vendor Id to get products created by vendor"
    )
    @GetMapping("/vendor/{userId}")
    public ResponseEntity<List<ProductResponse>> getAllPagedVendorsProducts(

            @PathVariable String userId) {
        List<ProductResponse> allProducts = productService.getAllVendorProducts(userId);
        return ResponseEntity.status(HttpStatus.OK).body(allProducts);
    }


    @Operation(
            description = "Gets vendors products ordered by customers",
            summary = "Gets vendors products created by vendors and ordered by customers"
    )
    @GetMapping("/vendors/productsOrdered/{vendorId}")
    public ResponseEntity<List<OrderedProductsByUserFromVendor>> getMyProductsOrderByUsers(
            @PathVariable(value = "vendorId") Long vendorId
    ) {
        List<OrderedProductsByUserFromVendor> orders = productService.getMyProductsOrderedByUsers(vendorId);
        return ResponseEntity.status(HttpStatus.OK).body(orders);
    }


}
