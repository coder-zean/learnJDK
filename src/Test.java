import java.util.*;

public class Test {

    public static void main(String[] args) {

        ArrayList<String> arrayList=new ArrayList();
        arrayList.add("a");
        arrayList.add("b");
        arrayList.add("c");

        for(String a:arrayList){
            if(a.equals("a"))
                arrayList.remove("b");
        }

    }
}
