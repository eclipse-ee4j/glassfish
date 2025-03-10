DROP TABLE app.Numbers;
DROP TABLE app.Orders;
DROP TABLE app.Inventory;

CREATE TABLE app.Numbers
(
    item     INT,
    quantity INT
);

CREATE TABLE app.Orders
(
    orderID  INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) PRIMARY KEY,
    itemID   INT,
    quantity INT
);

CREATE TABLE app.Inventory
(
    itemID   INT NOT NULL PRIMARY KEY,
    quantity INT NOT NULL
);

INSERT INTO app.Inventory
VALUES (1, 100);

INSERT INTO app.Numbers
VALUES (1, 10);

INSERT INTO app.Numbers
VALUES (2, 10);

INSERT INTO app.Numbers
VALUES (3, 10);

INSERT INTO app.Numbers
VALUES (4, 10);

INSERT INTO app.Numbers
VALUES (5, 10);

INSERT INTO app.Numbers
VALUES (6, 10);

INSERT INTO app.Numbers
VALUES (7, 10);

INSERT INTO app.Numbers
VALUES (8, 10);

INSERT INTO app.Numbers
VALUES (9, 10);

INSERT INTO app.Numbers
VALUES (10, 10);

INSERT INTO app.Numbers
VALUES (11, 10);

INSERT INTO app.Numbers
VALUES (12, 10);

INSERT INTO app.Numbers
VALUES (13, 10);

INSERT INTO app.Numbers
VALUES (14, 10);

INSERT INTO app.Numbers
VALUES (15, 10);

INSERT INTO app.Numbers
VALUES (16, 10);

INSERT INTO app.Numbers
VALUES (17, 10);

INSERT INTO app.Numbers
VALUES (18, 10);

INSERT INTO app.Numbers
VALUES (19, 10);

INSERT INTO app.Numbers
VALUES (20, 10);
