package com.contractmonitor.orderservice;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    
    private final OrderService orderService;
    
    /**
     * Create a new order
     * POST /api/orders
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Order createOrder(@Valid @RequestBody Order order) {
        return orderService.createOrder(order);
    }
    
    /**
     * Get order by ID
     * GET /api/orders/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        try {
            Order order = orderService.getOrderById(id);
            return ResponseEntity.ok(order);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get all orders
     * GET /api/orders
     */
    @GetMapping
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }
    
    /**
     * Get orders by user ID
     * GET /api/orders/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public List<Order> getOrdersByUserId(@PathVariable Long userId) {
        return orderService.getOrdersByUserId(userId);
    }
    
    /**
     * Get orders by status
     * GET /api/orders/status/{status}
     */
    @GetMapping("/status/{status}")
    public List<Order> getOrdersByStatus(@PathVariable String status) {
        return orderService.getOrdersByStatus(status);
    }
    
    /**
     * Update order status
     * PUT /api/orders/{id}/status
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<Order> updateOrderStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            Order order = orderService.updateOrderStatus(id, status);
            return ResponseEntity.ok(order);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Cancel order
     * PUT /api/orders/{id}/cancel
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long id) {
        try {
            orderService.cancelOrder(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Delete order
     * DELETE /api/orders/{id}
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
    }
}