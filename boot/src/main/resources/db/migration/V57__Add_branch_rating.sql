SET search_path TO carfix;

-- Aggregate cache for the future reviews feature (results-page spec §6 #2, decision 2026-08-09).
-- When real reviews land, that feature maintains these columns; the search contract stays put.
ALTER TABLE branches
    ADD COLUMN rating decimal(2,1),
    ADD COLUMN review_count int;

ALTER TABLE branches
    ADD CONSTRAINT check_branches_rating CHECK (rating >= 0 AND rating <= 5),
    ADD CONSTRAINT check_branches_review_count CHECK (review_count >= 0),
    ADD CONSTRAINT check_branches_rating_pair CHECK ((rating IS NULL) = (review_count IS NULL));

-- Plausible seeds; 'Serwis Ursus' stays NULL/NULL on purpose — it exercises the "New" card state.
UPDATE branches SET rating = 4.7, review_count = 236 WHERE name = 'AutoSerwis Kowalski Mokotow';
UPDATE branches SET rating = 4.5, review_count = 118 WHERE name = 'AutoSerwis Kowalski Wola';
UPDATE branches SET rating = 4.8, review_count = 164 WHERE name = 'AutoSerwis Kowalski Podgorze';
UPDATE branches SET rating = 4.2, review_count = 87  WHERE name = 'Opony Express Praga';
UPDATE branches SET rating = 3.9, review_count = 41  WHERE name = 'Opony Express Nowa Huta';
UPDATE branches SET rating = 4.9, review_count = 203 WHERE name = 'Diagnostyka Ursynow';
