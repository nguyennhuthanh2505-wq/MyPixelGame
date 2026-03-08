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
    BitmapFont font;

    float W, H;

    // Player
    float px, py;
    float pSpeed = 200f;
    float pSize = 20f;

    // Joystick
    float jBaseX, jBaseY, jKnobX, jKnobY;
    float jRadius = 80f;
    int jPointer = -1;
    boolean jActive = false;

    // Bullets
    Array<Vector2> bullets = new Array<>();
    Array<Vector2> bulletDirs = new Array<>();
    float bulletSpeed = 400f;
    float shootTimer = 0;

    // Zombies
    Array<Vector2> zombies = new Array<>();
    float zombieSpeed = 80f;
    float spawnTimer = 0;

    int score = 0;
    boolean gameOver = false;

    @Override
    public void create() {
        shape = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.setColor(Color.WHITE);
        font.getData().setScale(1.5f);

        W = Gdx.graphics.getWidth();
        H = Gdx.graphics.getHeight();
        px = W / 2;
        py = H / 2;

        jBaseX = 150;
        jBaseY = 150;
        jKnobX = jBaseX;
        jKnobY = jBaseY;

        spawnZombie();
    }

    void spawnZombie() {
        float x, y;
        int side = MathUtils.random(3);
        if (side == 0) { x = MathUtils.random(W); y = -30; }
        else if (side == 1) { x = MathUtils.random(W); y = H + 30; }
        else if (side == 2) { x = -30; y = MathUtils.random(H); }
        else { x = W + 30; y = MathUtils.random(H); }
        zombies.add(new Vector2(x, y));
    }

    void restart() {
        px = W / 2; py = H / 2;
        bullets.clear(); bulletDirs.clear();
        zombies.clear();
        score = 0; gameOver = false;
        spawnTimer = 0; shootTimer = 0;
        spawnZombie();
    }

    @Override
    public void render() {
        float dt = Gdx.graphics.getDeltaTime();

        // Joystick input
        for (int i = 0; i < 5; i++) {
            if (Gdx.input.isTouched(i)) {
                float tx = Gdx.input.getX(i);
                float ty = H - Gdx.input.getY(i);

                if (gameOver) {
                    // Nhấn restart
                    float bx = W / 2, by = H / 2 - 60;
                    if (Math.abs(tx - bx) < 120 && Math.abs(ty - by) < 40) {
                        restart();
                    }
                    continue;
                }

                if (!jActive && tx < W / 2) {
                    jActive = true;
                    jPointer = i;
                    jBaseX = tx; jBaseY = ty;
                    jKnobX = tx; jKnobY = ty;
                } else if (jActive && jPointer == i) {
                    float dx = tx - jBaseX;
                    float dy = ty - jBaseY;
                    float dist = (float) Math.sqrt(dx * dx + dy * dy);
                    if (dist > jRadius) {
                        dx = dx / dist * jRadius;
                        dy = dy / dist * jRadius;
                    }
                    jKnobX = jBaseX + dx;
                    jKnobY = jBaseY + dy;
                }
            } else if (jActive && jPointer == i) {
                jActive = false;
                jKnobX = jBaseX;
                jKnobY = jBaseY;
            }
        }

        if (!gameOver) {
            // Di chuyển player bằng joystick
            if (jActive) {
                float dx = jKnobX - jBaseX;
                float dy = jKnobY - jBaseY;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                if (dist > 5) {
                    px += (dx / jRadius) * pSpeed * dt;
                    py += (dy / jRadius) * pSpeed * dt;
                }
            }

            // Bàn phím backup
            if (Gdx.input.isKeyPressed(Input.Keys.W)) py += pSpeed * dt;
            if (Gdx.input.isKeyPressed(Input.Keys.S)) py -= pSpeed * dt;
            if (Gdx.input.isKeyPressed(Input.Keys.A)) px -= pSpeed * dt;
            if (Gdx.input.isKeyPressed(Input.Keys.D)) px += pSpeed * dt;

            px = MathUtils.clamp(px, 0, W);
            py = MathUtils.clamp(py, 0, H);

            // Bắn tự động
            shootTimer += dt;
            if (shootTimer > 0.4f && zombies.size > 0) {
                shootTimer = 0;
                Vector2 nearest = zombies.first();
                float minDist = Vector2.dst(px, py, nearest.x, nearest.y);
                for (Vector2 z : zombies) {
                    float d = Vector2.dst(px, py, z.x, z.y);
                    if (d < minDist) { minDist = d; nearest = z; }
                }
                Vector2 dir = new Vector2(nearest.x - px, nearest.y - py).nor();
                bullets.add(new Vector2(px, py));
                bulletDirs.add(dir);
            }

            // Di chuyển đạn
            for (int i = bullets.size - 1; i >= 0; i--) {
                bullets.get(i).add(bulletDirs.get(i).x * bulletSpeed * dt,
                    bulletDirs.get(i).y * bulletSpeed * dt);
                Vector2 b = bullets.get(i);
                if (b.x < -50 || b.x > W + 50 || b.y < -50 || b.y > H + 50) {
                    bullets.removeIndex(i); bulletDirs.removeIndex(i);
                }
            }

            // Spawn zombie
            spawnTimer += dt;
            float spawnRate = Math.max(0.5f, 2f - score * 0.05f);
            if (spawnTimer > spawnRate) { spawnTimer = 0; spawnZombie(); }

            // Di chuyển zombie
            float zSpeed = zombieSpeed + score * 2f;
            for (int i = zombies.size - 1; i >= 0; i--) {
                Vector2 z = zombies.get(i);
                Vector2 dir = new Vector2(px - z.x, py - z.y).nor();
                z.add(dir.x * zSpeed * dt, dir.y * zSpeed * dt);

                if (Vector2.dst(px, py, z.x, z.y) < pSize + 15f) {
                    gameOver = true;
                }

                for (int j = bullets.size - 1; j >= 0; j--) {
                    if (Vector2.dst(bullets.get(j).x, bullets.get(j).y, z.x, z.y) < 22f) {
                        zombies.removeIndex(i);
                        bullets.removeIndex(j);
                        bulletDirs.removeIndex(j);
                        score++;
                        break;
                    }
                }
            }
        }

        // Vẽ
        Gdx.gl.glClearColor(0.08f, 0.08f, 0.12f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        shape.begin(ShapeRenderer.ShapeType.Filled);

        // Zombie
        shape.setColor(0.2f, 0.8f, 0.2f, 1);
        for (Vector2 z : zombies) shape.circle(z.x, z.y, 18f);

        // Đạn
        shape.setColor(1f, 0.9f, 0f, 1);
        for (Vector2 b : bullets) shape.circle(b.x, b.y, 6f);

        // Player
        shape.setColor(0.2f, 0.8f, 1f, 1);
        shape.circle(px, py, pSize);

        // Joystick
        if (!gameOver) {
            shape.setColor(1, 1, 1, 0.15f);
            shape.circle(jBaseX, jBaseY, jRadius);
            shape.setColor(1, 1, 1, 0.4f);
            shape.circle(jKnobX, jKnobY, 35f);
        }

        // Nút Restart
        if (gameOver) {
            shape.setColor(0.8f, 0.2f, 0.2f, 1);
            shape.rect(W/2 - 120, H/2 - 100, 240, 80);
        }

        shape.end();

        batch.begin();
        font.draw(batch, "Score: " + score, 20, H - 20);
        font.draw(batch, "Zombies: " + zombies.size, 20, H - 50);
        if (gameOver) {
            font.getData().setScale(2.5f);
            font.draw(batch, "GAME OVER!", W/2 - 130, H/2 + 60);
            font.getData().setScale(1.8f);
            font.draw(batch, "Score: " + score, W/2 - 70, H/2 + 10);
            font.getData().setScale(1.5f);
            font.draw(batch, "RESTART", W/2 - 70, H/2 - 50);
        }
        batch.end();
    }

    @Override
    public void dispose() {
        shape.dispose();
        batch.dispose();
        font.dispose();
    }
}
