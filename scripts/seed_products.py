import argparse
import json
import re
import subprocess
import sys
import urllib.request
from dataclasses import dataclass
from decimal import Decimal, ROUND_HALF_UP


DEFAULT_API_URL = "https://dummyjson.com/products?limit=100"
DEFAULT_DB_NAME = "hypermall_products"
DEFAULT_USERS_DB_NAME = "hypermall_users"
DEFAULT_MYSQL_PASSWORD = "root"
DEFAULT_SELLER_ID = 1
MYSQL_CONTAINERS = [
    "hypermall-mysql-dev",
    "hypermall-mysql-alt",
]


@dataclass
class MysqlTarget:
    container: str
    password: str
    database: str


def mysql_command(
    target: MysqlTarget, *, database: str | None = None, batch: bool = True
) -> list[str]:
    command = [
        "docker",
        "exec",
        "-i",
        target.container,
        "mysql",
        "-uroot",
        f"-p{target.password}",
    ]
    if database:
        command.extend(["-D", database])
    if batch:
        command.extend(["-N", "-B"])
    return command


def slugify(value: str) -> str:
    slug = re.sub(r"[^a-z0-9]+", "-", value.lower()).strip("-")
    return slug or "item"


def sql_escape(value: str) -> str:
    return value.replace("\\", "\\\\").replace("'", "''")


def run_command(
    command: list[str], *, input_text: str | None = None
) -> subprocess.CompletedProcess:
    return subprocess.run(
        command,
        input=input_text,
        text=True,
        capture_output=True,
        check=False,
    )


def detect_mysql_container() -> str:
    result = run_command(["docker", "ps", "--format", "{{.Names}}"])
    if result.returncode != 0:
        raise RuntimeError(
            result.stderr.strip() or "Unable to inspect Docker containers"
        )

    running = {line.strip() for line in result.stdout.splitlines() if line.strip()}
    for container in MYSQL_CONTAINERS:
        if container in running:
            return container

    raise RuntimeError("No running HyperMall MySQL container was found")


def mysql_exec(target: MysqlTarget, sql: str) -> str:
    command = mysql_command(target, database=target.database)
    command.extend(["-e", sql])
    result = run_command(command)
    if result.returncode != 0:
        raise RuntimeError(result.stderr.strip() or "MySQL command failed")
    return result.stdout.strip()


def fetch_products(api_url: str) -> list[dict]:
    request = urllib.request.Request(
        api_url,
        headers={
            "User-Agent": "HyperMallSeeder/1.0",
            "Accept": "application/json",
        },
    )
    with urllib.request.urlopen(request, timeout=30) as response:
        payload = json.loads(response.read().decode("utf-8"))

    products = payload.get("products", [])
    if not products:
        raise RuntimeError("No products were returned from the public product API")
    return products


def to_decimal(value: float | int) -> Decimal:
    return Decimal(str(value)).quantize(Decimal("0.01"), rounding=ROUND_HALF_UP)


def sale_price(
    base_price: Decimal, discount_percentage: float | int | None
) -> Decimal | None:
    if not discount_percentage or discount_percentage <= 0:
        return None

    discount_ratio = Decimal(str(discount_percentage)) / Decimal("100")
    discounted = (base_price * (Decimal("1") - discount_ratio)).quantize(
        Decimal("0.01"), rounding=ROUND_HALF_UP
    )
    if discounted <= 0 or discounted >= base_price:
        return None
    return discounted


def shorten(text: str, max_length: int) -> str:
    if len(text) <= max_length:
        return text
    return text[: max_length - 3].rstrip() + "..."


def validate_limit(limit: int) -> int:
    if limit <= 0:
        raise ValueError("--limit phai lon hon 0")
    return limit


def normalise_images(product: dict) -> list[str]:
    image_urls: list[str] = []
    thumbnail = product.get("thumbnail")
    if thumbnail:
        image_urls.append(thumbnail)

    for image_url in product.get("images", []):
        if image_url and image_url not in image_urls:
            image_urls.append(image_url)

    return image_urls[:6]


def validate_seller(target: MysqlTarget, users_database: str, seller_id: int) -> None:
    sql = f"SELECT COUNT(*) FROM users WHERE id = {seller_id};"
    command = mysql_command(target, database=users_database)
    command.extend(["-e", sql])
    result = run_command(command)
    if result.returncode != 0:
        raise RuntimeError(
            result.stderr.strip()
            or "Unable to validate the seller in the user database"
        )

    if result.stdout.strip() != "1":
        raise RuntimeError(
            f"Seller ID {seller_id} does not exist in database {users_database}"
        )


