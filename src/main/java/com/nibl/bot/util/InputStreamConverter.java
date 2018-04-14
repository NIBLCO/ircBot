package com.nibl.bot.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

public class InputStreamConverter
{
    public static RandomAccessFile toRandomAccessFile(InputStream is) throws IOException 
    {
        RandomAccessFile raf = new RandomAccessFile(File.createTempFile("isc", "tmp"), "rwd");
  
        byte[] buffer = new byte[2048];
        int    tmp    = 0;
  
        while ((tmp = is.read(buffer)) != -1) 
        {
          raf.write(buffer, 0, tmp);
        }
         
        raf.seek(0);
         
        return raf;
    }
}