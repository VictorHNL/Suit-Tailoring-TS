create table accounts (
 id uuid primary key, email varchar(254) not null unique, name varchar(255) not null,
 password_hash varchar(255) not null, role varchar(255) not null check (role in ('CUSTOMER','CATALOG','FINANCE','OWNER'))
);
create table addresses (
 id uuid primary key, customer_id uuid not null references accounts(id), street varchar(500) not null,
 city varchar(255) not null, state varchar(2) not null, postal_code varchar(8) not null
);
create index idx_addresses_customer on addresses(customer_id);
create table products (
 id uuid primary key, version bigint not null, name varchar(255) not null, description varchar(4000) not null,
 slug varchar(255) not null unique, category varchar(255) not null, collection_name varchar(255), care varchar(2000),
 audience varchar(255) not null, body_part varchar(255) not null, status varchar(255) not null,
 price numeric(14,2) not null check(price >= 0), promotional_price numeric(14,2),
 video_url varchar(1000), check(promotional_price is null or (promotional_price > 0 and promotional_price <= price))
);
create index idx_products_catalog on products(status,audience,category);
create table product_media (product_id uuid not null references products(id), position integer not null, url varchar(1000), primary key(product_id,position));
create table variants (
 id uuid primary key, product_id uuid not null references products(id), sku varchar(255) not null unique,
 size varchar(255) not null, color varchar(255) not null, stock integer not null check(stock >= 0)
);
create index idx_variants_product on variants(product_id);
create table audit_events (id uuid primary key, actor varchar(255) not null, action varchar(255) not null, target_id uuid not null, created_at timestamp with time zone not null);
