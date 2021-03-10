package com.team23.stim.classes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonPropertyOrder({"name", "sku", "price"})
@JsonIgnoreProperties(ignoreUnknown = true)
public class MainItem {
  @JsonProperty("name")
  String name;
  @JsonProperty("sku")
  int sku;
  @JsonProperty("price")
  BigDecimal price;
  int qty;
  List<Integer> subSKUs;
  MainItem(String name, int sku, BigDecimal price, List<Integer> subSKUs)
  {
    this.name = name;
    this.sku = sku;
    this.price = price;
    this.subSKUs = new ArrayList<Integer>();
  }

  String getName()
  {
    return this.name;
  }
  int getSKU()
  {
    return this.sku;
  }
  BigDecimal getPrice()
  {
    return this.price;
  }
  List<Integer> getSubSkus()
  {
    return this.subSKUs;
  }
}
