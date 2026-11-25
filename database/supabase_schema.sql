-- Minimal Supabase schema for HKUEats
-- Run this in the Supabase SQL editor after creating the project.

create extension if not exists "pgcrypto";

create table if not exists public.restaurants (
    id uuid primary key default gen_random_uuid(),
    name text not null unique,
    cuisine text not null default 'Unspecified',
    location text not null,
    latitude double precision,
    longitude double precision,
    price text not null default '$$',
    rating double precision not null default 0,
    image_uri text,
    created_at timestamptz not null default now(),
    constraint restaurants_valid_coordinates check (
        (latitude is null and longitude is null)
        or (latitude is not null and longitude is not null
            and latitude between -90 and 90 and longitude between -180 and 180)
    )
);

create table if not exists public.reviews (
    id uuid primary key default gen_random_uuid(),
    restaurant_id uuid not null references public.restaurants(id) on delete cascade,
    author text not null,
    rating double precision not null check (rating >= 0 and rating <= 5),
    comment text not null,
    visit_date text,
    image_uri text,
    created_at timestamptz not null default now()
);

create index if not exists restaurants_name_idx on public.restaurants (name);
create index if not exists reviews_restaurant_idx on public.reviews (restaurant_id);

alter table public.restaurants enable row level security;
alter table public.reviews enable row level security;

-- The app uses the anon key for the prototype. For production, tighten these policies.
create policy "Allow public read access to restaurants"
    on public.restaurants for select
    to public using (true);

create policy "Allow public read access to reviews"
    on public.reviews for select
    to public using (true);

create policy "Allow public insert to restaurants"
    on public.restaurants for insert
    to public with check (true);

create policy "Allow public insert to reviews"
    on public.reviews for insert
    to public with check (true);

create table if not exists public.profiles (
    id uuid primary key references auth.users(id) on delete cascade,
    email text not null unique,
    display_name text,
    created_at timestamptz not null default now()
);

alter table public.profiles enable row level security;

-- Allow users to read and update their own profiles
create policy "Public profiles are viewable by everyone"
    on public.profiles for select
    to public using (true);

create policy "Users can insert their own profile"
    on public.profiles for insert
    to public with check (auth.uid() = id);

-- Restaurant photos uploaded from the Add Restaurant screen.
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
