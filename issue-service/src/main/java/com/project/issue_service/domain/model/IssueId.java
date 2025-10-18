package com.project.issue_service.domain.model;

import lombok.Value;
import org.bson.types.ObjectId;

@Value
public class IssueId {
    String value;

    public static IssueId newId(){
        return new IssueId(new ObjectId().toHexString());
    }

    public static IssueId from(String id){
        return new IssueId(id);
    }

}
