# MoneyTransfer
**RESTful API for money transfer between accounts**

Simple multithreaded standalone application with in-memory datastore

## Technology stack:
 - Java [1.8+](http://www.oracle.com/technetwork/java/javase/overview/index.html)
 - Maven [3+](https://maven.apache.org)
 - Javalin [2.8.0](https://javalin.io)
 - Slf4J [simple-1.7.26](https://www.slf4j.org)
 - Gson [2.8.5](https://github.com/google/gson)
 - JUnit [4.12](https://junit.org/junit4)
 - Concurrent Unit [0.4.4](https://github.com/jhalterman/concurrentunit)
 - REST Assured [3.3.0](https://github.com/rest-assured/rest-assured)
 
## Build and Run
Maven build & run
```sh
./mvnw exec:java
```

JAR build & run
```sh
# Build the project:
./mvnw clean install
 
# Run the application
java -jar target/adventureworks-1.0.jar
```

Application starts on 
```sh 
http://localhost:8000/
```

## Endpoints
| HTTP METHOD | PATH | USAGE | EXAMPLE REQUEST |
| ----------- | ------ | ------ | ------ |
| GET | /health | health check | curl -X GET http://localhost:8000/health |
| POST | /transfer | perform transfer from one account to another | curl -X POST http://localhost:8000/transaction -F fromAccountNo=0ec8ab7c-5af2-11e9-8647-d663bd873d93 -F toAccountNo=0ec8b1e4-5af2-11e9-8647-d663bd873d93 -F amount=10.00 |

