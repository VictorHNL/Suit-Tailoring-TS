package br.com.suittailoring.fiscal.application;

import java.util.UUID;

/** External adapter must use invoiceId as provider idempotency key and enforce timeouts. */
public interface FiscalGateway {
  String issue(UUID invoiceId, UUID orderId);
}
