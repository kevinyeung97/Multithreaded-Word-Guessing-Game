DROP DATABASE IF EXISTS Assignment4;
CREATE DATABASE Assignment4;
USE Assignment4;

CREATE TABLE Users (
	username VARCHAR(100) PRIMARY KEY,
    password VARCHAR(100) NOT NULL,
    wins INT(11) NOT NULL,
    losses INT(11) NOT NULL
);

CREATE TABLE Games (
	gameName VARCHAR(100) PRIMARY KEY,
    numPlayers INT(11) NOT NULL,
    user1 VARCHAR(100) NOT NULL,
    FOREIGN KEY fk1 (user1) REFERENCES Users(username),
    user2 VARCHAR(100),
    FOREIGN KEY fk2 (user2) REFERENCES Users(username),
    user3 VARCHAR(100),
    FOREIGN KEY fk3 (user3) REFERENCES Users(username),
    user4 VARCHAR(100),
    FOREIGN KEY fk4 (user4) REFERENCES Users(username)
);