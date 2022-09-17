set search_path to mhmarket;

create table market
(
    market_id      uuid primary key default gen_random_uuid(),
    name           varchar(255) not null,
    code varchar(255) not null ,
    google_map text not null ,
    location_id    uuid       not null,
    address text         not null,
    status         integer      not null default 0, -- 0 -> inactive, 1 -> active
    type           integer      not null,
    class          integer,
    created_at     timestamptz  not null,
    updated_at     timestamptz  not null
);

alter table market
    add constraint market_location_fk foreign key (location_id) references location (location_id) on delete restrict on update cascade;
