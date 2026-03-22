package ecommerce.backend.controller;

import ecommerce.backend.dto.CustomerDTO;
import ecommerce.backend.model.Customer;
import ecommerce.backend.service.CustomerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    public CustomerDTO createCustomer(@RequestBody CustomerDTO dto) {
        System.out.println("Request has been made to create a customer");
        return customerService.createCustomer(dto);
    }

    @GetMapping
    public List<CustomerDTO> getAllCustomers() {
        System.out.println("Request has been made to get all customer");
        return customerService.getAllCustomers();
    }

    @GetMapping("/{id}")
    public CustomerDTO getCustomerById(@PathVariable Long id) {
        return customerService.getCustomerById(id);
    }
}