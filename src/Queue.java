public class Queue {

    private PassengerNode front, rear;

    public Queue() {
        front = rear = null;
    }

    public boolean isEmpty() {
        return front == null;
    }

    public void enqueue(Passenger data) {
        PassengerNode newNode = new PassengerNode(data);

        if (rear == null) {
            front = rear = newNode;
            return;
        }

        rear.next = newNode;
        rear = newNode;
    }

    public Passenger dequeue() {
        if (isEmpty()) {
            System.out.println("Queue Underflow");
            return null;
        }

        PassengerNode temp = front;
        front = front.next;

        if (front == null) {
            rear = null;
        }

        return temp.getData();
    }

    public PassengerNode getFront() {
        if (isEmpty()) {
            System.out.println("Queue is empty");
            return null;
        }
        return front;
    }

    public PassengerNode getRear() {
        if (isEmpty()) {
            System.out.println("Queue is empty");
            return null;
        }
        return rear;
    }

    public void printQueue() {
        if (isEmpty()) {
            System.out.println("Queue is empty");
            return;
        }

        PassengerNode current = front;
        System.out.println("Queue elements:");
        while (current != null) {
            System.out.println(current.getData().toString());
            current = current.next;
        }
    }

    public boolean remove(Passenger target) {
        if (isEmpty()) {
            System.out.println("Queue is empty. Cannot remove.");
            return false;
        }

        PassengerNode current = front;
        PassengerNode previous = null;

        while (current != null) {
            if (current.getData().equals(target)) {
                if (previous == null) {
                    front = current.next;
                    if (front == null) {
                        rear = null;
                    }
                } else {
                    previous.next = current.next;
                    if (current == rear) {
                        rear = previous;
                    }
                }
                return true;
            }
            previous = current;
            current = current.next;
        }

        System.out.println("Passenger not found in the queue.");
        return false;
    }

    public int getSize() {
        int size = 0;
        PassengerNode current = front;

        while (current != null) {
            size++;
            current = current.next;
        }

        return size;
    }
}
