SET search_path TO carfix;

CREATE TABLE branches (
                          branch_id uuid  DEFAULT gen_random_uuid() PRIMARY KEY,
                          name varchar(100)  NOT NULL,
                          phone_number varchar(17)  NOT NULL,
                          email varchar(50)  NOT NULL,
                          status text  NOT NULL,
                          tz text  NOT NULL,
                          address_id int  NOT NULL,
                          owner_id uuid  NOT NULL,

                          CONSTRAINT check_branches_status
                              CHECK (status IN ('VERIFICATION_PENDING', 'ACTIVE', 'SUSPENDED'))
);

