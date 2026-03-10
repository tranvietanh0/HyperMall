# HyperMall API Documentation

## Base URL

```
Development: http://localhost:8080/api
Production:  https://api.hypermall.com/api
```

## Authentication

Most endpoints require JWT authentication. Include the token in the Authorization header:

```
Authorization: Bearer <access_token>
```

---

## Auth Endpoints

### Register User

```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "Password123!",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+84123456789"
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "id": 1,
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "roles": ["ROLE_USER"]
  }
}
```

### Login

```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "Password123!"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400000
  }
}
```

### Refresh Token

```http
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### Logout

```http
POST /api/auth/logout
Authorization: Bearer <access_token>
```

---

## User Endpoints

### Get Current User Profile

```http
GET /api/users/me
Authorization: Bearer <access_token>
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "phone": "+84123456789",
    "avatar": "https://...",
    "roles": ["ROLE_USER"],
    "createdAt": "2024-01-01T00:00:00Z"
  }
}
```

### Update Profile

```http
PUT /api/users/me
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Smith",
  "phone": "+84987654321"
}
```

### Change Password

```http
POST /api/users/me/change-password
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "currentPassword": "OldPassword123!",
  "newPassword": "NewPassword123!"
}
```

---

## Address Endpoints

### Get My Addresses

```http
GET /api/users/me/addresses
Authorization: Bearer <access_token>
```

### Add Address

```http
POST /api/users/me/addresses
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "fullName": "John Doe",
  "phone": "+84123456789",
  "province": "Ho Chi Minh",
  "district": "District 1",
  "ward": "Ben Nghe",
  "addressLine": "123 Nguyen Hue Street",
  "isDefault": true
}
```

### Update Address

```http
PUT /api/users/me/addresses/{addressId}
Authorization: Bearer <access_token>
```

### Delete Address

```http
DELETE /api/users/me/addresses/{addressId}
Authorization: Bearer <access_token>
```

---

## Product Endpoints (Public)

### Get Products (with filters)

```http
GET /api/products?keyword=phone&categoryId=1&brandId=2&minPrice=100&maxPrice=1000&minRating=4&page=0&size=20&sort=price,asc
```

**Query Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| keyword | string | Search keyword |
| categoryId | long | Filter by category |
| brandId | long | Filter by brand |
| minPrice | decimal | Minimum price |
| maxPrice | decimal | Maximum price |
| minRating | double | Minimum rating (0-5) |
| page | int | Page number (0-based) |
| size | int | Page size (default 20) |
| sort | string | Sort field and direction |

**Response:**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "name": "iPhone 15 Pro",
        "slug": "iphone-15-pro",
        "thumbnail": "https://...",
        "basePrice": 999.00,
        "salePrice": 899.00,
        "averageRating": 4.5,
        "reviewCount": 120,
        "category": {
          "id": 1,
          "name": "Smartphones"
        },
        "brand": {
          "id": 1,
          "name": "Apple"
        }
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 100,
    "totalPages": 5,
    "first": true,
    "last": false
  }
}
```

### Get Product by ID

```http
GET /api/products/{id}
```

### Get Product by Slug

```http
GET /api/products/slug/{slug}
```

### Get Products by Category

```http
GET /api/products/category/{categoryId}?page=0&size=20
```

---

## Category Endpoints (Public)

### Get All Categories (Flat)

```http
GET /api/categories
```

### Get Category Tree (Hierarchical)

