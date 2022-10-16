set search_path to mhmarket;


alter table market
    add column previous_version boolean default false;
alter table market
    add column deleted boolean default false;
alter table market
    add column state integer not null;
