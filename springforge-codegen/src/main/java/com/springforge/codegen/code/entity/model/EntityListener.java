
package com.springforge.codegen.code.entity.model;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

@XmlType
public class EntityListener {

  @XmlAttribute(name = "class", required = true)
  private String clazz;

  public String getClazz() {
    return clazz;
  }

  public void setClazz(String value) {
    this.clazz = value;
  }
}
