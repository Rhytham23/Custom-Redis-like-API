package com.rhytham.redisapi.service;

import com.rhytham.redisapi.exception.KeyNotFoundException;
import com.rhytham.redisapi.model.KeyValueEntry;
import com.rhytham.redisapi.repository.KeyValueRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Service
public class RedisService {

    private final KeyValueRepository repository;

    public void set(String key, String value, Long ttlSeconds){
        Long expiryTime = null;
        if(ttlSeconds != null && ttlSeconds > 0){
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

    public Map<String, Object> getEntry(String key) {
        KeyValueEntry entry = repository.findById(key).orElse(null);
        if (entry != null && !entry.isExpired()) {
            Map<String, Object> allEntries = new LinkedHashMap<>();
            allEntries.put("key", entry.getKey());
            allEntries.put("value", entry.getValue());
            if (entry.getExpiryTime() != null && entry.getExpiryTime() > 0) {
                long ttl = (entry.getExpiryTime() - System.currentTimeMillis()) / 1000;
                allEntries.put("ttl", ttl >= 0 ? ttl : "No expiration");
            } else {
                allEntries.put("ttl", "No expiration");
            }
            return allEntries;
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

    public List<Map<String, Object>> getAllEntries() {
        List<Map<String, Object>> keyList = new ArrayList<>();
        for (KeyValueEntry keyValue : repository.findAll()) {
            if (!keyValue.isExpired()) {
                Map<String, Object> keyDetails = new LinkedHashMap<>();
                keyDetails.put("key", keyValue.getKey());
                keyDetails.put("value", keyValue.getValue());
                Long expiryTime = keyValue.getExpiryTime();

                if (expiryTime != null && expiryTime > 0) {
                    long ttl = (expiryTime - System.currentTimeMillis()) / 1000;
                    keyDetails.put("ttl", ttl >= 0 ? ttl : "No expiration");
                } else {
                    keyDetails.put("ttl", "No expiration");
                }
                keyList.add(keyDetails);
            }
        }
        return keyList;
    }

    public boolean expire(String key,long ttl){
        KeyValueEntry entry = repository.findById(key).orElse(null);

        if(entry == null || entry.isExpired()){
            throw new KeyNotFoundException("Key " + key + " not found or is already expired");
        }
        long newExpiryTime = System.currentTimeMillis() + ttl * 1000;
        entry.setnewExpiryTime(newExpiryTime);
        repository.save(entry);
        System.out.println("TTL for key " + key + " updated successfully");
        return true;
    }

    public String getTTL(String key){
        KeyValueEntry entry = repository.findById(key).orElse(null);;

        if(entry == null || entry.isExpired()){
            throw new KeyNotFoundException("Key " + key + " not found or is already expired");
        }
        Long expiryTime = entry.getExpiryTime();
        if(expiryTime == null || expiryTime <= 0){
            return "No expiration set for this key";
        }
        Long ttlMillis = expiryTime - System.currentTimeMillis();
        if(ttlMillis > 0){
            return "TTL for key " + key + " is " + ttlMillis / 1000 + " seconds.";
        }else{
            throw new KeyNotFoundException("Key " + key + " not found or is already expired");
        }
    }

    public void flushAll(){
        repository.deleteAll();
    }

    public void cleanupExpiredKeys(){
        long now = System.currentTimeMillis();
        repository.deleteAllExpired(now);
        }

}
