# Microservices with Redis

This project shows how you can modernize a legacy application that use RDBMS with Redis.

* Caching: take some data out of RDBMS
* Use RediSearch to index relational data and provides autocomplete feature
* Use Redis Graph to provide a new way to navigate and use the data
* Build an event based architecture using Redis Streams


### 1- [Cache Invalidation](cache-invalidator-service)

This Spring Boot Application is a service that use Debezium in an embedded mode and listen to CDC event from MySQL.
Depending of the configuration, the table content is automatically cached as a hash or just invalidated based on the table primary key.


### 2. Extend your Relational Model with Redis Modules

Todo:

* show how to integrate RedisGraph and Search to enrich RDBMS application



## Build and Run with Docker


If you want to use the Web Service cache demo that call the OMDB API you must:

1. Generate a key here: [http://www.omdbapi.com/](http://www.omdbapi.com/)

2. Put this key in the `cache-invalidator-service/src/main/resources/application-prod.properties` (see last property)

Then



```
$ mvn clean package

$ docker-compose up --build

```

Cleanup

```

$ docker-compose down -v --rmi local --remove-orphans

```

# Using RedisInsight


```
docker run -v redisinsight:/db -p 8001:8001 redislabs/redisinsight
```

While connecting to Redis use the below IP/Hostname:

```
host.docker.internal
```

## RedisGraph


```
MATCH (a:actor{actor_id:1}) RETURN a
```

## RediSearch

Type wars -Strip -Sith on Web & "wars -Strip -Sith" on RedisInsight

You can also do search with "wars -Strip redis" RETURN 1 title

```
"wars -Strip redis " RETURN 2 title plot
```



```
FT.SEARCH ms:search:index:movies "wars -Strip -Sith" RETURN 1 title
```

```
FT.SEARCH ms:search:index:movies "redis " RETURN 2 title plot
```

```
FT.AGGREGATE ms:search:index:movies: "@release_year:2015" GROUPBY 1 @genre REDUCE COUNT 0 AS sum SORTBY 2 @genre ASC MAX 100
```

## RediSearch

```
"stars jedi dooku"
```


