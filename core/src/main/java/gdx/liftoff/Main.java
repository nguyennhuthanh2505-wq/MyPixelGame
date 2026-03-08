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

    // Screens: 0=menu, 1=game, 2=settings, 3=weapon select
    int screen = 0;

    // Settings
    float volume = 0.7f;
    int difficulty = 1; // 0=Easy, 1=Normal, 2=Hard
    float joystickSize = 80f;
    int weaponType = 0; // 0=Pistol, 1=Shotgun, 2=MachineGun
    String[] diffNames = {"EASY", "NORMAL", "HARD"};
    String[] weaponNames = {"PISTOL", "SHOTGUN", "MACHINE GUN"};
    float[] weaponFireRate = {0.4f, 0.8f, 0.15f};
    int[] weaponBullets = {1, 5, 1};
    float[] weaponBulletSpeed = {400f, 300f, 500f};

    // Player
    float px, py;
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
    float shootTimer = 0;
    float fireRate = 0.4f;

    // Zombies
    Array<Vector2> zombies = new Array<>();
    float zombieSpeed = 80f;
    float spawnTimer = 0;

    int score = 0;
    boolean gameOver = false;

    // Touch
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
        px = W/2; py = H/2;
        bullets.clear(); bulletDirs.clear();
        zombies.clear();
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
        if (side == 0) { x = MathUtils.random(W); y = -30; }
        else if (side == 1) { x = MathUtils.random(W); y = H+30; }
        else if (side == 2) { x = -30; y = MathUtils.random(H); }
        else { x = W+30; y = MathUtils.random(H); }
        zombies.add(new Vector2(x, y));
    }

    boolean touched(float bx, float by, float bw, float bh) {
        return justTouched && touchX > bx && touchX < bx+bw && touchY > by && touchY < by+bh;
    }

    @Override
    public void render() {
        float dt = Math.min(Gdx.graphics.getDeltaTime(), 0.05f);

        justTouched = Gdx.input.justTouched();
        if (justTouched) {
            touchX = Gdx.input.getX();
            touchY = H - Gdx.input.getY();
        }

        Gdx.gl.glClearColor(0.08f, 0.08f, 0.12f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (screen == 0) renderMenu(dt);
        else if (screen == 1) renderGame(dt);
        else if (screen == 2) renderSettings(dt);
        else if (screen == 3) renderWeaponSelect(dt);
    }

    void renderMenu(float dt) {
        batch.begin();
        fontBig.setColor(Color.CYAN);
        fontBig.draw(batch, "ZOMBIE", W/2-100, H*0.75f);
        fontBig.draw(batch, "SHOOTER", W/2-120, H*0.65f);
        font.setColor(Color.WHITE);
        batch.end();

        shape.begin(ShapeRenderer.ShapeType.Filled);
        // Play button
        shape.setColor(0.2f, 0.7f, 0.2f, 1);
        shape.rect(W/2-120, H*0.45f, 240, 70);
        // Settings button
        shape.setColor(0.2f, 0.4f, 0.8f, 1);
        shape.rect(W/2-120, H*0.33f, 240, 70);
        // Weapon button
        shape.setColor(0.8f, 0.5f, 0.1f, 1);
        shape.rect(W/2-120, H*0.21f, 240, 70);
        shape.end();

        batch.begin();
        font.setColor(Color.WHITE);
        font.draw(batch, "PLAY", W/2-30, H*0.45f+45);
        font.draw(batch, "SETTINGS", W/2-60, H*0.33f+45);
        font.draw(batch, "WEAPONS", W/2-60, H*0.21f+45);
        font.draw(batch, "Weapon: " + weaponNames[weaponType], W/2-80, H*0.13f);
        font.draw(batch, "Diff: " + diffNames[difficulty], W/2-60, H*0.07f);
        batch.end();

        if (touched(W/2-120, H*0.45f, 240, 70)) screen = 1;
        if (touched(W/2-120, H*0.33f, 240, 70)) screen = 2;
        if (touched(W/2-120, H*0.21f, 240, 70)) screen = 3;
    }

    void renderSettings(float dt) {
        batch.begin();
        fontBig.setColor(Color.YELLOW);
        fontBig.draw(batch, "SETTINGS", W/2-130, H*0.88f);
        font.setColor(Color.WHITE);
        font.draw(batch, "VOLUME: " + (int)(volume*100) + "%", W/2-80, H*0.75f);
        font.draw(batch, "DIFFICULTY: " + diffNames[difficulty], W/2-90, H*0.58f);
        font.draw(batch, "JOYSTICK SIZE: " + (int)joystickSize, W/2-100, H*0.41f);
        batch.end();

        shape.begin(ShapeRenderer.ShapeType.Filled);

        // Volume slider
        shape.setColor(0.3f, 0.3f, 0.3f, 1);
        shape.rect(W/2-150, H*0.68f, 300, 20);
        shape.setColor(0.2f, 0.8f, 0.4f, 1);
        shape.rect(W/2-150, H*0.68f, 300*volume, 20);

        // Difficulty buttons
        String[] diffs = {"EASY", "NORMAL", "HARD"};
        float[] colors = {0.2f, 0.6f, 0.9f};
        for (int i = 0; i < 3; i++) {
            shape.setColor(difficulty == i ? 0.9f : 0.3f, difficulty == i ? colors[i] : 0.3f, 0.2f, 1);
            shape.rect(W/2-150+i*105, H*0.49f, 95, 50);
        }

        // Joystick size slider
        shape.setColor(0.3f, 0.3f, 0.3f, 1);
        shape.rect(W/2-150, H*0.33f, 300, 20);
        shape.setColor(0.2f, 0.6f, 1f, 1);
        float jPct = (joystickSize - 50f) / 100f;
        shape.rect(W/2-150, H*0.33f, 300*jPct, 20);

        // Back button
        shape.setColor(0.7f, 0.2f, 0.2f, 1);
        shape.rect(W/2-100, H*0.08f, 200, 60);
        shape.end();

        batch.begin();
        font.setColor(Color.WHITE);
        for (int i = 0; i < 3; i++) {
            font.draw(batch, diffs[i], W/2-140+i*105, H*0.49f+35);
        }
        font.draw(batch, "BACK", W/2-35, H*0.08f+40);
        batch.end();

        // Volume touch
        if (justTouched && touchY > H*0.65f && touchY < H*0.75f && touchX > W/2-150 && touchX < W/2+150) {
            volume = MathUtils.clamp((touchX-(W/2-150))/300f, 0, 1);
        }
        // Difficulty touch
        for (int i = 0; i < 3; i++) {
            if (touched(W/2-150+i*105, H*0.49f, 95, 50)) difficulty = i;
        }
        // Joystick size touch
        if (justTouched && touchY > H*0.30f && touchY < H*0.40f && touchX > W/2-150 && touchX < W/2+150) {
            joystickSize = MathUtils.clamp(50f + ((touchX-(W/2-150))/300f)*100f, 50, 150);
        }
        if (touched(W/2-100, H*0.08f, 200, 60)) screen = 0;
    }

    void renderWeaponSelect(float dt) {
        batch.begin();
        fontBig.setColor(Color.ORANGE);
        fontBig.draw(batch, "WEAPONS", W/2-120, H*0.88f);
        batch.end();

        String[] descs = {"Balanced\nFire Rate: Normal\nDamage: Normal", "Wide spread\nFire Rate: Slow\n5 bullets/shot", "Fast fire\nFire Rate: Fast\nLight damage"};
        float[] yPos = {0.62f, 0.42f, 0.22f};
        Color[] colors2 = {Color.GRAY, Color.YELLOW, Color.RED};

        for (int i = 0; i < 3; i++) {
            shape.begin(ShapeRenderer.ShapeType.Filled);
            if (weaponType == i) shape.setColor(0.2f, 0.6f, 0.9f, 1);
            else shape.setColor(0.25f, 0.25f, 0.3f, 1);
            shape.rect(W/2-180, H*yPos[i]-10, 360, 90);
            shape.end();

            batch.begin();
            font.setColor(colors2[i]);
            font.draw(batch, weaponNames[i], W/2-160, H*yPos[i]+70);
            font.setColor(Color.WHITE);
            font.getData().setScale(1.1f);
            font.draw(batch, descs[i].split("\n")[1], W/2-160, H*yPos[i]+45);
            font.getData().setScale(1.5f);
            batch.end();

            if (touched(W/2-180, H*yPos[i]-10, 360, 90)) weaponType = i;
        }

        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0.7f, 0.2f, 0.2f, 1);
        shape.rect(W/2-100, H*0.06f, 200, 60);
        shape.end();

        batch.begin();
        font.setColor(Color.WHITE);
        font.draw(batch, "BACK", W/2-35, H*0.06f+40);
        batch.end();

        if (touched(W/2-100, H*0.06f, 200, 60)) screen = 0;
    }

    void renderGame(float dt) {
        // Joystick input
        for (int i = 0; i < 5; i++) {
            if (Gdx.input.isTouched(i)) {
                float tx = Gdx.input.getX(i);
                float ty = H - Gdx.input.getY(i);
                if (!jActive && tx < W/2) {
                    jActive = true; jPointer = i;
                    jBaseX = tx; jBaseY = ty;
                    jKnobX = tx; jKnobY = ty;
                } else if (jActive && jPointer == i) {
                    float dx = tx - jBaseX, dy = ty - jBaseY;
                    float dist = (float)Math.sqrt(dx*dx+dy*dy);
                    if (dist > joystickSize) { dx=dx/dist*joystickSize; dy=dy/dist*joystickSize; }
                    jKnobX = jBaseX+dx; jKnobY = jBaseY+dy;
                }
            } else if (jActive && jPointer == i) {
                jActive = false; jKnobX = jBaseX; jKnobY = jBaseY;
            }
        }

        if (!gameOver) {
            if (jActive) {
                float dx = jKnobX-jBaseX, dy = jKnobY-jBaseY;
                float dist = (float)Math.sqrt(dx*dx+dy*dy);
                if (dist > 5) { px += (dx/joystickSize)*pSpeed*dt; py += (dy/joystickSize)*pSpeed*dt; }
            }
            px = MathUtils.clamp(px, 0, W); py = MathUtils.clamp(py, 0, H);

            // Shoot
            shootTimer += dt;
            if (shootTimer > fireRate && zombies.size > 0) {
                shootTimer = 0;
                Vector2 nearest = zombies.first();
                float minDist = Vector2.dst(px, py, nearest.x, nearest.y);
                for (Vector2 z : zombies) {
                    float d = Vector2.dst(px, py, z.x, z.y);
                    if (d < minDist) { minDist = d; nearest = z; }
                }
                Vector2 baseDir = new Vector2(nearest.x-px, nearest.y-py).nor();
                int numBullets = weaponBullets[weaponType];
                float spread = weaponType == 1 ? 25f : 0f;
                for (int b = 0; b < numBullets; b++) {
                    float angle = spread > 0 ? MathUtils.random(-spread, spread) : 0;
                    Vector2 dir = baseDir.cpy().rotateDeg(angle);
                    bullets.add(new Vector2(px, py));
                    bulletDirs.add(dir);
                }
            }

            // Move bullets
            for (int i = bullets.size-1; i >= 0; i--) {
                bullets.get(i).add(bulletDirs.get(i).x*bulletSpeed*dt, bulletDirs.get(i).y*bulletSpeed*dt);
                Vector2 b = bullets.get(i);
                if (b.x<-50||b.x>W+50||b.y<-50||b.y>H+50) { bullets.removeIndex(i); bulletDirs.removeIndex(i); }
            }

            // Spawn
            spawnTimer += dt;
            float spawnRate = Math.max(0.3f, 2f - score*0.03f);
            if (spawnTimer > spawnRate) { spawnTimer = 0; spawnZombie(); }

            // Move zombies
            float zSpeed = zombieSpeed + score*1.5f;
            for (int i = zombies.size-1; i >= 0; i--) {
                Vector2 z = zombies.get(i);
                Vector2 dir = new Vector2(px-z.x, py-z.y).nor();
                z.add(dir.x*zSpeed*dt, dir.y*zSpeed*dt);
                if (Vector2.dst(px, py, z.x, z.y) < pSize+15f) gameOver = true;
                for (int j = bullets.size-1; j >= 0; j--) {
                    if (Vector2.dst(bullets.get(j).x, bullets.get(j).y, z.x, z.y) < 22f) {
                        zombies.removeIndex(i); bullets.removeIndex(j); bulletDirs.removeIndex(j); score++; break;
                    }
                }
            }
        }

        // Draw
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0.2f, 0.8f, 0.2f, 1);
        for (Vector2 z : zombies) shape.circle(z.x, z.y, 18f);
        shape.setColor(1f, 0.9f, 0f, 1);
        for (Vector2 b : bullets) shape.circle(b.x, b.y, 6f);
        shape.setColor(0.2f, 0.8f, 1f, 1);
        shape.circle(px, py, pSize);

        // Joystick
        shape.setColor(1,1,1,0.15f);
        shape.circle(jBaseX, jBaseY, joystickSize);
        shape.setColor(1,1,1,0.4f);
        shape.circle(jKnobX, jKnobY, joystickSize*0.4f);

        if (gameOver) {
            shape.setColor(0.8f, 0.2f, 0.2f, 1);
            shape.rect(W/2-120, H/2-120, 240, 70);
            shape.setColor(0.2f, 0.5f, 0.8f, 1);
            shape.rect(W/2-120, H/2-200, 240, 70);
        }
        shape.end();

        batch.begin();
        font.setColor(Color.WHITE);
        font.draw(batch, "Score: "+score, 20, H-20);
        font.draw(batch, diffNames[difficulty], 20, H-50);
        font.draw(batch, weaponNames[weaponType], 20, H-80);
        if (gameOver) {
            fontBig.setColor(Color.RED);
            fontBig.draw(batch, "GAME OVER", W/2-150, H/2+80);
            font.setColor(Color.YELLOW);
            font.draw(batch, "Score: "+score, W/2-60, H/2+20);
            font.setColor(Color.WHITE);
            font.draw(batch, "RESTART", W/2-55, H/2-85);
            font.draw(batch, "MENU", W/2-35, H/2-165);
        }
        batch.end();

        if (gameOver) {
            if (touched(W/2-120, H/2-120, 240, 70)) resetGame();
            if (touched(W/2-120, H/2-200, 240, 70)) { screen = 0; resetGame(); }
        }
    }

    @Override
    public void dispose() {
        shape.dispose(); batch.dispose(); font.dispose(); fontBig.dispose();
    }
}
