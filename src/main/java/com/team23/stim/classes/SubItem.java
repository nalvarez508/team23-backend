package com.team23.stim.classes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SubItem {
  String name;
  int sku;
  int qty;
  int muq;
  String muqType;
  SubItem(String name, int sku, int qty, int muq, String muqType)
  {
    this.name = name;
    this.sku = sku;
    this.qty = qty;
    this.muq = muq;
    this.muqType = (muq + " " + muqType);
  }

  String getName()
  {
    return this.name;
  }
  int getSKU()
  {
    return this.sku;
  }
  int getQty()
  {
    return this.qty;
  }
  int getMuq()
  {
    return this.muq;
  }
  String getType()
  {
    return this.muqType;
  }
}
