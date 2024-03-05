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
    ('28f2124c-e6eb-4b99-a749-4a54ea0bccb6',
    true,
    true,
    'MedTech Chain',
    true,
    'ivan.andrews@medtechchain.nl',
    true,
    'Ivan',
    'Andrews',
    '$2a$12$6eC3khhEr21kFmKQrUWl.Opl6/ol.0pdVlnOe/0ehlDqZZe/9rE5i',
    0,
    'iandrews')
ON CONFLICT (user_id) DO NOTHING;


