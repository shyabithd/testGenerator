package generator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;

public class ReflectionUtils {

  protected static final Logger logger = LoggerFactory.getLogger(ReflectionUtils.class);

  public static Class<?>[] getDeclaredClasses(Class<?> clazz) {
    try {
      return clazz.getDeclaredClasses();
    } catch (NoClassDefFoundError e) {
      // TODO: What shall we do?
      logger.info("Error while analyzing all classes of class " + clazz + ": " + e);
      return new Class<?>[0];
    }
  }

  public static Class<?>[] getClasses(Class<?> clazz) {
    try {
      return clazz.getClasses();
    } catch (NoClassDefFoundError e) {
      // TODO: What shall we do?
      logger.info("Error while analyzing all classes of class " + clazz + ": " + e);
      return new Class<?>[0];
    }
  }

  public static Constructor<?>[] getDeclaredConstructors(Class<?> clazz) {
    try {
      return clazz.getDeclaredConstructors();
    } catch (NoClassDefFoundError e) {
      // TODO: What shall we do?
      logger.info("Error while analyzing declared constructors of class " + clazz + ": " + e);
      return new Constructor<?>[0];
    }
  }

  public static Constructor<?>[] getConstructors(Class<?> clazz) {
    try {
      return clazz.getConstructors();
    } catch (NoClassDefFoundError e) {
      // TODO: What shall we do?
      logger.info("Error while analyzing constructors of class " + clazz + ": " + e);
      return new Constructor<?>[0];
    }
  }

  public static Class<?>[] getInterfaces(Class<?> clazz) {
    try {
      return clazz.getInterfaces();
    } catch (NoClassDefFoundError e) {
      // TODO: What shall we do?
      logger.info("Error while analyzing interfaces of class " + clazz + ": " + e);
      return new Class<?>[0];
    }
  }

  public static ClassReader.Method[] getDeclaredMethods(ClassReader clazz) {
    try {
      return clazz.getDeclaredMethods();
    } catch (NoClassDefFoundError e) {
      // TODO: What shall we do?
      logger.info(
          "Error while trying to load declared methods of class " + clazz.getClassName() + ": " + e);
      return new ClassReader.Method[0];
    }
  }

  public static ClassReader.Method[] getMethods(ClassReader clazz) {
    try {
      return clazz.getDeclaredMethods();
    } catch (NoClassDefFoundError e) {
      // TODO: What shall we do?
      logger.info("Error while trying to load methods of class " + clazz.getCanonicalName() + ": " + e);
      return new ClassReader.Method[0];
    }
  }

  public static ClassReader.Field[] getDeclaredFields(ClassReader clazz) {
    try {
      return clazz.getDeclaredFields();
    } catch (NoClassDefFoundError e) {
      // TODO: What shall we do?
      logger.info(
          "Error while trying to load declared fields of class " + clazz.getCanonicalName() + ": " + e);
      return new ClassReader.Field[0];
    }
  }

  public static ClassReader.Field getDeclaredField(ClassReader clazz, String fieldName) {
    try {
      return clazz.getDeclaredField(fieldName);
    } catch (NoClassDefFoundError e) {
      logger.info("Error while trying to load declared field '" + fieldName + "' of class "
          + clazz.getCanonicalName() + ": " + e);
      return null;
    }
  }

  public static ClassReader.Field[] getFields(ClassReader clazz) {
    try {
      return clazz.getDeclaredFields();
    } catch (NoClassDefFoundError e) {
      // TODO: What shall we do?
      logger.info("Error while trying to load fields of class " + clazz.getCanonicalName() + ": " + e);
      return new ClassReader.Field[0];
    }
  }
}
