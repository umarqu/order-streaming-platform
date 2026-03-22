INSERT INTO customer (name, email) VALUES
('Alice Johnson', 'alice.johnson@example.com'),
('Bob Smith', 'bob.smith@example.com'),
('Clara Nguyen', 'clara.nguyen@example.com');


INSERT INTO product (name, price) VALUES
('Wireless Mouse', 19.99),
('Mechanical Keyboard', 79.50),
('27-inch Monitor', 229.99);

INSERT INTO `order` (customer_id, product_id, quantity, order_date) VALUES
(1, 1, 2, '2025-10-15 10:30:00'),
(2, 2, 1, '2025-10-15 14:45:00'),
(3, 3, 3, '2025-10-16 09:00:00');
