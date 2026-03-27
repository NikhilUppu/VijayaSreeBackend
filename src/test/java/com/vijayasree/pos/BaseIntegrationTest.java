package com.vijayasree.pos;

import com.vijayasree.pos.entity.*;
import com.vijayasree.pos.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.EnumSet;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    @Autowired protected MockMvc mockMvc;
    @Autowired protected UserRepository userRepository;
    @Autowired protected CustomRoleRepository customRoleRepository;
    @Autowired protected ProductRepository productRepository;
    @Autowired protected CategoryRepository categoryRepository;
    @Autowired protected CustomerRepository customerRepository;
    @Autowired protected SaleRepository saleRepository;
    @Autowired protected PasswordEncoder passwordEncoder;

    protected CustomRole adminRole;
    protected User adminUser;
    protected Category category;

    @BeforeEach
    void baseSetup() {
        // Clean DB before each test
        saleRepository.deleteAll();
        customerRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
        customRoleRepository.deleteAll();

        // Create Admin role
        adminRole = customRoleRepository.save(CustomRole.builder()
                .name("Admin")
                .description("Full access")
                .isSystem(true)
                .permissions(EnumSet.allOf(Permission.class))
                .build());

        // Create admin user
        adminUser = userRepository.save(User.builder()
                .name("Ramamohan")
                .username("ramamohan")
                .passwordHash(passwordEncoder.encode("vst@2026"))
                .role(adminRole)
                .active(true)
                .build());

        // Create a category
        category = categoryRepository.save(Category.builder()
                .name("Pesticides")
                .build());
    }
}