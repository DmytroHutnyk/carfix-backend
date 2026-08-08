SET search_path TO carfix;

CREATE TABLE bookings (
                          booking_id uuid  DEFAULT gen_random_uuid() PRIMARY KEY,
                          date date  NOT NULL,
                          status text  NOT NULL,
                          start_time time  NOT NULL,
                          end_time time  NOT NULL,
                          branch_id uuid  NOT NULL,
                          car_profile_id uuid  NOT NULL,
                          created_at timestamp  NOT NULL DEFAULT now(),

                          CONSTRAINT check_bookings_status
                              CHECK (status IN ('SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'))
);

