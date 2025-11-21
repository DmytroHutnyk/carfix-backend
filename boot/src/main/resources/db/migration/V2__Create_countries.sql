SET search_path TO carfix;

CREATE TABLE countries (
                           iso varchar(2)  PRIMARY KEY,
                           name varchar(100)  NOT NULL,
--TODO temporary solution
                       CONSTRAINT check_countries_iso
                            CHECK (iso IN (
                                           'US', 'CA', 'GB', 'DE', 'FR', 'IT', 'ES', 'PL', 'UA', 'PT',
                                           'NL', 'SE', 'NO', 'FI', 'DK', 'CH', 'AT', 'BE', 'CZ', 'SK',
                                           'HU', 'RO', 'BG', 'GR', 'TR', 'IE', 'IS', 'AU', 'NZ', 'JP',
                                           'CN', 'KR', 'IN', 'BR', 'AR', 'MX', 'ZA', 'EG', 'IL', 'SA', 'AE'
                                ))
);

