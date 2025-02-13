public class PassengerNode {

    Passenger data ;
    PassengerNode next;
    PassengerNode prev;
   
   public PassengerNode(Passenger data) {
       super();
       this.data = data;
   }

   public Passenger getData() {
       return data;
   }
   public void setData(Passenger object) {
       this.data = object;
   }
   public PassengerNode getNext() {
       return next;
   }
   public void setNext(PassengerNode next) {
       this.next = next;
   }
   public PassengerNode getPrev() {
       return prev;
   }
   public void setPrev(PassengerNode prev) {
       this.prev = prev;
   }
   
   
   
}
