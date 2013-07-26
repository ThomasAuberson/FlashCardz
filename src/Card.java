
/**
 * Author: Thomas Auberson 
 * Version: 0.1
 */
public class Card
{
    private String question;
    private String answer;
    private int number;
    private String type;

    public Card(String q, String a, int n, String t){
        question = q;
        answer = a;
        number = n;
        type = t;
    }

    public String getA(){
        return answer;
    }

    public String getQ(){
        return question;
    }

    public int getNum(){
        return number;
    }

    public String getT(){
        return type;
    }

    public void setA(String a){
        answer = a;
    }

    public void setQ(String q){
        question = q;
    }

    public void setT(String t){
        type = t;
    }

    public void setNum(int n){
        number = n;
    }

    public boolean compareCards(boolean b, Card c){ //Lexicographically compare this card's question with that of another
        String card2 = c.getQ().toLowerCase();
        String card1 = question.toLowerCase();        
        if(b){      //b = askAnswers. If askAnswers is true compare cards by answer string rather than question string
            card2 = c.getA().toLowerCase();
            card1 = answer.toLowerCase();
        }
        if (card1.compareTo(card2)>0) return true; //Return true if this Card is greater than parameter card
        return false;
    }
}
