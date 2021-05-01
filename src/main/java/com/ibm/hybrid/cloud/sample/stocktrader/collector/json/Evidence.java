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


/** JSON-B POJO class representing an SCC Evidence JSON object.
  * I am loosely basing this on the evidence format described at
  * https://pages.github.ibm.com/one-pipeline/docs/#/evidence */
public class Evidence {
    public static final String SUCCESS="success"; //result value
    public static final String FAILURE="failure"; //result value

    private String _id;                   //Cloudant required field - primary key
    private String _rev;                  //Cloudant required field - latest revision
    private String evidence_type_id;      //name of the evidence
    private String evidence_type_version; //version of the evidence schema
    private String date;                  //actually an ISO-8601 timestamp, not sure why just called date
    private String crn;                   //Cloud Resource Name (https://cloud.ibm.com/docs/account?topic=account-crn)
    private String result;                //Boolean-ish string that must either be "success" or "failure"
    private Artifact[] log;               //logs related to the evidence
    private Artifact[] artifacts;         //additional artifacts related to the evidence


    public Evidence() { //default constructor
    }

    public Evidence(String initialDate) { //primary key constructor
        setDate(initialDate);
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String new_id) {
        _id = new_id;
    }

    public String get_rev() {
        return _rev;
    }

    public void set_rev(String new_rev) {
        _rev = new_rev;
    }

    public String getEvidence_type_id() {
        return evidence_type_id;
    }

    public void setEvidence_type_id(String newEvidence_type_id) {
        evidence_type_id = newEvidence_type_id;
    }

    public String getEvidence_type_version() {
        return evidence_type_version;
    }

    public void setEvidence_type_version(String newEvidence_type_version) {
        evidence_type_version = newEvidence_type_version;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String newDate) {
        date = newDate;
        set_id(newDate);
    }

    public String getCrn() {
        return crn;
    }

    public void setCrn(String newCrn) {
        crn = newCrn;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String newResult) {
        result = newResult;
    }

    public Artifact[] getLog() {
        return log;
    }

    public void setLog(Artifact[] newLog) {
        log = newLog;
    }

    public Artifact[] getArtifacts() {
        return artifacts;
    }

    public void setArtifacts(Artifact[] newArtifacts) {
        artifacts = newArtifacts;
    }

    public boolean equals(Object obj) {
        boolean isEqual = false;
        if ((obj != null) && (obj instanceof Evidence)) isEqual = toString().equals(obj.toString());
        return isEqual;
   }

    public String toString() {
        return "{\"_id\": \""+_id+"\", \"_rev\": \""+_rev+"\", \"evidence_type_id\": \""+evidence_type_id+
            "\", \"evidence_type_version\": \""+evidence_type_version+"\", \"date\": \""+date+
            "\", \"crn\": \""+crn+"\", \"result\": \""+result+"\", \"log\": \""+log+"\", \"artifacts\": \""+artifacts+"\"}";
    }
}
