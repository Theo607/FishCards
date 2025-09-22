package backend;

public class Card {
    public int id;
    public String pol;
    public String esp;
    public String imgp;

    public Card() {
        id = -1;
        pol = "";
        esp = "";
        imgp = "";
    }

    public Card(int i, String s1, String s2, String s3) {
        id = i;
        pol = s1;
        esp = s2;
        imgp = s3;
    }

    @Override
    public String toString() {
        return "Card{id=" + id + ", pol='" + pol + "', esp='" + esp + "', imgp='" + imgp + "'}";
    }

}
