package ecommerce.backend.repository;

import ecommerce.backend.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;


public interface OrderRepository extends JpaRepository<Order, Long> { }
