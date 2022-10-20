set search_path to mhmarket;

alter table market
    alter column updated_at drop not null ;