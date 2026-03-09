package gdx.liftoff;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

public class Main extends ApplicationAdapter {
    ShapeRenderer shape;
    SpriteBatch batch;
    BitmapFont font, fontBig;

    float W, H;
    int screen = 0;

    // Settings
    float volume = 0.7f;
    int difficulty = 1;
    float joystickSize = 80f;
    int weaponType = 0;
    String[] diffNames = {"EASY","NORMAL","HARD"};
    String[] weaponNames = {"PISTOL","SHOTGUN","MACHINE GUN"};
    float[] weaponFireRate = {0.4f, 0.8f, 0.15f};
    int[] weaponBullets = {1, 5, 1};
    float[] weaponBulletSpeed = {400f, 300f, 500f};

    // Player
    float px, py, pAngle = 0;
    float pSpeed = 200f;
    float pSize = 20f;

    // Joystick
    float jBaseX, jBaseY, jKnobX, jKnobY;
    int jPointer = -1;
    boolean jActive = false;

    // Bullets
    Array<Vector2> bullets = new Array<>();
    Array<Vector2> bulletDirs = new Array<>();
    float bulletSpeed = 400f;
    float shootTimer = 0, fireRate = 0.4f;

    // Zombies
    Array<Vector2> zombies = new Array<>();
    Array<Float> zombieAngles = new Array<>();
    float zombieSpeed = 80f;
    float spawnTimer = 0;
    float animTimer = 0;

    int score = 0;
    boolean gameOver = false;
    float touchX, touchY;
    boolean justTouched = false;

    @Override
    public void create() {
        shape = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();
        fontBig = new BitmapFont();
        font.getData().setScale(1.5f);
        fontBig.getData().setScale(2.5f);
        W = Gdx.graphics.getWidth();
        H = Gdx.graphics.getHeight();
        resetGame();
    }

    void resetGame() {
        px = W/2; py = H/2; pAngle = 0;
        bullets.clear(); bulletDirs.clear();
        zombies.clear(); zombieAngles.clear();
        score = 0; gameOver = false;
        spawnTimer = 0; shootTimer = 0;
        jBaseX = 150; jBaseY = 150;
        jKnobX = jBaseX; jKnobY = jBaseY;
        jActive = false;
        fireRate = weaponFireRate[weaponType];
        bulletSpeed = weaponBulletSpeed[weaponType];
        float[] speeds = {70f, 90f, 120f};
        zombieSpeed = speeds[difficulty];
        spawnZombie();
    }

    void spawnZombie() {
        float x, y;
        int side = MathUtils.random(3);
        if (side == 0) { x = MathUtils.random(W); y = -40; }
        else if (side == 1) { x = MathUtils.random(W); y = H+40; }
        else if (side == 2) { x = -40; y = MathUtils.random(H); }
        else { x = W+40; y = MathUtils.random(H); }
        zombies.add(new Vector2(x, y));
        zombieAngles.add(0f);
    }

    // Vẽ nhân vật người (nhìn từ trên xuống)
    void drawPlayer(float x, float y, float angle) {
        // Thân
        shape.setColor(0.2f, 0.4f, 0.9f, 1);
        shape.rect(x-10, y-12, 20, 22);
        // Đầu
        shape.setColor(0.9f, 0.75f, 0.6f, 1);
        shape.circle(x, y+14, 12);
        // Tay trái
        shape.setColor(0.2f, 0.4f, 0.9f, 1);
        shape.rect(x-18, y-5, 8, 16);
        // Tay phải
        shape.rect(x+10, y-5, 8, 16);
        // Chân trái
        shape.setColor(0.15f, 0.15f, 0.4f, 1);
        shape.rect(x-10, y-24, 8, 14);
        // Chân phải
        shape.rect(x+2, y-24, 8, 14);
        // Súng
        shape.setColor(0.3f, 0.3f, 0.3f, 1);
        shape.rect(x+10, y+2, 18, 6);
    }

    // Vẽ zombie
    void drawZombie(float x, float y, float anim) {
        float wobble = MathUtils.sin(anim * 8) * 2f;
        // Thân
        shape.setColor(0.3f, 0.6f, 0.2f, 1);
        shape.rect(x-10+wobble, y-12, 20, 22);
        // Đầu
        shape.setColor(0.4f, 0.7f, 0.3f, 1);
        shape.circle(x+wobble, y+14, 12);
        // Mắt đỏ
        shape.setColor(0.9f, 0.1f, 0.1f, 1);
        shape.circle(x-4+wobble, y+16, 3);
        shape.circle(x+4+wobble, y+16, 3);
        // Tay zombie (vươn ra)
        shape.setColor(0.3f, 0.6f, 0.2f, 1);
        shape.rect(x-22+wobble, y+2, 12, 8);
        shape.rect(x+10-wobble, y+2, 12, 8);
        // Chân
        shape.setColor(0.2f, 0.4f, 0.15f, 1);
        shape.rect(x-10, y-24, 8, 14);
        shape.rect(x+2, y-24, 8, 14);
    }

