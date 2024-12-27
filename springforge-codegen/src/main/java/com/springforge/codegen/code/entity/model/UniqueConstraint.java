
package com.springforge.codegen.code.entity.model;

import static com.springforge.codegen.code.entity.model.Utils.*;

import com.springforge.codegen.code.JavaAnnotation;
import com.springforge.codegen.code.JavaCode;
import jakarta.xml.bind.annotation.XmlType;

@XmlType
public class UniqueConstraint extends Index {

  @Override
  public JavaAnnotation toJavaAnnotation(Entity entity) {
    JavaAnnotation annotation = new JavaAnnotation("jakarta.persistence.UniqueConstraint");
    if (notBlank(getName())) {
      annotation.param("name", "{0:s}", getName());
    }
    annotation.param("columnNames", list(getColumnList(entity)), s -> new JavaCode("{0:s}", s));
    return annotation;
  }
}
