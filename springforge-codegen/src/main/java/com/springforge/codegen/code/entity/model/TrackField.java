package com.springforge.codegen.code.entity.model;

import static com.springforge.codegen.code.entity.model.Utils.*;

import com.springforge.codegen.code.JavaAnnotation;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

@XmlType
public class TrackField {

  @XmlAttribute(name = "name", required = true)
  private String name;

  @XmlAttribute(name = "if")
  private String condition;

  @XmlAttribute(name = "on")
  private TrackEvent on;

  public String getName() {
    return name;
  }

  public void setName(String value) {
    this.name = value;
  }

  public String getCondition() {
    return condition;
  }

  public TrackEvent getOn() {
    return on;
  }

  public void setOn(TrackEvent value) {
    this.on = value;
  }

  public JavaAnnotation toJavaAnnotation() {
    JavaAnnotation a =
            new JavaAnnotation("com.springforge.db.annotations.TrackField").param("name", "{0:s}", name);

    if (notBlank(condition)) a.param("condition", "{0:s}", condition);
    if (on != null) a.param("on", "{0:m}", "com.springforge.db.annotations.TrackEvent." + on);

    return a;
  }
}