package project.util;

// 일반 클래스
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import org.apache.commons.codec.binary.Base64;

public class util {
	private static long MaxTimeInterval = 60 * 1000;

	public static String generateSID(String key, String wctNo) {
		String timestamp = ""+(new java.util.Date()).getTime();
		String source = wctNo + timestamp;
		System.out.println("generateSID.source="+ source);

		String sid = "";
		try {
			sid = secure.encryptAES(key, source);
		} catch(Exception e) {
			System.out.println("generateSID.e="+ e.toString());
			sid = "error";
		}
		return sid;
	}

	public static String decryptSID(String key, String sid) {
    	String decrypted = "";
    	try {
        	decrypted = secure.decryptAES(key, sid);
		} catch (Exception e) {
			System.out.println("e="+e);
			decrypted = "error";
		}
		return decrypted;
	}

	public static boolean checkSID(String key, String wctNo, String sid) {
		String decSid = decryptSID(key, sid);
		System.out.println("checkSID.decSid="+ decSid);

		return checkDecSID(wctNo, decSid);
	}

	public static boolean checkDecSID(String wctNo, String decSid) {
		if (decSid.length() < 18) {
			System.out.println("checkDecSID. decSid's length is too short. - "+ decSid.length());
			return false;
		}

		String sidWctNo = decSid.substring(0,5);
		String sidTimestamp = decSid.substring(5);
		long iSidTimestamp = Long.parseLong(sidTimestamp);
		long timestamp = (new java.util.Date()).getTime();

		if (!sidWctNo.equals(wctNo)) {
			System.out.println("checkDecSID. wctNo is different.");
			return false;
		}

		long timeInterval = timestamp - iSidTimestamp;
		System.out.println("checkDecSID.timeInterval= "+ timestamp +" - "+ iSidTimestamp +" = "+ timeInterval);
		if (timeInterval > MaxTimeInterval) {
			System.out.println("checkDecSID. time interval is too large. - "+ timeInterval);
			return false;
		}
		return true;
	}

    public static String generateSID2(String key, String device) {
        String timestamp = ""+(new java.util.Date()).getTime();
        String source = device + timestamp;
        System.out.println("generateSID.source="+ source);

        String sid = "";
        try {
            sid = secure.encryptAES(key, source);
        } catch(Exception e) {
            System.out.println("generateSID.e="+ e.toString());
            sid = "error";
        }
        return sid;
    }

	public static boolean checkSID2(String key, String device, String sid) {
		String decSid = decryptSID(key, sid);
		System.out.println("decSid2="+ decSid);

		return checkDecSID2(device, decSid);
	}

	public static boolean checkDecSID2(String device, String decSid) {
		if (decSid.length() < 15) {
			System.out.println("checkDecSID2. decSid's length is too short. - "+ decSid.length());
			return false;
		}

		String sidDevice = decSid.substring(0,2);
		String sidTimestamp = decSid.substring(2);
		long iSidTimestamp = Long.parseLong(sidTimestamp);
		long timestamp = (new java.util.Date()).getTime();

		if (!sidDevice.equals(device)) {
			System.out.println("checkDecSID2. device is different.");
			return false;
		}

		long timeInterval = timestamp - iSidTimestamp;
		System.out.println("checkDecSID2.timeInterval= "+ timestamp +" - "+ iSidTimestamp +" = "+ timeInterval);
		if (timeInterval > MaxTimeInterval) {
			System.out.println("checkDecSID2. time interval is too large. - "+ timeInterval);
			return false;
		}
		return true;
	}

	// 문자열(숫자) -> 정수
	public static int parseInt(String strNumber) {
		int iResult = 0;
		if (strNumber == null) return iResult;
		try {
			iResult = Integer.parseInt(strNumber, 10);
		} catch (Exception e) {
			System.out.println("parseInt:e="+e.toString());
		}
		return iResult;
	}

	// utf-16 -> string
	public static String unescape(String src) {
		StringBuffer tmp = new StringBuffer();
		tmp.ensureCapacity(src.length());
		int lastPos = 0, pos = 0;
		char ch;

		while (lastPos < src.length()) {
			pos = src.indexOf("%", lastPos);
			if (pos == lastPos) {
				if (src.charAt(pos + 1) == 'u') {
					ch = (char) Integer.parseInt(src
					  .substring(pos + 2, pos + 6), 16);
					tmp.append(ch);
					lastPos = pos + 6;
				} else {
					ch = (char) Integer.parseInt(src
					  .substring(pos + 1, pos + 3), 16);
					tmp.append(ch);
					lastPos = pos + 3;
				}
			} else {
				if (pos == -1) {
					tmp.append(src.substring(lastPos));
					lastPos = src.length();
				} else {
					tmp.append(src.substring(lastPos, pos));
					lastPos = pos;
				}
			}
		}
		return tmp.toString();
	}

	// trim, null 처리
	public static String trim(Object obj) {
		if (obj != null && obj instanceof String) {
			return ((String)obj).trim();
		}
		return "";
	}

	public static String trim(Object obj, String str2) {
		String str = trim(obj);
		str2 = trim(str2);
		return str.equals("")?str2:str;
	}

	public static String trim(String str) {
		return (str == null)?"":str.trim();
	}

	public static String trim(String str, String str2) {
		str = trim(str);
		str2 = trim(str2);
		return str.equals("")?str2:str;
	}

	// 직렬화, class -> string
	public static String serialize(Object o) {
		ByteArrayOutputStream bos = null;
		ObjectOutputStream oos = null;

		String result = null;
		try {
			bos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(bos);
			oos.writeObject(o);

			Base64 base64 = new Base64();
			byte[] base64EncodedByteArray = base64.encode(bos.toByteArray());
			result = new String(base64EncodedByteArray);
			//System.out.println("serialize.result:"+result);

			result = URLEncoder.encode(result, "UTF-8");
			//System.out.println("serialize.result:"+result);

		} catch (Exception e) {
			System.out.println("serialize.e:"+e.toString());
			result = null;
		}

		return result;
	}

	// 역직렬화, string -> class
	public static Object deserialize(String data) {
		ObjectInputStream ois = null;

		Object result = null;
		try {
			data = URLDecoder.decode(data, "UTF-8");
			//System.out.println("deserialize.data:"+data);

			// Base64 decode
			Base64 base64 = new Base64();
			byte[] dataBytes = data.getBytes("UTF-8");
			byte[] base64DecodedByteArray = base64.decode(dataBytes);
			ois = new ObjectInputStream(new ByteArrayInputStream(base64DecodedByteArray));
			result = ois.readObject();

		} catch (Exception e) {
			System.out.println("deserialize.e:"+e.toString());
			result = null;
		}

		return result;
	}

	public static String timestampToDateString(String timestamp) {
		timestamp = trim(timestamp);
		//System.out.println("timestampToDateString.timestamp:"+timestamp);
		if (timestamp.equals("")) return "";

		String result = "";
		try {
			long lTime = Long.parseLong(timestamp);
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
			result = formatter.format(new java.util.Date(lTime));
		} catch(Exception e) {
			System.out.println("timestampToDateString.e:"+e.toString());
		}
		return result;
	}
}
