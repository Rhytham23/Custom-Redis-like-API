package com.rhytham.redisapi.controller;

import com.rhytham.redisapi.dto.KeyValueRequest;
import com.rhytham.redisapi.exception.KeyNotFoundException;
import com.rhytham.redisapi.model.KeyValueEntry;
import com.rhytham.redisapi.service.RedisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@AllArgsConstructor
@RestController
@Tag(name = "Redis Controller", description = "CRUD operations for Redis-like key-value store")
public class RedisController {

    private final RedisService redisService;

    @Operation(summary = "Set a key with value and optional TTL", description = "Stores a key-value pair in the store. TTL (Time-To-Live) is optional and specified in seconds.")
    @PostMapping("/set")
    public ResponseEntity<String> setKey(@Valid @RequestBody KeyValueRequest request){
        redisService.set(request.getKey(), request.getValue(), request.getTtl());
        return ResponseEntity.ok("Key stored successfully");
    }

    @Operation(summary = "Get value for a key", description = "Retrieves the value of a key. Throws an error if key is not found or has expired.")
    @GetMapping("/get/{key}")
    public String getKey(@PathVariable String key) {
        String value = redisService.get(key);
        if (value == null) {
            throw new KeyNotFoundException("Key " + key + " not found or is expired");
        }
        return value;
    }

    //GET key with TTL info
    @Operation(summary = "Get key details with TTL", description = "Retrieves the key, value, and remaining TTL (if any) for a given key.")
    @GetMapping("/get/details/{key}")
    public ResponseEntity<?> getKeyWithTTL(@PathVariable String key) {
        KeyValueEntry entry = redisService.getEntry(key);

        if (entry == null || entry.isExpired()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Key not found or is expired");
        }
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("key", entry.getKey());
        response.put("value", entry.getValue());
        if(entry.getExpiryTime() != null &&  entry.getExpiryTime() > 0) {
            long ttl = (entry.getExpiryTime() - System.currentTimeMillis()) / 1000;
            response.put("ttl", ttl >= 0 ? ttl : "No expiration");
        }
        else{
            response.put("ttl","No expiration");
        }
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete a key", description = "Deletes the specified key from the store, whether expired or not.")
    @DeleteMapping("/delete/{key}")
    public ResponseEntity<String> deleteKey(@PathVariable String key) {
        boolean removed = redisService.delete(key);
        if (removed) {
            return ResponseEntity.ok("Key deleted successfully");
        } else {
            throw new KeyNotFoundException("Key " + key + " not found or is already expired.");
        }
    }

    //Check if a key exists
    @Operation(summary = "Check if a key exists", description = "Checks whether the given key exists and is not expired.")
    @GetMapping("/exists/{key}")
    public ResponseEntity<Boolean> keyExists(@PathVariable String key) {
        boolean exists = redisService.exists(key);
        return ResponseEntity.ok(exists);
    }

    //Return a list of all entries with TTL
    @Operation(summary = "List all unexpired keys", description = "Returns a list of all keys with their values and remaining TTLs, excluding expired keys.")
    @GetMapping("/keys")
    public ResponseEntity<?> listAllKeys() {
        List<Map<String, Object>> keyList = new ArrayList<>();

        for (Map.Entry<String, KeyValueEntry> entry : redisService.getAllEntries().entrySet()) {
            KeyValueEntry keyValue = entry.getValue();

            if (!keyValue.isExpired()) {
                Map<String, Object> keyDetails = new LinkedHashMap<>();

                keyDetails.put("key", entry.getKey());
                keyDetails.put("value", entry.getValue().getValue());

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
        if (keyList.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No keys present");
        } else {
            return ResponseEntity.ok(keyList);
        }
    }

    @Operation(summary = "Set TTL for a key", description = "Updates or sets the TTL (in seconds) for an existing key.")
    @PatchMapping("expire/{key}/{ttl}")
    public ResponseEntity<String> expireKey(@PathVariable String key, @PathVariable long ttl){
        boolean success = redisService.expire(key,ttl);
        if(success){
            return ResponseEntity.ok("TTL set successfully for key: " + key);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Key not found or is already expired");

    }

    //Get TTL of a key
    @Operation(summary = "Get TTL of a key", description = "Returns the remaining time-to-live for the given key in seconds.")
    @GetMapping("/ttl/{key}")
    public ResponseEntity<String> getKetTTL(@PathVariable String key){
        long ttl = redisService.getTTL(key);
        if(ttl == -2){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Key not found or expired");
        }else if(ttl == -1){
            return ResponseEntity.ok("No expiration set for this key");
        }else{
            return ResponseEntity.ok("TTL for key " + key + " is " + ttl + " seconds.");
        }
    }

    @Operation(summary = "Delete all keys", description = "Flushes the entire key-value store, removing all keys.")
    @DeleteMapping("/flushall")
    public ResponseEntity<String> flushAllKeys(){
        redisService.flushAll();;
        return ResponseEntity.ok("All keys have been permanently deleted");
    }

    }


