# HyperMall Code Standards and Conventions

## Table of Contents
1. [General Principles](#general-principles)
2. [Java Backend Standards](#java-backend-standards)
3. [TypeScript/React Standards](#typescriptreact-standards)
4. [Database Standards](#database-standards)
5. [API Design Standards](#api-design-standards)
6. [Git Workflow](#git-workflow)
7. [Documentation Standards](#documentation-standards)
8. [Testing Standards](#testing-standards)

---

## 1. General Principles

### Code Quality Goals
- **Readability**: Code should be self-documenting with clear naming
- **Maintainability**: Easy to modify and extend without introducing bugs
- **Performance**: Efficient algorithms and database queries
- **Security**: Input validation, parameterized queries, no secrets in code

### Naming Conventions
- Use meaningful, descriptive names
- Avoid abbreviations unless widely understood (URL, ID, API)
- Use PascalCase for class names, camelCase for variables and methods
- Use SCREAMING_SNAKE_CASE for constants
- Use kebab-case for file names (except Java/TypeScript)

### File Organization
- One class per file (Java)
- One component per file (React)
- Group related files in dedicated directories
- Follow standard project structure conventions

---

## 2. Java Backend Standards

### Project Structure
```
src/main/java/com/hypermall/{service}/
├── {ServiceName}Application.java    # Main class
├── config/                           # Configuration classes
├── controller/                      # REST controllers
├── service/                         # Business logic
├── repository/                      # Data access
├── entity/                          # JPA entities
├── dto/
│   ├── request/                    # Request DTOs
│   └── response/                   # Response DTOs
├── mapper/                         # MapStruct mappers
├── exception/                      # Custom exceptions
├── security/                       # Security-related code
└── util/                          # Utility classes
```

### Package Naming
- Use reverse domain: `com.hypermall.{service-name}`
- All lowercase for package names
- Singular nouns for entity packages

### Class Naming
- Controllers: `{EntityName}Controller.java`
- Services: `{EntityName}Service.java`
- Repositories: `{EntityName}Repository.java`
- Entities: `{EntityName}.java`
- DTOs: `{EntityName}{Request,Response}.java`

### Code Style

#### Method Length
- Maximum 30 lines per method
- Extract complex logic to private methods

#### Constructor Injection (Preferred)
```java
@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }
}
```

#### Use Lombok Wisely
```java
// Good: Use @Data for simple DTOs
@Data
@Builder
public class ProductResponse {
    private Long id;
    private String name;
    private BigDecimal price;
}

// Good: Use @Getter/@Setter for entities
@Entity
@Table(name = "products")
@Getter
@Setter
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;
}
```

### Spring Best Practices

#### Controller Guidelines
```java
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ApiResponse<PageResponse<ProductResponse>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        // Implementation
    }

    @PostMapping
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ApiResponse<ProductResponse> createProduct(
            @Valid @RequestBody CreateProductRequest request,
            @CurrentUser UserPrincipal user) {
        // Implementation
    }
}
```

#### Service Layer
- One service per domain entity
- Use interfaces for service definitions
- Keep business logic in services, not controllers
- Handle transactions at service layer

#### Repository Layer
- Extend `JpaRepository` or `CrudRepository`
- Use custom queries only when necessary
- Prefer method naming conventions over @Query

### Exception Handling
```java
// Custom exception
public class ProductNotFoundException extends ResourceNotFoundException {
    public ProductNotFoundException(Long id) {
        super("Product not found with id: " + id);
    }
}

// Service layer
public Product getProduct(Long id) {
    return productRepository.findById(id)
        .orElseThrow(() -> new ProductNotFoundException(id));
}
```

---

## 3. TypeScript/React Standards

### Project Structure
```
src/
├── components/              # Reusable UI components
│   ├── common/             # Generic components
│   ├── layout/             # Layout components
│   └── {feature}/          # Feature-specific components
├── pages/                  # Page components
├── hooks/                  # Custom hooks
├── services/               # API services
├── store/                  # Redux store
│   └── slices/            # Redux slices
├── types/                  # TypeScript types
├── utils/                  # Utility functions
└── config/                 # Configuration
```

### Naming Conventions

#### Files
- PascalCase for components: `ProductCard.tsx`
- camelCase for utilities: `formatCurrency.ts`
- kebab-case for configs: `api-config.ts`

#### Components
```tsx
// Functional component with TypeScript
interface ProductCardProps {
  product: Product;
  onAddToCart: (productId: number) => void;
}

export const ProductCard: React.FC<ProductCardProps> = ({
  product,
  onAddToCart
}) => {
  return (
    <div className="product-card">
      <h3>{product.name}</h3>
      <button onClick={() => onAddToCart(product.id)}>
        Add to Cart
      </button>
    </div>
  );
};
```

### React Best Practices

#### State Management
- Use Redux Toolkit for global state
- Use useState for component-local state
- Use useReducer for complex state logic

#### Hooks
```tsx
// Custom hook example
export const useProduct = (productId: number) => {
  const [product, setProduct] = useState<Product | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<Error | null>(null);

  useEffect(() => {
    fetchProduct(productId)
      .then(setProduct)
      .catch(setError)
      .finally(() => setLoading(false));
  }, [productId]);

  return { product, loading, error };
};
```

#### API Services
```typescript
// api/productService.ts
import axios from 'axios';
import { Product, ProductListResponse } from '@/types';

const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' }
});

export const productService = {
  getProducts: (params: ProductQueryParams) =>
    api.get<ProductListResponse>('/products', { params }),

  getProduct: (id: number) =>
    api.get<Product>(`/products/${id}`),

  createProduct: (data: CreateProductRequest) =>
    api.post<Product>('/seller/products', data),

  updateProduct: (id: number, data: UpdateProductRequest) =>
    api.put<Product>(`/seller/products/${id}`, data),

  deleteProduct: (id: number) =>
    api.delete(`/seller/products/${id}`)
};
```

### TypeScript Guidelines

#### Interfaces vs Types
```typescript
// Use interface for object shapes
interface User {
  id: number;
  email: string;
  name: string;
}

// Use type for unions, primitives
type OrderStatus = 'pending' | 'confirmed' | 'shipped' | 'delivered';
```

#### Avoid 'any'
```typescript
// Bad
const data: any = fetchData();

// Good
const data: ProductResponse = fetchData();

// If type is unknown
const data: unknown = fetchData();
if (isProductResponse(data)) {
  // use data
}
```

---

## 4. Database Standards

### Naming Conventions

#### Tables
- Singular noun: `user`, `product`, `order`
- Snake_case: `user_address`, `order_item`
- Prefix with service name in shared DB: `hypermall_users`

#### Columns
- Primary key: `id`
- Foreign key: `{table_name}_id` (e.g., `user_id`, `product_id`)
- Timestamps: `created_at`, `updated_at`
- Boolean: `is_active`, `is_deleted`

### Entity Design
```java
@Entity
@Table(name = "products")
@Getter
@Setter
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(unique = true, length = 255)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
```

### Indexing
- Index foreign keys
- Index columns used in WHERE clauses
- Index columns used in ORDER BY
- Use composite indexes for multi-column queries

---

## 5. API Design Standards

### RESTful Conventions

#### URL Structure
- Resource names: plural nouns (`/products`, `/orders`)
- Nested resources: `/orders/{orderId}/items`
- Actions: use HTTP methods, not verbs in URL

#### HTTP Methods
| Method | Usage | Example |
|--------|-------|---------|
| GET | Retrieve | GET /products, GET /products/1 |
| POST | Create | POST /products |
| PUT | Update (full) | PUT /products/1 |
| PATCH | Update (partial) | PATCH /products/1 |
| DELETE | Delete | DELETE /products/1 |

### Response Format
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { }
}
```

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

### Pagination
```json
{
  "success": true,
  "data": {
    "content": [],
    "page": 0,
    "size": 20,
    "totalElements": 100,
    "totalPages": 5,
    "first": true,
    "last": false
  }
}
```

### Error Codes
| Code | Usage |
|------|-------|
| 200 | Success |
| 201 | Created |
| 400 | Bad Request |
| 401 | Unauthorized |
| 403 | Forbidden |
| 404 | Not Found |
| 409 | Conflict |
| 500 | Internal Error |

---

## 6. Git Workflow

### Branch Naming
- Feature: `feature/{ticket-number}-{short-description}`
- Bugfix: `bugfix/{ticket-number}-{short-description}`
- Hotfix: `hotfix/{ticket-number}-{short-description}`
- Example: `feature/HM-123-user-authentication`

### Commit Messages
```
type(scope): description

[optional body]

[optional footer]
```

Types: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`

Example:
```
feat(auth): add password reset functionality

- Add forgot password endpoint
- Add email sending with reset token
- Add reset password endpoint

Closes #123
```

### Pull Request
- Title: Clear description of changes
- Description: What, Why, How
- Link to ticket
- Screenshots for UI changes

---

## 7. Documentation Standards

### Code Comments
- Comment WHY, not WHAT
- Use Javadoc for public APIs
- Keep comments updated with code changes

### README Files
Each service should have a README with:
- Brief description
- Prerequisites
- Configuration
- Running locally
- API endpoints

### API Documentation
- Use Swagger/OpenAPI annotations
- Document all parameters
- Provide request/response examples

---

## 8. Testing Standards

### Backend Testing
```java
@SpringBootTest
class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @Test
    void getProduct_WithValidId_ReturnsProduct() {
        // Given
        Long productId = 1L;

        // When
        Product result = productService.getProduct(productId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(productId);
    }
}
```

### Frontend Testing
```typescript
import { render, screen, fireEvent } from '@testing-library/react';
import { ProductCard } from './ProductCard';

describe('ProductCard', () => {
  it('renders product name', () => {
    const product = { id: 1, name: 'Test Product', price: 100 };
    render(<ProductCard product={product} onAddToCart={() => {}} />);
    expect(screen.getByText('Test Product')).toBeInTheDocument();
  });

  it('calls onAddToCart when button clicked', () => {
    const onAddToCart = vi.fn();
    const product = { id: 1, name: 'Test Product', price: 100 };
    render(<ProductCard product={product} onAddToCart={onAddToCart} />);
    fireEvent.click(screen.getByText('Add to Cart'));
    expect(onAddToCart).toHaveBeenCalledWith(1);
  });
});
```

### Test Coverage Goals
- Minimum 80% coverage for business logic
- 100% coverage for critical paths
- Include integration tests for API endpoints

---

## Code Review Checklist

### Before Submitting PR
- [ ] Code follows naming conventions
- [ ] No hardcoded secrets
- [ ] Unit tests added/updated
- [ ] No console.log/debug code
- [ ] Code formatted (Prettier/Formatter)
- [ ] No TODO comments (or documented)

### Reviewer Checklist
- [ ] Logic is correct
- [ ] Error handling is adequate
- [ ] Performance considerations
- [ ] Security implications
- [ ] Documentation updated

---

## Tools & Configuration

### Backend
- IDE: IntelliJ IDEA (recommended)
- Formatter: google-java-format
- Lombok: Enable annotation processing

### Frontend
- IDE: VS Code (recommended)
- Formatter: Prettier
- Linter: ESLint
- Package Manager: npm

### Git
- Hooks: Husky (pre-commit, commit-msg)
- Merge: Squash and merge PRs

---

*Last Updated: 2024-03-13*
