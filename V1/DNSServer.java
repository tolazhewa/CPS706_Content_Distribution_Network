import java.util.ArrayList;

/**
 * Created by tolaz on 2016-11-07.
 */
public class DNSServer extends Server{
    ArrayList<Record> records;

    public DNSServer(String name, String IP){
        super(name,IP);
        records = new ArrayList<>();
    }

    public void addRecord(Record rec){
        this.records.add(rec);
    }

    public void removeRecord(Record rec){
        this.records.remove(rec);
    }

    public void printRecords(){
        System.out.println("\n\nRECORDS:\n");
        for(Record r: records){
            System.out.println(r);
        }
    }

    public String getName(){
        return this.name;
    }

    public Record getRecord(String name){
        for(Record r: records){
            if(r.getName().equals(name))
                return r;
        }
        return null;
    }

    public int getNumOfRecords(){
        return records.size();
    }

    public String toString(){
        return "[Name: " + getName() + ", Number of Records" + getNumOfRecords();
    }
}
