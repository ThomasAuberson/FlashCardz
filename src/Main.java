import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * FlashCardz
 * 
 * Author: Thomas Auberson
 * Version 0.51
 * 
 * This is the main class that contains most of the program.
 * This class opens the main window, from which card objects can be created and viewed
 * and sets of cards can be created, saved and loaded.
 */
public class Main implements ActionListener
{
    private final String version = "0.5";   //The current version number

    private ArrayList<Card> cardSet;        //Complete properly ordered Card Set
    private ArrayList<String> cardSets;     //A list of currently saved card sets arranged from oldes to most recent
    private JFrame frame;                   //The main window frame
    private JLabel display;                 //The main display where the introduction title and the flash cards are displayed
    private JMenu filterMenu;
    private JButton button1,button3,button4;                //This button is stored as a field because it changes throughout execution of program
    private int num;                        //Currently selected card number
    private ArrayList<Card> currentCardSet; //Currently navigated card set inc. filters and randomizing
    private String cardSetName;
    private boolean answer;                 //Whether or not display is set to display the answer to the flash card or not
    private boolean onTop = false;          //Whether window is set to always on top
    private boolean saved = true;           //Has current card set been saved since last changes made?
    private boolean askAnswers = false;
    private boolean frontPage = true;
    private String currentCategory = "";    //Most recently chosen category. Will default when you create new cards
    private HashMap<String,Boolean> categories = new HashMap<String,Boolean>();
    private HashSet<String> selectCategories = new HashSet<String>();

    //CONSTRUCTOR
    public Main(){
        loadReferences();
        if(!cardSets.isEmpty()){
            loadCardSet(cardSets.get(0));
        }
        else cardSet = new ArrayList<Card>();
        openWindow();
    }

    public static void main(String [] args){
        new Main();
    }

    public void openWindow(){
        frame = new JFrame("FlashCardz");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);   //Closing the main window kills program
        Container pane = frame.getContentPane();      //Layout pane        
        pane.setLayout(new BorderLayout()); 
        pane.setBackground(new Color(184,208,222));     //Sets backgtound of the pane to be a pale blue colour

        //BOTTOM PANEL
        JPanel bottom = new JPanel(new GridLayout(1,4));
        bottom.add(button1 = createButton("Previous Card","<html>Previous<br>Card</html>",KeyEvent.VK_LEFT));
        bottom.add(createButton("Add Card","<html>Add<br>Card</html>",KeyEvent.VK_UP));
        bottom.add(button3 = createButton("First Card","<html>First<br>Card</html>",KeyEvent.VK_DOWN));
        bottom.add(button4 = createButton("Skip Card","<html>Skip<br>Card</html>",KeyEvent.VK_RIGHT));
        pane.add(bottom,BorderLayout.PAGE_END);
        button1.setEnabled(false);
        button4.setEnabled(false);

        //CENTER PANEL
        display = new JLabel("FLASHCARDZ",JLabel.CENTER);
        display.setFont(new Font("Aharoni", Font.BOLD, 42)); //Title is set to 42 font       
        display.setForeground(new Color(0,39,79));  //Sets text in display to be a dark navy blue colour
        pane.add(display,BorderLayout.CENTER);

        //MISCELLANEOUS TASKS
        frame.setJMenuBar(createMenuBar());
        frame.setSize(360,240);         //Frame resolution and visibility are set
        frame.setAlwaysOnTop(onTop);
        frame.setResizable(false);
        frame.setVisible(true);

        //updateCategories();

