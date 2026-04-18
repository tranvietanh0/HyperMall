import argparse
import subprocess
import sys


MYSQL_CONTAINERS = [
    "hypermall-mysql-dev",
    "hypermall-mysql-alt",
]


def run(
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
    result = run(["docker", "ps", "--format", "{{.Names}}"])
    if result.returncode != 0:
        raise RuntimeError(result.stderr.strip() or "Unable to list Docker containers")

    running = {line.strip() for line in result.stdout.splitlines() if line.strip()}
    for container in MYSQL_CONTAINERS:
        if container in running:
            return container

    raise RuntimeError("No running MySQL container was found")


def mysql_exec(container: str, sql: str, database: str | None = None) -> str:
    command = ["docker", "exec", "-i", container, "mysql", "-uroot", "-proot"]
    if database:
        command.extend(["-D", database])
    result = run(command, input_text=sql)
    if result.returncode != 0:
        raise RuntimeError(result.stderr.strip() or "MySQL script failed")
    return result.stdout.strip()


def build_seed_sql(limit: int) -> str:
    return f"""
CREATE TABLE IF NOT EXISTS flash_sales (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  description TEXT NULL,
  banner_image VARCHAR(500) NULL,
  start_time DATETIME NOT NULL,
  end_time DATETIME NOT NULL,
  status VARCHAR(50) NOT NULL,
  created_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_flash_sale_status (status),
  KEY idx_flash_sale_dates (start_time, end_time)
);

CREATE TABLE IF NOT EXISTS flash_sale_products (
  id BIGINT NOT NULL AUTO_INCREMENT,
  flash_sale_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  variant_id BIGINT NULL,
  product_name VARCHAR(255) NOT NULL,
  product_image VARCHAR(500) NULL,
  original_price DECIMAL(15,2) NOT NULL,
  flash_sale_price DECIMAL(15,2) NOT NULL,
  stock_limit INT NOT NULL,
  sold_count INT NOT NULL DEFAULT 0,
  sort_order INT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_fsp_product (product_id),
  KEY idx_fsp_flash_sale (flash_sale_id),
  CONSTRAINT fk_flash_sale_products_flash_sale FOREIGN KEY (flash_sale_id) REFERENCES flash_sales (id) ON DELETE CASCADE
);

START TRANSACTION;

SET @flash_sale_name = 'Weekend Flash Sale';
SET @flash_sale_description = 'Auto-seeded flash sale from local product catalog';
SET @flash_sale_banner = (
  SELECT thumbnail FROM hypermall_products.products WHERE deleted_at IS NULL ORDER BY total_sold DESC, id ASC LIMIT 1
);

DELETE FROM flash_sale_products
WHERE flash_sale_id IN (SELECT id FROM flash_sales WHERE name = @flash_sale_name);

DELETE FROM flash_sales WHERE name = @flash_sale_name;

INSERT INTO flash_sales (name, description, banner_image, start_time, end_time, status, created_at, updated_at)
VALUES (
  @flash_sale_name,
  @flash_sale_description,
  @flash_sale_banner,
  DATE_SUB(NOW(), INTERVAL 1 DAY),
  DATE_ADD(NOW(), INTERVAL 1 DAY),
  'ACTIVE',
  NOW(),
  NOW()
);

SET @flash_sale_id = LAST_INSERT_ID();

INSERT INTO flash_sale_products (
  flash_sale_id,
  product_id,
  variant_id,
  product_name,
  product_image,
  original_price,
  flash_sale_price,
  stock_limit,
  sold_count,
  sort_order
)
SELECT
  @flash_sale_id,
  p.id,
  NULL,
  p.name,
  p.thumbnail,
  p.base_price,
  ROUND(COALESCE(p.sale_price, p.base_price) * 0.85, 2),
  100,
  FLOOR((ROW_NUMBER() OVER (ORDER BY COALESCE(p.sale_price, p.base_price) DESC, p.id ASC) * 7) % 60),
  ROW_NUMBER() OVER (ORDER BY COALESCE(p.sale_price, p.base_price) DESC, p.id ASC) - 1
FROM hypermall_products.products p
WHERE p.deleted_at IS NULL AND p.status = 'ACTIVE'
ORDER BY COALESCE(p.sale_price, p.base_price) DESC, p.id ASC
LIMIT {limit};

COMMIT;

SELECT COUNT(*) AS flash_sales_count FROM flash_sales;
SELECT COUNT(*) AS flash_sale_products_count FROM flash_sale_products WHERE flash_sale_id = @flash_sale_id;
"""


def main() -> int:
    parser = argparse.ArgumentParser(
        description="Seed HyperMall flash sale data from current products"
    )
    parser.add_argument("--limit", type=int, default=6)
    args = parser.parse_args()

    if args.limit <= 0:
        print("[ERROR] --limit must be greater than 0", file=sys.stderr)
        return 1

    try:
        container = detect_mysql_container()
        output = mysql_exec(
            container, build_seed_sql(args.limit), database="hypermall_promotion"
        )
        print(f"Seeded flash sale in container {container}.")
        if output:
            print(output)
        return 0
    except Exception as exc:
        print(f"[ERROR] {exc}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
