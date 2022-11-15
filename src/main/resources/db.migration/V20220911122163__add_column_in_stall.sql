set search_path to mhmarket;

alter table stall
    add column is_updated_detail boolean not null default false;