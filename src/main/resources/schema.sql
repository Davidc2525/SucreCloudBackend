connect 'jdbc:derby:db/HHCloud;create=true';  --comentar esta linea si no es se ejecutara en la base de datos embebida

CREATE TABLE USERS
    (ID varchar(100) NOT NULL,
    EMAIL varchar(100) NOT NULL,
    EMAILVERIFIED BOOLEAN NOT NULL,
    USERNAME varchar(100)  NOT NULL,
    FIRSTNAME varchar(100) NOT NULL,
    LASTNAME varchar(100) NOT NULL,
    GENDER varchar (1) NOT NULL, --f,m,n
    CREATEAT BIGINT NOT NULL,
    PASS varchar(1000)  NOT NULL);

CREATE UNIQUE INDEX uid ON USERS (ID);

CREATE UNIQUE INDEX email ON USERS (EMAIL);

CREATE UNIQUE INDEX username ON USERS (USERNAME);

INSERT INTO USERS VALUES
('123', 'luisa@gmail.com', true, 'luisa.s','luisa','gomes','f', 1533252085955,'AUQk/iNJ+0h0xfHhyTwlUVuIXu7VofOHaS9J1HOtbvrd9KQmcNK7mlk='),
('1234', 'david@gmail.com', true, 'david.c', 'david','colmenares','m',1533252085955,'AUQk/iNJ+0h0xfHhyTwlUVuIXu7VofOHaS9J1HOtbvrd9KQmcNK7mlk=');

CREATE TABLE SHAREBYUSER
    (IDUSER VARCHAR(100) NOT NULL,
    IDSHARE VARCHAR(100) NOT NULL);

CREATE TABLE SHAREBYPATH
    (PATH VARCHAR(10000) NOT NULL,
    IDSHARE VARCHAR(100) NOT NULL);

CREATE TABLE SHARE
    (ID VARCHAR(100) NOT NULL,
    PATH VARCHAR(10000) NOT NULL,
    OWNERUSER VARCHAR(100) NOT NULL,
    CREATEAT BIGINT NOT NULL);







