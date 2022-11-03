set search_path to mhmarket;

alter table floorplan
    add column image_url text default '' not null ;
