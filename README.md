# Vert.x Case Study
## Start
0. Start PostgreSQL Database

    ```
    docker-compose up --detach
    ```
0. Start Application 

    ```sh
    ./gradlew run
    ```

## Test
0. Execute integration tests

    ```sh
    ./gradlew test
    ```