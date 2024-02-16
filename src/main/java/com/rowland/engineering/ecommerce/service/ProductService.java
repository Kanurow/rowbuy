package com.rowland.engineering.ecommerce.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import com.rowland.engineering.ecommerce.dto.*;
import com.rowland.engineering.ecommerce.exception.*;
import com.rowland.engineering.ecommerce.model.*;
import com.rowland.engineering.ecommerce.repository.*;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

import java.util.stream.Collectors;



@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ShoppingCartRepository shoppingCartRepository;
    private final CartCheckoutRepository cartCheckoutRepository;

    private Cloudinary cloudinary;

    @Value("${cloudinary.cloud_name}")
    private String cloudName;

    @Value("${cloudinary.api_key}")
    private String apiKey;

    @Value("${cloudinary.api_secret}")
    private String apiSecret;

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    @PostConstruct
    public void initializeCloudinary() {
        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret));
    }

    @Transactional
    public ResponseEntity<String> createProduct(@Valid MultipartFile imageFile, String productName, Double price,
                                                Integer percentageDiscount, Category category, Integer quantity, String userId,
                                                String description)  {

        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(imageFile.getBytes(), ObjectUtils.emptyMap());
            String imageUrl = (String) uploadResult.get("secure_url");

            Product product = new Product();
            Double productPrice;
            Double amountDiscountedFromOriginalPrice;
            if (percentageDiscount > 0) {
                amountDiscountedFromOriginalPrice = price * (percentageDiscount /  (double) 100);
                productPrice = price - amountDiscountedFromOriginalPrice;
                product.setAmountDiscounted(amountDiscountedFromOriginalPrice);
                product.setSellingPrice(productPrice);

            } else {
                productPrice = price;
                product.setSellingPrice(productPrice);
                product.setAmountDiscounted(0.0);
            }

            product.setProductName(productName);
            product.setPercentageDiscount(percentageDiscount);
            product.setQuantity(quantity);
            product.setCategory(category);
            product.setDescription(description);
            product.setImageUrl(imageUrl);
            product.setUserId(Long.valueOf(userId));

            Product savedProduct = productRepository.save(product);

            return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct.getProductName() + " has been created successfully under " +savedProduct.getCategory() + " category" );
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to process product upload.");
        }
    }



    public ApiResponse addToCart(Long productId, Long userId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Optional<ShoppingCart> existingCartEntry = shoppingCartRepository.findByProductAndUser(product, user);
        if (existingCartEntry.isPresent()) {
            throw new BadRequestException("Sorry! You have already added this product to your shopping cart");
        }
        ShoppingCart cart = new ShoppingCart();
        cart.setProduct(product);
        cart.setUser(user);

        shoppingCartRepository.save(cart);
        return new ApiResponse(true, "Product Added to cart");
    }




    public List<ShoppingCart> getUserCart(Long userId) {
        return shoppingCartRepository.findAllByUserId(userId);
    }


    @Transactional
    public ApiResponse checkoutCart(CartCheckoutRequest checkoutRequest, Long userId)  {
        CartCheckout cartCheckout = new CartCheckout();

        cartCheckout.setDeliveryAddress(checkoutRequest.getDeliveryAddress());
        cartCheckout.setFirstName(checkoutRequest.getFirstName());
        cartCheckout.setLastName(checkoutRequest.getLastName());
        cartCheckout.setPhoneNumber(checkoutRequest.getPhoneNumber());
        cartCheckout.setAlternativePhoneNumber(checkoutRequest.getAlternativePhoneNumber());
        cartCheckout.setAdditionalInformation(checkoutRequest.getAdditionalInformation());
        cartCheckout.setRegion(checkoutRequest.getRegion());
        cartCheckout.setState(checkoutRequest.getState());

        cartCheckout.setPrice(checkoutRequest.getTotal());
        cartCheckout.setQuantity(checkoutRequest.getQuantity());
        cartCheckout.setUserId(userId);
        cartCheckout.setPurchaseDate(LocalDateTime.now());

        cartCheckout.setPaymentReference(checkoutRequest.getPaystackReference());
        if (Objects.equals(checkoutRequest.getPaystackApproved(), "Approved")) {
            cartCheckout.setPaymentStatus(PaymentStatus.APPROVED);
        } else {
            cartCheckout.setPaymentStatus(PaymentStatus.FAILED);
        }

        List<CartCheckout.CartItem> cartItems = checkoutRequest.getCart().stream()
                .map(item -> {

                    Product product = productRepository.findById(item.getProductId())
                            .orElseThrow(() -> new ProductNotFoundException(item.getProductId()));

                    int newQuantity = product.getQuantity() - item.getQuantity();

                    if (newQuantity < 0) {
                        throw new InsufficientProductQuantityException(product.getId());
                    }
                    product.setQuantity(newQuantity);
                    productRepository.save(product);

                    CartCheckout.CartItem cartItem = new CartCheckout.CartItem();
                    cartItem.setProductId(item.getProductId());
                    cartItem.setProductName(item.getProductName());
                    cartItem.setPrice(item.getPrice());
                    cartItem.setImageUrl(item.getImageUrl());
                    cartItem.setQuantity(item.getQuantity());
                    cartItem.setSubtotal(item.getSubtotal());
                    return cartItem;
                })
                .collect(Collectors.toList());
        cartCheckout.setCart(cartItems);

        cartCheckoutRepository.save(cartCheckout);

        List<ShoppingCart> userShoppingCartItems = shoppingCartRepository.findAllByUserId(userId);
        shoppingCartRepository.deleteAll(userShoppingCartItems);

        return new ApiResponse(true, "Checked Out");
    }


    public List<CartCheckout> getCheckedOutCart(Long userId) {
        return cartCheckoutRepository.findByUserId(userId);
    }



    public List<ProductResponse> getAllProducts() {
        List<Product> products = productRepository.findAll();

        return products.stream().map(item -> {
            ProductResponse product = new ProductResponse();
            product.setId(item.getId());
            product.setPercentageDiscount(item.getPercentageDiscount());
            product.setSellingPrice(item.getSellingPrice());
            product.setAmountDiscounted(item.getAmountDiscounted());
            product.setProductName(item.getProductName());
            product.setQuantity(item.getQuantity());
            product.setCategory(item.getCategory());
            product.setDescription(item.getDescription());
            product.setImageUrl(item.getImageUrl());
            return product;
        }).collect(Collectors.toList());
    }

    public List<ProductResponse> getAllSupermarketProducts() {
        List<Product> supermarket = productRepository.findAllByCategory(Category.SUPERMARKET);
        return supermarket.stream().map(item -> {
            ProductResponse product = new ProductResponse();
            product.setId(item.getId());
            product.setPercentageDiscount(item.getPercentageDiscount());
            product.setSellingPrice(item.getSellingPrice());
            product.setAmountDiscounted(item.getAmountDiscounted());
            product.setProductName(item.getProductName());
            product.setQuantity(item.getQuantity());
            product.setCategory(item.getCategory());
            product.setDescription(item.getDescription());
            product.setImageUrl(item.getImageUrl());
            return product;
        }).collect(Collectors.toList());
    }

    public ApiResponse removeFromCart(Long id) {
        shoppingCartRepository.deleteById(id);
        return new ApiResponse(true, "Item removed from cart");
    }


    public List<OrderHistoryResponse> getUserOrderHistory(Long id) {
        List<CartCheckout> userCartItems = cartCheckoutRepository.findByUserId(id);
        return userCartItems.stream().map(item -> {
            OrderHistoryResponse order = new OrderHistoryResponse();
            order.setId(item.getId());
            order.setQuantity(item.getQuantity());
            order.setFirstName(item.getFirstName());
            order.setLastName(item.getLastName());
            order.setPhoneNumber(item.getPhoneNumber());
            order.setAlternativePhoneNumber(item.getAlternativePhoneNumber());
            order.setDeliveryAddress(item.getDeliveryAddress());
            order.setAdditionalInformation(item.getAdditionalInformation());
            order.setState(item.getState());
            order.setRegion(item.getRegion());
            order.setPaymentStatus(item.getPaymentStatus());
            order.setPaymentReference(item.getPaymentReference());

            List<OrderHistoryResponse.CartItem> cartItems = item.getCart().stream()
                    .map(cartItem -> {
                        OrderHistoryResponse.CartItem historyCartItem = new OrderHistoryResponse.CartItem();
                        historyCartItem.setProductId(cartItem.getProductId());
                        historyCartItem.setProductName(cartItem.getProductName());
                        historyCartItem.setPrice(cartItem.getPrice());
                        historyCartItem.setImageUrl(cartItem.getImageUrl());
                        historyCartItem.setQuantity(cartItem.getQuantity());
                        historyCartItem.setSubtotal(cartItem.getSubtotal());
                        return historyCartItem;
                    })
                    .collect(Collectors.toList());

            order.setCart(cartItems);
            logger.error("order "+ order);

            return order;
        }).collect(Collectors.toList());
    }


    public List<ProductResponse> getAllComputingProducts() {
        List<Product> computing = productRepository.findAllByCategory(Category.COMPUTING);
        return computing.stream().map(item -> {
            ProductResponse product = new ProductResponse();
            product.setId(item.getId());
            product.setPercentageDiscount(item.getPercentageDiscount());
            product.setSellingPrice(item.getSellingPrice());
            product.setAmountDiscounted(item.getAmountDiscounted());
            product.setProductName(item.getProductName());
            product.setQuantity(item.getQuantity());
            product.setCategory(item.getCategory());
            product.setDescription(item.getDescription());
            product.setImageUrl(item.getImageUrl());
            return product;
        }).collect(Collectors.toList());
    }

    public List<ProductResponse> getAllTravelsProducts() {
        List<Product> travels = productRepository.findAllByCategory(Category.TRAVELS);
        return travels.stream().map(item -> {
            ProductResponse product = new ProductResponse();
            product.setId(item.getId());
            product.setPercentageDiscount(item.getPercentageDiscount());
            product.setSellingPrice(item.getSellingPrice());
            product.setAmountDiscounted(item.getAmountDiscounted());
            product.setProductName(item.getProductName());
            product.setQuantity(item.getQuantity());
            product.setCategory(item.getCategory());
            product.setDescription(item.getDescription());
            product.setImageUrl(item.getImageUrl());
            return product;
        }).collect(Collectors.toList());

    }

    public List<ProductResponse> getAllBabyProducts() {
        List<Product> babyProducts = productRepository.findAllByCategory(Category.BABY);
        return babyProducts.stream().map(item -> {
            ProductResponse product = new ProductResponse();
            product.setId(item.getId());
            product.setPercentageDiscount(item.getPercentageDiscount());
            product.setSellingPrice(item.getSellingPrice());
            product.setAmountDiscounted(item.getAmountDiscounted());
            product.setProductName(item.getProductName());
            product.setQuantity(item.getQuantity());
            product.setCategory(item.getCategory());
            product.setDescription(item.getDescription());
            product.setImageUrl(item.getImageUrl());
            return product;
        }).collect(Collectors.toList());
    }

    public List<ProductResponse> getAllAppliancesProducts() {
        List<Product> appliances = productRepository.findAllByCategory(Category.APPLIANCES);
        return appliances.stream().map(item -> {
            ProductResponse product = new ProductResponse();
            product.setId(item.getId());
            product.setPercentageDiscount(item.getPercentageDiscount());
            product.setSellingPrice(item.getSellingPrice());
            product.setAmountDiscounted(item.getAmountDiscounted());
            product.setProductName(item.getProductName());
            product.setQuantity(item.getQuantity());
            product.setCategory(item.getCategory());
            product.setDescription(item.getDescription());
            product.setImageUrl(item.getImageUrl());
            return product;
        }).collect(Collectors.toList());
    }

    public List<ProductResponse> getAllOtherProduct() {

        List<Product> others = productRepository.findAllByCategory(Category.OTHERS);
        return others.stream().map(item -> {
            ProductResponse product = new ProductResponse();
            product.setId(item.getId());
            product.setPercentageDiscount(item.getPercentageDiscount());
            product.setSellingPrice(item.getSellingPrice());
            product.setAmountDiscounted(item.getAmountDiscounted());
            product.setProductName(item.getProductName());
            product.setQuantity(item.getQuantity());
            product.setCategory(item.getCategory());
            product.setDescription(item.getDescription());
            product.setImageUrl(item.getImageUrl());
            return product;
        }).collect(Collectors.toList());
    }

    public List<ProductResponse> getAllBooksProduct() {

        List<Product> books = productRepository.findAllByCategory(Category.BOOKS);
        return books.stream().map(item -> {
            ProductResponse product = new ProductResponse();
            product.setId(item.getId());
            product.setPercentageDiscount(item.getPercentageDiscount());
            product.setSellingPrice(item.getSellingPrice());
            product.setAmountDiscounted(item.getAmountDiscounted());
            product.setProductName(item.getProductName());
            product.setQuantity(item.getQuantity());
            product.setCategory(item.getCategory());
            product.setDescription(item.getDescription());
            product.setImageUrl(item.getImageUrl());
            return product;
        }).collect(Collectors.toList());
    }

    public List<ProductResponse> getAllElectronicsProduct() {

        List<Product> electronics = productRepository.findAllByCategory(Category.ELECTRONICS);
        return electronics.stream().map(item -> {
            ProductResponse product = new ProductResponse();
            product.setId(item.getId());
            product.setPercentageDiscount(item.getPercentageDiscount());
            product.setSellingPrice(item.getSellingPrice());
            product.setAmountDiscounted(item.getAmountDiscounted());
            product.setProductName(item.getProductName());
            product.setQuantity(item.getQuantity());
            product.setCategory(item.getCategory());
            product.setDescription(item.getDescription());
            product.setImageUrl(item.getImageUrl());
            return product;
        }).collect(Collectors.toList());
    }

    public List<ProductResponse> getAllPhonesAndTabletsProduct() {
        List<Product> phones = productRepository.findAllByCategory(Category.PHONES);
        return phones.stream().map(item -> {
            ProductResponse product = new ProductResponse();
            product.setId(item.getId());
            product.setPercentageDiscount(item.getPercentageDiscount());
            product.setSellingPrice(item.getSellingPrice());
            product.setAmountDiscounted(item.getAmountDiscounted());
            product.setProductName(item.getProductName());
            product.setQuantity(item.getQuantity());
            product.setCategory(item.getCategory());
            product.setDescription(item.getDescription());
            product.setImageUrl(item.getImageUrl());
            return product;
        }).collect(Collectors.toList());
    }

    public Product getProduct(Long productId) {
        return productRepository.findById(productId).orElseThrow(() -> new ProductNotFoundException(productId));
    }


    public List<ProductResponse> getAllProductsWithPagination(int offset, int pageSize, String field) {
        Page<Product> products = productRepository.findAll(PageRequest.of(offset, pageSize).withSort(Sort.by(Sort.Direction.ASC,field)));
        return products.stream().map(item -> {
            ProductResponse product = new ProductResponse();
            product.setId(item.getId());
            product.setPercentageDiscount(item.getPercentageDiscount());
            product.setSellingPrice(item.getSellingPrice());
            product.setAmountDiscounted(item.getAmountDiscounted());
            product.setProductName(item.getProductName());
            product.setQuantity(item.getQuantity());
            product.setCategory(item.getCategory());
            product.setDescription(item.getDescription());
            product.setImageUrl(item.getImageUrl());
            return product;
        }).collect(Collectors.toList());
    }


    public List<ProductResponse> getAllVendorProducts(String userId) {
        List<Product> products = productRepository.findAllByUserId(Long.valueOf(userId));

        return products.stream().map(item -> {
            ProductResponse product = new ProductResponse();
            product.setId(item.getId());
            product.setPercentageDiscount(item.getPercentageDiscount());
            product.setSellingPrice(item.getSellingPrice());
            product.setAmountDiscounted(item.getAmountDiscounted());
            product.setProductName(item.getProductName());
            product.setQuantity(item.getQuantity());
            product.setCategory(item.getCategory());
            product.setDescription(item.getDescription());
            product.setImageUrl(item.getImageUrl());
            return product;
        }).collect(Collectors.toList());
    }

 
    public List<OrderedProductsByUserFromVendor> getMyProductsOrderedByUsers(Long id) {
        List<Product> productsCreatedByVendor = productRepository.findAllByUserId(id);
        List<Long> productIds = productsCreatedByVendor.stream().map(Product::getId).toList();

        List<CartCheckout.CartItem> cartItems = cartCheckoutRepository.findCartItemsByProductIds(productIds);

        Map<Long, List<CartCheckout.CartItem>> cartItemsByCartCheckoutId = cartItems.stream()
                .collect(Collectors.groupingBy(CartCheckout.CartItem::getCartCheckoutId));


        List<OrderedProductsByUserFromVendor> result = new ArrayList<>();

        for (Map.Entry<Long, List<CartCheckout.CartItem>> entry : cartItemsByCartCheckoutId.entrySet()) {
            Long cartCheckoutId = entry.getKey();
            List<CartCheckout.CartItem> cartItemsForCheckout = entry.getValue();

            Optional<CartCheckout> optionalCartCheckout = cartCheckoutRepository.findById(cartCheckoutId);
            if (optionalCartCheckout.isPresent()) {
                CartCheckout cartCheckout = optionalCartCheckout.get();

                OrderedProductsByUserFromVendor orderedProductsByUserFromVendor = new OrderedProductsByUserFromVendor();

                orderedProductsByUserFromVendor.setId(cartCheckout.getId());
                orderedProductsByUserFromVendor.setFirstName(cartCheckout.getFirstName());
                orderedProductsByUserFromVendor.setLastName(cartCheckout.getLastName());
                orderedProductsByUserFromVendor.setPhoneNumber(cartCheckout.getPhoneNumber());
                orderedProductsByUserFromVendor.setAlternativePhoneNumber(cartCheckout.getAlternativePhoneNumber());
                orderedProductsByUserFromVendor.setDeliveryAddress(cartCheckout.getDeliveryAddress());
                orderedProductsByUserFromVendor.setAdditionalInformation(cartCheckout.getAdditionalInformation());
                orderedProductsByUserFromVendor.setRegion(cartCheckout.getRegion());
                orderedProductsByUserFromVendor.setState(cartCheckout.getState());
                orderedProductsByUserFromVendor.setTotal(cartCheckout.getPrice());
                orderedProductsByUserFromVendor.setQuantity(cartCheckout.getQuantity());
                orderedProductsByUserFromVendor.setPaymentStatus(cartCheckout.getPaymentStatus());
                orderedProductsByUserFromVendor.setPaymentReference(cartCheckout.getPaymentReference());
                orderedProductsByUserFromVendor.setPurchaseDate(cartCheckout.getPurchaseDate());

                List<OrderedProductsByUserFromVendor.CartItem> orderedCartItems = cartItemsForCheckout.stream()
                        .map(cartItem -> new OrderedProductsByUserFromVendor.CartItem(
                                cartItem.getProductId(),
                                cartItem.getProductName(),
                                cartItem.getPrice(),
                                cartItem.getImageUrl(),
                                cartItem.getQuantity(),
                                cartItem.getSubtotal()
                        ))
                        .collect(Collectors.toList());

                orderedProductsByUserFromVendor.setCart(orderedCartItems);

                result.add(orderedProductsByUserFromVendor);
            }
        }

        return result;
    }

}
