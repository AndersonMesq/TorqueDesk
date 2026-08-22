-- ============================================
-- V2 - Initial SUPER_ADMIN
-- TorqueDesk
-- ============================================

insert into users (
    id,
    full_name,
    email,
    password,
    system_role,
    enabled,
    created_at,
    updated_at
)
select
    '00000000-0000-0000-0000-000000000001',
    'System Administrator',
    'admin@torquedesk.com',
    '$2y$10$XKVfrB9SsoX1tr5ydwTyTOTEf19qWFTN6PMqzMNGBQJNtwce44wLW',
    'SUPER_ADMIN',
    true,
    current_timestamp,
    current_timestamp
    where not exists (
    select 1
    from users
    where email = 'admin@torquedesk.com'
);