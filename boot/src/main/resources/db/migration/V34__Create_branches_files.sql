SET search_path TO carfix;

CREATE TABLE branches_files (
                                file_id int,
                                branch_id uuid,
                                is_public boolean  NOT NULL DEFAULT TRUE,
                                display_order int  NOT NULL,

                                CONSTRAINT pk_branches_files PRIMARY KEY (file_id, branch_id)
);

