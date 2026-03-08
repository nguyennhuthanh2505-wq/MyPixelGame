package gdx.liftoff;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

public class Main extends ApplicationAdapter {
    ShapeRenderer shape;
    SpriteBatch batch;
    BitmapFont font;

    // Player
    float px = 400, py = 300;
    float pSpeed = 200f;
    float pSize = 20f;

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
        spawnZombie();
    }

    void spawnZombie() {
        float x, y;
        int side = MathUtils.random(3);
        if (side == 0) { x = MathUtils.random(800f); y = 0; }
        else if (side == 1) { x = MathUtils.random(800f); y = 480; }
        else if (side == 2) { x = 0; y = MathUtils.random(480f); }
        else { x = 800; y = MathUtils.random(480f); }
        zombies.add(new Vector2(x, y));
    }

    @Override
    public void render() {
        float dt = Gdx.graphics.getDeltaTime();

        if (!gameOver) {
            // Di chuyển player
            if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) py += pSpeed * dt;
            if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) py -= pSpeed * dt;
            if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) px -= pSpeed * dt;
            if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) px += pSpeed * dt;

            px = MathUtils.clamp(px, 0, 800);
            py = MathUtils.clamp(py, 0, 480);

            // Bắn tự động về hướng zombie gần nhất
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
                bullets.get(i).add(bulletDirs.get(i).x * bulletSpeed * dt, bulletDirs.get(i).y * bulletSpeed * dt);
                Vector2 b = bullets.get(i);
                if (b.x < 0 || b.x > 800 || b.y < 0 || b.y > 480) {
                    bullets.removeIndex(i); bulletDirs.removeIndex(i);
                }
            }

            // Di chuyển zombie
            spawnTimer += dt;
            if (spawnTimer > 2f) { spawnTimer = 0; spawnZombie(); }

            for (int i = zombies.size - 1; i >= 0; i--) {
                Vector2 z = zombies.get(i);
                Vector2 dir = new Vector2(px - z.x, py - z.y).nor();
                z.add(dir.x * zombieSpeed * dt, dir.y * zombieSpeed * dt);

                // Zombie chạm player
                if (Vector2.dst(px, py, z.x, z.y) < pSize + 15f) {
                    gameOver = true;
                }

                // Đạn chạm zombie
                for (int j = bullets.size - 1; j >= 0; j--) {
                    if (Vector2.dst(bullets.get(j).x, bullets.get(j).y, z.x, z.y) < 20f) {
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
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        shape.begin(ShapeRenderer.ShapeType.Filled);

        // Vẽ zombie (xanh lá)
        shape.setColor(Color.GREEN);
        for (Vector2 z : zombies) shape.circle(z.x, z.y, 15f);

        // Vẽ đạn (vàng)
        shape.setColor(Color.YELLOW);
        for (Vector2 b : bullets) shape.circle(b.x, b.y, 5f);

        // Vẽ player (xanh dương)
        shape.setColor(Color.CYAN);
        shape.circle(px, py, pSize);

        shape.end();

        batch.begin();
        font.draw(batch, "Score: " + score, 10, 470);
        font.draw(batch, "Zombies: " + zombies.size, 10, 450);
        if (gameOver) {
            font.getData().setScale(2f);
            font.draw(batch, "GAME OVER! Score: " + score, 250, 260);
            font.getData().setScale(1f);
            font.draw(batch, "Restart app to play again", 280, 230);
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
