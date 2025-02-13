public class Flight {
    private int flightID;
    private String Destination;
    private String Status;
    private Queue regularQueue;
    private Queue VIPQueue;
    private Stack undoStack;
    private Stack redoStack;
    private PassengersLinkedList boardedPassengersList;
    private PassengersLinkedList canceledPassengersList;
    private Passenger passenger;
    private String action;
    
    public Flight(int flightID, String destination, String status, Queue regularQueue, Queue vIPQueue, Stack undoStack,
            Stack redoStack, PassengersLinkedList boardedPassengersList, PassengersLinkedList canceledPassengersList) {
        this.flightID = flightID;
        Destination = destination;
        Status = status;
        this.regularQueue = regularQueue;
        VIPQueue = vIPQueue;
        this.undoStack = undoStack;
        this.redoStack = redoStack;
        this.boardedPassengersList = boardedPassengersList;
        this.canceledPassengersList = canceledPassengersList;
    }

    public Flight(int id, Passenger passenger, String action) {
        this.flightID = id;
        this.passenger = passenger;
        this.action = action;
    }

    public int getFlightID() {
        return flightID;
    }

    public void setFlightID(int flightID) {
        this.flightID = flightID;
    }

    public String getDestination() {
        return Destination;
    }

    public void setDestination(String destination) {
        Destination = destination;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public Queue getRegularQueue() {
        return regularQueue;
    }

    public void setRegularQueue(Queue regularQueue) {
        this.regularQueue = regularQueue;
    }

    public Queue getVIPQueue() {
        return VIPQueue;
    }

    public void setVIPQueue(Queue vIPQueue) {
        VIPQueue = vIPQueue;
    }

    public Stack getUndoStack() {
        return undoStack;
    }

    public void setUndoStack(Stack undoStack) {
        this.undoStack = undoStack;
    }

    public Stack getRedoStack() {
        return redoStack;
    }

    public void setRedoStack(Stack redoStack) {
        this.redoStack = redoStack;
    }

    public PassengersLinkedList getBoardedPassengersList() {
        return boardedPassengersList;
    }

    public void setBoardedPassengersList(PassengersLinkedList boardedPassengersList) {
        this.boardedPassengersList = boardedPassengersList;
    }

    public PassengersLinkedList getCanceledPassengersList() {
        return canceledPassengersList;
    }

    public void setCanceledPassengersList(PassengersLinkedList canceledPassengersList) {
        this.canceledPassengersList = canceledPassengersList;
    }

    public Passenger getPassenger() {
        return passenger;
    }

    public void setPassenger(Passenger passenger) {
        this.passenger = passenger;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
  
}