```http
GET /api/categories/tree
```

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Electronics",
      "slug": "electronics",
      "image": "https://...",
      "children": [
        {
          "id": 2,
          "name": "Smartphones",
          "slug": "smartphones",
          "children": null
        },
        {
          "id": 3,
          "name": "Laptops",
          "slug": "laptops",
          "children": null
        }
      ]
    }
  ]
}
```

### Get Category by ID

```http
GET /api/categories/{id}
```

### Get Category by Slug

```http
GET /api/categories/slug/{slug}
```

---

## Brand Endpoints (Public)

### Get All Brands

```http
GET /api/brands
```

### Get Brand by ID

```http
GET /api/brands/{id}
```

---

## Cart Endpoints

### Get Cart

```http
GET /api/cart
Authorization: Bearer <access_token>
```

**Response:**
```json
{
  "success": true,
  "data": {
    "userId": 1,
    "items": [
      {
        "id": "uuid-123",
        "productId": 1,
        "productName": "iPhone 15 Pro",
        "variantId": 5,
        "variantName": "256GB - Black",
        "thumbnail": "https://...",
        "price": 999.00,
        "quantity": 2,
        "subtotal": 1998.00
      }
    ],
    "totalItems": 2,
    "totalAmount": 1998.00,
    "updatedAt": "2024-01-01T00:00:00Z"
  }
}
```

### Add to Cart

```http
POST /api/cart/items
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "productId": 1,
  "variantId": 5,
  "quantity": 2
}
```

### Update Cart Item

```http
PUT /api/cart/items/{itemId}
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "quantity": 3
}
```

### Remove Cart Item

```http
DELETE /api/cart/items/{itemId}
Authorization: Bearer <access_token>
```

### Clear Cart

```http
DELETE /api/cart
Authorization: Bearer <access_token>
```

---

## Seller Product Endpoints

### Create Product

```http
POST /api/seller/products
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "name": "New Product",
  "slug": "new-product",
  "description": "Product description...",
  "shortDescription": "Short desc",
  "categoryId": 1,
  "brandId": 2,
  "basePrice": 100.00,
  "salePrice": 80.00,
  "thumbnail": "https://...",
  "status": "DRAFT",
  "images": [
    {
      "url": "https://...",
      "sortOrder": 0,
      "isMain": true
    }
  ],
  "variants": [
    {
      "sku": "SKU-001",
      "name": "Size S",
      "price": 100.00,
      "stock": 50,
      "attributes": {
        "size": "S",
        "color": "Red"
      }
    }
  ]
}
```

### Update Product

```http
PUT /api/seller/products/{id}
Authorization: Bearer <access_token>
```

### Delete Product

```http
DELETE /api/seller/products/{id}
Authorization: Bearer <access_token>
```

### Get My Products

```http
GET /api/seller/products?page=0&size=20
Authorization: Bearer <access_token>
```

---

## Admin Endpoints

### Categories (Admin)

```http
POST /api/admin/categories
PUT /api/admin/categories/{id}
DELETE /api/admin/categories/{id}
Authorization: Bearer <access_token>  # Must have ADMIN role
```

### Brands (Admin)

```http
POST /api/admin/brands
PUT /api/admin/brands/{id}
DELETE /api/admin/brands/{id}
Authorization: Bearer <access_token>  # Must have ADMIN role
```

---

## Error Responses

### 400 Bad Request

```json
{
  "success": false,
  "message": "Validation failed",
  "errors": [
    {
      "field": "email",
      "message": "Email is required"
    }
  ]
}
```

### 401 Unauthorized

```json
{
  "success": false,
  "message": "Invalid or expired token"
}
```

### 403 Forbidden

```json
{
  "success": false,
  "message": "You don't have permission to access this resource"
}
```

### 404 Not Found

```json
{
  "success": false,
  "message": "Product not found with id: 999"
}
```

### 409 Conflict

```json
{
  "success": false,
  "message": "Email already exists"
}
```

### 500 Internal Server Error

```json
{
  "success": false,
  "message": "An unexpected error occurred"
}
```

---

## Rate Limiting

API Gateway enforces rate limiting:
- **Default**: 100 requests per minute per IP
- **Authenticated**: 500 requests per minute per user

Rate limit headers:
```
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1704067200
```

---

## Swagger UI

Interactive API documentation available at:
- User Service: http://localhost:8081/swagger-ui.html
- Product Service: http://localhost:8082/swagger-ui.html

---

## Postman Collection

Import the Postman collection from:
```
docs/postman/HyperMall.postman_collection.json
```
