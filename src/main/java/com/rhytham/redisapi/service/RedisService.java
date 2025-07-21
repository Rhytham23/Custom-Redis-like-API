package com.rhytham.redisapi.service;

import com.rhytham.redisapi.exception.KeyNotFoundException;
import com.rhytham.redisapi.model.KeyValueEntry;
import com.rhytham.redisapi.repository.KeyValueRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@AllArgsConstructor
@Service
public class RedisService {

    private final KeyValueRepository repository;

    public void set(String key, String value, Long ttlSeconds){
        Long expiryTime = null;
        if(ttlSeconds != null){
            expiryTime = System.currentTimeMillis() + (ttlSeconds * 1000);
        }
        KeyValueEntry entry = new KeyValueEntry(key,value,expiryTime);
        repository.save(entry);
    }

    public String get(String key){
        KeyValueEntry entry = repository.findById(key).orElse(null);
        if(entry == null || entry.isExpired()){
            repository.deleteById(key);
            throw new KeyNotFoundException("Key " + key + " not found or has expired.");
        }
        return entry.getValue();
    }

    public KeyValueEntry getEntry(String key){
        KeyValueEntry entry = repository.findById(key).orElse(null);
        if(entry != null && !entry.isExpired()){
            return entry;
        }
        throw new KeyNotFoundException("Key " + key + " not found or is expired");
    }

    public boolean delete(String key){
        KeyValueEntry entry = repository.findById(key).orElse(null);
        if(entry != null){  //deletes whether expired or not
            repository.deleteById(key);;
            return true;
        }
        throw new KeyNotFoundException("Key " + key + " not found or is already expired");
    }

    public boolean exists(String key){
        KeyValueEntry entry = repository.findById(key).orElse(null);
        return entry != null && !entry.isExpired();
    }

    public Map<String, KeyValueEntry> getAllEntries(){
        Map<String, KeyValueEntry> result = new LinkedHashMap<>();
        for (KeyValueEntry entry : repository.findAll()) {
            if (!entry.isExpired()) {
                result.put(entry.getKey(), entry);
            }
        }
        return result;
    }

    public boolean expire(String key,long ttl){
        KeyValueEntry entry = repository.findById(key).orElse(null);

        if(entry == null || entry.isExpired()){
            throw new KeyNotFoundException("Key " + key + " not found or is already expired");
        }
        long newExpiryTime = System.currentTimeMillis() + ttl * 1000;
        entry.setnewExpiryTime(newExpiryTime);
        repository.save(entry);
        return true;
    }

    public long getTTL(String key){
        KeyValueEntry entry = repository.findById(key).orElse(null);;

        if(entry == null || entry.isExpired()){
            return -2;
        }
        Long expiryTime = entry.getExpiryTime();
        if(expiryTime == null || expiryTime <= 0){
            return -1;
        }
        long ttlMillis = expiryTime - System.currentTimeMillis();
        return ttlMillis > 0 ? ttlMillis / 1000 : -2;
    }

    public void flushAll(){
        repository.deleteAll();
    }

    public void cleanupExpiredKeys(){
        long now = System.currentTimeMillis();
        repository.deleteAllExpired(now);
        }

}
