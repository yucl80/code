sysbench --test=oltp --oltp-table-size=10000 --db-driver=mysql --mysql-db=test --mysql-user=root --mysql-host=127.0.0.1 --mysql-password=### prepare
sysbench --test=oltp --oltp-table-size=10000 --db-driver=mysql --mysql-db=test --mysql-user=root --mysql-password=### --max-time=60 --oltp-read-only=on --max-requests=0 --num-threads=8 run
sysbench --test=oltp --db-driver=mysql --mysql-db=test --mysql-user=root --mysql-password=### cleanup
./bin/sysbench --test=./share/tests/db/oltp.lua \
--mysql-host=10.0.201.36 --mysql-port=8066 --mysql-user=ecuser --mysql-password=ecuser \
--mysql-db=dbtest1a --oltp-tables-count=10 --oltp-table-size=500000 \
--report-interval=10 --oltp-dist-type=uniform --rand-init=on --max-requests=0 \
--oltp-test-mode=nontrx --oltp-nontrx-mode=select \
--oltp-read-only=on --oltp-skip-trx=on \
--max-time=120 --num-threads=12 \
[prepare|run|cleanup]
sysbench /usr/share/sysbench/tests/include/oltp_legacy/insert.lua --oltp-table-size=100000 --db-driver=mysql --mysql-db=test --mysql-user=root --mysql-host=127.0.0.1 --mysql-password=###  --threads=2 run


wget http://mysqltuner.pl/ -O mysqltuner.pl
wget https://raw.githubusercontent.com/major/MySQLTuner-perl/master/basic_passwords.txt -O basic_passwords.txt
wget https://raw.githubusercontent.com/major/MySQLTuner-perl/master/vulnerabilities.csv -O vulnerabilities.csv
perl mysqltuner.pl


https://github.com/major/MySQLTuner-perl/blob/master/INTERNALS.md
