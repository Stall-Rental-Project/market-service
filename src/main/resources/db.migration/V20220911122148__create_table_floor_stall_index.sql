set search_path to mhmarket;

create table floor_stall_index
(
    floor_stall_index_id uuid primary key default gen_random_uuid(),
    floor_code varchar(255) not null,
    current_index int default 1
);

create unique index floor_stall_index_unique_floor_code_idx on floor_stall_index using btree (floor_code);