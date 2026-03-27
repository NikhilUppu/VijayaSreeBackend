package com.vijayasree.pos.unit;

import com.vijayasree.pos.dto.request.CreditPaymentRequest;
import com.vijayasree.pos.dto.request.CustomerRequest;
import com.vijayasree.pos.dto.response.CustomerResponse;
import com.vijayasree.pos.entity.Customer;
import com.vijayasree.pos.entity.User;
import com.vijayasree.pos.exceptions.ResourceNotFoundException;
import com.vijayasree.pos.repository.*;
import com.vijayasree.pos.service.impl.CustomerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock CustomerRepository customerRepository;
    @Mock CreditPaymentRepository creditPaymentRepository;
    @Mock UserRepository userRepository;
    @Mock SaleRepository saleRepository;

    @InjectMocks CustomerServiceImpl customerService;

    private Customer mockCustomer;
    private User mockUser;

    @BeforeEach
    void setup() {
        mockCustomer = Customer.builder()
                .id(1L)
                .name("Farmer Ramaiah")
                .phone("9876543210")
                .village("Kuppam")
                .creditLimit(new BigDecimal("5000.00"))
                .creditBalance(new BigDecimal("1000.00"))
                .active(true)
                .build();

        mockUser = User.builder()
                .id(1L)
                .name("Ramamohan")
                .username("ramamohan")
                .active(true)
                .build();
    }

    @Test
    @DisplayName("Create customer — saves and returns correct response")
    void create_validRequest_savesCustomer() {
        CustomerRequest req = new CustomerRequest();
        req.setName("New Farmer");
        req.setPhone("8888888888");
        req.setVillage("Madanapalli");
        req.setCreditLimit(new BigDecimal("2000.00"));

        when(customerRepository.existsByPhone("8888888888")).thenReturn(false);
        when(customerRepository.save(any())).thenAnswer(inv -> {
            Customer c = inv.getArgument(0);
            c.setId(2L);
            return c;
        });
        when(saleRepository.findByCustomerIdOrderByCreatedAtDesc(any()))
                .thenReturn(List.of());

        CustomerResponse response = customerService.create(req);

        assertThat(response.getName()).isEqualTo("New Farmer");
        assertThat(response.getCreditBalance())
                .isEqualByComparingTo(BigDecimal.ZERO); // always starts at 0
        assertThat(response.getCreditLimit())
                .isEqualByComparingTo(new BigDecimal("2000.00"));
    }

    @Test
    @DisplayName("Create customer with duplicate phone — throws IllegalArgumentException")
    void create_duplicatePhone_throwsException() {
        CustomerRequest req = new CustomerRequest();
        req.setPhone("9876543210");
        req.setName("Duplicate");

        when(customerRepository.existsByPhone("9876543210")).thenReturn(true);

        assertThatThrownBy(() -> customerService.create(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("Record credit payment — decreases customer credit balance")
    void recordCreditPayment_validAmount_decreasesBalance() {
        CreditPaymentRequest req = new CreditPaymentRequest();
        req.setReceivedById(1L);
        req.setAmount(new BigDecimal("400.00"));
        req.setNote("Partial payment");

        when(customerRepository.findById(1L)).thenReturn(Optional.of(mockCustomer));
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(customerRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(creditPaymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        customerService.recordCreditPayment(1L, req);

        // was ₹1000, paid ₹400 → should be ₹600
        assertThat(mockCustomer.getCreditBalance())
                .isEqualByComparingTo(new BigDecimal("600.00"));
    }

    @Test
    @DisplayName("Record credit payment exceeding balance — throws IllegalArgumentException")
    void recordCreditPayment_exceedsBalance_throwsException() {
        CreditPaymentRequest req = new CreditPaymentRequest();
        req.setReceivedById(1L);
        req.setAmount(new BigDecimal("9999.00")); // more than ₹1000 balance

        when(customerRepository.findById(1L)).thenReturn(Optional.of(mockCustomer));
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        assertThatThrownBy(() -> customerService.recordCreditPayment(1L, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("exceeds credit balance");
    }

    @Test
    @DisplayName("Record credit payment — saves CreditPayment record")
    void recordCreditPayment_success_savesCreditPaymentRecord() {
        CreditPaymentRequest req = new CreditPaymentRequest();
        req.setReceivedById(1L);
        req.setAmount(new BigDecimal("500.00"));
        req.setNote("Full payment");

        when(customerRepository.findById(1L)).thenReturn(Optional.of(mockCustomer));
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(customerRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(creditPaymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        customerService.recordCreditPayment(1L, req);

        // verify the payment record was actually persisted
        verify(creditPaymentRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Get customer by phone — returns correct customer")
    void getByPhone_exists_returnsCustomer() {
        when(customerRepository.findByPhone("9876543210"))
                .thenReturn(Optional.of(mockCustomer));
        when(saleRepository.findByCustomerIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of());

        CustomerResponse response = customerService.getByPhone("9876543210");

        assertThat(response.getName()).isEqualTo("Farmer Ramaiah");
        assertThat(response.getPhone()).isEqualTo("9876543210");
    }

    @Test
    @DisplayName("Get customer by phone — not found throws ResourceNotFoundException")
    void getByPhone_notFound_throwsException() {
        when(customerRepository.findByPhone("0000000000"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.getByPhone("0000000000"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("No customer found");
    }

    @Test
    @DisplayName("Update customer credit limit — persists new limit")
    void update_creditLimit_persistsCorrectly() {
        CustomerRequest req = new CustomerRequest();
        req.setName("Farmer Ramaiah");
        req.setPhone("9876543210");
        req.setCreditLimit(new BigDecimal("10000.00"));

        when(customerRepository.findById(1L)).thenReturn(Optional.of(mockCustomer));
        when(customerRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(saleRepository.findByCustomerIdOrderByCreatedAtDesc(any()))
                .thenReturn(List.of());

        CustomerResponse response = customerService.update(1L, req);

        assertThat(response.getCreditLimit())
                .isEqualByComparingTo(new BigDecimal("10000.00"));
    }
}