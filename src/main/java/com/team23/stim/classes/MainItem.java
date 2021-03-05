package com.team23.stim.classes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class MainItem {
  String name;
  int sku;
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
