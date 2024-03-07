During the initial migration where the db schema is created, the db user needs select grant on the "mysql" schema.
Any following migrations do not need this grant.
