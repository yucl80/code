sysbench --test=oltp --oltp-table-size=10000 --db-driver=mysql --mysql-db=test --mysql-user=root --mysql-host=127.0.0.1 --mysql-password=### prepare
sysbench --test=oltp --oltp-table-size=10000 --db-driver=mysql --mysql-db=test --mysql-user=root --mysql-password=### --max-time=60 --oltp-read-only=on --max-requests=0 --num-threads=8 run
sysbench --test=oltp --db-driver=mysql --mysql-db=test --mysql-user=root --mysql-password=### cleanup

sysbench /usr/share/sysbench/tests/include/oltp_legacy/insert.lua --oltp-table-size=100000 --db-driver=mysql --mysql-db=test --mysql-user=root --mysql-host=127.0.0.1 --mysql-password=###  --threads=2 run
