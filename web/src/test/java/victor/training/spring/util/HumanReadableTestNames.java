package victor.training.spring.util;

import org.junit.jupiter.api.DisplayNameGenerator;

import java.lang.reflect.Method;

public class HumanReadableTestNames extends DisplayNameGenerator.Standard {
  public HumanReadableTestNames() {
  }

  public String generateDisplayNameForClass(Class<?> testClass) {
    return replaceCapitals(super.generateDisplayNameForClass(testClass));
  }

  public String generateDisplayNameForNestedClass(Class<?> nestedClass) {
    return replaceCapitals(super.generateDisplayNameForNestedClass(nestedClass));
  }

  public String generateDisplayNameForMethod(Class<?> testClass, Method testMethod) {
    return replaceCapitals(testMethod.getName());
  }

  private String replaceCapitals(String name) {
    name = name.replaceAll("([A-Z])", " $1")
        .replaceAll("_", " ")
        .replaceAll("\\$", " >")
        .replaceAll("\\s+", " ")
        .toLowerCase();
    return name;
  }
}