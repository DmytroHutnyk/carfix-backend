SET search_path TO carfix;

CREATE TABLE bookings_services (
                                   booking_id uuid,
                                   service_id int,
                                   start_time time  NOT NULL,
                                   end_time time  NOT NULL,
                                   price decimal(7,2)  NOT NULL,

                                   CONSTRAINT bookings_services_pk PRIMARY KEY (booking_id,service_id)
);

