SET search_path TO carfix;

ALTER TABLE branches
    ADD COLUMN description text,
    ADD COLUMN cancellation_policy text;

-- Seeds for the workshop page; 'Serwis Ursus' stays NULL/NULL on purpose —
-- it exercises the hidden Description/Cancellation cards (same trick as V57's NULL rating).
UPDATE branches SET
    description = 'Professional automotive service center specializing in brake systems, engine diagnostics, and general maintenance. We use only original parts and provide warranty on all services.',
    cancellation_policy = 'Free cancellation up to 24 hours before the appointment. Cancellations within 24 hours may incur a 50 PLN fee.'
WHERE name = 'AutoSerwis Kowalski Mokotow';

UPDATE branches SET
    description = 'Full-service garage in Wola handling everything from routine maintenance to complex engine repairs. Experienced mechanics and transparent pricing.',
    cancellation_policy = 'Free cancellation up to 24 hours before the appointment. Later cancellations may incur a fee of up to 20% of the booked services.'
WHERE name = 'AutoSerwis Kowalski Wola';

UPDATE branches SET
    description = 'Krakow-based service center focused on suspension, brakes and periodic inspections. Modern diagnostic equipment and certified staff.',
    cancellation_policy = 'Free cancellation up to 24 hours before the appointment. Cancellations within 24 hours may incur a 60 PLN fee.'
WHERE name = 'AutoSerwis Kowalski Podgorze';

UPDATE branches SET
    description = 'Tire specialists: seasonal changes, balancing, storage and repairs. Quick service while you wait.',
    cancellation_policy = 'Free cancellation up to 12 hours before the appointment.'
WHERE name = 'Opony Express Praga';

UPDATE branches SET
    description = 'Tire and wheel service in Nowa Huta. Seasonal tire swaps, TPMS service and alignment checks.',
    cancellation_policy = 'Free cancellation up to 12 hours before the appointment.'
WHERE name = 'Opony Express Nowa Huta';

UPDATE branches SET
    description = 'Computer diagnostics for all major brands: engine, electronics, ADAS calibration and pre-purchase inspections.',
    cancellation_policy = 'Free cancellation up to 48 hours before the appointment. Within 48 hours the diagnostic fee of 100 PLN applies.'
WHERE name = 'Diagnostyka Ursynow';
