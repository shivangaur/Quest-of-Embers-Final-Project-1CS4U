import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.security.Key;
import java.util.ArrayList;
import java.util.Scanner;

class GamePanel extends JPanel implements KeyListener {
    // Window related Objects
    public boolean paused = false;
    private boolean[] keysPressed; // Array that keeps track of keys that are pressed down
    private MainGame gameFrame;

    // Game related Objects
    private Player player = new Player(this);
    private Image enemyHealthBar;
    private Image staminaBar;
    private Image healthBar;

    private Image[] backgroundLayers = new Image[3];
    private ArrayList<LevelProp> platforms = new ArrayList<LevelProp>();
    private ArrayList<LevelProp> noCollideProps = new ArrayList<LevelProp>();
    private ArrayList<Enemy> enemies = new ArrayList<Enemy>();
    private Sound test = new Sound("Assets/Sounds/Music/level1.wav");
    private Sound testEffect = new Sound("Assets/Sounds/Effects/coin5.wav");
    // Game fields
    private int time;
    private int levelOffset = 0;
    // Fonts
    Font gameFont;
    Font gameFontBig;
    // Constructor for GamePanel
    public GamePanel(MainGame game){
        // Setting up the GamePanel
        gameFrame = game;
        setSize(960,590);
        keysPressed = new boolean[KeyEvent.KEY_LAST+1];
        addKeyListener(this);
        try{
            // Loading Images
            enemyHealthBar = ImageIO.read(new File("Assets/Images/Enemies/healthBar.png"));
            staminaBar = ImageIO.read(new File("Assets/Images/Player/staminaBar.png"));
            healthBar = ImageIO.read(new File("Assets/Images/Player/healthBar.png"));
            for(int i = 0; i < 3; i++){
                backgroundLayers[i] = ImageIO.read(new File("Assets/Images/Background/BG" + (i+1) + ".png"));
            }
            // Loading fonts
            gameFont = Font.createFont(Font.TRUETYPE_FONT, new File("Assets/Fonts/8BitFont.ttf"));
            gameFont = gameFont.deriveFont(30f);
            gameFontBig = gameFont.deriveFont(50f);
        }
        catch (IOException | FontFormatException e) {
            e.printStackTrace();
        }
        // Initalizing the enemy Classes
        Slime.init();
        loadLevel(1);
    }

