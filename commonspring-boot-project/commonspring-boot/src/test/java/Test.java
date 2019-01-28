import java.util.Properties;

public class Test {

    public static void main(String[] args) {
        Properties properties=System.getProperties();
        for (Object obj:properties.keySet()){
            System.out.println(obj+":"+properties.get(obj));

        }
    }
}
