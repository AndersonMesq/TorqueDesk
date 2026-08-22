-- ============================================
-- TENANTS
-- ============================================
create table tenants (
                         id uuid not null,
                         name varchar(255) not null,
                         slug varchar(255) not null,
                         status varchar(255) not null,

                         created_at timestamp(6) not null,
                         updated_at timestamp(6) not null,

                         constraint pk_tenants primary key (id),
                         constraint uk_tenants_slug unique (slug),
                         constraint ck_tenants_status
                             check (status in ('ACTIVE', 'INACTIVE'))
);

-- ============================================
-- USERS
-- ============================================
create table users (
                       id uuid not null,
                       full_name varchar(150) not null,
                       user_name varchar(255),
                       email varchar(255),
                       password varchar(255) not null,
                       system_role varchar(255) not null,
                       enabled boolean not null,

                       created_at timestamp(6) not null,
                       updated_at timestamp(6) not null,

                       constraint pk_users primary key (id),
                       constraint uk_users_username unique (user_name),
                       constraint uk_users_email unique (email),
                       constraint ck_users_system_role
                           check (system_role in ('USER', 'SUPER_ADMIN'))
);

-- ============================================
-- USER TENANTS
-- ============================================
create table user_tenants (
                              id uuid not null,
                              user_id uuid not null,
                              tenant_id uuid not null,
                              role varchar(255) not null,
                              enabled boolean not null,

                              created_at timestamp(6) not null,
                              updated_at timestamp(6) not null,

                              constraint pk_user_tenants primary key (id),

                              constraint uk_user_tenants_user_tenant
                                  unique (user_id, tenant_id),

                              constraint fk_user_tenants_user
                                  foreign key (user_id)
                                      references users (id),

                              constraint fk_user_tenants_tenant
                                  foreign key (tenant_id)
                                      references tenants (id),

                              constraint ck_user_tenants_role
                                  check (role in ('OWNER', 'ATTENDANT', 'MECHANIC'))
);

-- ============================================
-- CUSTOMERS
-- ============================================
create table customers (
                           id uuid not null,
                           tenant_id uuid not null,
                           name varchar(150) not null,
                           email varchar(255) not null,
                           phone varchar(255) not null,

                           created_at timestamp(6) not null,
                           updated_at timestamp(6) not null,

                           constraint pk_customers primary key (id),

                           constraint uk_customers_email
                               unique (email),

                           constraint uk_customers_phone
                               unique (phone),

                           constraint fk_customers_tenant
                               foreign key (tenant_id)
                                   references tenants (id)
);

-- ============================================
-- VEHICLES
-- ============================================
create table vehicles (
                          id uuid not null,
                          customer_id uuid not null,
                          brand varchar(100) not null,
                          model varchar(100) not null,
                          plate varchar(100) not null,
                          model_year integer not null,

                          created_at timestamp(6) not null,
                          updated_at timestamp(6) not null,

                          constraint pk_vehicles primary key (id),

                          constraint fk_vehicles_customer
                              foreign key (customer_id)
                                  references customers (id)
);

-- ============================================
-- SERVICE ORDERS
-- ============================================
create table service_orders (
                                id uuid not null,
                                vehicle_id uuid not null,
                                mechanic_id uuid,
                                description varchar(300) not null,
                                price numeric(10,2),
                                status varchar(255) not null,

                                created_at timestamp(6) not null,
                                updated_at timestamp(6) not null,

                                constraint pk_service_orders primary key (id),

                                constraint fk_service_orders_vehicle
                                    foreign key (vehicle_id)
                                        references vehicles (id),

                                constraint fk_service_orders_mechanic
                                    foreign key (mechanic_id)
                                        references users (id),

                                constraint ck_service_orders_status
                                    check (
                                        status in (
                                                   'OPEN',
                                                   'IN_PROGRESS',
                                                   'COMPLETED',
                                                   'CANCELLED'
                                            )
                                        )
);

-- ============================================
-- SERVICE ORDER ASSIGNMENTS
-- ============================================
create table service_order_assignment (
                                          id uuid not null,
                                          service_order_id uuid not null,
                                          employee_id uuid not null,
                                          started_at timestamp(6) not null,
                                          finished_at timestamp(6),

                                          constraint pk_service_order_assignment
                                              primary key (id),

                                          constraint fk_assignment_service_order
                                              foreign key (service_order_id)
                                                  references service_orders (id),

                                          constraint fk_assignment_employee
                                              foreign key (employee_id)
                                                  references users (id)
);

-- ============================================
-- INDEXES
-- ============================================
create index idx_customers_tenant_id
    on customers (tenant_id);

create index idx_user_tenants_tenant_id
    on user_tenants (tenant_id);

create index idx_service_orders_vehicle_id
    on service_orders (vehicle_id);

create index idx_service_orders_mechanic_id
    on service_orders (mechanic_id);

create index idx_service_order_assignment_service_order_id
    on service_order_assignment (service_order_id);

create index idx_service_order_assignment_employee_id
    on service_order_assignment (employee_id);

create index idx_vehicles_customer_id
    on vehicles (customer_id);