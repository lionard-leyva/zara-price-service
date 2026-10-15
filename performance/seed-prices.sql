DROP TABLE IF EXISTS PRICES;
CREATE TABLE PRICES (
  id          BIGSERIAL PRIMARY KEY,
  brand_id    BIGINT        NOT NULL,
  product_id  BIGINT        NOT NULL,
  price_list  INT           NOT NULL,
  start_date  TIMESTAMP     NOT NULL,
  end_date    TIMESTAMP     NOT NULL,
  priority    INT           NOT NULL,
  price       DECIMAL(10,2) NOT NULL,
  curr        VARCHAR(3)    NOT NULL
);

-- 2 brands x 100,000 products x 10 price rows = 2,000,000 rows
INSERT INTO PRICES (brand_id, product_id, price_list, start_date, end_date, priority, price, curr)
SELECT
  b.brand_id,
  p.product_id,
  r.n,
  TIMESTAMP '2020-01-01' + (r.n * INTERVAL '3 days') + (p.product_id % 7) * INTERVAL '1 hour',
  TIMESTAMP '2020-01-01' + (r.n * INTERVAL '3 days') + INTERVAL '2 days 23 hours',
  (r.n + p.product_id) % 3,
  round((10 + random() * 90)::numeric, 2),
  'EUR'
FROM generate_series(1, 2) AS b(brand_id)
CROSS JOIN generate_series(1, 100000) AS p(product_id)
CROSS JOIN generate_series(0, 9) AS r(n);

CREATE INDEX idx_prices_lookup ON PRICES (brand_id, product_id, start_date, end_date);
ANALYZE PRICES;
SELECT count(*) AS rows FROM PRICES;
