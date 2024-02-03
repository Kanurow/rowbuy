package com.rowland.engineering.ecommerce.service;

import com.rowland.engineering.ecommerce.dto.*;
import com.rowland.engineering.ecommerce.exception.BadRequestException;
import com.rowland.engineering.ecommerce.exception.InsufficientProductQuantityException;
import com.rowland.engineering.ecommerce.exception.ProductNotFoundException;
import com.rowland.engineering.ecommerce.exception.ResourceNotFoundException;
import com.rowland.engineering.ecommerce.model.*;
import com.rowland.engineering.ecommerce.repository.CartCheckoutRepository;
import com.rowland.engineering.ecommerce.repository.ProductRepository;
import com.rowland.engineering.ecommerce.repository.ShoppingCartRepository;
import com.rowland.engineering.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;




@SpringBootTest
class ProductServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceTest.class);
    @Mock
    private ProductRepository productRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CartCheckoutRepository cartCheckoutRepository;

    @Mock
    private ShoppingCartRepository shoppingCartRepository;

    @InjectMocks
    private ProductService productService;


    @Test
    @DisplayName("Ensures we successfully adds a product in our shopping cart")
    void addToCart() {
        // Arrange
        Long productId = 1L;
        Long userId = 2L;

        Product mockProduct = new Product();
        mockProduct.setId(productId);
        mockProduct.setProductName("HP Laptop");

        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setFirstName("Rowland");

        when(productRepository.findById(productId)).thenReturn(Optional.of(mockProduct));
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(shoppingCartRepository.findByProductAndUser(mockProduct, mockUser)).thenReturn(Optional.empty());

        ArgumentCaptor<ShoppingCart> shoppingCartArgumentCaptor = ArgumentCaptor.forClass(ShoppingCart.class);

        // Act
        ApiResponse response = productService.addToCart(productId, userId);

        // Assert
        assertEquals("Product Added to cart", response.getMessage());
        verify(shoppingCartRepository).save(shoppingCartArgumentCaptor.capture());
        ShoppingCart savedCart = shoppingCartArgumentCaptor.getValue();

        assertEquals(mockProduct, savedCart.getProduct());
        assertEquals(mockUser, savedCart.getUser());
    }


    @Test
    @DisplayName("Ensures we do not add the same product twice in our shopping cart")
    void addToCart_ProductAlreadyInCartAndExpectedExceptionsGetsThrown() {
        // Arrange
        Long productId = 1L;
        Long userId = 2L;

        Product mockProduct = new Product();
        mockProduct.setId(productId);

        User mockUser = new User();
        mockUser.setId(userId);

        ShoppingCart existingCartEntry = new ShoppingCart();
        existingCartEntry.setProduct(mockProduct);
        existingCartEntry.setUser(mockUser);

        //Act
        when(productRepository.findById(productId)).thenReturn(Optional.of(mockProduct));
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(shoppingCartRepository.findByProductAndUser(mockProduct, mockUser)).thenReturn(Optional.of(existingCartEntry));

        //Assert
        BadRequestException badRequestException = assertThrows(BadRequestException.class, () -> productService.addToCart(productId, userId));
        assertEquals("Sorry! You have already added this product to your shopping cart", badRequestException.getMessage());

        ResourceNotFoundException invalidUser = assertThrows(ResourceNotFoundException.class, () -> productService.addToCart(productId, 999L));
        assertEquals(String.format("%s not found with %s : '%s'", invalidUser.getResourceName(), invalidUser.getFieldName(), invalidUser.getFieldValue()), invalidUser.getMessage());

        ResourceNotFoundException invalidProduct = assertThrows(ResourceNotFoundException.class, () -> productService.addToCart(999L, userId));
        assertEquals(String.format("%s not found with %s : '%s'", invalidProduct.getResourceName(), invalidProduct.getFieldName(), invalidProduct.getFieldValue()), invalidProduct.getMessage());

        verify(shoppingCartRepository, never()).save(any(ShoppingCart.class));
    }

    @Test
    @DisplayName("Returns all shopping cart items by a given user")
    void getUserCart() {
        //Arrange
        User mockUser = User.builder()
                .id(1L)
                .username("row")
                .firstName("Rowland")
                .mobile("+2348143358911")
                .build();

        Product mockProduct1 = Product.builder()
                .userId(mockUser.getId())
                .id(4L)
                .category(Category.COMPUTING)
                .build();

        Product mockProduct2 = Product.builder()
                .userId(mockUser.getId())
                .id(5L)
                .category(Category.BABY)
                .build();

        List<ShoppingCart> mockUserShoppingCart = new ArrayList<>();

        ShoppingCart mockCart1 = ShoppingCart.builder()
                .id(3L)
                .user(mockUser)
                .product(mockProduct1)
                .build();

        ShoppingCart mockCart2 = ShoppingCart.builder()
                .id(4L)
                .user(mockUser)
                .product(mockProduct2)
                .build();
        mockUserShoppingCart.add(mockCart1);
        mockUserShoppingCart.add(mockCart2);

        when(shoppingCartRepository.findAllByUserId(mockUser.getId())).thenReturn(mockUserShoppingCart);
        //Act
        List<ShoppingCart> userCart = productService.getUserCart(mockUser.getId());

        //Assert
        assertEquals(mockUserShoppingCart.size(), userCart.size());

        for (int j = 0; j < mockUserShoppingCart.size(); j++) {
            ShoppingCart shoppingCart = mockUserShoppingCart.get(j);
            ShoppingCart result = userCart.get(j);

            assertEquals(shoppingCart.getProduct(), result.getProduct());
            assertEquals(shoppingCart.getUser(), result.getUser());
            assertEquals(shoppingCart.getId(), result.getId());
        }
    }

    @Test
    @DisplayName("Successfully checks out user cart items after payment using paystack")
    void checkoutCart() {
        // Arrange
        Long userId = 123L;
        List<CartCheckoutRequest.CartItem> itemList = new ArrayList<>();

        CartCheckoutRequest.CartItem cartItem1 = CartCheckoutRequest.CartItem.builder()
                .productId(2L)
                .productName("Apple")
                .price(89.7)
                .imageUrl("www.img.com")
                .quantity(20)
                .subtotal(45.4)
                .build();

        CartCheckoutRequest.CartItem cartItem2 = CartCheckoutRequest.CartItem.builder()
                .productId(2L)
                .productName("Apple")
                .price(89.7)
                .imageUrl("www.img.com")
                .quantity(20)
                .subtotal(45.4)
                .build();


        itemList.add(cartItem1);
        itemList.add(cartItem2);
        CartCheckoutRequest cartCheckoutRequest = CartCheckoutRequest.builder()
                .firstName("Rowland")
                .lastName("Kanu")
                .deliveryAddress("Plot 11 Kubwa")
                .quantity(890)
                .alternativePhoneNumber("8766")
                .phoneNumber("0988888")
                .total(4500)
                .state("Abuja")
                .region("North")
                .userId(userId)
                .paystackApproved("Approved")
                .paystackReference("paymentReferenceCode")
                .cart(itemList)
                .build();


        Product product = new Product();
        product.setId(2L);
        product.setProductName("Apple");
        product.setCategory(Category.BABY);
        product.setImageUrl("www.img.com");
        product.setQuantity(50);

        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        List<ShoppingCart> shoppingCartItems = new ArrayList<>();
        User mockUser = User.builder()
                .id(1L)
                .firstName("Rowland")
                .build();
        Product mockProduct = Product.builder()
                .id(2L)
                .productName("Rice")
                .build();
        ShoppingCart cart = ShoppingCart.builder()
                .user(mockUser)
                .product(mockProduct)
                .build();
        shoppingCartItems.add(cart);

        // Act
        when(shoppingCartRepository.findAllByUserId(userId)).thenReturn(shoppingCartItems);

        ApiResponse response = productService.checkoutCart(cartCheckoutRequest, userId);

        // Assert
        verify(cartCheckoutRepository, times(1)).save(any(CartCheckout.class));
        verify(shoppingCartRepository, times(1)).deleteAll(shoppingCartItems);
        verify(productRepository, times(itemList.size())).save(any((Product.class)));

        assertTrue(response.getSuccess());
        assertEquals("Checked Out", response.getMessage());
    }

    @Test
    @DisplayName("Should not check out cart as exceptions gets thrown")
    void checkoutCart_badScenario() {
        // Arrange
        Long userId = 123L;
        CartCheckoutRequest cartCheckoutRequestInsufficientQuantityException = cartCheckoutRequestException(3L, 50, userId);
        CartCheckoutRequest cartCheckoutRequestProductNotFountException = cartCheckoutRequestException(999L, 50, userId);

        Product product = new Product();
        product.setId(3L);
        product.setProductName("Apple");
        product.setCategory(Category.BABY);
        product.setImageUrl("www.img.com");
        product.setQuantity(30);

        //Act
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        List<ShoppingCart> shoppingCartItems = new ArrayList<>();
        when(shoppingCartRepository.findAllByUserId(userId)).thenReturn(shoppingCartItems);

        //Assert
        InsufficientProductQuantityException insufficientProductQuantityException = assertThrows(InsufficientProductQuantityException.class, () -> productService.checkoutCart(cartCheckoutRequestInsufficientQuantityException, userId));
        assertEquals(insufficientProductQuantityException.getProductId(), 3);

        ProductNotFoundException productNotFoundException = assertThrows(ProductNotFoundException.class, () -> productService.checkoutCart(cartCheckoutRequestProductNotFountException, userId));
        assertEquals(productNotFoundException.getProductId(), 999);

        verify(cartCheckoutRepository, times(0)).save(any(CartCheckout.class));
        verify(shoppingCartRepository, times(0)).deleteAll(shoppingCartItems);
        verify(productRepository, times(0)).save(product);
    }

    private static CartCheckoutRequest cartCheckoutRequestException(long productId, int quantity, Long userId) {
        List<CartCheckoutRequest.CartItem> itemList = new ArrayList<>();

        CartCheckoutRequest.CartItem cartItem1 = CartCheckoutRequest.CartItem.builder()
                .productId(productId)
                .productName("Apple")
                .price(89.7)
                .imageUrl("www.img.com")
                .quantity(quantity)
                .subtotal(45.4)
                .build();

        CartCheckoutRequest.CartItem cartItem2 = CartCheckoutRequest.CartItem.builder()
                .productId(productId)
                .productName("Apple")
                .price(89.7)
                .imageUrl("www.img.com")
                .quantity(quantity)
                .subtotal(45.4)
                .build();


        itemList.add(cartItem1);
        itemList.add(cartItem2);
        CartCheckoutRequest cartCheckoutRequest = CartCheckoutRequest.builder()
                .firstName("Rowland")
                .lastName("Kanu")
                .deliveryAddress("Plot 11 Kubwa")
                .quantity(890)
                .alternativePhoneNumber("8766")
                .phoneNumber("0988888")
                .total(4500)
                .state("Abuja")
                .region("North")
                .userId(userId)
                .paystackApproved("Approved")
                .paystackReference("paymentReferenceCode")
                .cart(itemList)
                .build();
        return cartCheckoutRequest;
    }



    @Test
    void getCheckedOutCart() {
        // Arrange
        Long userId = 1L;

        List<CartCheckout> expectedCartCheckouts = new ArrayList<>();
        expectedCartCheckouts.add(new CartCheckout());
        expectedCartCheckouts.add(new CartCheckout());

        // Act
        when(cartCheckoutRepository.findByUserId(userId)).thenReturn(expectedCartCheckouts);

        List<CartCheckout> actualCartCheckouts = productService.getCheckedOutCart(userId);

        // Assert
        assertEquals(expectedCartCheckouts.size(), actualCartCheckouts.size());
        assertTrue(expectedCartCheckouts.containsAll(actualCartCheckouts));
        assertTrue(actualCartCheckouts.containsAll(expectedCartCheckouts));

        verify(cartCheckoutRepository, times(1)).findByUserId(userId);
    }


    @Test
    @DisplayName("Returns list of all products")
    void getAllProducts() {
        // Arrange
        List<Product> mockProducts = new ArrayList<>();
        Product product1 = Product.builder()
                .id(1L)
                .productName("MicroEconomics")
                .description("TextBook")
                .category(Category.BOOKS)
                .sellingPrice(12.0)
                .percentageDiscount(10)
                .build();
        Product product2 = Product.builder()
                .id(2L)
                .productName("HP Laptop")
                .description("Productivity")
                .category(Category.ELECTRONICS)
                .sellingPrice(100.0)
                .percentageDiscount(20)
                .build();

        mockProducts.add(product1);
        mockProducts.add(product2);

        when(productRepository.findAll()).thenReturn(mockProducts);

        // Act
        List<ProductResponse> actualProductResponses = productService.getAllProducts();

        // Assert
        assertNotNull(actualProductResponses);
        assertEquals(mockProducts.size(), actualProductResponses.size());

        verify(productRepository, times(1)).findAll();

        for (int i = 0; i < mockProducts.size(); i++) {
            ProductResponse actualResponse = actualProductResponses.get(i);
            Product mockProduct = mockProducts.get(i);

            assertEquals(mockProduct.getId(), actualResponse.getId());
            assertEquals(mockProduct.getProductName(), actualResponse.getProductName());
            assertEquals(mockProduct.getSellingPrice(), actualResponse.getSellingPrice());
            assertEquals(mockProduct.getCategory(), actualResponse.getCategory());
            assertEquals(mockProduct.getDescription(), actualResponse.getDescription());
            assertEquals(mockProduct.getPercentageDiscount(), actualResponse.getPercentageDiscount());
        }
    }



    @Test
    void getAllSupermarketProducts() {
        //Arrange
        List<Product> mockSupermarketProducts = new ArrayList<>();
        Product product1 = Product.builder()
                .id(1L)
                .quantity(30)
                .percentageDiscount(20)
                .sellingPrice(340.3)
                .productName("Rolex")
                .description("Row Luxury")
                .category(Category.SUPERMARKET)
                .build();
        Product product2 = Product.builder()
                .id(2L)
                .quantity(30)
                .percentageDiscount(50)
                .sellingPrice(355.6)
                .productName("Rolex")
                .description("Row Luxury")
                .category(Category.SUPERMARKET)
                .build();

        mockSupermarketProducts.add(product2);
        mockSupermarketProducts.add(product1);

        //Act
        when(productRepository.findAllByCategory(Category.SUPERMARKET)).thenReturn(mockSupermarketProducts);
        List<ProductResponse> allSupermarketProducts = productService.getAllSupermarketProducts();

        //Assert
        verify(productRepository, times(1)).findAllByCategory(Category.SUPERMARKET);
        for (int i = 0; i < mockSupermarketProducts.size(); i++) {
            ProductResponse actualResponse = allSupermarketProducts.get(i);
            Product mockProduct = mockSupermarketProducts.get(i);

            assertEquals(mockProduct.getId(), actualResponse.getId());
            assertEquals(mockProduct.getProductName(), actualResponse.getProductName());
            assertEquals(mockProduct.getSellingPrice(), actualResponse.getSellingPrice());
            assertEquals(mockProduct.getCategory(), actualResponse.getCategory());
            assertEquals(mockProduct.getDescription(), actualResponse.getDescription());
            assertEquals(mockProduct.getPercentageDiscount(), actualResponse.getPercentageDiscount());
        }
    }



    @Test
    @DisplayName("Successfully removes an item from users shopping cart")
    void removeFromCart() {
        //Arrange
        List<ShoppingCart> shoppingCartList = new ArrayList<>();

        Product product1 = Product.builder()
                .id(1L)
                .productName("Shoe")
                .quantity(3)
                .category(Category.BABY)
                .description("foot ware")
                .build();

        Product product2 = Product.builder()
                .id(2L)
                .productName("Bag")
                .quantity(34)
                .category(Category.TRAVELS)
                .description("school bag")
                .build();

        User mockUser = User.builder()
                .id(1L)
                .firstName("Rowland")
                .lastName("Kanu")
                .mobile("8143358911")
                .build();

        ShoppingCart cart1 = ShoppingCart.builder()
                .id(1L)
                .product(product1)
                .user(mockUser)
                .build();
        ShoppingCart cart2 = ShoppingCart.builder()
                .id(2L)
                .product(product2)
                .user(mockUser)
                .build();

        shoppingCartList.add(cart1);
        shoppingCartList.add(cart2);

        //Act
        doAnswer(invocation -> shoppingCartList.removeIf(cart -> cart.getId().equals(cart1.getId())))
                .when(shoppingCartRepository).deleteById(cart1.getId());

        ApiResponse apiResponse = productService.removeFromCart(cart1.getId());

        //Assert
        assertEquals(1, shoppingCartList.size());
        assertEquals("Item removed from cart", apiResponse.getMessage());
    }

    @Test
    @DisplayName("Gets a list of all checked out items by a given user")
    void getUserOrderHistory() {
        //Arrange
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("Flames");
        mockUser.setFirstName("Rowland");

        List<CartCheckout.CartItem> itemList = new ArrayList<>();

        CartCheckout.CartItem lineItem1 = new CartCheckout.CartItem();
        lineItem1.setProductId(1L);
        lineItem1.setProductName("Product1");
        lineItem1.setPrice(450.2);
        lineItem1.setImageUrl("www.product1.com");
        lineItem1.setQuantity(5);
        lineItem1.setSubtotal(30.0);

        CartCheckout.CartItem lineItem2 = new CartCheckout.CartItem();
        lineItem2.setProductId(2L);
        lineItem2.setProductName("Product2");
        lineItem2.setPrice(45.0);
        lineItem2.setImageUrl("www.product2.com");
        lineItem2.setQuantity(6);
        lineItem2.setSubtotal(38.80);

        itemList.add(lineItem1);
        itemList.add(lineItem2);

        List<CartCheckout> cartCheckoutList = new ArrayList<>();

        CartCheckout mockCheckout = new CartCheckout();
        mockCheckout.setId(1L);
        mockCheckout.setFirstName(mockUser.getFirstName());
        mockCheckout.setLastName(mockUser.getLastName());
        mockCheckout.setPhoneNumber("08143358911");
        mockCheckout.setUserId(mockUser.getId());
        mockCheckout.setRegion("Abuja");
        mockCheckout.setPaymentStatus(PaymentStatus.APPROVED);
        mockCheckout.setPurchaseDate(LocalDateTime.now());
        mockCheckout.setCart(itemList);

        cartCheckoutList.add(mockCheckout);

        //Act
        when(cartCheckoutRepository.findByUserId(mockUser.getId())).thenReturn(cartCheckoutList);

        List<OrderHistoryResponse> result = productService.getUserOrderHistory(mockUser.getId());

        //Assert
        assertEquals(cartCheckoutList.size(), result.size());

        for (int i = 0; i < cartCheckoutList.size(); i++) {
            CartCheckout mockCartItem = cartCheckoutList.get(i);
            OrderHistoryResponse orderResponse = result.get(i);

            assertEquals(mockCartItem.getId(), orderResponse.getId());
            assertEquals(mockCartItem.getQuantity(), orderResponse.getQuantity());
            assertEquals(mockCartItem.getFirstName(), orderResponse.getFirstName());
            assertEquals(mockCartItem.getLastName(), orderResponse.getLastName());
            assertEquals(mockCartItem.getPhoneNumber(), orderResponse.getPhoneNumber());
            assertEquals(mockCartItem.getPaymentStatus(), orderResponse.getPaymentStatus());

            List<OrderHistoryResponse.CartItem> orderCartItems = orderResponse.getCart();
            List<CartCheckout.CartItem> mockCartItems = mockCartItem.getCart();

            assertEquals(mockCartItems.size(), orderCartItems.size());

            for (int j = 0; j < mockCartItems.size(); j++) {
                CartCheckout.CartItem mockCartItemDetails = mockCartItems.get(j);
                OrderHistoryResponse.CartItem orderCartItemDetails = orderCartItems.get(j);

                assertEquals(mockCartItemDetails.getProductId(), orderCartItemDetails.getProductId());
                assertEquals(mockCartItemDetails.getProductName(), orderCartItemDetails.getProductName());
                assertEquals(mockCartItemDetails.getImageUrl(), orderCartItemDetails.getImageUrl());
                assertEquals(mockCartItemDetails.getQuantity(), orderCartItemDetails.getQuantity());
                assertEquals(mockCartItemDetails.getPrice(), orderCartItemDetails.getPrice());
                assertEquals(mockCartItemDetails.getSubtotal(), orderCartItemDetails.getSubtotal());
            }
        }

    }
    @Test
    void getAllComputingProducts() {
        //Arrange
        List<Product> mockProducts = new ArrayList<>();
        Product computingProduct1 = new Product();
        computingProduct1.setId(1L);
        computingProduct1.setPercentageDiscount(50);
        computingProduct1.setSellingPrice(500.0);
        computingProduct1.setAmountDiscounted(250.0);
        computingProduct1.setProductName("Laptop1");
        computingProduct1.setQuantity(5);
        computingProduct1.setCategory(Category.COMPUTING);
        computingProduct1.setDescription("16GB RAM");
        computingProduct1.setImageUrl("Image1.com");

        Product computingProduct2 = new Product();
        computingProduct2.setId(3L);
        computingProduct2.setPercentageDiscount(50);
        computingProduct2.setSellingPrice(500.0);
        computingProduct2.setAmountDiscounted(250.0);
        computingProduct2.setProductName("Laptop2");
        computingProduct2.setQuantity(55);
        computingProduct2.setCategory(Category.COMPUTING);
        computingProduct2.setDescription("8GB RAM");
        computingProduct2.setImageUrl("Image2.com");

        mockProducts.add(computingProduct1);
        mockProducts.add(computingProduct2);

        //Act
        when(productRepository.findAllByCategory(Category.COMPUTING)).thenReturn(mockProducts);

        List<ProductResponse> result = productService.getAllComputingProducts();

        //Assert
        assertEquals(mockProducts.size(), result.size());

        for (int i = 0; i < mockProducts.size(); i++) {
            Product mockProduct = mockProducts.get(i);
            ProductResponse productResponse = result.get(i);

            assertEquals(mockProduct.getId(), productResponse.getId());
            assertEquals(mockProduct.getPercentageDiscount(), productResponse.getPercentageDiscount());
            assertEquals(mockProduct.getSellingPrice(), productResponse.getSellingPrice());
            assertEquals(mockProduct.getAmountDiscounted(), productResponse.getAmountDiscounted());
            assertEquals(mockProduct.getProductName(), productResponse.getProductName());
            assertEquals(mockProduct.getQuantity(), productResponse.getQuantity());
            assertEquals(mockProduct.getCategory(), productResponse.getCategory());
            assertEquals(mockProduct.getDescription(), productResponse.getDescription());
            assertEquals(mockProduct.getImageUrl(), productResponse.getImageUrl());
        }

    }


    @Test
    @DisplayName("Should return list of all travel products as product response")
    void getAllTravelsProducts() {
        // Arrange
        List<Product> mockProducts = new ArrayList<>();
        Product firstAidKit = new Product();
        firstAidKit.setId(1L);
        firstAidKit.setPercentageDiscount(30);
        firstAidKit.setSellingPrice(100.0);
        firstAidKit.setAmountDiscounted(30.0);
        firstAidKit.setProductName("First Aid Kit");
        firstAidKit.setQuantity(5);
        firstAidKit.setCategory(Category.TRAVELS);
        firstAidKit.setDescription("Health Kit ");
        firstAidKit.setImageUrl("Image1.com");

        Product campTravelingBag = new Product();
        campTravelingBag.setId(2L);
        campTravelingBag.setPercentageDiscount(50);
        campTravelingBag.setSellingPrice(200.0);
        campTravelingBag.setAmountDiscounted(100.0);
        campTravelingBag.setProductName("Travelling Bag");
        campTravelingBag.setQuantity(15);
        campTravelingBag.setCategory(Category.TRAVELS);
        campTravelingBag.setDescription("TravellingBag");
        campTravelingBag.setImageUrl("Image2.com");


        mockProducts.add(firstAidKit);
        mockProducts.add(campTravelingBag);

        when(productRepository.findAllByCategory(Category.TRAVELS)).thenReturn(mockProducts);

        // Act
        List<ProductResponse> result = productService.getAllTravelsProducts();
        logger.info(result.toString());

        // Assert
        assertEquals(mockProducts.size(), result.size());

        for (int i = 0; i < mockProducts.size(); i++) {
            Product mockProduct = mockProducts.get(i);
            ProductResponse productResponse = result.get(i);

            assertEquals(mockProduct.getId(), productResponse.getId());
            assertEquals(mockProduct.getPercentageDiscount(), productResponse.getPercentageDiscount());
            assertEquals(mockProduct.getSellingPrice(), productResponse.getSellingPrice());
            assertEquals(mockProduct.getAmountDiscounted(), productResponse.getAmountDiscounted());
            assertEquals(mockProduct.getProductName(), productResponse.getProductName());
            assertEquals(mockProduct.getQuantity(), productResponse.getQuantity());
            assertEquals(mockProduct.getCategory(), productResponse.getCategory());
            assertEquals(mockProduct.getDescription(), productResponse.getDescription());
            assertEquals(mockProduct.getImageUrl(), productResponse.getImageUrl());
        }
    }



    @Test
    void getAllBabyProducts() {

        // Arrange
        List<Product> mockProducts = new ArrayList<>();
        Product babyCream = new Product();
        babyCream.setId(1L);
        babyCream.setPercentageDiscount(50);
        babyCream.setSellingPrice(1000.0);
        babyCream.setAmountDiscounted(500.0);
        babyCream.setProductName("Cream for babies");
        babyCream.setQuantity(25);
        babyCream.setCategory(Category.BABY);
        babyCream.setDescription("Ages between 1 to 5");
        babyCream.setImageUrl("Image1.com");

        Product babyFood = new Product();
        babyFood.setId(2L);
        babyFood.setPercentageDiscount(40);
        babyFood.setSellingPrice(200.0);
        babyFood.setAmountDiscounted(100.0);
        babyFood.setProductName("Travelling Bag");
        babyFood.setQuantity(15);
        babyFood.setCategory(Category.BABY);
        babyFood.setDescription("TravellingBag");
        babyFood.setImageUrl("Image2.com");

        mockProducts.add(babyFood);
        mockProducts.add(babyCream);

        when(productRepository.findAllByCategory(Category.BABY)).thenReturn(mockProducts);

        // Act
        List<ProductResponse> result = productService.getAllBabyProducts();
        logger.info(result.toString());

        // Assert
        assertEquals(mockProducts.size(), result.size());

        for (int i = 0; i < mockProducts.size(); i++) {
            Product mockProduct = mockProducts.get(i);
            ProductResponse productResponse = result.get(i);

            assertEquals(mockProduct.getId(), productResponse.getId());
            assertEquals(mockProduct.getPercentageDiscount(), productResponse.getPercentageDiscount());
            assertEquals(mockProduct.getSellingPrice(), productResponse.getSellingPrice());
            assertEquals(mockProduct.getAmountDiscounted(), productResponse.getAmountDiscounted());
            assertEquals(mockProduct.getProductName(), productResponse.getProductName());
            assertEquals(mockProduct.getQuantity(), productResponse.getQuantity());
            assertEquals(mockProduct.getCategory(), productResponse.getCategory());
            assertEquals(mockProduct.getDescription(), productResponse.getDescription());
            assertEquals(mockProduct.getImageUrl(), productResponse.getImageUrl());
        }
    }

    @Test
    @DisplayName("Should return all products under appliances category")
    void getAllAppliancesProducts() {

        // Arrange
        List<Product> mockProducts = new ArrayList<>();
        Product appliances1 = new Product();
        appliances1.setId(1L);
        appliances1.setPercentageDiscount(50);
        appliances1.setSellingPrice(1000.0);
        appliances1.setAmountDiscounted(500.0);
        appliances1.setProductName("Microwave");
        appliances1.setQuantity(25);
        appliances1.setCategory(Category.APPLIANCES);
        appliances1.setDescription("Kitchen appliance");
        appliances1.setImageUrl("Image1.com");

        Product appliances2 = new Product();
        appliances2.setId(2L);
        appliances2.setPercentageDiscount(40);
        appliances2.setSellingPrice(200.0);
        appliances2.setAmountDiscounted(100.0);
        appliances2.setProductName("Slicing Knife");
        appliances2.setQuantity(15);
        appliances2.setCategory(Category.APPLIANCES);
        appliances2.setDescription("Kitchen Knives");
        appliances2.setImageUrl("Image2.com");

        mockProducts.add(appliances2);
        mockProducts.add(appliances1);

        //Act
        when(productRepository.findAllByCategory(Category.APPLIANCES)).thenReturn(mockProducts);

        List<ProductResponse> allAppliancesProducts = productService.getAllAppliancesProducts();

        //Assert
        verify(productRepository, times(1)).findAllByCategory(Category.APPLIANCES);

        for (int i = 0; i < mockProducts.size(); i++) {
            Product mockedProducts = mockProducts.get(i);
            ProductResponse response = allAppliancesProducts.get(i);

            assertEquals(mockedProducts.getId(), response.getId());
            assertEquals(mockedProducts.getPercentageDiscount(), response.getPercentageDiscount());
            assertEquals(mockedProducts.getQuantity(), response.getQuantity());
            assertEquals(mockedProducts.getProductName(), response.getProductName());
            assertEquals(mockedProducts.getCategory(), response.getCategory());
            assertEquals(mockedProducts.getImageUrl(), response.getImageUrl());
        }

    }

    @Test
    @DisplayName("Should return all products under other category")
    void getAllOtherProduct() {
        // Arrange
        List<Product> mockProducts = new ArrayList<>();
        Product others1 = new Product();
        others1.setId(1L);
        others1.setPercentageDiscount(50);
        others1.setSellingPrice(1000.0);
        others1.setAmountDiscounted(500.0);
        others1.setProductName("Face mask");
        others1.setQuantity(25);
        others1.setCategory(Category.OTHERS);
        others1.setDescription("Health kit");
        others1.setImageUrl("Image1.com");

        Product others2 = new Product();
        others2.setId(2L);
        others2.setPercentageDiscount(40);
        others2.setSellingPrice(200.0);
        others2.setAmountDiscounted(100.0);
        others2.setProductName("Slicing Knife");
        others2.setQuantity(15);
        others2.setCategory(Category.OTHERS);
        others2.setDescription("Kitchen Knives");
        others2.setImageUrl("Image2.com");

        mockProducts.add(others1);
        mockProducts.add(others2);

        //Act
        when(productRepository.findAllByCategory(Category.OTHERS)).thenReturn(mockProducts);

        List<ProductResponse> allOtherProduct = productService.getAllOtherProduct();

        //Assert
        verify(productRepository, times(1)).findAllByCategory(Category.OTHERS);

        for (int i = 0; i < allOtherProduct.size(); i++) {
            Product mockedProducts = mockProducts.get(i);
            ProductResponse response = allOtherProduct.get(i);

            assertEquals(mockedProducts.getId(), response.getId());
            assertEquals(mockedProducts.getPercentageDiscount(), response.getPercentageDiscount());
            assertEquals(mockedProducts.getQuantity(), response.getQuantity());
            assertEquals(mockedProducts.getProductName(), response.getProductName());
            assertEquals(mockedProducts.getCategory(), response.getCategory());
            assertEquals(mockedProducts.getImageUrl(), response.getImageUrl());
        }
    }


    @Test
    @DisplayName("Should return all products book other category")
    void getAllBooksProduct() {

        // Arrange
        List<Product> mockProducts = new ArrayList<>();
        Product books1 = new Product();
        books1.setId(1L);
        books1.setPercentageDiscount(50);
        books1.setSellingPrice(1000.0);
        books1.setAmountDiscounted(500.0);
        books1.setProductName("Rich Dad Poor Dad");
        books1.setQuantity(25);
        books1.setCategory(Category.BOOKS);
        books1.setDescription("Wealth Creation");
        books1.setImageUrl("Image1.com");

        Product books2 = new Product();
        books2.setId(2L);
        books2.setPercentageDiscount(40);
        books2.setSellingPrice(200.0);
        books2.setAmountDiscounted(100.0);
        books2.setProductName("Know your worth");
        books2.setQuantity(15);
        books2.setCategory(Category.BOOKS);
        books2.setDescription("Self Development");
        books2.setImageUrl("Image2.com");

        mockProducts.add(books1);
        mockProducts.add(books2);

        //Act
        when(productRepository.findAllByCategory(Category.BOOKS)).thenReturn(mockProducts);

        List<ProductResponse> allBooksProducts = productService.getAllBooksProduct();

        //Assert
        verify(productRepository, times(1)).findAllByCategory(Category.BOOKS);

        assertEquals(allBooksProducts.size(), mockProducts().size());
        for (int i = 0; i < mockProducts.size(); i++) {
            Product mockedProducts = mockProducts.get(i);
            ProductResponse response = allBooksProducts.get(i);

            assertEquals(mockedProducts.getId(), response.getId());
            assertEquals(mockedProducts.getPercentageDiscount(), response.getPercentageDiscount());
            assertEquals(mockedProducts.getQuantity(), response.getQuantity());
            assertEquals(mockedProducts.getProductName(), response.getProductName());
            assertEquals(mockedProducts.getCategory(), response.getCategory());
            assertEquals(mockedProducts.getImageUrl(), response.getImageUrl());
        }
    }

    @Test
    @DisplayName("Should return all products under electronics category")
    void getAllElectronicsProduct() {
        // Arrange
        List<Product> mockProducts = new ArrayList<>();
        Product electronics1 = new Product();
        electronics1.setId(1L);
        electronics1.setPercentageDiscount(50);
        electronics1.setSellingPrice(1000.0);
        electronics1.setAmountDiscounted(500.0);
        electronics1.setProductName("TV set");
        electronics1.setQuantity(25);
        electronics1.setCategory(Category.ELECTRONICS);
        electronics1.setDescription("Entertainment");
        electronics1.setImageUrl("Image1.com");

        Product electronics2 = new Product();
        electronics2.setId(2L);
        electronics2.setPercentageDiscount(20);
        electronics2.setSellingPrice(400.0);
        electronics2.setAmountDiscounted(300.0);
        electronics2.setProductName("Hot plates");
        electronics2.setQuantity(125);
        electronics2.setCategory(Category.ELECTRONICS);
        electronics2.setDescription("household electronics");
        electronics2.setImageUrl("Image2.com");

        mockProducts.add(electronics1);
        mockProducts.add(electronics2);

        //Act
        when(productRepository.findAllByCategory(Category.ELECTRONICS)).thenReturn(mockProducts);

        List<ProductResponse> allElectronicsProducts = productService.getAllElectronicsProduct();

        //Assert
        verify(productRepository, times(1)).findAllByCategory(Category.ELECTRONICS);

        assertEquals(allElectronicsProducts.size(), mockProducts().size());
        for (int i = 0; i < mockProducts.size(); i++) {
            Product mockedProducts = mockProducts.get(i);
            ProductResponse response = allElectronicsProducts.get(i);

            assertEquals(mockedProducts.getId(), response.getId());
            assertEquals(mockedProducts.getPercentageDiscount(), response.getPercentageDiscount());
            assertEquals(mockedProducts.getQuantity(), response.getQuantity());
            assertEquals(mockedProducts.getProductName(), response.getProductName());
            assertEquals(mockedProducts.getCategory(), response.getCategory());
            assertEquals(mockedProducts.getImageUrl(), response.getImageUrl());
        }
    }

    @Test
    void getAllPhonesAndTabletsProduct() {
        // Arrange
        List<Product> mockProducts = new ArrayList<>();
        Product tecno = new Product();
        tecno.setId(1L);
        tecno.setPercentageDiscount(50);
        tecno.setSellingPrice(1540.0);
        tecno.setAmountDiscounted(500.0);
        tecno.setProductName("Tecno 10");
        tecno.setQuantity(25);
        tecno.setCategory(Category.PHONES);
        tecno.setDescription("Connectivity");
        tecno.setImageUrl("Image1.com");

        Product infinix = new Product();
        infinix.setId(2L);
        infinix.setPercentageDiscount(20);
        infinix.setSellingPrice(300.0);
        infinix.setAmountDiscounted(300.0);
        infinix.setProductName("Infinix Pro 20");
        infinix.setQuantity(125);
        infinix.setCategory(Category.PHONES);
        infinix.setDescription("Connectivity");
        infinix.setImageUrl("Image2.com");

        mockProducts.add(tecno);
        mockProducts.add(infinix);

        //Act
        when(productRepository.findAllByCategory(Category.PHONES)).thenReturn(mockProducts);

        List<ProductResponse> allPhonesAndTabletsProductProducts = productService.getAllPhonesAndTabletsProduct();

        //Assert
        verify(productRepository, times(1)).findAllByCategory(Category.PHONES);

        assertEquals(allPhonesAndTabletsProductProducts.size(), mockProducts().size());
        for (int i = 0; i < mockProducts.size(); i++) {
            Product mockedProducts = mockProducts.get(i);
            ProductResponse response = allPhonesAndTabletsProductProducts.get(i);

            assertEquals(mockedProducts.getId(), response.getId());
            assertEquals(mockedProducts.getPercentageDiscount(), response.getPercentageDiscount());
            assertEquals(mockedProducts.getQuantity(), response.getQuantity());
            assertEquals(mockedProducts.getProductName(), response.getProductName());
            assertEquals(mockedProducts.getCategory(), response.getCategory());
            assertEquals(mockedProducts.getImageUrl(), response.getImageUrl());
        }
    }

    @Test
    @DisplayName("Gets a product given it's ID")
    void getProduct() {
        Product mockProduct = Product.builder()
                .id(1L)
                .category(Category.TRAVELS)
                .productName("Inhaler")
                .description("Health")
                .quantity(4)
                .build();

        when(productRepository.findById(mockProduct.getId())).thenReturn(Optional.of(mockProduct));

        Optional<Product> productResponse = productService.getProduct(mockProduct.getId());
        verify(productRepository, times(1)).findById(mockProduct.getId());

        assertTrue(productResponse.isPresent());
        assertEquals(mockProduct.getId(), productResponse.get().getId());
        assertEquals(mockProduct.getProductName(), productResponse.get().getProductName());
        assertEquals(mockProduct.getCategory(), productResponse.get().getCategory());
        assertEquals(mockProduct.getDescription(), productResponse.get().getDescription());
        assertEquals(mockProduct.getQuantity(), productResponse.get().getQuantity());
    }


    @Test
    void getAllProductsWithPagination() {
        // Arrange
        int offset = 0;
        int pageSize = 2;
        String field = "productName";

        List<Product> sampleProducts = createSampleProducts();

        when(productRepository.findAll(PageRequest.of(offset, pageSize).withSort(Sort.by(Sort.Direction.ASC, field))))
                .thenReturn(new PageImpl<>(sampleProducts));

        // Act
        List<ProductResponse> result = productService.getAllProductsWithPagination(offset, pageSize, field);

        // Assert
        assertEquals(sampleProducts.size(), result.size());

        for (int i = 0; i < sampleProducts.size(); i++) {
            Product sampleProduct = sampleProducts.get(i);
            ProductResponse productResponse = result.get(i);

            assertEquals(sampleProduct.getId(), productResponse.getId());
            assertEquals(sampleProduct.getPercentageDiscount(), productResponse.getPercentageDiscount());
            assertEquals(sampleProduct.getSellingPrice(), productResponse.getSellingPrice());
            assertEquals(sampleProduct.getAmountDiscounted(), productResponse.getAmountDiscounted());
            assertEquals(sampleProduct.getProductName(), productResponse.getProductName());
            assertEquals(sampleProduct.getQuantity(), productResponse.getQuantity());
            assertEquals(sampleProduct.getCategory(), productResponse.getCategory());
            assertEquals(sampleProduct.getDescription(), productResponse.getDescription());
            assertEquals(sampleProduct.getImageUrl(), productResponse.getImageUrl());
        }
    }

    @Test
    void getAllVendorProducts() {
        User vendor = User.builder()
                .id(1L)
                .firstName("Rowland")
                .lastName("Kanu")
                .vendorCompany("GIGO")
                .territory("Lagos")
                .isVendor("True")
                .build();
        List<Product> vendorsProductList = mockProducts(vendor.getId());

        when(productRepository.findAllByUserId(vendor.getId())).thenReturn(vendorsProductList);

        List<ProductResponse> allVendorProducts = productService.getAllVendorProducts(String.valueOf(vendor.getId()));

        assertFalse(allVendorProducts.isEmpty());
        assertEquals(allVendorProducts.size(), vendorsProductList.size());

        for (int i = 0; i < allVendorProducts.size(); i++) {
            ProductResponse actualProduct = allVendorProducts.get(i);
            Product expectedProduct = vendorsProductList.get(i);

            assertEquals(expectedProduct.getId(), actualProduct.getId());
            assertEquals(expectedProduct.getCategory(), actualProduct.getCategory());
            assertEquals(expectedProduct.getDescription(), actualProduct.getDescription());
            assertEquals(expectedProduct.getProductName(), actualProduct.getProductName());
            assertEquals(expectedProduct.getQuantity(), actualProduct.getQuantity());
            assertEquals(expectedProduct.getImageUrl(), actualProduct.getImageUrl());
            assertEquals(expectedProduct.getSellingPrice(), actualProduct.getSellingPrice());
            assertEquals(expectedProduct.getPercentageDiscount(), actualProduct.getPercentageDiscount());
        }

    }


    @Test
    @DisplayName("Should return all products of a vendor, ordered by various users")
    void getMyProductsOrderedByUsers() {
        // Arrange
        User mockVendor = new User();
        mockVendor.setId(1L);
        mockVendor.setUsername("Flames");
        mockVendor.setFirstName("Rowland");
        mockVendor.setLastName("Kanu");

        List<CartCheckout.CartItem> itemList = new ArrayList<>();

        CartCheckout.CartItem lineItem1 = new CartCheckout.CartItem();
        lineItem1.setProductId(1L);
        lineItem1.setProductName("Product1");
        lineItem1.setPrice(450.2);
        lineItem1.setImageUrl("www.imageOfProduct1.com");
        lineItem1.setQuantity(32);
        lineItem1.setSubtotal(30.0);
        lineItem1.setCartCheckoutId(1L);

        CartCheckout.CartItem lineItem2 = new CartCheckout.CartItem();
        lineItem2.setProductId(2L);
        lineItem2.setProductName("Product2");
        lineItem2.setPrice(45.0);
        lineItem2.setImageUrl("www.imageOfProduct2.com");
        lineItem2.setQuantity(6);
        lineItem2.setSubtotal(38.80);
        lineItem2.setCartCheckoutId(1L);

        itemList.add(lineItem1);
        itemList.add(lineItem2);

        List<CartCheckout> cartCheckoutList = new ArrayList<>();

        CartCheckout mockCheckoutOne = new CartCheckout();
        mockCheckoutOne.setId(1L);
        mockCheckoutOne.setFirstName(mockVendor.getFirstName());
        mockCheckoutOne.setLastName(mockVendor.getLastName());
        mockCheckoutOne.setPhoneNumber("08143358911");
        mockCheckoutOne.setUserId(mockVendor.getId());
        mockCheckoutOne.setRegion("Kubwa");
        mockCheckoutOne.setState("Abuja");
        mockCheckoutOne.setPaymentStatus(PaymentStatus.APPROVED);
        mockCheckoutOne.setPurchaseDate(LocalDateTime.now());
        mockCheckoutOne.setCart(itemList);


        cartCheckoutList.add(mockCheckoutOne);
        List<Product> vendorsProducts = mockProducts(mockVendor.getId());
        List<Long> productIds = vendorsProducts.stream().map(Product::getId).toList();
        logger.info("cartCheckoutList "+ cartCheckoutList);
        logger.info("vendorsProducts "+ vendorsProducts);

        //Act
        when(productRepository.findAllByUserId(mockVendor.getId())).thenReturn(vendorsProducts);
        when(cartCheckoutRepository.findCartItemsByProductIds(productIds)).thenReturn(itemList);
        when(cartCheckoutRepository.findById(anyLong())).thenAnswer(invocation -> {
            Long cartCheckoutId = invocation.getArgument(0);
            return cartCheckoutList.stream()
                    .filter(checkout -> checkout.getId().equals(cartCheckoutId))
                    .findFirst();
        });

        List<OrderedProductsByUserFromVendor> result = productService.getMyProductsOrderedByUsers(mockVendor.getId());

        // Assert
        assertNotNull(result);
        assertEquals(cartCheckoutList.size(), result.size());

        for (int i = 0; i < cartCheckoutList.size(); i++) {
            CartCheckout expectedCart = cartCheckoutList.get(i);
            OrderedProductsByUserFromVendor actualCartItem =  result.get(i);
            logger.info("Expected cart " + expectedCart);
            logger.info("actualCartItem cart " + actualCartItem);

            assertEquals(expectedCart.getId(), actualCartItem.getId());
            assertEquals(expectedCart.getFirstName(), actualCartItem.getFirstName());
            assertEquals(expectedCart.getPhoneNumber(), actualCartItem.getPhoneNumber());
            assertEquals(expectedCart.getPaymentStatus(), actualCartItem.getPaymentStatus());
            assertEquals(expectedCart.getRegion(), actualCartItem.getRegion());
            assertEquals(expectedCart.getState(), actualCartItem.getState());
            for (int j = 0; j < expectedCart.getCart().size(); j++) {
                CartCheckout.CartItem expectedIndividualCartItem = expectedCart.getCart().get(i);
                OrderedProductsByUserFromVendor.CartItem actualIndividualCartItem = actualCartItem.getCart().get(i);

                assertEquals(expectedIndividualCartItem.getProductId(), actualIndividualCartItem.getProductId());
                assertEquals(expectedIndividualCartItem.getPrice(), actualIndividualCartItem.getPrice());
                assertEquals(expectedIndividualCartItem.getProductName(), actualIndividualCartItem.getProductName());
                assertEquals(expectedIndividualCartItem.getQuantity(), actualIndividualCartItem.getQuantity());
                assertEquals(expectedIndividualCartItem.getImageUrl(), actualIndividualCartItem.getImageUrl() );
            }


        }

        verify(productRepository, times(1)).findAllByUserId(mockVendor.getId());
        verify(cartCheckoutRepository, times(1)).findCartItemsByProductIds(anyList());
    }



    private List<Product> mockProducts() {
        return List.of(
                Product.builder()
                        .id(1L)
                        .productName("Product1")
                        .description("first product")
                        .quantity(32)
                        .userId(1L)
                        .amountDiscounted(3.2)
                        .imageUrl("www.imageOfProduct1.com")
                        .sellingPrice(23.1)
                        .percentageDiscount(20)
                        .build(),
                Product.builder()
                        .id(2L)
                        .productName("Product2")
                        .description("second product")
                        .quantity(31)
                        .userId(1L)
                        .amountDiscounted(30.2)
                        .imageUrl("www.imageOfProduct2.com")
                        .sellingPrice(29.0)
                        .percentageDiscount(10)
                        .build()
        );
    }


    private List<Product> mockProducts(Long userId) {
        return List.of(
                Product.builder()
                        .id(1L)
                        .productName("Product1")
                        .description("first product")
                        .quantity(32)
                        .userId(userId)
                        .amountDiscounted(3.2)
                        .imageUrl("www.imageOfProduct1.com")
                        .sellingPrice(23.1)
                        .percentageDiscount(20)
                        .build(),
                Product.builder()
                        .id(2L)
                        .productName("Product2")
                        .description("second product")
                        .quantity(31)
                        .amountDiscounted(30.2)
                        .imageUrl("www.imageOfProduct2.com")
                        .sellingPrice(29.0)
                        .percentageDiscount(10)
                        .userId(userId)
                        .build(),
                Product.builder()
                        .id(3L)
                        .productName("Product3")
                        .description("third product")
                        .quantity(49)
                        .amountDiscounted(120.2)
                        .imageUrl("www.imageOfProduct3.com")
                        .sellingPrice(29.0)
                        .percentageDiscount(10)
                        .userId(userId)
                        .build()
        );
    }


    private List<Product> createSampleProducts() {
        return List.of(
                Product.builder()
                        .productName("James")
                        .id(1L)
                        .category(Category.BABY)
                        .build(),
                Product.builder()
                        .productName("Rowland")
                        .id(2L)
                        .category(Category.ELECTRONICS)
                        .build(),
                Product.builder()
                        .productName("Samuel")
                        .id(3L)
                        .category(Category.ELECTRONICS)
                        .build()
        );
    }

}