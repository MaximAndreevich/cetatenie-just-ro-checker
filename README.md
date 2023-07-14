Program downloads PDF file from http://cetatenie.just.ro/ and puts it's content to the H2 database.
If the PDF file already processed it will be ignored.

Program supports testMode (at the properties) that will use local PDF file.

You can use H2 WebUI at the http://localhost:9400/ JDBC URL: "jdbc:h2:./dosardata.db;DB_CLOSE_ON_EXIT=TRUE"
WebServer lifetime and port are configurable.

TBD: analytic tools. Notifications. Cron tasks. 