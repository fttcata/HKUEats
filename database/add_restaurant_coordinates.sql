-- Apply to an existing HKUEats project before saving restaurants with coordinates.
begin;

alter table public.restaurants
    add column if not exists latitude double precision,
    add column if not exists longitude double precision;

do $$
begin
    if not exists (
        select 1 from pg_constraint
        where conrelid = 'public.restaurants'::regclass
          and conname = 'restaurants_valid_coordinates'
    ) then
        alter table public.restaurants add constraint restaurants_valid_coordinates check (
            (latitude is null and longitude is null)
            or (latitude is not null and longitude is not null
                and latitude between -90 and 90 and longitude between -180 and 180)
        );
    end if;
end;
$$;

commit;
