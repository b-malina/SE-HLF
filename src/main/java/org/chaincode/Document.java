/*
* SPDX-License-Identifier: Apache-2.0
*/

package org.chaincode;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;
import com.owlike.genson.Genson;

@DataType()
public class Document {

    private final static Genson genson = new Genson();

    @Property()
    private String id;
    @Property()
    private String name;
    @Property()
    private String keyword;
    @Property()
    private String content;

    public Document() {
    }

    public Document(String id, String name, String keyword, String content) {
        this.id = id;
        this.name = name;
        this.keyword = keyword;
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String toJSONString() {
        return genson.serialize(this).toString();
    }

    public static Document fromJSONString(String json) {
        Document document = genson.deserialize(json, Document.class);
        return document;
    }
}
