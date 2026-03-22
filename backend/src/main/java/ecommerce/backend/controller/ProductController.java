package ecommerce.backend.controller;

import ecommerce.backend.dto.OrderDTO;
import ecommerce.backend.dto.ProductDTO;
import ecommerce.backend.service.OrderService;
import ecommerce.backend.service.ProductService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ProductDTO createProduct(@RequestBody ProductDTO dto) {
        System.out.println("Request has been made to create a product");
        return productService.createProduct(dto);
    }

    @GetMapping
    public List<ProductDTO> getAllProducts() {
        System.out.println("Request has been made to get all products");
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public ProductDTO getProductById(@PathVariable Long id) {
        return productService.getProductById(id);
    }
}