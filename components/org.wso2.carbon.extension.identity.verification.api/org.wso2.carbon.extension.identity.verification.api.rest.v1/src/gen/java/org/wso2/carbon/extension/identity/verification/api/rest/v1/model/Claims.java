/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.extension.identity.verification.api.rest.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;
import javax.validation.Valid;
import javax.xml.bind.annotation.*;

public class Claims  {
  
    private String claimUri;
    private String claimValue;

    /**
    **/
    public Claims claimUri(String claimUri) {

        this.claimUri = claimUri;
        return this;
    }
    
    @ApiModelProperty(value = "")
    @JsonProperty("claim-uri")
    @Valid
    public String getClaimUri() {
        return claimUri;
    }
    public void setClaimUri(String claimUri) {
        this.claimUri = claimUri;
    }

    /**
    **/
    public Claims claimValue(String claimValue) {

        this.claimValue = claimValue;
        return this;
    }
    
    @ApiModelProperty(value = "")
    @JsonProperty("claim-value")
    @Valid
    public String getClaimValue() {
        return claimValue;
    }
    public void setClaimValue(String claimValue) {
        this.claimValue = claimValue;
    }



    @Override
    public boolean equals(java.lang.Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Claims claims = (Claims) o;
        return Objects.equals(this.claimUri, claims.claimUri) &&
            Objects.equals(this.claimValue, claims.claimValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(claimUri, claimValue);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class Claims {\n");
        
        sb.append("    claimUri: ").append(toIndentedString(claimUri)).append("\n");
        sb.append("    claimValue: ").append(toIndentedString(claimValue)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
    * Convert the given object to string with each line indented by 4 spaces
    * (except the first line).
    */
    private String toIndentedString(java.lang.Object o) {

        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n");
    }
}

