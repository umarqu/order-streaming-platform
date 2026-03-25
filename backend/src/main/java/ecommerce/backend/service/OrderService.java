package ecommerce.backend.service;

import ecommerce.backend.dto.OrderDTO;
import ecommerce.backend.exceptions.CustomerNotFoundException;
import ecommerce.backend.exceptions.ProductNotFoundException;
import ecommerce.backend.exceptions.ResourceNotFoundException;
import ecommerce.backend.model.Customer;
import ecommerce.backend.model.Order;
import ecommerce.backend.model.OrderStatus;
import ecommerce.backend.model.Product;
import ecommerce.backend.repository.CustomerRepository;
import ecommerce.backend.repository.OrderRepository;
import ecommerce.backend.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${kafka.topics.orders}")
    private String ordersTopic;

    public OrderService(OrderRepository orderRepository,
                        CustomerRepository customerRepository,
                        ProductRepository productRepository,
                        KafkaTemplate<String, String> kafkaTemplate) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public OrderDTO createOrder(OrderDTO dto) {
        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new CustomerNotFoundException(String.valueOf(dto.getCustomerId())));

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(String.valueOf(dto.getProductId())));

        Order order = new Order();
        order.setCustomer(customer);
        order.setProduct(product);
        order.setQuantity(dto.getQuantity());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);

        Order saved = orderRepository.save(order);
        OrderDTO response = convertToDTO(saved);

        publishOrderEvent(response);

        return response;
    }

    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public OrderDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        return convertToDTO(order);
    }

    private void publishOrderEvent(OrderDTO order) {
        String event = String.format(
                "{\"event\":\"order.created\",\"orderId\":%d,\"customerId\":%d,\"productId\":%d,\"quantity\":%d,\"orderDate\":\"%s\",\"status\":\"%s\"}",
                order.getId(), order.getCustomerId(), order.getProductId(),
                order.getQuantity(), order.getOrderDate(), order.getStatus());

        kafkaTemplate.send(ordersTopic, String.valueOf(order.getId()), event);
        log.info("Published order.created event for orderId={}", order.getId());
    }

    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setCustomerId(order.getCustomer().getId());
        dto.setProductId(order.getProduct().getId());
        dto.setQuantity(order.getQuantity());
        dto.setOrderDate(order.getOrderDate());
        dto.setStatus(order.getStatus());
        return dto;
    }

    public OrderDTO updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        order.setStatus(newStatus);
        Order updated = orderRepository.save(order);
        return convertToDTO(updated);
    }
}
