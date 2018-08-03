connect 'jdbc:derby:db/HHCloud;create=true';

CREATE TABLE USERS
    (ID varchar(100) NOT NULL,
    EMAIL varchar(100) NOT NULL,
    USERNAME varchar(100)  NOT NULL,
    FIRSTNAME varchar(100) NOT NULL,
    LASTNAME varchar(100) NOT NULL,
    CREATEAT BIGINT NOT NULL,
    PASS varchar(100)  NOT NULL);

CREATE UNIQUE INDEX uid ON USERS (ID);

CREATE UNIQUE INDEX email ON USERS (EMAIL);

CREATE UNIQUE INDEX username ON USERS (USERNAME);

INSERT INTO USERS VALUES
('123', 'luisa@gmail.com', 'luisa.s','luisa','gomes', 1533252085955,'2525'),
('1234', 'david@gmail.com', 'david.c', 'david','colmenares',1533252085955,'2525');



