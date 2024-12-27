package com.springforge.codegen.code.entity.model;

import jakarta.xml.bind.annotation.XmlEnum;

@XmlEnum
public enum TrackEvent {
  ALWAYS,

  CREATE,

  UPDATE;

  public String value() {
    return name();
  }

  public static TrackEvent fromValue(String v) {
    return valueOf(v);
  }
}