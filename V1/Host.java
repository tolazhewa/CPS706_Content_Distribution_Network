/**
 * Created by tolaz on 2016-11-08.
 */
public abstract class Host {

    public String name;
    public String IP;

    public Host(String name, String IP){
        this.name = name;
        this.IP = IP;
    }

    public String getName() {
        return name;
    }

    public String getIP() {
        return IP;
    }
}
