/* DROP INDEX */--DROP INDEX util.IX_T_UTIL_HEADER_LOOKUP_CT;/* DROP TABLE */--DROP TABLE util.T_UTIL_HEADER_LOOKUP;/* CREATE TABLE */CREATE TABLE util.T_UTIL_HEADER_LOOKUP(  header_type varchar(50) NOT NULL,  header_code varchar(30) NOT NULL,  header_desc varchar(100) NOT NULL);COMMENT ON TABLE util.T_UTIL_HEADER_LOOKUP IS E'util table to indicate available container codes/types on the container header';/* CREATE INDEX */CREATE UNIQUE INDEX IX_T_UTIL_HEADER_LOOKUP_CT  ON util.T_UTIL_HEADER_LOOKUP (header_code, header_type);--COMMIT;