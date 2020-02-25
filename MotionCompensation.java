import java.io.PrintWriter;

import jdk.internal.dynalink.support.BottomGuardingDynamicLinker;

import java.io.File;

public class MotionCompensation{
    Image2 targetImage;
    Image2 referenceImage;
    Image2 grayTarget;
    Image2 grayReference;
    Image2 errorImage;
    Image2 dynamicBlockedImage;
    Image2 moveOneImage;
    Image2 moveTwoImage;
    Image2 fifthFrame;;


    MotionVector[][] motionVectors;
    String targetName, referenceName;
    int n, p;

    public MotionCompensation(int t, int r, int n, int p){
        this.targetName = fileName(t);
        this.referenceName = fileName(r);
        this.n = n;
        this.p = p;

        targetImage = new Image2(fileName(t));
        grayTarget= new Image2(fileName(t));
        grayTarget.grayScale();

        referenceImage = new Image2(fileName(r));
        grayReference = new Image2(fileName(r));
        grayReference.grayScale();

        errorImage = new Image2(targetImage.getW(), targetImage.getH());
        motionVectors = new MotionVector[targetImage.getH() / n][targetImage.getW() / n];

        System.out.println("Target File: "+targetName);
        System.out.println("Reference Frame: "+referenceName);

    }
    public MotionCompensation(int frameNumber, int n, int p){
        this.targetName = fileName(frameNumber);
        this.referenceName = fileName(frameNumber-2);
        this.n = n;
        this.p = p;

        targetImage = new Image2(fileName(frameNumber));
        grayTarget= new Image2(fileName(frameNumber));
        grayTarget.grayScale();

        referenceImage = new Image2(fileName(frameNumber-2));
        grayReference = new Image2(fileName(frameNumber-2));
        grayReference.grayScale();

        dynamicBlockedImage = new Image2(fileName(frameNumber));
        moveOneImage = new Image2(fileName(frameNumber));
        moveTwoImage = new Image2(fileName(frameNumber));
        fifthFrame = new Image2(fileName(5));


        errorImage = new Image2(targetImage.getW(), targetImage.getH());
        motionVectors = new MotionVector[targetImage.getH() / n][targetImage.getW() / n];

        System.out.println("Target File: "+targetName);
        System.out.println("Reference Frame: "+referenceName);

    }
    public String fileName(int frameNumber){
        String number = "";
        if(frameNumber<10) number = "00" + frameNumber;
        if(frameNumber<=99 && frameNumber>=10) number = "0" + frameNumber;
        String fileName = "Walk_"+number +".ppm";
        return fileName;
    }
    public void searchAll(){
        for(int x = 0; x < grayTarget.getW(); x+=n){
            for(int y = 0; y < grayTarget.getH(); y+=n){
                searchIndividual(x, y);
            }
        }
    }
    public void searchIndividual(int targetX, int targetY){
        int startx = (targetX - p <= 0) ? 0 : targetX - p;
        int endx = (targetX + p >= grayReference.getW()) ? grayReference.getW() : targetX + p;
        int starty = (targetY - p <= 0) ? 0 : targetY - p;
        int endy = (targetY + p >= grayReference.getH()) ? grayReference.getH() : targetY + p;

        double error = MSD(targetX, targetY, targetX, targetY);
        int matchedX = targetX;
        int matchedY = targetY;
        MotionVector blockMotion = new MotionVector(targetX, targetY, matchedX, matchedY, error);

        for(int i = startx; i <= endx; i++){
            for(int j = starty; j <= endy; j++){
                if((i + 16) < grayReference.getW() && (j+16) < grayReference.getH()){
                    double newError = MSD(targetX, targetY, i, j);
                    if(newError < error){
                        error = newError;
                        matchedX = i;
                        matchedY = j;
                        int dx = targetX - matchedX;
                        int dy = targetY - matchedY;
                        blockMotion.update(dx, dy, error);
                    }
                }
            }
        }

        motionVectors[targetY/n][targetX/n] = blockMotion;
        
        int[] tar = new int[3];
        int[] ref = new int[3];

        for(int x = 0; x < n; x++){
            for(int y = 0; y < n; y++){
                grayTarget.getPixel(x+targetX, y+targetY, tar);
                grayReference.getPixel(x+matchedX, y+matchedY, ref);

                int residual = Math.abs(ref[0] - tar[0]);
                int[] resBlock = {residual, residual, residual};
                errorImage.setPixel(x+targetX, y+targetY, resBlock);
            }
        }
    }
    public double MSD(int targetX, int targetY, int referenceX, int referenceY){
        double sum = 0;
        int tarY = targetY;
        int refY = referenceY;
        int[] tar = new int[3];
        int[] ref = new int[3];

        double firstFactor = 1.0 / (n * n);

        for(int i = 0; i < n; i++){
            for(int j = 0; j < n; j++){
                if(referenceX < referenceImage.getW() && referenceY < referenceImage.getH()){
                    grayTarget.getPixel(targetX, targetY++, tar);
                    grayReference.getPixel(referenceX, referenceY++, ref);
                    sum += Math.pow( (ref[0]-tar[0]) , 2);
                }
            }
            targetX++;
            referenceX++;
            targetY = tarY;
            referenceY = refY;
        }

        return  firstFactor * sum;
    }
    public void scaleError(){
        int min = minError();
        int max = maxError();
        int newValue;

        int[] irgb = new int[3];
        for(int x = 0; x < errorImage.getW(); x++){
            for(int y = 0; y < errorImage.getH(); y++){
                errorImage.getPixel(x, y, irgb);
                newValue = scaledError(max, min, irgb[0]);
                for(int k = 0; k < 3; k++){irgb[k] = newValue;}
                errorImage.setPixel(x, y, irgb);
            }
        }   
    }
    public int scaledError(int oldMax, int oldMin, int oldValue){
        int oldRange = oldMax - oldMin;
        int newRange = 255 - 0;
        
        int newValue = ((oldValue - oldMin) * newRange / oldRange) + 0;
        return newValue;
    }
    public int minError(){
        int min = 255;
        int[] irgb = new int[3];
        for(int i = 0; i < errorImage.getW(); i++){
            for(int j = 0; j < errorImage.getH(); j++){
                errorImage.getPixel(i, j, irgb);
                if(irgb[0] < min){ min = irgb[0];}
            }
        }
        return min;
    }
    public int maxError(){
        int max = 255;
        int[] irgb = new int[3];
        for(int i = 0; i < errorImage.getW(); i++){
            for(int j = 0; j < errorImage.getH(); j++){
                errorImage.getPixel(i, j, irgb);
                if(irgb[0] > max){ max = irgb[0];}
            }
        }
        return max;
    }
    public void displayOne(){
        targetImage.display("Actual Target Image");
        referenceImage.display("Actual Reference Image");
        grayTarget.display("Gray Target Image");
        grayReference.display("Gray Reference Image");
        errorImage.display("Error Image");
    }
    public void displayTwo(){
        targetImage.display("Actual Target Image");
        referenceImage.display("Actual Reference Image");
        grayTarget.display("Gray Target Image");
        grayReference.display("Gray Reference Image");
        dynamicBlockedImage.display("Dynamic Blocked Image");
        moveOneImage.display("Replaced With Method One");
        moveTwoImage.display("Replaced With Method Two");
        errorImage.display("Error Image");
    }
    public void saveErrorImage(){
        errorImage.write2PPM("Error_Image.ppm");
    }
    public void printMV(){
        try{
            File motions = new File("mv.txt");
            PrintWriter out = new PrintWriter(motions);
                out.println("# Name : Saba Al Mahbub");
                out.println("#Target image name: " + targetName);
                out.println("#Reference image name: " + referenceName);
                out.println("# Number of target macro blocks: " + (targetImage.getW()/n) + " x " + (targetImage.getH()/n) + "(image size is " + targetImage.getW() + " x " + targetImage.getH() + ")");
                out.println();

                for(int i = 0; i < motionVectors.length; i++){
                    for(int j = 0; j < motionVectors[i].length; j++){
                        out.print(motionVectors[i][j].toString() + " ");
                    }
                    out.println();
                }
                out.close();
        }
        catch(Exception e){}
        finally{}
    }
    public void taskOne(){
        searchAll();
        System.out.println("Image Size: [" + targetImage.getW() + " x " + targetImage.getH() + "]");
        System.out.println("Target: " + targetName);
        System.out.println("Reference: " + referenceName);
        scaleError();
        printMV();
        displayOne();
        saveErrorImage();
    }
    public void changeColor(){
        for(int x = 0; x < motionVectors.length; x++){
            for(int y = 0; y < motionVectors[x].length; y++){
                if(!motionVectors[x][y].isStatic()){
                    colorBlock(y, x);
                }
            }
        }
        dynamicBlockedImage.write2PPM("Dynamic_Blocked_Image.ppm");
    }
    public void colorBlock(int x, int y){
        int startx = x * n;
        int starty = y * n;

        int[] irgb = new int[3];

        for(int i = startx; i < startx+n; i++){
            for(int j = starty; j < starty+n; j++){
                dynamicBlockedImage.getPixel(i, j, irgb);
                irgb[0] = irgb[0] + 50;
                dynamicBlockedImage.setPixel(i, j, irgb);
            }
        }
    }
    public void replaceDynamicOne(){
        int staticX = 0;
        int statixY = 0;
        Coordinate matched;
        for(int x = 0; x < motionVectors.length; x++){
            for(int y = 0; y < motionVectors[x].length; y++){
                if(!motionVectors[x][y].isStatic()){
                    matched = getClosestStatic(x, y);
                    replaceOne(y, x, matched.getY(), matched.getX());
                }
            }
        }
        moveOneImage.write2PPM("Replaced_Image_1st_Method.ppm");
    }
    public Coordinate getClosestStatic(int x, int y){
        Coordinate matched = new Coordinate(0, 0);
        boolean found = false;
        int increment = 1;

        while(!found){
            int row, col;
            //Left, y stays the same
            row = x-increment;
            for( col = y-increment; col < y+increment; col++){
                if(motionVectors[row][col].isStatic()){
                    matched = new Coordinate(row, col);
                    found = true;
                }
            }
            row = x+increment;
            //Right, y stays the same
            for( col = y-increment; col < y+increment; col++){
                if(motionVectors[row][col].isStatic()){
                    matched = new Coordinate(row, col);
                    found = true;
                }
            }

            //Top, x stays the same
            col = y-increment;
            for(row = x-increment; row < x+increment; row++ ){
                if(motionVectors[row][col].isStatic()){
                    matched = new Coordinate(row, col);
                    found = true;
                }
            } 

            //Bottom, x stays the same
            col = y+increment;
            for(row = x-increment; row < x+increment; row++ ){
                if(motionVectors[row][col].isStatic()){
                    matched = new Coordinate(row, col);
                    found = true;
                }
            }             

            increment++;
        }
        return matched;
    }

