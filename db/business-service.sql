-- staff.password is varchar(40) in Sakila (room for the original SHA-1 hex). A bcrypt hash is 60
-- characters, so business-service (which hashes staff passwords) needs the column wider.
ALTER TABLE staff ALTER COLUMN password TYPE varchar(255);
