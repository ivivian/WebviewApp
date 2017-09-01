package cn.zzv.push.yqqwebview;
import java.security.MessageDigest;
import java.util.Calendar;

public class md5 {
		
    public String getMD5Str(String txt) {
        try{
    		Calendar c=Calendar.getInstance();
    		txt=txt+c.get(Calendar.YEAR)+"zzv"+c.get(Calendar.MONTH)+"cn"+c.get(Calendar.MONTH);
    		
             MessageDigest md = MessageDigest.getInstance("MD5");
             md.update(txt.getBytes("GBK"));
             StringBuffer buf=new StringBuffer();            
             for(byte b:md.digest()){
                  buf.append(String.format("%02x", b&0xff));        
             }
            return  buf.toString();
          }catch( Exception e ){
              e.printStackTrace(); 

              return "zzv.cn";
           }
   } 
}
