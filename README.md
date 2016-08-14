# Stellar Core Elasticsearch Indexer

## Overview
This project reads the stellar-core database `txhistory` table, de-serializes the XDR data structures, and then sends the data to an elasticsearch cluster. It is intended to be used with Kibana to visualize, analyze and explore the stream of transaction/operation events.

## Configuration
Configuration is done within a properties file.

By default, it will try to find a `config.properties` file in the current directory. See the `config.properties.example` file for an example configuration. 

To use an alternative location, provide a file location to parameter before running.

E.g.
`java -jar stellar-core-es-indexer.jar ../path/to/myconfig.properties`


## Configuration Values

### REFRESH_INTERVAL 
The number of seconds to wait before polling the database for new ledgers.
If set to 0, indexing will only run once.
Default Value is 0.

## Building
Run `./gradlew shadowJar`

This builds a runnable, shaded jar into `build/libs/stellar-core-es-indexer.jar`.

Alternatively run `./gradlew distShadowZip` to get a zipped distribution in the `/distributions` folder which also contains bin scripts for running.

## Running
`java -jar build/libs/stellar-core-es-indexer.jar`
`java -jar build/libs/stellar-core-es-indexer.jar ../path/to/myconfig.properties`

## License
See LICENSE file.

## TODO
- Index more operations
- Document json objects
- Example Queries