    // Method to load up all level Objects from the corresponding text files
    public void loadLevel(int levelNum){
        try{
            for(String data: loadFile("Platforms.txt", levelNum)){
                platforms.add(new LevelProp(data));
            }
            for(String data: loadFile("NoCollideProps.txt", levelNum)){
                noCollideProps.add(new LevelProp(data));
            }
            for(String data: loadFile("Slimes.txt", levelNum)){
                enemies.add(new Slime(data));
            }
        }
        catch (IOException e) {
            System.out.println("Level " + levelNum + " data incomplete!");
            e.printStackTrace();
        }
    }
    // Helper method to load up individual files into ArrayLists with their lines as Strings
    public ArrayList<String> loadFile(String fileName, int levelNum) throws IOException{
        Scanner inFile = new Scanner(new BufferedReader(new FileReader("Data/Level " + levelNum + "/" + fileName)));
        ArrayList<String> fileContents = new ArrayList<String>();
        while(inFile.hasNextLine()){
            String line = inFile.nextLine();
            if(!line.startsWith("//")){ // Making sure that the line is not a comment
                fileContents.add(line);
            }
        }
        inFile.close();
        return fileContents;
    }
    // All window related methods
    public void addNotify() {
        super.addNotify();
        requestFocus();
        System.out.println("add notfiy");
    }
    public void removeNotify(){
        super.removeNotify();
        paused = true;
        System.out.println("remove notify");
    }
    public void paintComponent(Graphics g){
        Graphics2D g2d = (Graphics2D) g;
        // Drawing the background
        g.setColor(new Color(0,0,0));
        g.fillRect(0, 0, 960, 590);
        for(int i = 0; i < 3; i ++){
            g.drawImage(backgroundLayers[i], 0, 0, this);
        }
        // Drawing the level
        for(LevelProp platform: platforms){
            Rectangle platformRect = platform.getRect();
            g.drawImage(platform.getPlatformImage(), platformRect.x - levelOffset, platformRect.y, this);
        }
        for(LevelProp platform: noCollideProps){
            Rectangle platformRect = platform.getRect();
            g.drawImage(platform.getPlatformImage(), platformRect.x - levelOffset, platformRect.y, this);
        }
        // Drawing enemies
        for(Enemy enemy: enemies){
            g.drawImage(enemy.getSprite(), (int)enemy.getX() - levelOffset, (int)enemy.getY(), this);
            drawHealth(g, enemy);
            g.drawRect(enemy.getHitbox().x - levelOffset, enemy.getHitbox().y, enemy.getHitbox().width, enemy.getHitbox().height);
        }
        // Drawing the Player
        g.drawImage(player.getSprite(), (int)player.getX() - levelOffset, (int)player.getY(), this);
        g.drawRect(player.getHitbox().x - levelOffset, player.getHitbox().y, player.getHitbox().width, player.getHitbox().height);
        g.drawRect(player.getAttackBox().x - levelOffset, player.getAttackBox().y, player.getAttackBox().width, player.getAttackBox().height);
        // Drawing game stats
        g.setColor(new Color(255,255,255));
        g.setFont(gameFont);

        for(int i=0;i<100;i++) {
            //Health
            g.setColor(new Color(155+i,0,0));
            g.fillRect(59+i, 30, (int) (((double) player.getStamina() / player.getMaxStamina()) * 198)-i, 14);
            //Stamina
            g.setColor(new Color(0,155+i,0));
            g.fillRect(59+i, 83, (int) (((double) player.getStamina() / player.getMaxStamina()) * 198)-i, 14);
        }


        /*
        g.drawString("Stamina:" + player.getStamina(),10,20);
        g.setColor(new Color(247,255,10));
        g.fillRect(25,33,(int)(((double)player.getStamina()/player.getMaxStamina())*120),22);
        g.drawImage(staminaBar,8,30,this);

         */

        g.drawImage(healthBar, 10,10,this);
        g.drawImage(staminaBar, 10,65,this);
        g.drawString("Health:", 320, 43);
        g.drawString("Stamina:", 320, 80);

        // Drawing pause screen
        if(paused){
            g.setColor(new Color(0,0,0, 100));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(new Color(255,255,255));
            g.drawString("Press ESC to unpause", 335, 330);
            g.setFont(gameFontBig);
            g.drawString("Paused", 400, 300);
        }
    }
    public void drawHealth(Graphics g, Enemy enemy){
        double health = enemy.getHealth();
        double maxHealth = enemy.getMaxHealth();
        Rectangle hitBox = enemy.getHitbox();
        int healthBarOffset = (hitBox.width/2)-(int)((health/maxHealth)*44);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(new Color(255,0,0));

        g2d.fillRect(hitBox.x-levelOffset+healthBarOffset,hitBox.y-10,(int)((health/maxHealth)*88),13);

        g2d.drawImage(enemyHealthBar,hitBox.x-levelOffset-10+healthBarOffset,hitBox.y-15,this);

    }
    // Keyboard related methods
    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        // Running code for initially clicked keys
        if(!keysPressed[keyCode]){
            if(keyCode == KeyEvent.VK_SPACE && !paused){
                player.jump(Player.INITIAL);
            }
            else if(keyCode == KeyEvent.VK_P && !paused){
                player.attack();
            }
            else if(keyCode == KeyEvent.VK_O && !paused){
                player.castMagic();
            }
            else if(keyCode == KeyEvent.VK_ESCAPE){
                paused = !paused;
                repaint();
            }
        }
        // SOUND TEST
        if(keyCode == KeyEvent.VK_0){
            if(!test.hasStarted()){
                test.play();
            }
            else if(test.isPlaying()) {
                test.pause();
            }
            else{
                test.resume();
            }
        }
        if(keyCode == KeyEvent.VK_8 && !keysPressed[KeyEvent.VK_8]){
            testEffect.play();
        }
        // Keeping track of whether or not the key is pressed down
        keysPressed[keyCode] = true;
        // DEBUG KEYS (REMOVE THESE AFTER)
        if(keyCode == KeyEvent.VK_BACK_SLASH){
            if(getMousePosition() != null){
                System.out.println(getMousePosition() + " True x = " + (getMousePosition().x + levelOffset));
            }
        }
        else if(keyCode == KeyEvent.VK_CLOSE_BRACKET){
            player.resetPos();
        }
    }
    @Override
    public void keyReleased(KeyEvent e) {
        keysPressed[e.getKeyCode()] = false;
    }
    @Override
    public void keyTyped(KeyEvent e) {}

    // Game related methods
    public void update(){
        player.update();
        for(Enemy enemy: enemies){
            enemy.update(player);
        }
        calculateOffset();
    }
    public void calculateOffset(){
        Rectangle hitbox = player.getHitbox();
        if(hitbox.x + hitbox.width > 480){
            levelOffset = (hitbox.x + hitbox.width) - 480;
        }
        else{
            levelOffset = 0;
        }
    }
    public void checkCollision(){
        for(LevelProp platform: platforms){
            player.checkCollision(platform.getRect());
            for(Enemy enemy: enemies){
                enemy.checkCollision(platform.getRect());
            }
        }
    }

    public void checkInputs(){
        // Side-to-side movement inputs
        if(keysPressed[KeyEvent.VK_D] && keysPressed[KeyEvent.VK_A]){
            // Stop movement
        }
        else if(keysPressed[KeyEvent.VK_D]){
            player.move(Player.RIGHT);
        }
        else if(keysPressed[KeyEvent.VK_A]){
            player.move(Player.LEFT);
        }
        // Jumping input
        if(keysPressed[KeyEvent.VK_SPACE]){
            player.jump(Player.NORMAL);
        }
    }

    // Getter methods
    public double getLevelOffset(){
        return levelOffset;
    }
}