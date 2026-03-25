package ecommerce.backend.controller;

import ecommerce.backend.dto.OrderDTO;
import ecommerce.backend.model.OrderStatus;
import ecommerce.backend.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public List<OrderDTO> getAllOrders() {
        return orderService.getAllOrders();
    }

    @PostMapping
    public OrderDTO addOrder(@RequestBody OrderDTO dto) {
        log.info("Received order request: customerId={}, productId={}, quantity={}",
                dto.getCustomerId(), dto.getProductId(), dto.getQuantity());
        return orderService.createOrder(dto);
    }

    @GetMapping("/{id}")
    public OrderDTO getOrder(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }

    @PutMapping("/{id}/status")
    public OrderDTO updateOrderStatus(@PathVariable Long id, @RequestParam OrderStatus status) {
        log.info("Updating order {} status to {}", id, status);
        return orderService.updateOrderStatus(id, status);
    }
}