    // Vẽ đạn theo loại súng
    void drawBullet(float x, float y) {
        if (weaponType == 0) { // Pistol
            shape.setColor(1f, 0.9f, 0.2f, 1);
            shape.circle(x, y, 5f);
        } else if (weaponType == 1) { // Shotgun
            shape.setColor(1f, 0.6f, 0.1f, 1);
            shape.circle(x, y, 4f);
        } else { // Machine gun
            shape.setColor(0.9f, 0.9f, 1f, 1);
            shape.circle(x, y, 3f);
        }
    }

    boolean touched(float bx, float by, float bw, float bh) {
        return justTouched && touchX > bx && touchX < bx+bw && touchY > by && touchY < by+bh;
    }

    @Override
    public void render() {
        float dt = Math.min(Gdx.graphics.getDeltaTime(), 0.05f);
        animTimer += dt;

        justTouched = Gdx.input.justTouched();
        if (justTouched) {
            touchX = Gdx.input.getX();
            touchY = H - Gdx.input.getY();
        }

        Gdx.gl.glClearColor(0.08f, 0.08f, 0.12f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (screen == 0) renderMenu();
        else if (screen == 1) renderGame(dt);
        else if (screen == 2) renderSettings();
        else if (screen == 3) renderWeaponSelect();
    }

    void renderMenu() {
        // Vẽ nhân vật demo ở menu
        shape.begin(ShapeRenderer.ShapeType.Filled);
        drawPlayer(W*0.3f, H*0.5f, 0);
        drawZombie(W*0.7f, H*0.5f, animTimer);
        shape.end();

        batch.begin();
        fontBig.setColor(Color.CYAN);
        fontBig.draw(batch, "ZOMBIE", W/2-100, H*0.88f);
        fontBig.draw(batch, "SHOOTER", W/2-120, H*0.78f);
        batch.end();

        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0.2f, 0.7f, 0.2f, 1);
        shape.rect(W/2-120, H*0.58f, 240, 65);
        shape.setColor(0.2f, 0.4f, 0.8f, 1);
        shape.rect(W/2-120, H*0.46f, 240, 65);
        shape.setColor(0.8f, 0.5f, 0.1f, 1);
        shape.rect(W/2-120, H*0.34f, 240, 65);
        shape.end();

        batch.begin();
        font.setColor(Color.WHITE);
        font.draw(batch, "PLAY", W/2-30, H*0.58f+42);
        font.draw(batch, "SETTINGS", W/2-60, H*0.46f+42);
        font.draw(batch, "WEAPONS", W/2-60, H*0.34f+42);
        font.draw(batch, "Weapon: "+weaponNames[weaponType], W/2-90, H*0.25f);
        font.draw(batch, "Diff: "+diffNames[difficulty], W/2-60, H*0.18f);
        batch.end();

        if (touched(W/2-120, H*0.58f, 240, 65)) { screen=1; resetGame(); }
        if (touched(W/2-120, H*0.46f, 240, 65)) screen=2;
        if (touched(W/2-120, H*0.34f, 240, 65)) screen=3;
    }

    void renderSettings() {
        batch.begin();
        fontBig.setColor(Color.YELLOW);
        fontBig.draw(batch, "SETTINGS", W/2-130, H*0.9f);
        font.setColor(Color.WHITE);
        font.draw(batch, "VOLUME: "+(int)(volume*100)+"%", W/2-80, H*0.77f);
        font.draw(batch, "DIFFICULTY:", W/2-70, H*0.60f);
        font.draw(batch, "JOYSTICK SIZE: "+(int)joystickSize, W/2-100, H*0.43f);
        batch.end();

        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0.3f,0.3f,0.3f,1);
        shape.rect(W/2-150, H*0.70f, 300, 20);
        shape.setColor(0.2f,0.8f,0.4f,1);
        shape.rect(W/2-150, H*0.70f, 300*volume, 20);

        String[] diffs = {"EASY","NORMAL","HARD"};
        for (int i = 0; i < 3; i++) {
            shape.setColor(difficulty==i ? 0.2f:0.3f, difficulty==i ? 0.7f:0.3f, difficulty==i ? 0.2f:0.3f, 1);
            shape.rect(W/2-150+i*105, H*0.51f, 95, 50);
        }
        shape.setColor(0.3f,0.3f,0.3f,1);
        shape.rect(W/2-150, H*0.35f, 300, 20);
        shape.setColor(0.2f,0.6f,1f,1);
        shape.rect(W/2-150, H*0.35f, 300*((joystickSize-50f)/100f), 20);
        shape.setColor(0.7f,0.2f,0.2f,1);
        shape.rect(W/2-100, H*0.08f, 200, 60);
        shape.end();

