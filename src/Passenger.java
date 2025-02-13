public class Passenger {
    private int passengerID;
    private String name;
    private int flightID;
    private String status;
    
    public Passenger(int passengerID, String name, int flightID, String status) {
        this.passengerID = passengerID;
        this.name = name;
        this.flightID = flightID;
        this.status = status;
    }

    public Passenger(Passenger new_data) {
        this.passengerID = new_data.passengerID;
        this.name=new_data.name;
        this.flightID=new_data.flightID;
        this.status=new_data.status;
    }

    public Passenger(){}

    public int getPassengerID() {
        return passengerID;
    }

    public void setPassengerID(int passengerID) {
        this.passengerID = passengerID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getFlightID() {
        return flightID;
    }

    public void setFlightID(int flightID) {
        this.flightID = flightID;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
    
}
