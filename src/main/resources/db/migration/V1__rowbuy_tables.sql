CREATE TABLE roles (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL
);


CREATE TABLE user_roles (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    role_id INT NOT NULL
);

CREATE TABLE product (
  id INT PRIMARY KEY AUTO_INCREMENT,
  product_name VARCHAR(40) NOT NULL,
  selling_price DECIMAL(10, 2) NOT NULL,
  amount_discounted DECIMAL(10, 2) NOT NULL,
  quantity INT NOT NULL,
  percentage_discount INT NOT NULL,
  description TEXT,
  image_url VARCHAR(500) NOT NULL,
  category VARCHAR(40) NOT NULL,
  user_id INT NOT NULL

);


CREATE TABLE users_table (
    id INT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    is_vendor VARCHAR(10),
    vendor_company VARCHAR(80),
    company_logo_url VARCHAR(500),
    profile_picture_url VARCHAR(500),
    territory VARCHAR(100),
    `password` VARCHAR(10000) NOT NULL ,
    mobile VARCHAR(20),
    date_of_birth VARCHAR(100) ,
    authorities VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);


CREATE TABLE cart_table (
    id INT PRIMARY KEY AUTO_INCREMENT,
    product_id INT,
    user_id INT
);

CREATE TABLE cart_checked_out (
  id INT PRIMARY KEY AUTO_INCREMENT,
  last_name VARCHAR(255) NOT NULL,
  first_name VARCHAR(255) NOT NULL,
  phone_number VARCHAR(20) NOT NULL,
  alternative_phone_number VARCHAR(20),
  delivery_address VARCHAR(300) NOT NULL,
  additional_information VARCHAR(300),
  region VARCHAR(50) NOT NULL,
  state VARCHAR(50) NOT NULL,
  price DOUBLE NOT NULL,
  quantity INT NOT NULL,
  user_id INT NOT NULL,
  payment_status VARCHAR(20) NOT NULL,
  payment_reference VARCHAR(150) NOT NULL,
  purchase_date TIMESTAMP

);

CREATE TABLE cart_item (
  id INT PRIMARY KEY AUTO_INCREMENT,
  product_name VARCHAR(255) NOT NULL,
  product_id INT NOT NULL,
  image_url VARCHAR(2000) NOT NULL,
  price DOUBLE NOT NULL,
  quantity INT NOT NULL,
  subtotal DOUBLE NOT NULL,
  cart_checked_out_id INT,
  FOREIGN KEY (cart_checked_out_id) REFERENCES cart_checked_out(id)
);


INSERT INTO roles(name) VALUES('ROLE_USER');
INSERT INTO roles(name) VALUES('ROLE_ADMIN');
