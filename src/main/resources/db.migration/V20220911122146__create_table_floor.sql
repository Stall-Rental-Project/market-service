set search_path to mhmarket;

create table floorplan
(
    floorplan_id            uuid primary key default gen_random_uuid(),
    market_id               uuid         not null,
    name                    varchar(255) not null,
    image                   text         not null,
    code                    varchar(255) not null,
    state                   integer      not null,
    previous_version        uuid,
    deleted                 boolean          default false,
    published_at_least_once boolean          default false,
    created_at              timestamptz  not null,
    updated_at              timestamptz
);

alter table floorplan
    add constraint floorplan_market_fk foreign key (market_id) references market (market_id) on delete cascade on update cascade;