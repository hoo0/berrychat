package project.util;

public class sample {

    public static void main(String[] args) {
	    /*
         * For sid generation, private key and wctNo are required
         * The private key matches the version. In other words, when the version changes, the private key also changes.
         * For security reasons, the version and private key may change.
         * The private key of version "171001" is "dcf0d7ff6a65fb7d".
         */

        // version
        String version = "170602";
        // private key.
        String privateKey = "e00bc84be189dce9"; // 16byte = 128bit AES.
        // wctNo
        String wctNo = "24001";

        ////////////////////////////////////////////////////////////////////////////
        // sid generation
        String sid = util.generateSID(privateKey, wctNo);
        System.out.println("sid="+ sid);
        ////////////////////////////////////////////////////////////////////////////

        System.out.println();
        System.out.println();


        ////////////////////////////////////////////////////////////////////////////
        // 아래는 코레일의 API 서버 로직. Below is KORAIL's API server logic
        // sid check
        boolean sidCheck = util.checkSID(privateKey, wctNo, sid);
        System.out.println("sidCheck="+ (sidCheck?"true":"false"));
        ////////////////////////////////////////////////////////////////////////////
    }
}
