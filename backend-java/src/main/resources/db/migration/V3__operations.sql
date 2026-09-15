create table ledger_entries (
 id uuid primary key, order_id uuid references purchase_orders(id), kind varchar(255) not null,
 amount numeric(14,2) not null check(amount > 0), description varchar(500) not null,
 created_at timestamp with time zone not null, unique(order_id,kind)
);
create table invoices (
 id uuid primary key, order_id uuid not null unique references purchase_orders(id), status varchar(255) not null,
 attempts integer not null, last_error varchar(500), document_url varchar(1000), created_at timestamp with time zone not null
);
create table customer_notes (
 id uuid primary key, customer_id uuid not null references accounts(id), content varchar(2000) not null,
 author varchar(255) not null, created_at timestamp with time zone not null
);
create index idx_notes_customer on customer_notes(customer_id,created_at);
