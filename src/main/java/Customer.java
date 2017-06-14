import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by bhartichourasiya on 6/10/17.
 */
public class Customer {
    HttpUtil httpUtil = new HttpUtil();
    private int limit=25;

    /*

    Below function will calculate the unique customers using the api
    ASSUMPTION : I assumed that id and email id are unique for every customer. Whenever a new customer is added, an id is created and it also
    verifies that there is no existing email.

     */
    public int getCustomerCount(){
        int count=0;
        String uri = "/admin/customers/count.json";
        JSONObject countJson = httpUtil.shopifyGet(uri, null);
        try {
            count = countJson.getInt("count");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return count;
    }

    /*

    Below function will calculate the shortest interval between any two consecutive orders placed by the customer.

    */
    public void getShortestInterval(){
        int page=1, orders_count=0;
        Long customerId;
        String customerEmail;
        JSONArray customerArray, orderArray;
        JSONObject customer, orderJson, order;
        HashMap<String, Integer> queryParameter = new HashMap<String, Integer>();
        queryParameter.put("page", page);
        queryParameter.put("limit", limit);
        customerArray = getCustomersList(queryParameter);

        try{
            while(customerArray.length() > 0){
                for(int i=0; i<customerArray.length(); i++){
                    customer = (JSONObject) customerArray.get(i);
                    customerId = customer.getLong("id");
                    orders_count = customer.getInt("orders_count");
                    customerEmail = customer.getString("email");

                    /*
                    Getting orders_count from customer.json itself, so that we don't need to call api for customers whose orders_count<2.
                    It will save lot of calls to GET /admin/customers/#{id}/orders.json api.
                    */
                    if(orders_count > 1){
                        String created_at;
                        String uri = "/admin/customers/"+customerId+"/orders.json";
                        orderJson = httpUtil.shopifyGet(uri, null);
                        orderArray = orderJson.getJSONArray("orders");
                        Date date1;
                        Long diff=null;
                        ArrayList<Date> duration = new ArrayList<Date>();

                        for(int j=0; j<orderArray.length(); j++){
                            order = (JSONObject) orderArray.get(j);
                            created_at = order.getString("created_at");
                            date1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(created_at);
                            duration.add(date1);
                        }
                        System.out.println("Customer Id : "+customerId);
                        System.out.println("Customer email id : "+customerEmail);
                        diff = getDifference(duration);
                        long second = (diff / 1000) % 60;
                        long minute = (diff / (1000 * 60)) % 60;
                        long hour = (diff / (1000 * 60 * 60)) % 24;
                        long days = (diff / (1000 * 60 * 60))/24;
                        String time = String.format("%d days, %d hours, %d minutes, %d seconds", days, hour, minute, second);
                        System.out.println("Shortest Interval : "+time+"\n");
                    }
                }
                page++;
                queryParameter.put("page", page);
                customerArray = getCustomersList(queryParameter);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*

    Below function will get the list of customers using the API /admin/customers.json?page=1&limit=10

     */
    public JSONArray getCustomersList(HashMap<String, Integer> hm){
        JSONObject customersJson;
        JSONArray customerArray=null;
        String uri;
        uri = "/admin/customers.json";
        customersJson = httpUtil.shopifyGet(uri, hm);
        try {
            customerArray = customersJson.getJSONArray("customers");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return customerArray;
    }


    /*

    Below function will calculate the shortest interval using the list of date&time.

    */
    public long getDifference(ArrayList<Date> duration){
        Collections.sort(duration, new Comparator<Date>() {
            public int compare(Date o1, Date o2) {
                return o1.compareTo(o2);
            }
        });
        Long min=null, diff=null;
        for(int i=1; i<duration.size(); i++){
            diff = duration.get(i).getTime() - duration.get(i-1).getTime();
            if(min == null || diff<min)
                min = diff;
        }
        return min ;
    }
}
