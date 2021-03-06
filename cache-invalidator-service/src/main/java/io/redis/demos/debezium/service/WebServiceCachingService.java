package io.redis.demos.debezium.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.params.SetParams;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class WebServiceCachingService {

    // URI used to connect to Redis database
    @Value("${redis.uri}")
    private String redisUri;

    // OMDB API KEY
    @Value("${omdb.api}")
    private String omdbAPIKEY;

    private JedisPool jedisPool;

    private boolean cacheEnable  = true; // TODO make it configurable

    public static final String KEY_PREFIX = "ms:cache:ws:";
    public static final String OMDB_API_URL = "http://www.omdbapi.com/?apikey=";
    public static final int TTL = 120;
    ObjectMapper jsonMapper = new ObjectMapper();

    public WebServiceCachingService(){
    }

    @PostConstruct
    private void afterConstruct(){
        try {
            log.info("Create Jedis Pool with {} ", redisUri);
            URI redisConnectionString = new URI(redisUri);
            jedisPool = new JedisPool(new JedisPoolConfig(), redisConnectionString);
        } catch (URISyntaxException use) {
            log.error("Error creating JedisPool {}", use.getMessage());
        }
    }

    /**
     *
     * @param imdbId
     * @param withCache
     * @return
     */
    public Map<String,String> getRatings(String imdbId, boolean withCache) {
        log.info("calling rating {} - wih cache {}", imdbId, withCache);
        long start = System.currentTimeMillis();
        String url = OMDB_API_URL + omdbAPIKEY +"&i="+imdbId;

        Map<String,String> returnValue = new HashMap();


        String restCallKey = KEY_PREFIX + UUID.nameUUIDFromBytes(url.getBytes()).toString();

        try (Jedis jedis = jedisPool.getResource() ){

            // Look in the map to see if the value has been cached
            if (withCache) {
                returnValue = jedis.hgetAll(restCallKey);
            }

            if ( returnValue.isEmpty()) {
                returnValue.put("imdb_id", imdbId);
                CloseableHttpClient httpClient = HttpClientBuilder.create().build();
                HttpGet getRequest = new HttpGet(url);
                getRequest.addHeader("accept", "application/json");
                ResponseHandler<String> responseHandler = new BasicResponseHandler();

                String WsCall = httpClient.execute(getRequest,responseHandler);


                Map<String, Object> map = jsonMapper.readValue(WsCall, Map.class);
                List<Map<String, String>> ratings = (List<Map<String, String>>)map.get("Ratings");

                Map<String,String> ratingAsMap = new HashMap<>();
                for (Map<String,String> it : ratings) {
                    ratingAsMap.put( it.get("Source"), it.get("Value") );
                }


                returnValue.putAll(ratingAsMap);

                jedis.hset(restCallKey, returnValue);
                jedis.expire(restCallKey, TTL);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        long end = System.currentTimeMillis();
        returnValue.put("elapsedTimeMs", Long.toString(end - start) );
        return returnValue;
    }

}
