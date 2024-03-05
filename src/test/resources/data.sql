INSERT INTO user_data
    (user_id,
    account_non_expired,
    account_non_locked,
    affiliation,
    credentials_non_expired,
    email,
    enabled,
    first_name,
    last_name,
    password,
    role,
    username)
VALUES
    ('87f8304e-4740-45e6-9934-1bce37ac3d1b',
    true,
    true,
    'MedTech Chain',
    true,
    'admin.test@medtechchain.nl',
    true,
    'Admin',
    'Test',
    '$2a$12$p7ghltJaGDIM9txtLU7O.O9xbcaXNz/nPfx.GAraFZCPUlaDCY4aa',
    0,
    'iandrews')
ON CONFLICT (user_id) DO NOTHING;


