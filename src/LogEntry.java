import javafx.beans.property.SimpleStringProperty;

public class LogEntry {
    private final SimpleStringProperty date;
    private final SimpleStringProperty action;
    private final SimpleStringProperty name;
    private final SimpleStringProperty flight;
    private final SimpleStringProperty details;

    public LogEntry(String date, String action, String name, String flight, String details) {
        this.date = new SimpleStringProperty(date);
        this.action = new SimpleStringProperty(action);
        this.name = new SimpleStringProperty(name);
        this.flight = new SimpleStringProperty(flight);
        this.details = new SimpleStringProperty(details);
    }

    public String getDate(){
        return date.get(); 
    }

    public String getAction(){
        return action.get(); 
    }

    public String getName(){ 
        return name.get(); 
    }

    public String getFlight(){ 
        return flight.get(); 
    }
    
    public String getDetails(){ 
        return details.get(); 
    }
}
