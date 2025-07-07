package com.app;

import com.app.model.*;
import com.app.service.OrderAnalytics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests for OrderAnalytics Service")
class OrderAnalyticsTest {

    private OrderAnalytics analytics;
    private List<Order> testOrders;

    private final Customer customer1 = new Customer("c1", "Ivan Petrov", "ivan@example.com", LocalDateTime.now(), 30, "Moscow");
    private final Customer customer2 = new Customer("c2", "Anna Sidorova", "anna@example.com", LocalDateTime.now(), 25, "Minsk");
    private final Customer customer3 = new Customer("c3", "Sergey Smirnov", "sergey@example.com", LocalDateTime.now(), 42, "Moscow");
    private final Customer customer4 = new Customer("c4", "Elena Kuznetsova", "elena@example.com", LocalDateTime.now(), 35, "Kyiv");

    @BeforeEach
    void setUp() {
        analytics = new OrderAnalytics();

        testOrders = List.of(
                new Order("o1", LocalDateTime.now(), customer1, List.of(
                        new OrderItem("Laptop", 1, 1500.0, Category.ELECTRONICS),
                        new OrderItem("Mouse", 1, 50.0, Category.ELECTRONICS)
                ), OrderStatus.DELIVERED),

                new Order("o2", LocalDateTime.now(), customer2, List.of(
                        new OrderItem("Book 'Java'", 2, 30.0, Category.BOOKS)
                ), OrderStatus.SHIPPED),

                new Order("o3", LocalDateTime.now(), customer3, List.of(
                        new OrderItem("T-Shirt", 5, 20.0, Category.CLOTHING)
                ), OrderStatus.CANCELLED),

                new Order("o4", LocalDateTime.now(), customer4, List.of(
                        new OrderItem("Book 'Java'", 1, 30.0, Category.BOOKS),
                        new OrderItem("Dress", 1, 100.0, Category.CLOTHING)
                ), OrderStatus.DELIVERED),

                new Order("o5", LocalDateTime.now(), customer1, List.of(
                        new OrderItem("Headphones", 1, 200.0, Category.ELECTRONICS)
                ), OrderStatus.NEW),

                new Order("o6", LocalDateTime.now(), customer1, List.of(
                        new OrderItem("Book 'Java'", 1, 30.0, Category.BOOKS)
                ), OrderStatus.DELIVERED)
        );
    }

    @Test
    @DisplayName("Should return unique cities")
    void getUniqueCities_shouldReturnUniqueCities() {
        Set<String> uniqueCities = analytics.getUniqueCities(testOrders);
        assertEquals(3, uniqueCities.size(), "Number of unique cities should be 3");
        assertTrue(uniqueCities.containsAll(Set.of("Moscow", "Minsk", "Kyiv")), "The list should contain the correct cities");
    }

    @Test
    @DisplayName("Should return an empty Set for an empty order list")
    void getUniqueCities_whenOrderListIsEmpty_shouldReturnEmptySet() {
        Set<String> uniqueCities = analytics.getUniqueCities(Collections.emptyList());
        assertTrue(uniqueCities.isEmpty(), "An empty Set should be returned for an empty order list");
    }

    @Test
    @DisplayName("Should correctly calculate total income from completed orders")
    void calculateTotalIncome_shouldSumOnlyShippedAndDeliveredOrders() {
        double expectedIncome = 1550.0 + 60.0 + 130.0 + 30.0;
        double actualIncome = analytics.calculateTotalIncome(testOrders);
        assertEquals(expectedIncome, actualIncome, 0.001, "Total income calculated incorrectly");
    }

    @Test
    @DisplayName("Should return 0 for an empty order list when calculating income")
    void calculateTotalIncome_whenOrderListIsEmpty_shouldReturnZero() {
        double actualIncome = analytics.calculateTotalIncome(Collections.emptyList());
        assertEquals(0.0, actualIncome, "Income for an empty list should be 0");
    }

    @Test
    @DisplayName("Should find the most popular product by quantity")
    void findMostPopularProduct_shouldReturnCorrectProduct() {
        Optional<String> mostPopularProduct = analytics.findMostPopularProduct(testOrders);
        assertTrue(mostPopularProduct.isPresent(), "A popular product should be found");
        assertEquals("Book 'Java'", mostPopularProduct.get(), "The most popular product is incorrect");
    }

    @Test
    @DisplayName("Should return Optional.empty for an empty order list")
    void findMostPopularProduct_whenOrderListIsEmpty_shouldReturnEmptyOptional() {
        Optional<String> mostPopularProduct = analytics.findMostPopularProduct(Collections.emptyList());
        assertTrue(mostPopularProduct.isEmpty(), "An empty Optional should be returned for an empty list");
    }

    @Test
    @DisplayName("Should correctly calculate the average check for delivered orders")
    void calculateAverageCheckForDeliveredOrders_shouldCalculateCorrectly() {
        double expectedAverage = 570.0;
        double actualAverage = analytics.calculateAverageCheckForDeliveredOrders(testOrders).orElse(0.0);
        assertEquals(expectedAverage, actualAverage, 0.001, "Average check calculated incorrectly");
    }

    @Test
    @DisplayName("Should return an empty Optional if there are no delivered orders")
    void calculateAverageCheckForDeliveredOrders_whenNoDeliveredOrders_shouldReturnEmpty() {
        List<Order> noDeliveredOrders = List.of(
                new Order("o1", LocalDateTime.now(), customer1, List.of(), OrderStatus.SHIPPED),
                new Order("o2", LocalDateTime.now(), customer2, List.of(), OrderStatus.CANCELLED)
        );
        assertTrue(analytics.calculateAverageCheckForDeliveredOrders(noDeliveredOrders).isEmpty(), "An empty Optional should be returned if there are no delivered orders");
    }

    @Test
    @DisplayName("Should find customers with more than N orders")
    void findCustomersWithMoreThanNOrders_shouldReturnCorrectCustomers() {
        int orderThreshold = 2;
        List<Customer> loyalCustomers = analytics.findCustomersWithMoreThanNOrders(testOrders, orderThreshold);
        assertEquals(1, loyalCustomers.size(), "1 loyal customer should be found");
        assertEquals(customer1.getCustomerId(), loyalCustomers.get(0).getCustomerId(), "Incorrect customer found");
    }

    @Test
    @DisplayName("Should return an empty list if no customer matches the condition")
    void findCustomersWithMoreThanNOrders_whenNoCustomerMatches_shouldReturnEmptyList() {
        int orderThreshold = 5;
        List<Customer> loyalCustomers = analytics.findCustomersWithMoreThanNOrders(testOrders, orderThreshold);
        assertTrue(loyalCustomers.isEmpty(), "The list of loyal customers should be empty");
    }
}
