create table carts (id uuid primary key, customer_id uuid references accounts(id), consumed boolean not null, created_at timestamp with time zone not null);
create table cart_items (cart_id uuid not null references carts(id), variant_id uuid not null references variants(id), quantity integer not null check(quantity between 1 and 99), primary key(cart_id,variant_id));
create table purchase_orders (
 id uuid primary key, customer_id uuid not null references accounts(id), cart_id uuid not null references carts(id),
 address_id uuid not null, idempotency_key varchar(80) not null, shipping_address varchar(1000) not null,
 total numeric(14,2) not null check(total > 0), status varchar(255) not null,
 created_at timestamp with time zone not null, expires_at timestamp with time zone not null, tracking_code varchar(255),
 unique(customer_id,idempotency_key), unique(cart_id)
);
create index idx_orders_customer on purchase_orders(customer_id,created_at);
create index idx_orders_expiration on purchase_orders(status,expires_at);
create table order_lines (
 order_id uuid not null references purchase_orders(id), position integer not null, variant_id uuid not null references variants(id),
 product_name varchar(255) not null, sku varchar(255) not null, quantity integer not null check(quantity > 0),
 unit_price numeric(14,2) not null check(unit_price > 0), primary key(order_id,position)
);
