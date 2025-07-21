package com.rhytham.redisapi.service;

import com.rhytham.redisapi.exception.KeyNotFoundException;
import com.rhytham.redisapi.model.KeyValueEntry;
import com.rhytham.redisapi.repository.KeyValueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;


public class RedisServiceTest {

    private RedisService redisService;
    private KeyValueRepository keyValueRepository;

    @BeforeEach
    void setUp(){
        keyValueRepository = mock(KeyValueRepository.class);
        redisService = new RedisService(keyValueRepository);
    }

    @Test
    void testSetAndGetEntry(){
        String key = "myKey";
        String value = "Hello";
        KeyValueEntry entry = new KeyValueEntry(key,value,null);

        when(keyValueRepository.save(any())).thenReturn(entry);
        when(keyValueRepository.findById(key)).thenReturn(Optional.of(entry));

        redisService.set(key,value, 0L);
        KeyValueEntry retrieved = redisService.getEntry(key);

        assertNotNull(retrieved);
        assertEquals("Hello",retrieved.getValue());

        verify(keyValueRepository).save(any());
        verify(keyValueRepository).findById(key);
    }

    @Test
    void testGetEntryThrowExceptionNotFound(){
        when(keyValueRepository.findById("noKey")).thenReturn(Optional.empty());

        assertThrows(KeyNotFoundException.class , () -> redisService.getEntry("noKey"));
    }

    @Test
    void testDeleteEntrySuccess(){
        String key = "delKey";
        KeyValueEntry entry = new KeyValueEntry(key,"data",null);

        when(keyValueRepository.findById(key)).thenReturn(Optional.of(entry));
        doNothing().when(keyValueRepository).deleteById(key);

        assertTrue(redisService.delete(key));
    }

    @Test
    void testDeleteEntryFailure(){
        when(keyValueRepository.findById("nonExistent")).thenReturn(Optional.empty());
        assertThrows(KeyNotFoundException.class ,() -> redisService.delete("nonExistent"));
    }

    @Test
    void testGetWithTTL_NotExpired() {
        String key = "ttlKey";
        String value = "someValue";
        long futureTime = System.currentTimeMillis() + 10000; // 10 seconds ahead

        KeyValueEntry entry = new KeyValueEntry(key, value, futureTime);
        when(keyValueRepository.findById(key)).thenReturn(Optional.of(entry));

        KeyValueEntry result = redisService.getEntry(key);

        assertNotNull(result);
        assertEquals(value, result.getValue());
        assertEquals(futureTime, result.getExpiryTime());
    }

    @Test
    void testGetWithTTL_Expired() {
        String key = "expiredKey";
        String value = "expired";
        long pastTime = System.currentTimeMillis() - 10000; // 10 seconds ago

        KeyValueEntry entry = new KeyValueEntry(key, value, pastTime);
        when(keyValueRepository.findById(key)).thenReturn(Optional.of(entry));

        assertThrows(KeyNotFoundException.class, () -> redisService.getEntry(key));
        //verify(keyValueRepository).deleteById(key); // ensures expired key was deleted
    }

    @Test
    void testExists_ReturnsTrue() {
        String key = "existsKey";
        KeyValueEntry entry = new KeyValueEntry();
        entry.setKey(key);
        entry.setValue("someValue");
        entry.setExpiryTime(null); // No expiry = not expired

        when(keyValueRepository.findById(key)).thenReturn(Optional.of(entry));

        assertTrue(redisService.exists(key));
    }


    @Test
    void testExists_ReturnsFalse() {
        String key = "missingKey";
        when(keyValueRepository.existsById(key)).thenReturn(false);

        assertFalse(redisService.exists(key));
    }

    @Test
    void testGetAllEntriesReturnsOnlyUnexpired() {

        KeyValueEntry entry1 = new KeyValueEntry("key1", "value1", null); // not expired
        KeyValueEntry entry2 = new KeyValueEntry("key2", "value2", System.currentTimeMillis() - 1000); // expired

        List<KeyValueEntry> mockList = List.of(entry1, entry2);

        when(keyValueRepository.findAll()).thenReturn(mockList);

        Map<String, KeyValueEntry> result = redisService.getAllEntries();

        assertEquals(1, result.size());
        assertTrue(result.containsKey("key1"));
        assertFalse(result.containsKey("key2"));
    }
}
