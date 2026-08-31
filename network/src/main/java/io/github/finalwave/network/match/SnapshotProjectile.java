package io.github.finalwave.network.match;

public final class SnapshotProjectile {
    private String id;
    private String type;
    private double x;
    private double y;
    private int row;
    private boolean fromZombie;
    private boolean reverse;
    private String trajectory;
    private double vx;
    private double vy;
    private int damage;
    private String sourcePlantId;

    public SnapshotProjectile() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public boolean isFromZombie() {
        return fromZombie;
    }

    public void setFromZombie(boolean fromZombie) {
        this.fromZombie = fromZombie;
    }

    public boolean isReverse() {
        return reverse;
    }

    public void setReverse(boolean reverse) {
        this.reverse = reverse;
    }

    public String getTrajectory() {
        return trajectory;
    }

    public void setTrajectory(String trajectory) {
        this.trajectory = trajectory;
    }

    public double getVx() {
        return vx;
    }

    public void setVx(double vx) {
        this.vx = vx;
    }

    public double getVy() {
        return vy;
    }

    public void setVy(double vy) {
        this.vy = vy;
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public String getSourcePlantId() {
        return sourcePlantId;
    }

    public void setSourcePlantId(String sourcePlantId) {
        this.sourcePlantId = sourcePlantId;
    }
}
