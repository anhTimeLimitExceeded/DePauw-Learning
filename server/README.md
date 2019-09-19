# Run

`mvn clean spring-boot:run`

## Check that it is running

`curl -G http://localhost:8080/ping --data-urlencode "ping=Hello World"`

The Terminal should recieve a ping with the words "Hello World". Try changing the request to receive different strings from the server.

You can also go on a browser to [http://localhost:8080/ping](curl -G http://localhost:8080/ping --data-urlencode "ping=Hello World") and it should say ping.
