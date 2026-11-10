CREATE TABLE accounts (
                          id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          name        VARCHAR(50)  NOT NULL UNIQUE,
                          owner_type  VARCHAR(20)  NOT NULL,
                          currency    VARCHAR(3)   NOT NULL,
                          created_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE payments (
                          id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          merchant_id  UUID         NOT NULL,
                          amount       BIGINT       NOT NULL,
                          currency     VARCHAR(3)   NOT NULL,
                          status       VARCHAR(20)  NOT NULL,
                          provider_ref VARCHAR(100),
                          idem_key     VARCHAR(255) NOT NULL,
                          version      BIGINT       NOT NULL DEFAULT 0,
                          created_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE idempotency_keys (
                                  merchant_id   UUID         NOT NULL,
                                  idem_key      VARCHAR(255) NOT NULL,
                                  request_hash  VARCHAR(64)  NOT NULL,
                                  response_body TEXT,
                                  status        VARCHAR(20)  NOT NULL,
                                  created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
                                  PRIMARY KEY (merchant_id, idem_key)
);

CREATE TABLE ledger_transactions (
                                     id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                     payment_id  UUID        NOT NULL,
                                     type        VARCHAR(20) NOT NULL,
                                     created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE ledger_entries (
                                id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                transaction_id UUID        NOT NULL,
                                account_id     UUID        NOT NULL,
                                amount         BIGINT      NOT NULL,
                                created_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

INSERT INTO accounts (name, owner_type, currency) VALUES
                                                      ('customer_funds',   'customer',     'INR'),
                                                      ('merchant_payable', 'merchant',     'INR'),
                                                      ('psp_clearing',     'psp_clearing', 'INR');