INSERT INTO roles(id, role_name) VALUES
(1, 'ROLE_USER'),
(2, 'ROLE_ADMIN');

INSERT INTO users(id, username, email, password) VALUES
(1, 'Mary', 'mary1234@gmail.com', '$2a$12$ujrtJeyCVy992nYx8SJ8i.b0lLycVo9D5beF8/OOWj.pt1uSFpzHq'),
(2, 'Carmen', 'carmen@example.com', '$2a$12$mr15uTxw.QQUkbeEEO850ekrpIbTUnbuLJv9id/bnxGm4b1cHPuSO'),
(3, 'Niamh', 'niamh@example.com', '$2a$12$cQRHt31sbvaFOsYKMVwZy.C9mhIOCRkfcbJWg4.H/HJnlxQsU7OiC'),
(4, 'Brian', 'brian@example.com', '$2a$12$cQRHt31sbvaFOsYKMVwZy.C9mhIOCRkfcbJWg4.H/HJnlxQsU7OiC');


INSERT INTO users_roles(user_id, role_id) VALUES
(1, 2),
(2, 1),
(3, 1),
(4, 1);

INSERT INTO challenges(id, title, description, status, classification, difficulty_level, prize, user_id) VALUES
(1, 'Read more', 'Read one novel each month for 12 months', 'PENDING', 'PERSONAL_DEVELOPMENT', 3, 'Special Spa day treatment', 1),
(2, 'Eat more fruit and veg', 'Eat one salad and 3 pieces of fruit every day for a month', 'PENDING', 'HEALTH_AND_WELLBEING', 2, 'Trip to the theatre with Sara', 2);