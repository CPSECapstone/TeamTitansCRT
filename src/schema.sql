CREATE TABLE captures (
id INTEGER PRIMARY KEY,
rds TEXT,
s3 TEXT,
startTime TEXT,
endTime TEXT,
status TEXT,
fileSizeLimit INTEGER,
transactionLimit INTEGER,
dbFileSize INTEGER,
numDbTransactions INTEGER
);
