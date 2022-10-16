set search_path to mhmarket;

create table supervisor
(
    supervisor_id uuid primary key default gen_random_uuid(),

    first_name varchar(255) not null,
    middle_name varchar(255),
    last_name varchar(255) not null,
    email varchar(255) not null ,
    position varchar(255),
    telephone varchar(255),
    mobile_phone varchar(255),
    market_id uuid not null
);

alter table supervisor
    add constraint supervisor_market_fk foreign key (market_id) references market (market_id) on delete restrict on update cascade;
