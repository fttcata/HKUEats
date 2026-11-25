-- Apply to an existing HKUEats project to persist restaurant photos.
begin;

alter table public.restaurants
    add column if not exists image_uri text;

insert into storage.buckets (id, name, public)
values ('restaurant-images', 'restaurant-images', true)
on conflict (id) do update set public = true;

drop policy if exists "Allow public read access to restaurant images" on storage.objects;
create policy "Allow public read access to restaurant images"
    on storage.objects for select
    to public using (bucket_id = 'restaurant-images');

drop policy if exists "Allow public upload to restaurant images" on storage.objects;
create policy "Allow public upload to restaurant images"
    on storage.objects for insert
    to public with check (bucket_id = 'restaurant-images');

commit;