package com.springforge.codegen.code.entity.model;

import static com.springforge.codegen.code.entity.model.Utils.*;

import com.springforge.codegen.code.JavaAnnotation;
import com.springforge.codegen.code.JavaCode;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.XmlValue;

@XmlType
public class TrackMessage {

  @XmlValue private String value;

  @XmlAttribute(name = "if", required = true)
  private String condition;

  @XmlAttribute(name = "on")
  private TrackEvent on;

  @XmlAttribute(name = "tag")
  private TrackTag tag;

  @XmlAttribute(name = "fields")
  private String fields;

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getCondition() {
    return condition;
  }

  public void setCondition(String condition) {
    this.condition = condition;
  }

  public TrackEvent getOn() {
    if (on == null) {
      return TrackEvent.ALWAYS;
    } else {
      return on;
    }
  }

  public void setOn(TrackEvent value) {
    this.on = value;
  }

  public TrackTag getTag() {
    return tag;
  }

  public void setTag(TrackTag value) {
    this.tag = value;
  }

  public String getFields() {
    return fields;
  }

  public void setFields(String value) {
    this.fields = value;
  }

  public JavaAnnotation toJavaAnnotation() {
    var annon =
            new JavaAnnotation("com.springforge.db.annotations.TrackMessage")
                    .param("message", "{0:s}", value)
                    .param("condition", "{0:s}", condition);

    if (tag != null) annon.param("tag", "{0:s}", tag.value());
    if (on != null) {
      annon.param("on", "{0:m}", "com.springforge.db.annotations.TrackEvent." + on);
    }

    annon.param("fields", list(fields), s -> new JavaCode("{0:s}", s));

    return annon;
  }
}