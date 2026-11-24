-- Akun induk (header) per tipe sebagai titik awal hierarki COA
INSERT INTO account (code, name, type)
VALUES ('1000', 'Aset', 'ASSET'),
       ('2000', 'Liabilitas', 'LIABILITY'),
       ('3000', 'Ekuitas', 'EQUITY'),
       ('4000', 'Pendapatan', 'REVENUE'),
       ('5000', 'Beban', 'EXPENSE');
