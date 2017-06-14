/**
 * Created by bhartichourasiya on 6/10/17.
 */
public class Main {
    /* Main function to call all the methods. */
    public  static  void main(String args[]){
        Order order = new Order();
        Customer customer = new Customer();
        Order.MinMax minMaxItem;

        System.out.println("---------------------xxxxx---------------------");
        System.out.println("Total Number of orders are : "+order.getOrderCount());
        System.out.println("---------------------xxxxx---------------------");

        System.out.println("Number of unique customers : "+customer.getCustomerCount());
        System.out.println("---------------------xxxxx---------------------");

        minMaxItem = order.getMinMaxOrderedItem();
        System.out.println("Least frequently ordered item is");
        System.out.println("Product Name : "+minMaxItem.getMinName());
        System.out.println("Product Id : "+minMaxItem.getMinId()+"\n");

        System.out.println("Most frequently ordered item is : ");
        System.out.println("Product Name : "+minMaxItem.getMaxName());
        System.out.println("Product Id : "+minMaxItem.getMaxId());
        System.out.println("---------------------xxxxx---------------------");

        System.out.println("Median of order price value is : "+order.getMedian());
        System.out.println("---------------------xxxxx---------------------");

        System.out.println("Shortest Interval between 2 consecutive orders\n");
        customer.getShortestInterval();
        System.out.println("---------------------xxxxx---------------------");
    }
}
