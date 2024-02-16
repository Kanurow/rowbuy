package com.rowland.engineering.ecommerce.utils;

import com.rowland.engineering.ecommerce.exception.AppException;
import com.rowland.engineering.ecommerce.model.*;
import com.rowland.engineering.ecommerce.repository.ProductRepository;
import com.rowland.engineering.ecommerce.repository.RoleRepository;
import com.rowland.engineering.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static com.rowland.engineering.ecommerce.model.RoleName.ROLE_ADMIN;
import static com.rowland.engineering.ecommerce.model.RoleName.ROLE_USER;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        List<Role> roles = Arrays.asList(
                new Role(ROLE_USER),
                new Role(ROLE_ADMIN)
        );
        roleRepository.saveAll(roles);
        persistUsers();
        persistProducts();
    }

    private void persistUsers() {
        User user1 = User.builder()
                .id(1L)
                .firstName("Rowland")
                .lastName("Kanu")
                .dateOfBirth("1996-10-05")
                .username("flames")
                .email("kanurowland92@gmail.com")
                .password(passwordEncoder.encode("rowland12"))
                .mobile("+2348143358911")
                .isVendor("False")
                .build();

        User user2 = User.builder()
                .id(2L)
                .firstName("Samuel")
                .lastName("Kanu")
                .dateOfBirth("1999-03-15")
                .username("sammy")
                .email("kanusam@gmail.com")
                .password(passwordEncoder.encode("sammy12"))
                .mobile("+234121212")
                .isVendor("True")
                .vendorCompany("Jumia Inc Ltd")
                .territory("NIGERIA")
                .build();

        Role roleUser = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new AppException("User Role not set."));
        Role roleAdmin = roleRepository.findByName(RoleName.ROLE_ADMIN)
                .orElseThrow(() -> new AppException("Admin Role not set."));

        user1.setRoles(new HashSet<>(List.of(roleUser)));
        user2.setRoles(new HashSet<>(List.of(roleAdmin)));

        userRepository.saveAll(List.of(user1, user2));

    }

    private void persistProducts() {
        Product product1 = Product.builder()
                .productName("Blazer")
                .category(Category.OTHERS)
                .sellingPrice(900.0)
                .amountDiscounted(100.0)
                .percentageDiscount(10)
                .quantity(20)
                .description("Polo Wool Twill Blazer.")
                .imageUrl("https://www.optimized-rlmedia.io/is/image/PoloGSI/s7-1189461_lifestyle?$rl_df_pdp_5_7_lif$")
                .userId(2L)
                .build();

        Product product2 = Product.builder()
                .productName("Corduroy Trouser")
                .category(Category.OTHERS)
                .sellingPrice(500.0)
                .amountDiscounted(500.0)
                .percentageDiscount(50)
                .quantity(100)
                .description("Stretch Slim Fit Corduroy Trouser.")
                .imageUrl("https://www.optimized-rlmedia.io/is/image/PoloGSI/s7-1487753_alternate10?$rl_df_pdp_5_7_a10$")
                .userId(2L)
                .build();

        Product product3 = Product.builder()
                .productName("Washing Machine")
                .category(Category.APPLIANCES)
                .sellingPrice(36000.0)
                .amountDiscounted(4000.0)
                .percentageDiscount(10)
                .quantity(12)
                .description("Washing machine with a sleek design.")
                .imageUrl("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQh1sRCtMNelrC-ZlMVFFH_GBByC4VZpuqI5t4wDoc&s")
                .userId(2L)
                .build();

        Product product4 = Product.builder()
                .productName("Iphone X")
                .category(Category.PHONES)
                .sellingPrice(90000.09)
                .amountDiscounted(0.0)
                .percentageDiscount(0)
                .quantity(56)
                .description("Feature-packed iPhone X showing IOS and, iPhone mobile Phone.")
                .imageUrl("https://www.freeiconspng.com/thumbs/iphone-x-pictures/new-iphone-x-photo-18.png")
                .userId(2L)
                .build();

        Product product5 = Product.builder()
                .productName("Blender")
                .category(Category.ELECTRONICS)
                .sellingPrice(14000.0)
                .amountDiscounted(1400.0)
                .percentageDiscount(10)
                .quantity(18)
                .description("silver and black blender, Smoothie Juice Blender Stainless steel Glass")
                .imageUrl("https://w7.pngwing.com/pngs/812/60/png-transparent-silver-and-black-blender-smoothie-juice-blender-stainless-steel-glass-blender-kitchen-kitchen-appliance-electric-kettle-thumbnail.png")
                .userId(2L)
                .build();
        Product product6 = Product.builder()
                .productName("School Bag")
                .category(Category.TRAVELS)
                .sellingPrice(500.00)
                .amountDiscounted(250.0)
                .percentageDiscount(50)
                .quantity(13)
                .description("A colour-blocked design and Ralph Lauren's signature Pony give this spacious backpack a playful Polo look.")
                .imageUrl("https://www.optimized-rlmedia.io/is/image/PoloGSI/s7-1492176_lifestyle?$rl_df_pdp_4_5_mob_lif$")
                .userId(2L)
                .build();

        Product product7 = Product.builder()
                .productName("iPhone 14")
                .category(Category.PHONES)
                .sellingPrice(156000.0)
                .amountDiscounted(7800.0)
                .percentageDiscount(5)
                .quantity(170)
                .description("Feature-packed iPhone 14 showing IOS and, iPhone mobile Phone.")
                .imageUrl("https://pngimg.com/d/iphone_14_PNG19.png")
                .userId(2L)
                .build();

        Product product8 = Product.builder()
                .productName("LG Television")
                .category(Category.ELECTRONICS)
                .sellingPrice(8000.0)
                .amountDiscounted(3200.0)
                .percentageDiscount(40)
                .quantity(87)
                .description("LG Uhd Tv 4k Feature-packed.")
                .imageUrl("https://www.pngitem.com/pimgs/m/297-2976751_lg-uhd-tv-4k-75uk65-hd-png-download.png")
                .userId(2L)
                .build();
        Product product9 = Product.builder()
                .productName("Air Conditioner")
                .category(Category.APPLIANCES)
                .sellingPrice(50000.0)
                .amountDiscounted(25000.0)
                .percentageDiscount(50)
                .quantity(87)
                .description("Fireman Air conditionerLG Uhd Feature-packed.")
                .imageUrl("https://pngimg.com/d/air_conditioner_PNG25.png")
                .userId(2L)
                .build();

        Product product10 = Product.builder()
                .productName("Deep Discord")
                .category(Category.BOOKS)
                .sellingPrice(2000.0)
                .amountDiscounted(200.0)
                .percentageDiscount(10)
                .quantity(47)
                .description("Self development best author Feature-packed.")
                .imageUrl("https://images.crowdspring.com/blog/wp-content/uploads/2020/12/29153507/wizardesign1_boldpattern.png")
                .userId(2L)
                .build();
        Product product11 = Product.builder()
                .productName("Milo Chocolate")
                .category(Category.SUPERMARKET)
                .sellingPrice(3000.0)
                .amountDiscounted(300.0)
                .percentageDiscount(20)
                .quantity(33)
                .description("Chocolate milk top brand.")
                .imageUrl("https://banner2.cleanpng.com/20180611/kqz/kisspng-milo-malted-milk-chocolate-drink-cocoa-solids-5b1e51515ed8b6.1888110015287135533885.jpg")
                .userId(2L)
                .build();

        Product product12 = Product.builder()
                .productName("Nivea Lotion")
                .category(Category.SUPERMARKET)
                .sellingPrice(3000.0)
                .amountDiscounted(1500.0)
                .percentageDiscount(50)
                .quantity(30)
                .description("Chocolate milk top brand.")
                .imageUrl("https://e7.pngegg.com/pngimages/281/637/png-clipart-nivea-nourishing-body-lotion-nivea-nourishing-body-lotion-nivea-nourishing-body-milk-400-ml-400-ml-cream-body-milk-poster-cream-body-wash.png")
                .userId(2L)
                .build();
        Product product13 = Product.builder()
                .productName("Baby Bed")
                .category(Category.BABY)
                .sellingPrice(5000.0)
                .amountDiscounted(2500.0)
                .percentageDiscount(50)
                .quantity(30)
                .description("Portable Newborn Baby Bed Mosquito Net, Pink.")
                .imageUrl("https://monmartt.com/img/products/d/d6847a547858ac4a30bf786d547d86aa.png")
                .userId(2L)
                .build();
        productRepository.saveAll(List.of(product1, product2, product3, product4, product5, product6, product7, product8, product9, product10, product11, product12, product13));

        System.out.println("Products persisted to the database.");
    }
}

