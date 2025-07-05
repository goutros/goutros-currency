package net.goutros.goutroscurrency.util;

import java.util.Objects;

public record WalkPos(float x, float z) {
   public static final WalkPos ZERO = new WalkPos(0.0F, 0.0F);

   public WalkPos(float x, float z) {
      this.x = x;
      this.z = z;
   }

   public float distanceTo(WalkPos other) {
      float dx = other.x - this.x;
      float dz = other.z - this.z;
      return (float)Math.sqrt((double)(dx * dx + dz * dz));
   }

   public boolean equals(Object o) {
      boolean var10000;
      if (o instanceof WalkPos) {
         WalkPos other = (WalkPos)o;
         if (Float.compare(other.x, this.x) == 0 && Float.compare(other.z, this.z) == 0) {
            var10000 = true;
            return var10000;
         }
      }

      var10000 = false;
      return var10000;
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.x, this.z});
   }

   public float x() {
      return this.x;
   }

   public float z() {
      return this.z;
   }
}
