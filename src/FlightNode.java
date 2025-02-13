public class FlightNode {

    Flight data ;
    FlightNode next;
    FlightNode prev;
   
   public FlightNode(Flight data) {
       super();
       this.data = data;
   }

   public Flight getData() {
       return data;
   }
   public void setData(Flight object) {
       this.data = object;
   }
   public FlightNode getNext() {
       return next;
   }
   public void setNext(FlightNode next) {
       this.next = next;
   }
   public FlightNode getPrev() {
       return prev;
   }
   public void setPrev(FlightNode prev) {
       this.prev = prev;
   }
   
   
   
}
