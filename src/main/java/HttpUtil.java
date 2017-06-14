import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by bhartichourasiya on 6/10/17.
 */
public class HttpUtil {
    private URL url;
    private String host = "https://100pure-demo.myshopify.com";
    private String token = "b1ade8379e97603f3b0d92846e238ad8";
    private InputStream is;
    JSONObject jsonObject;

    /* Below method will call the required API and returns the JSON response. */
    public JSONObject shopifyGet(String uri, HashMap<String, Integer> queryParameter) {
        StringBuilder query = new StringBuilder("");
        int index=0;
        if(queryParameter!=null && queryParameter.size() > 0){
            for(Map.Entry<String, Integer> entry : queryParameter.entrySet()){
                if(index==0)
                    query.append("?"+entry.getKey()+"="+String.valueOf(entry.getValue()));
                else
                    query.append("&"+entry.getKey()+"="+String.valueOf(entry.getValue()));
                index=1;
            }
        }

        try {
            url = new URL(host+uri+query.toString());
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setRequestProperty("X-Shopify-Access-Token", token);
            httpURLConnection.connect();

            BufferedReader br = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            String line="";
            StringBuilder response = new StringBuilder();

            while((line = br.readLine()) != null){
                response.append(line);
            }
            jsonObject = new JSONObject(response.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
}
