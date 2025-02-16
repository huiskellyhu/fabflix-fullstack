package ParsingFiles.src.main.java;

public class Stars {
    private String id;

    private String name;

    private int dob;

    public Stars(){

    }

    public Stars(String id, String name, int dob) {
        this.id = id;
        this.name = name;
        this.dob  = dob;

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public Integer getDOB() {
        return dob;
    }

    public void setDOB(Integer dob) {
        this.dob = dob;
    }


    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Star Details - ");
        sb.append("Id:" + getId());
        sb.append(", ");
        sb.append("Name:" + getName());
        sb.append(", ");
        sb.append("DOB:" + getDOB());

        return sb.toString();
    }
}
