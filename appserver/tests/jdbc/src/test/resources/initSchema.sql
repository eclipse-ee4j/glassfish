CREATE TABLE IF NOT EXISTS "JEEVersion" (
  id int not null,
  name varchar(32),
  year int,
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS "Brand" (
  id int not null,
  name varchar(255),
  PRIMARY KEY (id)
);

-- Empty table for the performance test
CREATE TABLE IF NOT EXISTS "GlassFishUser" (
  id int not null,
  name varchar(255),
  PRIMARY KEY (id)
);

DELETE FROM "JEEVersion" WHERE 1=1;
DELETE FROM "Brand" WHERE 1=1;
DELETE FROm "GlassFishUser" WHERE 1=1;

INSERT INTO "JEEVersion" (id, name, year) VALUES
    (1, 'J2EE 1.2', 1999),
    (2, 'J2EE 1.3', 2001),
    (3, 'J2EE 1.4', 2003),
    (4, 'Java EE 6', 2009),
    (5, 'Java EE 7', 2013),
    (6, 'Java EE 8', 2017),
    (7, 'Jakarta EE 8', 2019),
    (8, 'Jakarta EE 9', 2020),
    (9, 'Jakarta EE 10', 2022);

INSERT INTO "Brand" (id, name) VALUES
    (1, 'Sun'),
    (2, 'Oracle'),
    (3, 'Eclipse');
