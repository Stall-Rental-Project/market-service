set search_path to mhmarket;

delete from market;
delete from location;

alter table location
    drop column builtin;
alter table location
    drop column barangay;
alter table location
    add column ward varchar(255) not null;