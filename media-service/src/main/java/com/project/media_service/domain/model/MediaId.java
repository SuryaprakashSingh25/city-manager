package com.project.media_service.domain.model;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.UUID;

@ToString
@EqualsAndHashCode
public class MediaId {
    private final String id;

    private MediaId(String id){
        this.id=id;
    }

    public static MediaId generate(){
        return new MediaId(UUID.randomUUID().toString());
    }

    public static MediaId from(String id){
        return new MediaId(id);
    }

    public String getId(){
        return id;
    }

}
