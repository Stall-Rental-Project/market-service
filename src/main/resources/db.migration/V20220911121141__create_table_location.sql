set search_path to mhmarket;

create table location
(
    location_id uuid primary key default gen_random_uuid(),
    province varchar(255) not null,
    city varchar(255) not null,
    barangay varchar(255) not null,
    zipcode varchar(255),
    district varchar(255),
    builtin boolean default false
);

create unique index location_lookup_uidx on location using btree (province, city, barangay);

