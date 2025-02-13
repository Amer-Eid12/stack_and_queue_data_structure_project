public class Stack {

    private FlightNode top;

    public Stack() {
        this.top = null;
    }

    public boolean isEmpty() {
        return top == null;
    }

    public void push(Flight data) {
        FlightNode newNode = new FlightNode(data);
        if (newNode == null) {
            System.out.println("Stack Overflow");
            return;
        }
        newNode.next = top; 
        top = newNode; 
    }

    public Flight pop() {
        if (isEmpty()) {
            System.out.println("Stack Underflow");
            return null;
        }
        FlightNode temp = top; 
        top = top.next;  
        return temp.getData(); 
    }

    public Flight peek() {
        if (isEmpty()) {
            System.out.println("Stack is empty");
            return null;
        }
        return top.getData();
    }

    public void printStack() {
        if (isEmpty()) {
            System.out.println("Stack is empty");
            return;
        }
        FlightNode current = top;
        System.out.println("Stack elements:");
        while (current != null) {
            System.out.println(current.getData().toString());
            current = current.next;
        }
    }
}
