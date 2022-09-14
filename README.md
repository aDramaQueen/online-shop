# Online shop

A small show application that simulates an online store.

This application is meant to be used as a stand-alone JAR, since all necessary dependencies are compiled & packed
inside the JAR by [Gradle](https://gradle.org/). This means you can take this JAR & run it on any machine/server.
All you need on your target machine/server is an installed Java Runtime Environment (JRE). Everything else (even
the webserver & database), comes within the JAR.

## Application deployed on Heroku: [Try me out](#)

## Features

1. Application is an online shop webpage
2. Nonetheless, the application also holds REST endpoints for automatic surveillance or remote updating the offers/products
  - REST endpoints are documented via [OpenAPI 3.0](https://oai.github.io/Documentation/introduction.html) for an easy integration in other applications
3. Users
  - Application distinguishes 3 different user types: Admin, Staff, (simple) User
  - Each user has different permission levels, with inheritance working from bottom (**user**) to top (**admin**). Meaning the **admin** user is able to do everything a **staff** user can do, plus more. Same goes for **staff** user and "simple" **user**.
    - Admin (can do everything without any constraint, e.g.: he can create & delete new **staff** users/members)
    - Staff (can add/delete/manipulate products in the shop)
    - (simple) User (can look up the offers/products & buy some)
4. Offers/Products
  - Easy adding/deleting/manipulating of the offers/products of this shop
    - This can be done directly over an integrated webpage or remote via REST endpoints
5. Database
  - Integration of evolutionary database schemes with [Liquibase](https://www.liquibase.org/)
    - This helps easily transfer to any other arbitrary database, which is supported by Liquibase:
      - [List of all supported databases](https://www.liquibase.org/get-started/databases)
  - Default (embedded) database: [H2](https://www.h2database.com/html/main.html)
    - Is absolutely sufficient for small websites (< 100 visitors at the same time)
6. Security
   - HTTPS support with Letsencrypt certificates, but easily changeable with any other certificates
   - Auto-generation of SLL/TLS certificates for end-to-end encryption for bigger databases, like PostgreSQL/MySQL
     - If you use your own CA certificates & generate certificates for each application (this one & the bigger database), you're even able to install a zero-trust-environment
   - Integrated Sign-in/Login system for new users (no 3rd party reliance)

## Requirements

[Java 17 (LTS) or above](https://openjdk.java.net/projects/jdk/17/)

Recommended binary source: **https://adoptium.net/**

## Project Version

### Current version: 0.3.0

#### Version scheme follows [Semantic versioning](https://semver.org/):  "MAJOR.MINOR.PATCH"

+ MAJOR - Major version number increments for incompatible API changes
+ MINOR - Minor version number increments by adding functionality in a backwards compatible manner
+ PATCH - Bugfix for major/minor version in a backwards compatible manner

# [Changelog](CHANGELOG.md)