    public void replaceOne(int x, int y, int staticX, int staticY){
        int[] irgb = new int[3];

        int startX = x*n;
        int endX = startX + n;

        int startY = y*n;
        int endY = startY + n;

        int mX = staticX*n;
        int mY = staticY*n;
        int originalY = staticY*n;

        for(int i = startX; i < endX; i++){
            for(int j = startY; j < endY; j++){
                targetImage.getPixel(mX, mY, irgb);
                moveOneImage.setPixel(i, j, irgb);
                mY++;
            }
            mX++;
            mY = originalY;
        }
    }
    public void replaceDynamicTwo(){
        for(int x = 0; x < motionVectors.length; x++){
            for(int y = 0; y < motionVectors[x].length; y++){
                if(!motionVectors[x][y].isStatic()){
                    replaceTwo(y, x);
                }
            }
        }
        moveTwoImage.write2PPM("Replaced_Image_2nd_Method.ppm");
    }
    public void replaceTwo(int x, int y){
        int startx = x * n;
        int starty = y * n;

        int[] irgb = new int[3];

        for(int i = startx; i < startx+n; i++){
            for(int j = starty; j < starty+n; j++){
                fifthFrame.getPixel(i, j, irgb);
                moveTwoImage.setPixel(i, j, irgb);
            }
        }
    }
    public void taskTwo(){
        searchAll();
        System.out.println("Image Size: [" + targetImage.getW() + " x " + targetImage.getH() + "]");
        System.out.println("Target: " + targetName);
        System.out.println("Reference: " + referenceName);
        scaleError();
        changeColor();
        replaceDynamicOne();
        replaceDynamicTwo();
        printMV();
        displayTwo();
        saveErrorImage();
    }
}