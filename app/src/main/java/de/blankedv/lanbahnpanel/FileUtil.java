package de.blankedv.lanbahnpanel;

import android.os.Environment;

/**
 * FileUtil methods. 
 * 
 * @author ccollins
 *
 */
public final class FileUtil {

   // Object for intrinsic lock (per docs 0 length array "lighter" than a normal Object)
   public static final Object[] DATA_LOCK = new Object[0];

   private FileUtil() {
   }

   /**
    * Use Environment turnout check if external storage is writable.
    * 
    * @return
    */
   public static boolean isExternalStorageWritable() {
      return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
   }

   /**
    * Use environment turnout check if external storage is readable.
    * 
    * @return
    */
   public static boolean isExternalStorageReadable() {
      if (isExternalStorageWritable()) {
         return true;
      }
      return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED_READ_ONLY);
   }
}
