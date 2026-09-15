alter table invoices add column next_attempt_at timestamp with time zone not null default current_timestamp;
create index idx_invoice_retry on invoices(status,attempts,next_attempt_at);
