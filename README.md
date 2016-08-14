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

## Elasticsearch Data Examples

### Transaction:
`"_id": "3389e9f0f1a65f19736cacf544c2e825313e8447f569233bb8db39aa607c8889"`     
`"_type": "transaction"`
```
   {
     "status": "txSUCCESS",
     "created_at": "2015-09-30T17:15:54.000Z",
     "source_account": "GAAZI4TCR3TY5OJHCTJC2A4QSY6CJWJH5IAJTGKIN2ER7LBNVKOCCWN7"
   }
```

### Payment
`"_id": "3389e9f0f1a65f19736cacf544c2e825313e8447f569233bb8db39aa607c8889-0"`
`-0` is the index of the operation within the transaction.
     
`"_type": "payment"`

```
   {  
      "status":"txSUCCESS",
      "created_at":"2015-09-30T17:15:54.000Z",
      "amount":9.999999995999997E10,
      "asset":"XLM",
      "source_account":"GAAZI4TCR3TY5OJHCTJC2A4QSY6CJWJH5IAJTGKIN2ER7LBNVKOCCWN7",
      "to":"GALPCCZN4YXA3YMJHKL6CVIECKPLJJCTVMSNYWBTKJW4K5HQLYLDMZTB"
   }
```