-- Create databases for each microservice
CREATE DATABASE IF NOT EXISTS hypermall_users;
CREATE DATABASE IF NOT EXISTS hypermall_products;
CREATE DATABASE IF NOT EXISTS hypermall_order;
CREATE DATABASE IF NOT EXISTS hypermall_payment;
CREATE DATABASE IF NOT EXISTS hypermall_inventory;
CREATE DATABASE IF NOT EXISTS hypermall_shipping;
CREATE DATABASE IF NOT EXISTS hypermall_promotion;
CREATE DATABASE IF NOT EXISTS hypermall_review;
CREATE DATABASE IF NOT EXISTS hypermall_notification;
CREATE DATABASE IF NOT EXISTS hypermall_seller;
CREATE DATABASE IF NOT EXISTS hypermall_media;
CREATE DATABASE IF NOT EXISTS hypermall_analytics;

-- Grant permissions
GRANT ALL PRIVILEGES ON hypermall_users.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON hypermall_products.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON hypermall_order.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON hypermall_payment.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON hypermall_inventory.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON hypermall_shipping.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON hypermall_promotion.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON hypermall_review.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON hypermall_notification.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON hypermall_seller.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON hypermall_media.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON hypermall_analytics.* TO 'root'@'%';

FLUSH PRIVILEGES;
