SET search_path TO carfix;

CREATE TABLE bookings_services (
                                   booking_id uuid,
                                   service_id int,

                                   CONSTRAINT bookings_services_pk PRIMARY KEY (booking_id,service_id)
);

