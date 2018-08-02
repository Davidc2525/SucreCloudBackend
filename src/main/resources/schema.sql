connect 'jdbc:derby:db/HHCloud;create=true';

CREATE TABLE USERS
    (ID bigint NOT NULL,
    EMAIL varchar(100) NOT NULL,
    USERNAME varchar(100)  NOT NULL,
    PASS varchar(100)  NOT NULL);

CREATE UNIQUE INDEX uid ON USERS (ID);

CREATE UNIQUE INDEX email ON USERS (EMAIL);

CREATE UNIQUE INDEX username ON USERS (USERNAME);

INSERT INTO USERS VALUES
(123, 'luisa@gmail.com', 'luisa.s', '2525'),
(1234, 'david@gmail.com', 'david.c', '2525');



