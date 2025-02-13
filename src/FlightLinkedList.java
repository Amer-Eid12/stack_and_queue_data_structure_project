public class FlightLinkedList {
	
	private FlightNode first, last;
	static int count=0;
	

	public FlightNode getFirst() {
		if(first==null)
			return null;
		
		return first;
	}

	public FlightNode getLast() {
		if(last==null)
			return null;
		
		return last;
	}
	
	public void addFirst(Flight data) {
		FlightNode newFlightNode= new FlightNode(data);
		
		if(first==null){
			first = last = newFlightNode;
            newFlightNode.next = newFlightNode;
            newFlightNode.prev = newFlightNode;
		
		}else {
			newFlightNode.next = first;
            newFlightNode.prev = last;
            first.prev = newFlightNode;
            last.next = newFlightNode;
            first = newFlightNode;
		}
		count++;
	}

	public void addLast(Flight data) {
		FlightNode newFlightNode= new FlightNode(data);
		
		if(last==null){
			first = last = newFlightNode;
            newFlightNode.next = newFlightNode;
            newFlightNode.prev = newFlightNode;
		}
		
		else {
			newFlightNode.prev = last;
            newFlightNode.next = first;
            last.next = newFlightNode;
            first.prev = newFlightNode;
            last = newFlightNode;
		}
		count++;
	}

	public void add(Flight data, int index) {
		if (index <= 0) {
            addFirst(data);
        } else if (index >= count) {
            addLast(data);
        } else {
            FlightNode newFlightNode = new FlightNode(data);
            FlightNode temp = first;
            for (int i = 0; i < index - 1; i++) {
                temp = temp.next;
            }
            newFlightNode.next = temp.next;
            newFlightNode.prev = temp;
            temp.next.prev = newFlightNode;
            temp.next = newFlightNode;
            count++;
        }
	}

	public boolean removeFirst() {
		if (first == null) return false;

        if (count == 1) {
            first = last = null;
        } else {
            first = first.next;
            first.prev = last;
            last.next = first;
        }
        count--;
        return true;
	}

	public boolean removeLast() {
		if (last == null) return false;

        if (count == 1) {
            first = last = null;
        } else {
            last = last.prev;
            last.next = first;
            first.prev = last;
        }
        count--;
        return true;
	}

	public boolean remove(int index) {
		if (index < 0 || index >= count) return false;

        if (index == 0) {
            return removeFirst();
        } else if (index == count - 1) {
            return removeLast();
        } else {
            FlightNode temp = first;
            for (int i = 0; i < index; i++) {
                temp = temp.next;
            }
            temp.prev.next = temp.next;
            temp.next.prev = temp.prev;
            count--;
            return true;
        }
	}

	public boolean remove(Flight data) {
		FlightNode current = first;
        if (first == null) return false;

        for (int i = 0; i < count; i++) {
            if (current.getData().equals(data)) {
                if (i == 0) return removeFirst();
                else if (i == count - 1) return removeLast();
                else {
                    current.prev.next = current.next;
                    current.next.prev = current.prev;
                    count--;
                    return true;
                }
            }
            current = current.next;
        }
        return false;
	}

	public  void printFlightList() { 
	    if (first == null) return;
        FlightNode temp = first;
        do {
            System.out.println(((Flight) temp.getData()).getFlightID());
            temp = temp.next;
        } while (temp != first);
	}
	
	public int getSize() {
		return count;
	}

	public Flight getFlightByFlightID(int FlightID) {
        FlightNode current = first;
        if (current == null) return null;
        do {
            if (((Flight) current.getData()).getFlightID()==FlightID) {
                return (Flight) current.getData();
            }
            current = current.next;
        } while (current != first);
        return null;
    }
	
	public boolean addFlight(int flightID, String destination, String status, Queue regularQueue, Queue VIPQueue, Stack undoStack,
	Stack redoStack, PassengersLinkedList boardedPassengersList, PassengersLinkedList canceledPassengersList) {
		Flight newFlight = new Flight(flightID, destination, status, regularQueue, VIPQueue, undoStack, redoStack, boardedPassengersList, canceledPassengersList);
        if (first == null) {
            addFirst(newFlight);
            return true;
        } else  {
            addLast(newFlight);
            return true;
        }
        	
    }
	
	public Flight getFlightByIndex(int index) {
	    if (index < 0 || index >= count) return null;
        FlightNode curr = first;
        for (int i = 0; i < index; i++) {
            curr = curr.getNext();
        }
        return (Flight) curr.getData();
	}
	
	public boolean deleteFlight(int FlightID) {
	    Flight m = getFlightByFlightID(FlightID);
	    if (m != null) {  
			remove(m);
	        return true;
	    }
	    return false;
	}
	
	public boolean updateFlight(int FlightID1,int FlightID) {
		Flight m = getFlightByFlightID(FlightID1);
		if (m!=null) {
			m.setFlightID(FlightID);
			return true;
		}
		return false;
		
	}
	 
}
