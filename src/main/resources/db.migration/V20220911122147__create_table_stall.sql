set search_path to mhmarket;

create table stall
(
    stall_id                uuid primary key default gen_random_uuid(),
    market_id               uuid         not null,
    floorplan_id            uuid         not null,
    name                    varchar(255) not null,
    code                    varchar(255) not null,
    state                   integer      not null,
    status                  integer      not null,
    type                    integer      not null,
    previous_version        uuid,
    deleted                 boolean          default false,
    area                    double precision,
    class                   integer      not null,
    shape                   varchar(255),
    x_axis                  double precision,
    y_axis                  double precision,
    w_axis                  double precision,
    h_axis                  double precision,
    points                  text,
    label                   text,
    color                   text,
    occupied_by             uuid,
    lease_status            integer,
    published_at_least_once boolean          default false,
    cloned_from             uuid,
    font_size               double precision,
    rotate                  int,
    created_at              timestamptz  not null,
    updated_at              timestamptz
);

alter table stall
    add constraint stall_floorplan_fk foreign key (floorplan_id) references floorplan (floorplan_id) on delete restrict on update cascade;

alter table stall
    add constraint stall_market_fk foreign key (market_id) references market (market_id) on delete cascade on update cascade;