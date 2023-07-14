Program downloads PDF file from http://cetatenie.just.ro/ and put it's content to the H2 database.
If the PDF file has already been processed, it will be ignored.

Program supports the testMode (at the properties) that is using local PDF file.

You can use H2 WebUI at the http://localhost:9400/ JDBC URL: "jdbc:h2:./dosardata.db;DB_CLOSE_ON_EXIT=TRUE"
WebServer lifetime and port are configurable.


TBD: analytic tools. Notifications. Cron tasks, target DOSAR # report