        batch.begin();
        font.setColor(Color.WHITE);
        for (int i = 0; i < 3; i++) font.draw(batch, diffs[i], W/2-140+i*105, H*0.51f+35);
        font.draw(batch, "BACK", W/2-35, H*0.08f+40);
        batch.end();

        if (justTouched && touchY>H*0.67f && touchY<H*0.77f && touchX>W/2-150 && touchX<W/2+150)
            volume = MathUtils.clamp((touchX-(W/2-150))/300f, 0, 1);
        for (int i = 0; i < 3; i++) if (touched(W/2-150+i*105, H*0.51f, 95, 50)) difficulty=i;
        if (justTouched && touchY>H*0.32f && touchY<H*0.42f && touchX>W/2-150 && touchX<W/2+150)
            joystickSize = MathUtils.clamp(50f+((touchX-(W/2-150))/300f)*100f, 50, 150);
        if (touched(W/2-100, H*0.08f, 200, 60)) screen=0;
    }

    void renderWeaponSelect() {
        batch.begin();
        fontBig.setColor(Color.ORANGE);
        fontBig.draw(batch, "WEAPONS", W/2-120, H*0.9f);
        batch.end();

        String[] descs = {"Cân bằng - 1 viên/lần", "Bắn rộng - 5 viên/lần", "Bắn nhanh - liên tục"};
        float[] yPos = {0.65f, 0.45f, 0.25f};
        Color[] cols = {Color.GRAY, Color.YELLOW, Color.RED};

        for (int i = 0; i < 3; i++) {
            shape.begin(ShapeRenderer.ShapeType.Filled);
            shape.setColor(weaponType==i ? 0.2f:0.2f, weaponType==i ? 0.6f:0.25f, weaponType==i ? 0.9f:0.3f, 1);
            shape.rect(W/2-180, H*yPos[i]-10, 360, 90);
            shape.end();
            batch.begin();
            font.setColor(cols[i]);
            font.draw(batch, weaponNames[i], W/2-160, H*yPos[i]+70);
            font.setColor(Color.WHITE);
            font.draw(batch, descs[i], W/2-160, H*yPos[i]+40);
            batch.end();
            if (touched(W/2-180, H*yPos[i]-10, 360, 90)) weaponType=i;
        }

        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0.7f,0.2f,0.2f,1);
        shape.rect(W/2-100, H*0.06f, 200, 60);
        shape.end();
        batch.begin();
        font.setColor(Color.WHITE);
        font.draw(batch, "BACK", W/2-35, H*0.06f+40);
        batch.end();
        if (touched(W/2-100, H*0.06f, 200, 60)) screen=0;
    }

    void renderGame(float dt) {
        // Joystick
        for (int i = 0; i < 5; i++) {
            if (Gdx.input.isTouched(i)) {
                float tx = Gdx.input.getX(i), ty = H-Gdx.input.getY(i);
                if (!jActive && tx < W/2) {
                    jActive=true; jPointer=i; jBaseX=tx; jBaseY=ty; jKnobX=tx; jKnobY=ty;
                } else if (jActive && jPointer==i) {
                    float dx=tx-jBaseX, dy=ty-jBaseY;
                    float dist=(float)Math.sqrt(dx*dx+dy*dy);
                    if (dist>joystickSize) { dx=dx/dist*joystickSize; dy=dy/dist*joystickSize; }
                    jKnobX=jBaseX+dx; jKnobY=jBaseY+dy;
                }
            } else if (jActive && jPointer==i) {
                jActive=false; jKnobX=jBaseX; jKnobY=jBaseY;
            }
        }

        if (!gameOver) {
            float mdx=0, mdy=0;
            if (jActive) { mdx=jKnobX-jBaseX; mdy=jKnobY-jBaseY; }
            if (Gdx.input.isKeyPressed(Input.Keys.W)) mdy=joystickSize;
            if (Gdx.input.isKeyPressed(Input.Keys.S)) mdy=-joystickSize;
            if (Gdx.input.isKeyPressed(Input.Keys.A)) mdx=-joystickSize;
            if (Gdx.input.isKeyPressed(Input.Keys.D)) mdx=joystickSize;

            float dist=(float)Math.sqrt(mdx*mdx+mdy*mdy);
            if (dist>5) {
                px+=(mdx/joystickSize)*pSpeed*dt;
                py+=(mdy/joystickSize)*pSpeed*dt;
                pAngle = MathUtils.atan2(mdy, mdx) * MathUtils.radiansToDegrees;
            }
            px=MathUtils.clamp(px,30,W-30); py=MathUtils.clamp(py,30,H-30);

            // Bắn
            shootTimer+=dt;
            if (shootTimer>fireRate && zombies.size>0) {
                shootTimer=0;
                Vector2 nearest=zombies.first();
                float minD=Vector2.dst(px,py,nearest.x,nearest.y);
                for (Vector2 z : zombies) { float d=Vector2.dst(px,py,z.x,z.y); if(d<minD){minD=d;nearest=z;} }
                Vector2 baseDir=new Vector2(nearest.x-px,nearest.y-py).nor();
                int nb=weaponBullets[weaponType];
                float spread=weaponType==1?25f:0f;
                for (int b=0;b<nb;b++) {
                    float angle=spread>0?MathUtils.random(-spread,spread):0;
                    bullets.add(new Vector2(px,py));
                    bulletDirs.add(baseDir.cpy().rotateDeg(angle));
                }
            }

            for (int i=bullets.size-1;i>=0;i--) {
                bullets.get(i).add(bulletDirs.get(i).x*bulletSpeed*dt, bulletDirs.get(i).y*bulletSpeed*dt);
                Vector2 b=bullets.get(i);
                if(b.x<-50||b.x>W+50||b.y<-50||b.y>H+50){bullets.removeIndex(i);bulletDirs.removeIndex(i);}
            }

            spawnTimer+=dt;
            float spawnRate=Math.max(0.3f, 2f-score*0.03f);
            if(spawnTimer>spawnRate){spawnTimer=0;spawnZombie();}

            float zSpeed=zombieSpeed+score*1.5f;
            for (int i=zombies.size-1;i>=0;i--) {
                Vector2 z=zombies.get(i);
                Vector2 dir=new Vector2(px-z.x,py-z.y).nor();
                z.add(dir.x*zSpeed*dt,dir.y*zSpeed*dt);
                zombieAngles.set(i, zombieAngles.get(i)+dt);
                if(Vector2.dst(px,py,z.x,z.y)<pSize+15f) gameOver=true;
                for(int j=bullets.size()-1;j>=0;j--) {
                    if(Vector2.dst(bullets.get(j).x,bullets.get(j).y,z.x,z.y)<22f) {
                        zombies.removeIndex(i);zombieAngles.removeIndex(i);
                        bullets.removeIndex(j);bulletDirs.removeIndex(j);score++;break;
                    }
                }
            }
        }

        // Vẽ nền đất
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0.12f,0.18f,0.12f,1);
        shape.rect(0,0,W,H);

        // Grid nền
        shape.setColor(0.15f,0.22f,0.15f,1);
        for(int x=0;x<W;x+=60) shape.rect(x,0,2,H);
        for(int y=0;y<H;y+=60) shape.rect(0,y,W,2);

        // Vẽ zombie
        for(int i=0;i<zombies.size;i++) {
            Vector2 z=zombies.get(i);
            drawZombie(z.x, z.y, zombieAngles.get(i));
        }

        // Vẽ đạn
        for(Vector2 b:bullets) drawBullet(b.x,b.y);

        // Vẽ player
        drawPlayer(px,py,pAngle);

        // Joystick
        shape.setColor(1,1,1,0.1f);
        shape.circle(jBaseX,jBaseY,joystickSize);
        shape.setColor(1,1,1,0.35f);
        shape.circle(jKnobX,jKnobY,joystickSize*0.4f);

        if(gameOver) {
            shape.setColor(0,0,0,0.6f);
            shape.rect(0,0,W,H);
            shape.setColor(0.8f,0.2f,0.2f,1);
            shape.rect(W/2-120,H/2-80,240,65);
            shape.setColor(0.2f,0.5f,0.8f,1);
            shape.rect(W/2-120,H/2-160,240,65);
        }
        shape.end();

        batch.begin();
        font.setColor(Color.WHITE);
        font.draw(batch,"Score: "+score,20,H-20);
        font.draw(batch,diffNames[difficulty],20,H-50);
        font.draw(batch,weaponNames[weaponType],20,H-80);
        if(gameOver) {
            fontBig.setColor(Color.RED);
            fontBig.draw(batch,"GAME OVER",W/2-150,H/2+80);
            font.setColor(Color.YELLOW);
            font.draw(batch,"Score: "+score,W/2-60,H/2+20);
            font.setColor(Color.WHITE);
            font.draw(batch,"RESTART",W/2-55,H/2-42);
            font.draw(batch,"MENU",W/2-35,H/2-122);
        }
        batch.end();

        if(gameOver) {
            if(touched(W/2-120,H/2-80,240,65)) resetGame();
            if(touched(W/2-120,H/2-160,240,65)){screen=0;resetGame();}
        }
    }

    @Override
    public void dispose() {
        shape.dispose();batch.dispose();font.dispose();fontBig.dispose();
    }
}
