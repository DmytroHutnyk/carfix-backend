SET search_path TO carfix;

-- Every CountryIso constant with its Google-English name, so address / preferred-location writes can
-- reference any supported country. PL exists since V52 — ON CONFLICT keeps that row untouched.
INSERT INTO countries (iso, name) VALUES
    ('US', 'United States'), ('CA', 'Canada'), ('GB', 'United Kingdom'), ('DE', 'Germany'), ('FR', 'France'),
    ('IT', 'Italy'), ('ES', 'Spain'), ('PL', 'Poland'), ('UA', 'Ukraine'), ('PT', 'Portugal'),
    ('NL', 'Netherlands'), ('SE', 'Sweden'), ('NO', 'Norway'), ('FI', 'Finland'), ('DK', 'Denmark'),
    ('CH', 'Switzerland'), ('AT', 'Austria'), ('BE', 'Belgium'), ('CZ', 'Czechia'), ('SK', 'Slovakia'),
    ('HU', 'Hungary'), ('RO', 'Romania'), ('BG', 'Bulgaria'), ('GR', 'Greece'), ('TR', 'Türkiye'),
    ('IE', 'Ireland'), ('IS', 'Iceland'), ('AU', 'Australia'), ('NZ', 'New Zealand'), ('JP', 'Japan'),
    ('CN', 'China'), ('KR', 'South Korea'), ('IN', 'India'), ('BR', 'Brazil'), ('AR', 'Argentina'),
    ('MX', 'Mexico'), ('ZA', 'South Africa'), ('EG', 'Egypt'), ('IL', 'Israel'), ('SA', 'Saudi Arabia'),
    ('AE', 'United Arab Emirates')
ON CONFLICT (iso) DO NOTHING;
