INSERT INTO roles(id, role_name) VALUES
(1, 'ROLE_USER'),
(2, 'ROLE_ADMIN');

INSERT INTO users(id, username, email, password) VALUES
(1, 'Mary', 'mary1234@gmail.com', '$2a$12$JQdTEKNfVFd5MgXOPP1/0eYiDCwvQxCJyBBd/Z46oG/Qy9PZw3fri'),
(2, 'Carmen', 'carmen@example.com', '$2a$12$1uJRArQDAExM/rPq1khp.ua4ZWldvOvOlMeaEt/YPld9KDJamx6F2'),
(3, 'Niamh', 'niamh@example.com', '$2a$12$ZsJ1tKF1YSG0PA7QabPDMO3Pb6f1OG.HPcMK9BWVFe1GH7BR6sDQW'),
(4, 'Brian', 'brian@example.com', '$2a$12$DVmuDBhlkLslaR7U0M/vA.hLXYpHYnVcsvnM2k35ISTu7/eXclxMqMary.123');



INSERT INTO users_roles(user_id, role_id) VALUES
(1, 2),
(2, 1),
(3, 1),
(4, 1);

INSERT INTO challenges(id, title, description, status, classification, difficulty_level, prize, user_id) VALUES
(1, 'Read more', 'Read one novel each month for 12 months', 'PENDING', 'PERSONAL_DEVELOPMENT', 3, 'Special Spa day treatment', 1),
(2, 'Eat more fruit and veg', 'Eat one salad and 3 pieces of fruit every day for a month', 'PENDING', 'HEALTH_AND_WELLBEING', 2, 'Trip to the theatre with Sara', 2),
(3, 'Improve social connections by meeting more with the girls', 'Meet for a coffee once each week with Sara, Clara and/or Marie and continue until New Year', 'IN_PROGRESS', 'PERSONAL_RELATIONS', 1, 'Buy myself a 3-session ticket for massage', 1),
(4, 'Go to the gym more', 'Go to the gym 3 times every week all year 2025', 'IN_PROGRESS', 'HEALTH_AND_WELLBEING', 4, 'Go for a weekend away to Killarney', 4),
(5, 'Meditation', 'Meditate at least 5 days a week in the morning first thing for at least 10 mins.', 'IN_PROGRESS', 'HEALTH_AND_WELLBEING', 4, 'Go for a weekend away to Killarney in January.', 4);;