/*
       Copyright 2021 IBM Corp All Rights Reserved

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.ibm.hybrid.cloud.sample.stocktrader.collector.json;


/** JSON-B POJO class representing an SCC Artifact JSON object.
  * I am loosely basing this on the evidence format described at
  * https://pages.github.ibm.com/one-pipeline/docs/#/evidence */
public class Artifact {
   private String key;
   private String value;
   private String url;
   private String hash;


   public Artifact() {
   }

   public String getKey() {
      return key;
   }

   public void setKey(String newKey) {
      key = newKey;
   }

   public String getValue() {
      return value;
   }

   public void setValue(String newValue) {
      value = newValue;
   }

   public String getUrl() {
      return url;
   }

   public void setUrl(String newUrl) {
      url = newUrl;
   }

   public String getHash() {
      return hash;
   }

   public void setHash(String newHash) {
      hash = newHash;
   }

   public boolean equals(Object obj) {
      boolean isEqual = false;
      if ((obj != null) && (obj instanceof Artifact)) isEqual = toString().equals(obj.toString());
      return isEqual;
   }

   public String toString() {
      return "{\"key\": \""+key+"\", \"value\": \""+value+"\", \"url\": \""+url+"\", \"hash\": \""+hash+"\"}";
   }
}

