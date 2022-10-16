set search_path to mhmarket;


alter table market
    drop column previous_version;

alter table market
    add column previous_version uuid;
