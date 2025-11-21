SET search_path TO carfix;

CREATE TABLE reviews (
                         review_id uuid  DEFAULT gen_random_uuid() PRIMARY KEY,
                         stars_number int  NOT NULL,
                         contents text,
                         booking_id uuid  NOT NULL
);

