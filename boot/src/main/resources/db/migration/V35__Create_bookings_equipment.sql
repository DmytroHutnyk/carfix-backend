SET search_path TO carfix;

CREATE TABLE bookings_equipment (
                                    booking_id uuid,
                                    equipment_id int,

                                    CONSTRAINT bookings_equipment_pk PRIMARY KEY (equipment_id,booking_id)
);

