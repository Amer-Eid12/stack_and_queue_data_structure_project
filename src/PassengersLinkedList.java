public class PassengersLinkedList {
	
	private PassengerNode first, last;	

	public PassengerNode getFirst() {
		if(first==null)
			return null;
		
		return first;
	}

	public PassengerNode getLast() {
		if(last==null)
			return null;
		
		return last;
	}
	
	public void addFirst(Passenger data) {
		PassengerNode newPassengerNode= new PassengerNode(data);
		 
		if(first==null){
			first = last = newPassengerNode;
            newPassengerNode.next = newPassengerNode;
            newPassengerNode.prev = newPassengerNode;
		
		}else {
			newPassengerNode.next = first;
            newPassengerNode.prev = last;
            first.prev = newPassengerNode;
            last.next = newPassengerNode;
            first = newPassengerNode;
		}
	}

	public void addLast(Passenger data) {
		PassengerNode newPassengerNode= new PassengerNode(data);
		
		if(last==null){
			first = last = newPassengerNode;
            newPassengerNode.next = newPassengerNode;
            newPassengerNode.prev = newPassengerNode;
		}
		
		else {
			newPassengerNode.prev = last;
            newPassengerNode.next = first;
            last.next = newPassengerNode;
            first.prev = newPassengerNode;
            last = newPassengerNode;
		}
	}

	public void add(Passenger data, int index) {
		if (index <= 0) {
            addFirst(data);
        } else if (index >= getSize()) {
            addLast(data);
        } else {
            PassengerNode newPassengerNode = new PassengerNode(data);
            PassengerNode temp = first;
            for (int i = 0; i < index - 1; i++) {
                temp = temp.next;
            }
            newPassengerNode.next = temp.next;
            newPassengerNode.prev = temp;
            temp.next.prev = newPassengerNode;
            temp.next = newPassengerNode;
        }
	}

	public boolean removeFirst() {
		if (first == null) return false;

        if (getSize() == 1) {
            first = last = null;
        } else {
            first = first.next;
            first.prev = last;
            last.next = first;
        }
        return true;
	}

	public boolean removeLast() {
		if (last == null) return false;

        if (getSize() == 1) {
            first = last = null;
        } else {
            last = last.prev;
            last.next = first;
            first.prev = last;
        }
        return true;
	}

	public boolean remove(int index) {
		if (index < 0 || index >= getSize()) return false;

        if (index == 0) {
            return removeFirst();
        } else if (index == getSize() - 1) {
            return removeLast();
        } else {
            PassengerNode temp = first;
            for (int i = 0; i < index; i++) {
                temp = temp.next;
            }
            temp.prev.next = temp.next;
            temp.next.prev = temp.prev;
            return true;
        }
	}

	public boolean remove(Passenger data) {
		PassengerNode current = first;
        if (first == null) return false;

        for (int i = 0; i < getSize(); i++) {
            if (current.getData().equals(data)) {
                if (i == 0) return removeFirst();
                else if (i == getSize() - 1) return removeLast();
                else {
                    current.prev.next = current.next;
                    current.next.prev = current.prev;
                    return true;
                }
            }
            current = current.next;
        }
        return false;
	}

	public  void printPassengerList() { 
	    if (first == null) return;
        PassengerNode temp = first;
        do {
            System.out.println(((Passenger) temp.getData()).getPassengerID());
            temp = temp.next;
        } while (temp != first);
	}
	
	public int getSize() {
        int size = 0;
        if (first == null) {
            return size;
        }
        PassengerNode current = first;
        do {
            if (current.data != null) { 
                size++;
            }
            current = current.next;
        } while (current != first); 
        return size;
    }
    

	public Passenger getPassengerByName(String name) {
        PassengerNode current = first;
        if (current == null) return null;
        do {
            if (((Passenger) current.getData()).getName().equalsIgnoreCase(name)) {
                return (Passenger) current.getData();
            }
            current = current.next;
        } while (current != first);
        return null;
    }
	
	public boolean addPassenger(int passengerID, String name, int flightID, String status) {
		Passenger newPassenger = new Passenger(passengerID, name, flightID, status);
        if (first == null) {
            addFirst(newPassenger);
            return true;
        } else  {
            addLast(newPassenger);
            return true;
        }
        	
    }
	
	//Bubble sort
	/*public void sortAlphabetically() {
		if (first == null || first.next == first) {
			return;
		}
	
		boolean swapped;
		int size = getSize();
	
		do {
			swapped = false;
			PassengerNode curr = first;
	
			for (int i = 0; i < size - 1; i++) {
				PassengerNode nextPassengerNode = curr.next;
	
				if (((Passenger) curr.getData()).getName().compareToIgnoreCase(((Passenger) nextPassengerNode.getData()).getName()) > 0) {
					Passenger temp = (Passenger) curr.getData();
					curr.setData(nextPassengerNode.getData());
					nextPassengerNode.setData(temp);
					swapped = true;
				}
				curr = nextPassengerNode;
			}
			size--;
		} while (swapped);
	}*/
	
	public Passenger getPassengerByIndex(int index) {
        if (index < 0 || index >= this.getSize()) { 
            return null;
        }
    
        PassengerNode curr = this.first;
        int currentIndex = 0;
    
        while (curr != null && currentIndex < index) {
            curr = curr.getNext();
            currentIndex++;
        }
    
        return (curr != null) ? curr.getData() : null;
    }
    
	public boolean deletePassenger(String name) {
	   
	    Passenger m = getPassengerByName(name);
	    if (m != null) {  
			remove(m);
	        return true;
	    }
	    return false;
	}
	
	public boolean updatePassenger(String name1,String name) {
		Passenger m = getPassengerByName(name1);
		if (m!=null) {
			m.setName(name);
			return true;
		}
		return false;
		
	}
	 
}
