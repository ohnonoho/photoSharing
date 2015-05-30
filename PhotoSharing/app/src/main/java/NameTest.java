import net.named_data.jndn.Interest;
import net.named_data.jndn.Name;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by peiyang on 15/5/26.
 */
public class NameTest {
    public NameTest() {

    }

    public static void main(String[] args) {
        try {
            Name name = new Name("/test/1");
            name.appendSequenceNumber(1);
            Interest interest = new Interest(name);
            interest.setMaxSuffixComponents(2);
            name = interest.getName();
            long count = interest.getMaxSuffixComponents();
            Name.Component component1 = name.get(0);
            Name.Component component2 = name.get(1);
            System.out.println(count);
            System.out.println(component1.toEscapedString());
            System.out.println(component2.toEscapedString());
            System.out.println(name.toUri());
            System.out.println(name.size());
            System.out.println(name.get(2).toSequenceNumber());

            JSONObject json = new JSONObject();
            ArrayList<String> list = new ArrayList<>();
            list.add("filename1");
            list.add("filename2");
            JSONArray array = new JSONArray(list);
            json.put("file", array);json.put("mode", "public");


            String jsonString = json.toString();
            System.out.println(jsonString);
            JSONObject reJson = new JSONObject(jsonString);
            System.out.println(reJson.get("mode"));
            JSONArray reArray = reJson.getJSONArray("file");
            for(int i = 0; i < reArray.length(); ++i) {
                System.out.println(reArray.getString(i));
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
