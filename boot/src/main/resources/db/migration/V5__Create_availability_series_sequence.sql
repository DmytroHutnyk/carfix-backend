SET search_path TO carfix;

-- Shared across the three *_availability tables so one recurrence entry gets one unambiguous series id
CREATE SEQUENCE availability_series_seq;
