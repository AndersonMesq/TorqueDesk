ALTER TABLE service_orders
    RENAME COLUMN mechanic_id TO created_by_id;

ALTER TABLE service_order_assignment
    RENAME TO service_order_assignments;