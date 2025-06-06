INSERT INTO USER (id, email, password, name, phone, point, role, created_at, updated_at)
VALUES (
           UNHEX(REPLACE('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '-', '')),
           'testuser@example.com',
           'password123',
           'Test User',
           '010-1234-5678',
           10000,
           'USER',
           NOW(),
           NOW()
       );

INSERT INTO CONCERT (id, name)
VALUES
    (1, 'Concert_01'),
    (2, 'Concert_02');
INSERT INTO CONCERT_SCHEDULE (id, concert_id, schedule_at)
VALUES
    (1, 1, CONVERT_TZ('2025-06-10 19:00:00', 'Asia/Seoul', 'UTC')),
    (2, 1,  CONVERT_TZ('2025-06-11 20:00:00', 'Asia/Seoul', 'UTC')),
    (3, 2,  CONVERT_TZ('2025-06-06 11:51:00', 'Asia/Seoul', 'UTC'));
-- schedule_id = 1, seat number 1~50
INSERT INTO SEAT (id, concert_schedule_id, number, price, status)
VALUES
    (1, 1, 1, 50000, 'AVAILABLE'),
    (2, 1, 2, 50000, 'AVAILABLE'),
    (3, 1, 3, 50000, 'AVAILABLE'),
    (4, 1, 4, 50000, 'AVAILABLE'),
    (5, 1, 5, 50000, 'AVAILABLE'),
    (6, 1, 6, 50000, 'AVAILABLE'),
    (7, 1, 7, 50000, 'AVAILABLE'),
    (8, 1, 8, 50000, 'AVAILABLE'),
    (9, 1, 9, 50000, 'AVAILABLE'),
    (10, 1, 10, 50000, 'AVAILABLE'),
    (11, 1, 11, 50000, 'AVAILABLE'),
    (12, 1, 12, 50000, 'AVAILABLE'),
    (13, 1, 13, 50000, 'AVAILABLE'),
    (14, 1, 14, 50000, 'AVAILABLE'),
    (15, 1, 15, 50000, 'AVAILABLE'),
    (16, 1, 16, 50000, 'AVAILABLE'),
    (17, 1, 17, 50000, 'AVAILABLE'),
    (18, 1, 18, 50000, 'AVAILABLE'),
    (19, 1, 19, 50000, 'AVAILABLE'),
    (20, 1, 20, 50000, 'AVAILABLE'),
    (21, 1, 21, 50000, 'AVAILABLE'),
    (22, 1, 22, 50000, 'AVAILABLE'),
    (23, 1, 23, 50000, 'AVAILABLE'),
    (24, 1, 24, 50000, 'AVAILABLE'),
    (25, 1, 25, 50000, 'AVAILABLE'),
    (26, 1, 26, 50000, 'AVAILABLE'),
    (27, 1, 27, 50000, 'AVAILABLE'),
    (28, 1, 28, 50000, 'AVAILABLE'),
    (29, 1, 29, 50000, 'AVAILABLE'),
    (30, 1, 30, 50000, 'AVAILABLE'),
    (31, 1, 31, 50000, 'AVAILABLE'),
    (32, 1, 32, 50000, 'AVAILABLE'),
    (33, 1, 33, 50000, 'AVAILABLE'),
    (34, 1, 34, 50000, 'AVAILABLE'),
    (35, 1, 35, 50000, 'AVAILABLE'),
    (36, 1, 36, 50000, 'AVAILABLE'),
    (37, 1, 37, 50000, 'AVAILABLE'),
    (38, 1, 38, 50000, 'AVAILABLE'),
    (39, 1, 39, 50000, 'AVAILABLE'),
    (40, 1, 40, 50000, 'AVAILABLE'),
    (41, 1, 41, 50000, 'AVAILABLE'),
    (42, 1, 42, 50000, 'AVAILABLE'),
    (43, 1, 43, 50000, 'AVAILABLE'),
    (44, 1, 44, 50000, 'AVAILABLE'),
    (45, 1, 45, 50000, 'AVAILABLE'),
    (46, 1, 46, 50000, 'AVAILABLE'),
    (47, 1, 47, 50000, 'AVAILABLE'),
    (48, 1, 48, 50000, 'AVAILABLE'),
    (49, 1, 49, 50000, 'AVAILABLE'),
    (50, 1, 50, 50000, 'AVAILABLE');
-- schedule_id = 2, seat number 1~50
INSERT INTO SEAT (id, concert_schedule_id, number, price, status)
VALUES
    (51, 2, 1, 50000, 'AVAILABLE'),
    (52, 2, 2, 50000, 'AVAILABLE'),
    (53, 2, 3, 50000, 'AVAILABLE'),
    (54, 2, 4, 50000, 'AVAILABLE'),
    (55, 2, 5, 50000, 'AVAILABLE'),
    (56, 2, 6, 50000, 'AVAILABLE'),
    (57, 2, 7, 50000, 'AVAILABLE'),
    (58, 2, 8, 50000, 'AVAILABLE'),
    (59, 2, 9, 50000, 'AVAILABLE'),
    (60, 2, 10, 50000, 'AVAILABLE'),
    (61, 2, 11, 50000, 'AVAILABLE'),
    (62, 2, 12, 50000, 'AVAILABLE'),
    (63, 2, 13, 50000, 'AVAILABLE'),
    (64, 2, 14, 50000, 'AVAILABLE'),
    (65, 2, 15, 50000, 'AVAILABLE'),
    (66, 2, 16, 50000, 'AVAILABLE'),
    (67, 2, 17, 50000, 'AVAILABLE'),
    (68, 2, 18, 50000, 'AVAILABLE'),
    (69, 2, 19, 50000, 'AVAILABLE'),
    (70, 2, 20, 50000, 'AVAILABLE'),
    (71, 2, 21, 50000, 'AVAILABLE'),
    (72, 2, 22, 50000, 'AVAILABLE'),
    (73, 2, 23, 50000, 'AVAILABLE'),
    (74, 2, 24, 50000, 'AVAILABLE'),
    (75, 2, 25, 50000, 'AVAILABLE'),
    (76, 2, 26, 50000, 'AVAILABLE'),
    (77, 2, 27, 50000, 'AVAILABLE'),
    (78, 2, 28, 50000, 'AVAILABLE'),
    (79, 2, 29, 50000, 'AVAILABLE'),
    (80, 2, 30, 50000, 'AVAILABLE'),
    (81, 2, 31, 50000, 'AVAILABLE'),
    (82, 2, 32, 50000, 'AVAILABLE'),
    (83, 2, 33, 50000, 'AVAILABLE'),
    (84, 2, 34, 50000, 'AVAILABLE'),
    (85, 2, 35, 50000, 'AVAILABLE'),
    (86, 2, 36, 50000, 'AVAILABLE'),
    (87, 2, 37, 50000, 'AVAILABLE'),
    (88, 2, 38, 50000, 'AVAILABLE'),
    (89, 2, 39, 50000, 'AVAILABLE'),
    (90, 2, 40, 50000, 'AVAILABLE'),
    (91, 2, 41, 50000, 'AVAILABLE'),
    (92, 2, 42, 50000, 'AVAILABLE'),
    (93, 2, 43, 50000, 'AVAILABLE'),
    (94, 2, 44, 50000, 'AVAILABLE'),
    (95, 2, 45, 50000, 'AVAILABLE'),
    (96, 2, 46, 50000, 'AVAILABLE'),
    (97, 2, 47, 50000, 'AVAILABLE'),
    (98, 2, 48, 50000, 'AVAILABLE'),
    (99, 2, 49, 50000, 'AVAILABLE'),
    (100, 2, 50, 50000, 'AVAILABLE');

INSERT INTO POINT_HISTORY (id, user_id, type, amount, current_point, created_at)
VALUES
    (1, UNHEX(REPLACE('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '-', '')), 'CHARGE', 5000, 5000, NOW() - INTERVAL 5 DAY),
    (2, UNHEX(REPLACE('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '-', '')), 'USE', 1000, 4000, NOW() - INTERVAL 4 DAY),
    (3, UNHEX(REPLACE('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '-', '')), 'CHARGE', 3000, 7000, NOW() - INTERVAL 3 DAY),
    (4, UNHEX(REPLACE('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '-', '')), 'USE', 2000, 5000, NOW() - INTERVAL 2 DAY),
    (5, UNHEX(REPLACE('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '-', '')), 'CHARGE', 5000, 10000, NOW() - INTERVAL 1 DAY);

--콘서트 첫 번째 스케줄의 1번 좌석을 한 유저가 결제 → 좌석 예약 완료 상태
--1) USER 포인트 차감
UPDATE USER
SET point = point - 50000
WHERE id = UNHEX(REPLACE('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '-', ''));
--2) POINT_HISTORY 삽입 (포인트 사용)
INSERT INTO POINT_HISTORY (id, user_id, type, amount, current_point, created_at)
VALUES (6, UNHEX(REPLACE('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '-', '')), 'USE', 50000, 5000, NOW());
--3) PAY_HISTORY 삽입
INSERT INTO PAY_HISTORY (
    id, user_id, email, concert_id, concert_name,
    concert_schedule_id, schedule_at,
    seat_id, seat_number, seat_price,
    amount, pay_at, status, reason
)
VALUES (1, UNHEX(REPLACE('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '-', '')), 'testuser@example.com', 1, 'Test Concert',
        1, '2025-06-10 19:00:00',
        1, 1, 50000,
        50000, NOW(), 'SUCCESS', NULL);
--4) SEAT 예약 처리
UPDATE SEAT
SET user_id = UNHEX(REPLACE('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '-', '')),
    status = 'RESERVED',
    reserved_at = NOW()
WHERE id = 1;
--5) RESERVATION_TOKEN (expired)
INSERT INTO RESERVATION_TOKEN (id, user_id, concert_schedule_id, `order`, status, issued_at, expired_at)
VALUES (UNHEX(REPLACE('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '-', '')),
        UNHEX(REPLACE('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '-', '')),
        1, 1,'EXPIRED', NOW(), NULL);