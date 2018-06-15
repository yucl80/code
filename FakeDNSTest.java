import java.net.InetAddress;
import java.net.UnknownHostException;

//run with option  --add-opens java.base/java.net=ALL-UNNAMED

public class FakeDNSTest {

    public static void main(String[] args){     
        FakeDNS fakeDns=FakeDNS.getInstance();
        fakeDns.install();    
        try {
            fakeDns.addForwardResolution("test",InetAddress.getByName("192.168.142.2"));
            System.out.println(InetAddress.getByName("www.163.com").getHostAddress());
            System.out.println(InetAddress.getByName("test").getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
