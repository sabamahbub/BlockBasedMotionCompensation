public class MotionVector{
    int dx;
    int dy;
    double error = 500;
    
    public MotionVector(int dx, int dy, double error){
        this.dx = dx;
        this.dy = dy;
        this.error = error;
    }
    public MotionVector(int targetx, int targety, int referencex, int referencey, double error){
        this.dx = targetx - referencex;
        this.dy = targety - referencey;
        this.error = error;
    }
    public void update(int targetx, int targety, int referencex, int referencey, double error){
        this.dx = targetx - referencex;
        this.dy = targety - referencey;
        this.error = error;
    }
    public void update(int dx, int dy, double error){
        this.dx = dx;
        this.dy = dy;
        this.error = error;
    }
    public boolean isStatic(){
        return (dx == 0) && (dy == 0);
    }
    // public String toString(){
    //     return "[ " + this.dx + " , " + this.dy + " e:" + this.error + " ]";
    // }
    public String toString(){
        return "[ " + this.dx + " , " + this.dy + " ]";
    }

}