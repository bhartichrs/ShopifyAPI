import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

/**
 * Created by bhartichourasiya on 6/10/17.
 */
public class Order {

    class MinMax{
        Long minId;
        Long maxId;
        String minName;
        String maxName;

        public MinMax(Long minId, Long maxId, String minName, String  maxName){
            this.minId = minId;
            this.maxId = maxId;
            this.minName = minName;
            this.maxName = maxName;
        }

        public Long getMaxId() {
            return maxId;
        }

        public String getMinName() {
            return minName;
        }

        public String getMaxName() {
            return maxName;
        }

        public Long getMinId() {

            return minId;
        }
    }

    HttpUtil httpUtil = new HttpUtil();
    private int limit = 25;

    /*

    Below function will calculate the total number of orders using the api

    */
    public int getOrderCount(){
        int count=0;
        String uri = "/admin/orders/count.json";
        JSONObject countJson = httpUtil.shopifyGet(uri, null);
        try {
            count = countJson.getInt("count");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return count;
    }

    /*

    Below function will calculate the least and most frequently ordered items and returning an object of MinMax containing all the values.
     ASSUMPTION : If there is more than one quantity of an item in an order, it will still be considered as frequency 1.
     Appearance of an item in different orders will affect the frequency instead of quantity of an item in an order.

     */
    public MinMax getMinMaxOrderedItem(){
        int page=1;
        Long itemId;
        MinMax minMaxObj=null;
        JSONArray orderArray, itemArray;
        JSONObject order, item;
        HashMap<Long, Integer> hm = new HashMap<Long, Integer>();
        HashMap<String, Integer> queryParameter = new HashMap<String, Integer>();
        queryParameter.put("page", page);
        queryParameter.put("limit", limit);
        orderArray = getOrdersList(queryParameter);
        try {
            while(orderArray.length() > 0){
                for(int i=0; i<orderArray.length(); i++){
                    order= (JSONObject) orderArray.get(i);
                    itemArray = order.getJSONArray("line_items");
                    for(int j=0; j<itemArray.length(); j++) {
                        item = (JSONObject) itemArray.get(j);
                        itemId = item.getLong("product_id");

                        if(hm.containsKey(itemId))
                            hm.put(itemId, hm.get(itemId)+1);
                        else
                            hm.put(itemId, 1);
                    }
                }
                page++;
                queryParameter.put("page", page);
                orderArray = getOrdersList(queryParameter);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Map.Entry<Long, Integer> minEntry = null;
        Map.Entry<Long, Integer> maxEntry = null;
        for(Map.Entry<Long, Integer> entry : hm.entrySet()){
            if(minEntry==null || entry.getValue() < minEntry.getValue())
                minEntry = entry;
            if(maxEntry==null || entry.getValue() > maxEntry.getValue())
                maxEntry = entry;
        }

        JSONObject minName = httpUtil.shopifyGet("/admin/products/"+minEntry.getKey().toString()+".json",null);
        JSONObject maxName = httpUtil.shopifyGet("/admin/products/"+maxEntry.getKey().toString()+".json", null);

        try {
             minMaxObj = new MinMax(minEntry.getKey(), maxEntry.getKey(),
                    minName.getJSONObject("product").getString("title"), maxName.getJSONObject("product").getString("title"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return minMaxObj;
    }

    /*

    Below function will get the list of orders using the API /admin/orders.json?page=1&limit=10

     */
    public JSONArray getOrdersList(HashMap<String, Integer> hm){
        JSONObject orderJson;
        JSONArray orderArray=null;
        String uri;
        uri = "/admin/orders.json";
        orderJson = httpUtil.shopifyGet(uri, hm);
        try {
            orderArray = orderJson.getJSONArray("orders");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return orderArray;
    }

     /*

     Below function is calculating the median of order value i.e. total_price for all the orders.
     ASSUMPTION : I am assuming that order value is "total_price".

     */
    public Double getMedian(){
        Double median=0.0;
        int page=1;
        ArrayList<Double> input = new ArrayList<Double>();
        JSONArray orderArray;
        JSONObject order;
        HashMap<String, Integer> queryParameter = new HashMap<String, Integer>();
        queryParameter.put("page", page);
        queryParameter.put("limit", limit);
        orderArray = getOrdersList(queryParameter);
        try{
            while(orderArray.length() > 0){
                for(int i=0; i<orderArray.length(); i++) {
                    order = (JSONObject) orderArray.get(i);
                    input.add(Double.valueOf(order.getString("total_price")));
                }
                page++;
                queryParameter.put("page", page);
                orderArray = getOrdersList(queryParameter);
            }
        }catch (JSONException e) {
            e.printStackTrace();
        }

        int size=input.size();
        /*
        If number of elements are odd, return element at index size/2, If elements are even, return average of size/2 and (size/2)-1
         */
        if(size%2 != 0)
            median = calculateMedian(input, 0, size-1, (size/2)+1);
        else
            median = (calculateMedian(input, 0, size-1, (size/2)+1) + calculateMedian(input, 0, size-1, size/2))/2;
        return median;
    }

    /*

    Below function will calculate the median of arraylist in O(n), which improves the performance as compared to other sorting
     algorithms which takes O(nlogn)

     */
    public Double calculateMedian(ArrayList<Double> input, int left, int right, int k){
        int size=right-left+1;

        if(k > 0 && k <= size){
            ArrayList<Double> median = new ArrayList<Double>((size+4)/5);
            for(int j=0; j<((size+4)/5); j++)
                median.add(null);

            int i;
            for(i=0; i<(size/5); i++)
                median.set(i, findMedian(input, left+(i*5), 5));
            if(i*5 < size){
                median.set(i, findMedian(input, left+(i*5), size%5));
                i++;
            }

            Double recurMedian = (i==1) ? median.get(i-1) : calculateMedian(median, 0, i-1, i/2);
            int position = partition(input, left, right, recurMedian);

            if(position-left == k-1)
                return input.get(position);
            if(position-left > k-1)
                return calculateMedian(input, left, position-1, k);
            return calculateMedian(input, position+1, right, k-position+left-1);
        }
        return Double.MAX_VALUE;
    }

    /*

    Below function will calculate the median for arraylist of size 5, which end up taking constant time in sorting the arraylist.

    */
    public Double findMedian(ArrayList<Double> input, int start, int size){
        Collections.sort(input.subList(start, start+size));
        return input.get((start+start+size)/2);
    }

    /*

    Below function will partition the input arraylist based on the pivot value. All elements less than pivot will be on the left
     and all elements greater than pivot will be on right of the pivot.

     */
    public int partition(ArrayList<Double> input, int left, int right, Double pivot){
        int i;
        for(i=left; i<right; i++)
            if(input.get(i) == pivot)
                break;
        swap(input, i, right);

        i=left;
        for(int j=left; j<right; j++){
            if(input.get(j) <= pivot){
                swap(input, i, j);
                i++;
            }
        }
        swap(input, i, right);
        return i;
    }

    public void swap(ArrayList<Double> input, int i, int j){
        Double temp = input.get(i);
        input.set(i, input.get(j));
        input.set(j, temp);
    }
}