def ensure_product(
    target: MysqlTarget,
    product: dict,
    *,
    seller_id: int,
) -> int:
    title = product["title"].strip()
    slug = slugify(f"dummyjson-{product['id']}-{title}")
    description = product.get("description", "").strip()
    short_description = shorten(description or title, 180)
    image_urls = normalise_images(product)
    if not image_urls:
        raise RuntimeError(f"Product '{title}' does not have a valid image to seed")

    thumbnail = image_urls[0]
    base_price = to_decimal(product.get("price", 0))
    discount_price = sale_price(base_price, product.get("discountPercentage"))
    status = "ACTIVE"
    sale_sql = "NULL" if discount_price is None else str(discount_price)
    category_name = product.get("category", "general").replace("-", " ").title()
    category_slug = slugify(category_name)
    brand_name = product.get("brand")
    brand_slug = slugify(brand_name) if brand_name else None
    category_image_sql = f"'{sql_escape(thumbnail)}'"
    brand_logo_sql = f"'{sql_escape(thumbnail)}'" if brand_name else "NULL"
    brand_insert_sql = "SET @brand_id = NULL;"

    if brand_name and brand_slug:
        brand_insert_sql = f"""
INSERT INTO brands (name, slug, logo, description, created_at, updated_at)
VALUES ('{sql_escape(brand_name)}', '{brand_slug}', {brand_logo_sql}, 'Imported from public product API', NOW(), NOW())
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  logo = COALESCE(brands.logo, VALUES(logo)),
  updated_at = NOW(),
  id = LAST_INSERT_ID(id);
SET @brand_id = LAST_INSERT_ID();
"""

    image_values = ",\n  ".join(
        f"(@product_id, '{sql_escape(image_url)}', {index}, {1 if index == 0 else 0})"
        for index, image_url in enumerate(image_urls)
    )

    sql = f"""
START TRANSACTION;
INSERT INTO categories (name, slug, description, image, parent_id, level, sort_order, is_active, created_at, updated_at)
VALUES ('{sql_escape(category_name)}', '{category_slug}', 'Imported from public product API', {category_image_sql}, NULL, 0, 0, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  image = COALESCE(categories.image, VALUES(image)),
  updated_at = NOW(),
  id = LAST_INSERT_ID(id);
SET @category_id = LAST_INSERT_ID();

{brand_insert_sql}

INSERT INTO products (
  seller_id, category_id, brand_id, name, slug, description, short_description, thumbnail,
  base_price, sale_price, status, total_sold, avg_rating, total_reviews, has_variants,
  created_at, updated_at, deleted_at
)
VALUES (
  {seller_id}, @category_id, @brand_id, '{sql_escape(title)}', '{slug}',
  '{sql_escape(description)}', '{sql_escape(short_description)}', '{sql_escape(thumbnail)}',
  {base_price}, {sale_sql}, '{status}', 0, 0.00, 0, 0, NOW(), NOW(), NULL
)
ON DUPLICATE KEY UPDATE
  category_id = VALUES(category_id),
  brand_id = VALUES(brand_id),
  name = VALUES(name),
  description = VALUES(description),
  short_description = VALUES(short_description),
  thumbnail = VALUES(thumbnail),
  base_price = VALUES(base_price),
  sale_price = VALUES(sale_price),
  status = VALUES(status),
  updated_at = NOW(),
  deleted_at = NULL,
  id = LAST_INSERT_ID(id);
SET @product_id = LAST_INSERT_ID();

DELETE FROM product_images WHERE product_id = @product_id;
INSERT INTO product_images (product_id, url, sort_order, is_main)
VALUES
  {image_values};

COMMIT;
SELECT @product_id;
"""
    return int(mysql_exec(target, sql).splitlines()[-1])


def main() -> int:
    parser = argparse.ArgumentParser(
        description="Seed HyperMall products from a public product API"
    )
    parser.add_argument("--api-url", default=DEFAULT_API_URL)
    parser.add_argument("--database", default=DEFAULT_DB_NAME)
    parser.add_argument("--users-database", default=DEFAULT_USERS_DB_NAME)
    parser.add_argument("--mysql-password", default=DEFAULT_MYSQL_PASSWORD)
    parser.add_argument("--seller-id", type=int, default=DEFAULT_SELLER_ID)
    parser.add_argument("--limit", type=int, default=30)
    args = parser.parse_args()

    try:
        target = MysqlTarget(
            container=detect_mysql_container(),
            password=args.mysql_password,
            database=args.database,
        )
        limit = validate_limit(args.limit)
        validate_seller(target, args.users_database, args.seller_id)
        source_products = fetch_products(args.api_url)[:limit]

        inserted = 0
        for source_product in source_products:
            ensure_product(
                target,
                source_product,
                seller_id=args.seller_id,
            )
            inserted += 1

        categories = mysql_exec(target, "SELECT COUNT(*) FROM categories;")
        brands = mysql_exec(target, "SELECT COUNT(*) FROM brands;")
        products = mysql_exec(
            target, "SELECT COUNT(*) FROM products WHERE deleted_at IS NULL;"
        )

        print(
            f"Seeded {inserted} products from public API into {target.database} via container {target.container}."
        )
        print(
            f"Current totals -> categories: {categories}, brands: {brands}, products: {products}"
        )
        return 0
    except Exception as exc:
        print(f"[ERROR] {exc}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
