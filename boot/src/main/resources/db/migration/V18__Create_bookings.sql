SET search_path TO carfix;

CREATE TABLE bookings (
                          booking_id uuid  DEFAULT gen_random_uuid() PRIMARY KEY,
                          date date  NOT NULL,
                          status text  NOT NULL,
                          start_time timetz  NOT NULL,
                          end_time timetz  NOT NULL,
                          branch_id uuid  NOT NULL,
                          car_profile_id uuid  NOT NULL

                              CONSTRAINT check_bookings_status
                                  CHECK (status IN ('scheduled', 'in_progress', 'completed', 'cancelled'))
);

