cd $MYSQL_HOME/mysql-test
rm suite/cloud-test/r/*.reject
./mtr --suite=cloud-test --extern host=nic-hadoop-admin --force --retry=2 --max-test-fail=100