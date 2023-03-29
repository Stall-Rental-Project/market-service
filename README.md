# Getting Started

### Prepare database

* Make sure Postgres is running on your machine (version 9.6 or later)
* Create new database (if not exists) with name `emarketmarket`
* Access to created database and create schema named `emarket`

### Setup environment variables

* Add/Update the following environment variables
    * DB_HOST: the host:port where Postgres instance is running (e.g., localhost:5432)
    * DB_USER: your database username
    * DB_PASSWORD: your database password
    * CONFIG_URI: fully qualified URI where the Config service is running (e.g., http://localhost:9000)
    * CONFIG_PROFILE: the deployment environment (e.g, local, dev, staging, prod). For local development, it should be set to `local`
    * CONFIG_LABEL: your customized label (e.g., `duynt`, `duoctt`). For `prod` environment, it is a good practice to set it to `master`

### Repair database versioning (optional)

Sometimes, you might have an issue on database version (Flyway exceptions). It may be caused due to inconsistent changes on versioned SQL files.

* Create new `flyway.conf` file under project's root directory
* Copy content from `flyway.example.conf` into it
* Modify the file using your own parameters
* Open terminal from project's root directory and run `flyway-repair.sh` file
* Restart Spring Boot instance

### Start Spring Boot instance

* Start Spring Boot instance as usual
