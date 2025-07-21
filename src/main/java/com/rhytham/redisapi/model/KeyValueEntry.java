package com.rhytham.redisapi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "key_value_store")
public class KeyValueEntry {

    @Id
    private String key;

    @Column(nullable = false)
    private String value;

    private Long expiryTime;

    public void setnewExpiryTime(long newExpiryTime){
        this.expiryTime = newExpiryTime;
    }
    public boolean isExpired(){
        return expiryTime != null && System.currentTimeMillis() > expiryTime;
    }

}