        //WINDOW LISTENER
        frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    if(saved){
                        System.exit(0);
                    }
                    else{
                        Object options[] = {"Yes", "No","Cancel"};
                        int n = JOptionPane.showOptionDialog(frame,"You have not saved your current card set. Save now?","",JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE,null,options,options[0]);
                        if(n==1) System.exit(0);
                        else if(n==0){
                            String x = (String)JOptionPane.showInputDialog(frame,"Choose a name for this card set:","Save Card Set",JOptionPane.PLAIN_MESSAGE,null,null,cardSetName);
                            if(x==null) return;
                            saveCardSet(x);
                            System.exit(0);
                        }
                        else return;
                    }
                }
            });
    }

    public void refreshDisplay(){
        //display = new JLabel("",JLabel.CENTER);
        if(!frontPage){       
            display.setFont(new Font("Aharoni", Font.BOLD, 16));  //Regular text is set to 16 font      
            display.setForeground(new Color(0,39,79));      //Sets text in display to be a dark navy blue colour
            String q = currentCardSet.get(num).getQ();
            String a = currentCardSet.get(num).getA();
            if(askAnswers){
                q = currentCardSet.get(num).getA();
                a = currentCardSet.get(num).getQ();
            }
            if(answer){     //If display is set to answer mode the question and answer of the card will be displayed and the centre button will be set to next card
                button3.setText("<html>Next<br>Card</html>");
                button3.setActionCommand("%!Next Card");
                display.setText("<html>Q) "+q+"<br><br>A) "+a+"</html> ");
            }
            else{       //If display is not set to answer mode only question of card will be displayed and centre button will be set to answer which dislayes the answer
                display.setText("<html>Q) "+q+"</html> ");
                button3.setText("Answer");
                button3.setActionCommand("%!Answer");
            }
        }
        updateCategories();
    }

    public JButton createButton(String x, String h,int k){      //Creates a button for the bottom panel of the display
        JButton b = new JButton(h);
        b.setActionCommand("%!"+x);
        b.addActionListener(this);
        b.setMnemonic(k);
        return b;
    }

    public JMenuBar createMenuBar(){
        //Menubar Stuff
        JMenuBar menuBar;               //The Menu Bar
        JMenu menu;                     //The Menus
        menuBar = new JMenuBar();

        //MENU: MENU
        menu = new JMenu("Menu");
        menu.setMnemonic(KeyEvent.VK_M);
        menu.getAccessibleContext().setAccessibleDescription("Open,Load or Save Flash Card Sets");
        menuBar.add(menu);

        createMenuItem(menu,"New Card Set","Create a New Flash Card Set",KeyEvent.VK_N,2,true);
        createMenuItem(menu,"Open Card Set","Open a Saved Flash Card Set",KeyEvent.VK_O,2,true);
        createMenuItem(menu,"Save Card Set","Save Current Flash Card Set",KeyEvent.VK_S,2,true);
        menu.add(new JSeparator());
        createMenuItem(menu,"Modify Card","Modify current card",KeyEvent.VK_M,0,true);
        createMenuItem(menu,"Delete Card","Delete current card",KeyEvent.VK_D,0,true);
        createMenuItem(menu,"Card Count","Number of cards in current set",KeyEvent.VK_C,0,true);
        menu.add(new JSeparator());
        createMenuItem(menu,"Always on Top","Set FlashCardz to show up on top of other windows",KeyEvent.VK_S,1,onTop);
        menu.add(new JSeparator());
        createMenuItem(menu,"About","About FlashCardz",KeyEvent.VK_A,0,true);

        //MENU: FILTER
        filterMenu = new JMenu("Filter");
        filterMenu.setMnemonic(KeyEvent.VK_F);
        filterMenu.getAccessibleContext().setAccessibleDescription("Filter which categories of cards are shown and shuffle the set of cards");
        menuBar.add(filterMenu);
        createMenuItem(filterMenu,"Flip Questions/Answers","Switches the list of questions with the answers and vice versa",KeyEvent.VK_F,0,true);
        filterMenu.add(new JSeparator());
        createMenuItem(filterMenu,"Shuffle","Shuffle card set",KeyEvent.VK_S,0,true);
        createMenuItem(filterMenu,"Unshuffle","Restore card set to its original order",KeyEvent.VK_U,0,true);
        createMenuItem(filterMenu,"Order Alphabetically","Order cards in alphabetical order",KeyEvent.VK_A,0,true);
        filterMenu.add(new JSeparator());
        createMenuItem(filterMenu,"Filter All","Unselect all filters",KeyEvent.VK_F,0,true);
        createMenuItem(filterMenu,"Unfilter All","Select all filters",KeyEvent.VK_N,0,true);

        filterMenu.add(new JSeparator());
        updateCategories();

        return(menuBar);
    }

    public void createMenuItem(JMenu menu, String x,String d,int m, int z,boolean t){
        JMenuItem item;
        if (z==1 || z==3){ 
            if(x.equals("")) item = new JCheckBoxMenuItem("(NONE)");
            else item = new JCheckBoxMenuItem(x);
            item.setSelected(t);
        }
        else {
            item = new JMenuItem(x,m);            
        }
        if(z==2) item.setAccelerator(KeyStroke.getKeyStroke(m, ActionEvent.CTRL_MASK));
        item.getAccessibleContext().setAccessibleDescription(d);
        if (z==3){
            item.setActionCommand(x);
        }
        else{
            item.setActionCommand("%!"+x);
        }
        item.addActionListener(this);
        menu.add(item);
    }

    public void loadCardSet(String x){
        try{            
            cardSet = new ArrayList<Card>();
            Scanner scan = new Scanner(new File("Card Sets/"+x+".txt"));
            int n = 0;
            while(scan.hasNext()){
                String t = scan.nextLine();
                String q = scan.nextLine();
                String a = scan.nextLine();
                cardSet.add(new Card(q,a,n,t));  
                n++;
            }
            scan.close();
            cardSetName = x;            
            currentCardSet = new ArrayList<Card>(cardSet);
        }
        catch (IOException e) {
            JOptionPane.showMessageDialog(frame,"Failed to load Card Set","ERROR",JOptionPane.WARNING_MESSAGE);
        }
    }

    public void updateCategories(){
        try{
            //System.out.println("Execute: updateCategories");
            HashMap<String,Boolean> oldMap = categories;
            categories = new HashMap<String,Boolean>();
            for(int i = 0; i<cardSet.size(); i++){
                //if((new Scanner(cardSet.get(i).getT())).hasNext()){
                if(oldMap.keySet().contains(cardSet.get(i).getT())){
                    categories.put(cardSet.get(i).getT(),oldMap.get(cardSet.get(i).getT()));
                }
                else{
                    categories.put(cardSet.get(i).getT(),(Boolean)true);
                }
                //}
            }
            filterMenu.removeAll();
            createMenuItem(filterMenu,"Flip Questions/Answers","Switches the list of questions with the answers and vice versa",KeyEvent.VK_F,0,true);
            filterMenu.add(new JSeparator());
            createMenuItem(filterMenu,"Shuffle","Shuffle card set",KeyEvent.VK_S,0,true);
            createMenuItem(filterMenu,"Unshuffle","Restore card set to its original order",KeyEvent.VK_U,0,true);
            createMenuItem(filterMenu,"Order Alphabetically","Order cards in alphabetical order",KeyEvent.VK_A,0,true);
            filterMenu.add(new JSeparator());
            createMenuItem(filterMenu,"Filter All","Unselect all filters",KeyEvent.VK_F,0,true);
            createMenuItem(filterMenu,"Unfilter All","Select all filters",KeyEvent.VK_N,0,true);
            filterMenu.add(new JSeparator());
            selectCategories = new HashSet<String>();
            for(String s: categories.keySet()){
                createMenuItem(filterMenu,s,s,KeyEvent.VK_S,3,categories.get(s));
                //System.out.println(categories.get(s));
                if(categories.get(s)) selectCategories.add(s);
            }
        }
        catch (NullPointerException e){
            JOptionPane.showMessageDialog(frame,"Internal Error - Filter Menu","ERROR",JOptionPane.ERROR_MESSAGE);
        }
    }

    public void filter(){
        currentCardSet = new ArrayList<Card>(cardSet);
        //System.out.println(currentCardSet.size());
        for(int i = 0; i<currentCardSet.size();i++){
            if(!(selectCategories.contains(currentCardSet.get(i).getT()))){
                //System.out.println("Filter Out: "+currentCardSet.get(i).getQ());
                //System.out.println("Type: "+currentCardSet.get(i).getT());
                currentCardSet.remove(i);
                i--;
            }
            else{
                //System.out.println("NOT Filter Out: "+currentCardSet.get(i).getQ());
                //System.out.println("NOT Type: "+currentCardSet.get(i).getT());
            }
        }
        updateCategories();
        frontPage = true;
        display.setFont(new Font("Aharoni", Font.BOLD, 42));
        display.setText("FLASHCARDZ");
        button3.setText("<html>First<br>Card</html>");
        button3.setActionCommand("%!First Card");
        button1.setEnabled(false);
        button4.setEnabled(false);
    }

    public int currentSetSize(){
        return currentCardSet.size();
    }

    public void unfilterAll(){
        selectCategories = new HashSet<String>();
        for(String s :categories.keySet()){            
            categories.put(s,true);
            selectCategories.add(s);
        }
        updateCategories();
    }

    public void saveCardSet(String x){
        try{
            PrintStream prin = new PrintStream(new File("Card Sets/"+x+".txt"));
            for(int n = 0; n < cardSet.size(); n++){
                prin.println(cardSet.get(n).getT());
                prin.println(cardSet.get(n).getQ());
                prin.println(cardSet.get(n).getA());
            }
            if(cardSets.contains(x)) cardSets.remove(x);
            cardSets.add(0,x);
            prin = new PrintStream(new File("Refs/Set Lists.txt"));
            for(int n = 0; n < cardSets.size(); n++){
                prin.println(cardSets.get(n));
            }
            prin.close();
            saved = true;
            JOptionPane.showMessageDialog(frame,"Flash Card set "+x+" saved successfully","",JOptionPane.PLAIN_MESSAGE);
        }
        catch (IOException e){
            JOptionPane.showMessageDialog(frame,"Saving Failed!","ERROR",JOptionPane.WARNING_MESSAGE);
        }
    }

    public void loadReferences(){
        try{
            cardSets = new ArrayList<String>();
            Scanner scan = new Scanner(new File("Refs/Set Lists.txt"));
            while(scan.hasNext()){
                cardSets.add(scan.nextLine());
            }
            scan = new Scanner(new File("Refs/Settings.txt"));
            if(scan.nextLine().equals("true")) onTop = true;
            scan.close();
        }
        catch (IOException e) {
            JOptionPane.showMessageDialog(frame,"Failed to load References","ERROR",JOptionPane.ERROR_MESSAGE);
        }
    }

    public void actionPerformed(ActionEvent e) { 
        //System.out.println("Button: "+e.getActionCommand());
        if(e.getActionCommand().equals("%!New Card Set")){
            String x =(String)JOptionPane.showInputDialog(frame,"Type a name for this new card set","New Card Set",JOptionPane.PLAIN_MESSAGE);
            if(x==null) return;
            else  cardSetName = x;
            cardSet = new ArrayList<Card>();
            currentCardSet = new ArrayList<Card>(cardSet);
            frontPage = true;
            display.setFont(new Font("Aharoni", Font.BOLD, 42));
            display.setText("FLASHCARDZ");
            button3.setText("<html>First<br>Card</html>");
            button3.setActionCommand("%!First Card");
            button1.setEnabled(false);
            button4.setEnabled(false);
            saved = false;
            //             updateCategories();
        }
        else if(e.getActionCommand().equals("%!Open Card Set")){
            Object options[] = new Object[cardSets.size()];
            for(int i = 0; i<cardSets.size(); i++){
                options[i] = cardSets.get(i);
            }
            String x = (String)JOptionPane.showInputDialog(frame,"Select a set to open:","Open Card Set",JOptionPane.PLAIN_MESSAGE,null,options,options[0]);
            if(x==null) return;
            loadCardSet(x);
            updateCategories();
            frontPage = true;
            display.setFont(new Font("Aharoni", Font.BOLD, 42));
            display.setText("FLASHCARDZ");
            button3.setText("<html>First<br>Card</html>");
            button3.setActionCommand("%!First Card");
            button1.setEnabled(false);
            button4.setEnabled(false);
            //             updateCategories();
        }
        else if(e.getActionCommand().equals("%!Save Card Set")){
            String x = (String)JOptionPane.showInputDialog(frame,"Choose a name for this card set:","Save Card Set",JOptionPane.PLAIN_MESSAGE,null,null,cardSetName);
            if(x==null) return;
            saveCardSet(x);
            //             updateCategories();
        }
        else if(e.getActionCommand().equals("%!Add Card")){            
            String q = (String)JOptionPane.showInputDialog(frame,"Type in a question for this new card:","Add Card",JOptionPane.PLAIN_MESSAGE);
            if(q==null) return;
            String a = (String)JOptionPane.showInputDialog(frame,"Type in an answer for this question:","Add Card",JOptionPane.PLAIN_MESSAGE);
            if(a==null) return;
            String t = (String)JOptionPane.showInputDialog(frame,"Type in a category for this new card:","Add Card",JOptionPane.PLAIN_MESSAGE,null,null,currentCategory);
            if(t==null) return;
            currentCategory = t;
            int n = cardSet.size();
            cardSet.add(new Card(q,a,n,t)); 
            updateCategories();
            if(selectCategories.contains(t)){
                currentCardSet.add(new Card(q,a,n,t));
            }
            saved = false;
            //             updateCategories();
        }
        else if(e.getActionCommand().equals("%!Next Card")||e.getActionCommand().equals("%!Skip Card")){
            num++;
            if(num==currentCardSet.size()) num = 0;
            answer = false;
            refreshDisplay();
        }
        else if(e.getActionCommand().equals("%!Previous Card")){
            num--;
            if(num==-1) num = (currentCardSet.size()-1);
            answer = false;
            refreshDisplay();
        }
        else if(e.getActionCommand().equals("%!First Card")){
            if(currentCardSet == null || currentCardSet.isEmpty()) return;
            num = 0;
            answer = false;
            frontPage = false;
            button1.setEnabled(true);
            button4.setEnabled(true);
            refreshDisplay();
        }
        else if(e.getActionCommand().equals("%!Answer")){
            answer = true;
            refreshDisplay();
        }
        else if(e.getActionCommand().equals("%!Shuffle")){
            Collections.shuffle(currentCardSet);
            refreshDisplay();
        }
        else if(e.getActionCommand().equals("%!Unshuffle")){            
            unfilterAll();
            currentCardSet = new ArrayList<Card>(cardSet);
            frontPage = true;
            display.setFont(new Font("Aharoni", Font.BOLD, 42));
            display.setText("FLASHCARDZ");
            button3.setText("<html>First<br>Card</html>");
            button3.setActionCommand("%!First Card");
            button1.setEnabled(false);
            button4.setEnabled(false);
        }
        else if(e.getActionCommand().equals("%!Order Alphabetically")){
            //Bubble Sort cards alphabetically (lexicographically)
            for(int k = 0; k<currentCardSet.size(); k++){
                for(int i = 0; i<(currentCardSet.size()-1); i++){
                    if(currentCardSet.get(i).compareCards(askAnswers,currentCardSet.get(i+1))){
                        Card c = currentCardSet.get(i);
                        currentCardSet.set(i,currentCardSet.get(i+1));
                        currentCardSet.set((i+1),c);
                    }
                }
            }
            refreshDisplay();
        }
        else if(e.getActionCommand().equals("%!Filter All")){
            for(String s : categories.keySet()){
                categories.put(s,false);
            }            
            updateCategories();
            filter();
        }
        else if(e.getActionCommand().equals("%!Unfilter All")){
            for(String s : categories.keySet()){
                categories.put(s,true);
            }            
            updateCategories();
            filter();
        }
        else if(e.getActionCommand().equals("%!Card Count")){
            String s = "<html>Current Number of cards:";
            s = s+"<br>Current Set: "+currentCardSet.size();
            s = s+"<br>Total: "+cardSet.size()+"</html>";
            JOptionPane.showMessageDialog(frame,s,"Card Count",JOptionPane.PLAIN_MESSAGE);
        }
        else if(e.getActionCommand().equals("%!Flip Questions/Answers")){ 
            askAnswers = !askAnswers;
            refreshDisplay();
        }
        else if(e.getActionCommand().equals("%!About")){
            JOptionPane.showMessageDialog(frame,"<html>Version: "+version+"<br>Author: Thomas Auberson<br><br>FlashCardz allows you to create sets of virtual<br>flash cards with a question,answer and category,<br> and cycle through them.</html>","About FlashCardz",JOptionPane.PLAIN_MESSAGE);
        }
        else if(e.getActionCommand().equals("%!Delete Card")){
            Object options[] = {"Yes","No"};
            int n = JOptionPane.showOptionDialog(frame,"Are you sure you want to delete the current card?","",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE,null,options,options[1]);
            if(n==1) return;
            else if(n==0){
                cardSet.remove(currentCardSet.get(num)); 
                currentCardSet.remove(currentCardSet.get(num));
                num--;
                if(num<0) num=0;
                refreshDisplay();
                saved = false;
                //                 updateCategories();
            }
        }
        else if(e.getActionCommand().equals("%!Modify Card")){
            String q = (String)JOptionPane.showInputDialog(frame,"Type in a question for this new card:","Modify Card",JOptionPane.PLAIN_MESSAGE,null,null,currentCardSet.get(num).getQ());
            if(q==null) return;
            currentCardSet.get(num).setQ(q);
            String a = (String)JOptionPane.showInputDialog(frame,"Type in an answer for this question:","Modify Card",JOptionPane.PLAIN_MESSAGE,null,null,currentCardSet.get(num).getA());
            if(a==null) return;
            currentCardSet.get(num).setA(a);
            String t = (String)JOptionPane.showInputDialog(frame,"Type in a category for this new card:","Modify Card",JOptionPane.PLAIN_MESSAGE,null,null,currentCardSet.get(num).getT());
            if (t==null) return;
            currentCardSet.get(num).setT(t);
            saved = false;
            refreshDisplay();
            //             updateCategories();
        }
        else if(e.getActionCommand().equals("%!Always on Top")){
            onTop = !onTop;
            frame.setAlwaysOnTop(onTop);            
            try{
            	PrintStream prin = new PrintStream(new File("Refs/Settings.txt"));
                if(onTop) prin.println("true");
                else prin.println("false");
                prin.close();
            }
            catch(IOException ex){
            }
        }
        else{
            String type = (String)(e.getActionCommand());
            //System.out.println("Type Selected - T/F: "+categories.get(type));
            Boolean b = (Boolean)(!categories.get(type));
            categories.put(type,b);
            updateCategories();
            filter();
        }
    }
}
