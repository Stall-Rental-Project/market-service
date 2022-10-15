set search_path to mhmarket;

delete
from location cascade;

insert into location (province, city, ward, zipcode, district)
values ('Da Nang', 'Da Nang City', 'Hoa An', '50700', 'Cam Le'),
       ('Da Nang', 'Da Nang City', 'Hoa Phat', '50700', 'Cam Le'),
       ('Da Nang', 'Da Nang City', 'Hoa Tho Dong', '50700', 'Cam Le'),
       ('Da Nang', 'Da Nang City', 'Hoa Tho Tay', '50700', 'Cam Le'),
       ('Da Nang', 'Da Nang City', 'Hoa Xuan', '50700', 'Cam Le'),
       ('Da Nang', 'Da Nang City', 'Khue Trung', '50700', 'Cam Le'),
       --Hai Chau
       ('Da Nang', 'Da Nang City', 'Binh Hien', '50200', 'Hai Chau'),
       ('Da Nang', 'Da Nang City', 'Binh Thuan', '50200', 'Hai Chau'),
       ('Da Nang', 'Da Nang City', 'Hai Chau I', '50200', 'Hai Chau'),
       ('Da Nang', 'Da Nang City', 'Hai Chau II', '50200', 'Hai Chau'),
       ('Da Nang', 'Da Nang City', 'Hoa Cuong Bac', '50200', 'Hai Chau'),
       ('Da Nang', 'Da Nang City', 'Hoa Cuong Nam', '50200', 'Hai Chau'),
       ('Da Nang', 'Da Nang City', 'Hoa Thuan Dong', '50200', 'Hai Chau'),
       ('Da Nang', 'Da Nang City', 'Hoa Thuan Tay', '50200', 'Hai Chau'),
       ('Da Nang', 'Da Nang City', 'Nam Duong', '50200', 'Hai Chau'),
       ('Da Nang', 'Da Nang City', 'Phuoc Ninh', '50200', 'Hai Chau'),
       ('Da Nang', 'Da Nang City', 'Thach Thang', '50200', 'Hai Chau'),
       ('Da Nang', 'Da Nang City', 'Thanh Binh', '50200', 'Hai Chau'),
       ('Da Nang', 'Da Nang City', 'Thuan Phuoc', '50200', 'Hai Chau'),

       --Hoa Vang
       ('Da Nang', 'Da Nang City', 'Hoa Bac', '50800', 'Hoa Vang'),
       ('Da Nang', 'Da Nang City', 'Hoa Chau', '50800', 'Hoa Vang'),
       ('Da Nang', 'Da Nang City', 'Hoa Khuong', '50800', 'Hoa Vang'),
       ('Da Nang', 'Da Nang City', 'Hoa Lien', '50800', 'Hoa Vang'),
       ('Da Nang', 'Da Nang City', 'Hoa Nhon', '50800', 'Hoa Vang'),
       ('Da Nang', 'Da Nang City', 'Hoa Ninh', '50800', 'Hoa Vang'),
       ('Da Nang', 'Da Nang City', 'Hoa Phong', '50800', 'Hoa Vang'),
       ('Da Nang', 'Da Nang City', 'Hoa Phu', '50800', 'Hoa Vang'),
       ('Da Nang', 'Da Nang City', 'Hoa Phuoc', '50800', 'Hoa Vang'),
       ('Da Nang', 'Da Nang City', 'Hoa Son', '50800', 'Hoa Vang'),
       ('Da Nang', 'Da Nang City', 'Hoa Tien', '50800', 'Hoa Vang'),
       --Lien Chieu
       ('Da Nang', 'Da Nang City', 'Hoa Hiep Bac', '50600', 'Lien Chieu'),
       ('Da Nang', 'Da Nang City', 'Hoa Hiep Nam', '50600', 'Lien Chieu'),
       ('Da Nang', 'Da Nang City', 'Hoa Khanh Bac', '50600', 'Lien Chieu'),
       ('Da Nang', 'Da Nang City', 'Hoa Khanh Nam', '50600', 'Lien Chieu'),
       ('Da Nang', 'Da Nang City', 'Hoa Minh', '50600', 'Lien Chieu'),
       -- Ngu Hanh Son
       ('Da Nang', 'Da Nang City', 'Hoa Hai', '50500', 'Ngu Hanh Son'),
       ('Da Nang', 'Da Nang City', 'Hoa Quy', '50500', 'Ngu Hanh Son'),
       ('Da Nang', 'Da Nang City', 'Khue My', '50500', 'Ngu Hanh Son'),
       ('Da Nang', 'Da Nang City', 'My An', '50500', 'Ngu Hanh Son'),
       --Son Tra
       ('Da Nang', 'Da Nang City', 'An Hai Bac', '50400', 'Son Tra'),
       ('Da Nang', 'Da Nang City', 'An Hai Dong', '50400', 'Son Tra'),
       ('Da Nang', 'Da Nang City', 'An Hai Tay', '50400', 'Son Tra'),
       ('Da Nang', 'Da Nang City', 'Man Thai', '50400', 'Son Tra'),
       ('Da Nang', 'Da Nang City', 'Nai Hien Dong', '50400', 'Son Tra'),
       ('Da Nang', 'Da Nang City', 'Phuoc My', '50400', 'Son Tra'),
       ('Da Nang', 'Da Nang City', 'Tho Quang', '50400', 'Son Tra'),
        --Thanh Khe
       ('Da Nang', 'Da Nang City', 'An Khe', '50300', 'Thanh Khe'),
       ('Da Nang', 'Da Nang City', 'Chinh Gian', '50300', 'Thanh Khe'),
       ('Da Nang', 'Da Nang City', 'Hoa Khe', '50300', 'Thanh Khe'),
       ('Da Nang', 'Da Nang City', 'Tam Thuan', '50300', 'Thanh Khe'),
       ('Da Nang', 'Da Nang City', 'Tan Chinh', '50300', 'Thanh Khe'),
       ('Da Nang', 'Da Nang City', 'Thac Gian', '50300', 'Thanh Khe'),
       ('Da Nang', 'Da Nang City', 'Thanh Khe Dong', '50300', 'Thanh Khe'),
       ('Da Nang', 'Da Nang City', 'Thanh Khe Tay', '50300', 'Thanh Khe'),
       ('Da Nang', 'Da Nang City', 'Vinh Trung', '50300', 'Thanh Khe'),
       ('Da Nang', 'Da Nang City', 'Xuan Ha', '50300', 'Thanh Khe');

