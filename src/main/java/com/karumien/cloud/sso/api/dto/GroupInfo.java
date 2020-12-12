package com.karumien.cloud.sso.api.dto;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class GroupInfo {
	
	  @JsonProperty("moduleId")
	  private String moduleId;

	  @JsonProperty("groupId")
	  private String groupId;

	  @JsonProperty("name")
	  private String name;
	  
	  @JsonProperty("serviceId")
	  private String serviceId;

	  @JsonProperty("translation")
	  private String translation;

	  @JsonProperty("businessPriority")
	  private Integer businessPriority;

	  @JsonProperty("groups")
	  private List<GroupInfo> groups;
	  
	  @JsonProperty("attributes")
	  private Map<String, List<String>> attributes;

}
