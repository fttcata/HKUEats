-- Optional demo data for a Supabase project with the coordinate columns.
insert into public.restaurants (name, cuisine, location, latitude, longitude, price, rating)
values
    ('CYM Canteen', 'Hong Kong', 'HKU Main Campus', 22.28271, 114.13914, '$$', 4.4),
    ('Mido Cafe', 'Hong Kong', 'Yau Ma Tei', 22.31023, 114.17037, '$', 4.2),
    ('Starbucks Coffee (Composite Building)', 'Cafe', 'HKU Main Campus', 22.28317, 114.13590, '$$', 4.6),
    ('BIJAS Vegetarian', 'Vegetarian', 'HKU Centennial Campus', 22.28372, 114.13443, '$', 4.1)
on conflict (name) do update set
    location = excluded.location,
    latitude = excluded.latitude,
    longitude = excluded.longitude
where restaurants.latitude is null and restaurants.longitude is null;
