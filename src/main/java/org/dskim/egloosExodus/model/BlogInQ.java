package org.dskim.egloosExodus.model;

import io.jsondb.annotation.Document;
import io.jsondb.annotation.Id;

//@Data
@Document(collection = "blogsInQ", schemaVersion = "1.0")
public class BlogInQ {
    @Id String id;
    //String blogUrl;

    public BlogInQ(String id) {
        this.id = id;
    }
}
