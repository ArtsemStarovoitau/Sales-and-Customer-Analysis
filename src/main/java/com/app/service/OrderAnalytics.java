package com.app.service;

import com.app.model.Customer;
import com.app.model.Order;
import com.app.model.OrderItem;
import com.app.model.OrderStatus;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.stream.Collectors;


public class OrderAnalytics {

    public Set<String> getUniqueCities(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return Set.of();
        }
        return orders.stream()
                .map(order -> order.getCustomer().getCity())
                .collect(Collectors.toSet());
    }

    public double calculateTotalIncome(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return 0.0;
        }
        return orders.stream()
                .filter(order -> order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED)
                .flatMap(order -> order.getItems().stream())
                .mapToDouble(item -> item.getQuantity() * item.getPrice())
                .sum();
    }

    public Optional<String> findMostPopularProduct(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return Optional.empty();
        }
        return orders.stream()
                .flatMap(order -> order.getItems().stream())
                .collect(Collectors.groupingBy(
                        OrderItem::getProductName,
                        Collectors.summingInt(OrderItem::getQuantity)
                ))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);
    }

    public OptionalDouble calculateAverageCheckForDeliveredOrders(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return OptionalDouble.empty();
        }
        return orders.stream()
                .filter(order -> order.getStatus() == OrderStatus.DELIVERED)
                .mapToDouble(order -> order.getItems().stream()
                        .mapToDouble(item -> item.getQuantity() * item.getPrice())
                        .sum())
                .average();
    }

    public List<Customer> findCustomersWithMoreThanNOrders(List<Order> orders, int minOrderCount) {
        if (orders == null || orders.isEmpty()) {
            return List.of();
        }
        return orders.stream()
                .collect(Collectors.groupingBy(
                        Order::getCustomer,
                        Collectors.counting()
                ))
                .entrySet().stream()
                .filter(entry -> entry.getValue() > minOrderCount)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
