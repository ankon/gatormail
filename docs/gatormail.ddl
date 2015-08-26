# Tested on mySQL and Postgresql
create table addressbook(userid char(8), entry varchar(70));

# Create an index for the userid, tested on mySQL and  PostgreSQL
create index addressbook_idx on addressbook(userid);

# Create an index for the userid, tested on DB2
#create index addressbook_idx on addressbook(userid) cluster;


# Create preferences table in DB2 (and probably others)
CREATE TABLE preferences(userid char(8) NOT NULL, key varchar(128) NOT NULL, entry varchar(1024) NOT NULL);

# Create preferences table in Mysql
CREATE TABLE preferences(userid char(8) NOT NULL, preferences.key text NOT NULL, entry text NOT NULL);
