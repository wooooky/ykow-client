package skid.ykow.utils;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public final class RamAuthUtil {
   private static final AtomicReference<String> REGISTRY_A = new AtomicReference<>();
   private static final AtomicReference<String> REGISTRY_B = new AtomicReference<>();

   private RamAuthUtil() {
   }

   public static boolean setIfAbsentA(String value) {
      return REGISTRY_A.compareAndSet(null, value);
   }

   public static void forceSetA(String value) {
      REGISTRY_A.set(value);
   }

   public static Optional<String> getA() {
      return Optional.ofNullable(REGISTRY_A.get());
   }

   public static Optional<String> getAndClearA() {
      return Optional.ofNullable(REGISTRY_A.getAndSet(null));
   }

   public static boolean replaceIfMatchesA(String expected, String newValue) {
      return REGISTRY_A.compareAndSet(expected, newValue);
   }

   public static Optional<String> getAndOverwriteA(String newValue) {
      return Optional.ofNullable(REGISTRY_A.getAndSet(newValue));
   }

   public static void clearA() {
      REGISTRY_A.set(null);
   }

   public static boolean existsA() {
      return REGISTRY_A.get() != null;
   }

   public static boolean setIfAbsentB(String value) {
      return REGISTRY_B.compareAndSet(null, value);
   }

   public static void forceSetB(String value) {
      REGISTRY_B.set(value);
   }

   public static Optional<String> getB() {
      return Optional.ofNullable(REGISTRY_B.get());
   }

   public static Optional<String> getAndClearB() {
      return Optional.ofNullable(REGISTRY_B.getAndSet(null));
   }

   public static boolean replaceIfMatchesB(String expected, String newValue) {
      return REGISTRY_B.compareAndSet(expected, newValue);
   }

   public static Optional<String> getAndOverwriteB(String newValue) {
      return Optional.ofNullable(REGISTRY_B.getAndSet(newValue));
   }

   public static void clearB() {
      REGISTRY_B.set(null);
   }

   public static boolean existsB() {
      return REGISTRY_B.get() != null;
   }
}
